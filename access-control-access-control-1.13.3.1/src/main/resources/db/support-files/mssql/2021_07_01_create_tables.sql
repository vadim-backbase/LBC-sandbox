-- START OF THE SCRIPT
-- TABLES CREATION

CREATE TABLE [sequence_table] (
  [sequence_name]               [NVARCHAR](255) NOT NULL,
  [next_val]                    [BIGINT],
  CONSTRAINT pk_sequence_table  PRIMARY KEY CLUSTERED (sequence_name)
);

GO

CREATE TABLE [privilege] (
  [id]                          [NVARCHAR](36) NOT NULL,
  [code]                        [NVARCHAR](8)  NOT NULL,
  [name]                        [NVARCHAR](16) NOT NULL,
  CONSTRAINT pk_privilege       PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_privilege_01    UNIQUE (name),
  CONSTRAINT uq_privilege_02    UNIQUE (code)
);

GO

SET QUOTED_IDENTIFIER ON

GO

CREATE TABLE [business_function] (
  [id]                                  [NVARCHAR](36) NOT NULL,
  [function_code]                       [NVARCHAR](32) NOT NULL,
  [function_name]                       [NVARCHAR](32) NOT NULL,
  [resource_code]                       [NVARCHAR](32) NOT NULL,
  [resource_name]                       [NVARCHAR](32) NOT NULL,
  CONSTRAINT pk_business_function       PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_business_function_01    UNIQUE (function_name)
);

GO

CREATE TABLE [applicable_function_privilege] (
  [id]                      [NVARCHAR](36) NOT NULL,
  [business_function_name]  [NVARCHAR](32) NOT NULL,
  [function_resource_name]  [NVARCHAR](32) NOT NULL,
  [privilege_name]          [NVARCHAR](16) NOT NULL,
  [supports_limit]          [TINYINT]      NOT NULL,
  [business_function_id]    [NVARCHAR](36) NOT NULL,
  [privilege_id]            [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_afp         PRIMARY KEY CLUSTERED (id),
  CONSTRAINT fk_afp2bf      FOREIGN KEY (business_function_id) REFERENCES business_function (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_afp2priv    FOREIGN KEY (privilege_id) REFERENCES privilege (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_afp_01 (business_function_id),
  INDEX ix_afp_02 (privilege_id)
);

GO

CREATE TABLE [assignable_permission_set](
    [id]                                    [BIGINT]            NOT NULL,
    [name]                                  [NVARCHAR](128)     NOT NULL,
    [description]                           [NVARCHAR](255)     NOT NULL,
    [type]                                  [TINYINT] DEFAULT 2 NOT NULL,
    CONSTRAINT pk_assignable_permission_set PRIMARY KEY CLUSTERED (id),
    CONSTRAINT uq_aps_01                    UNIQUE (name)
);

GO

CREATE UNIQUE INDEX ix_aps_01 ON assignable_permission_set (type) WHERE type IN (0,1);

GO

-- SET QUOTED_IDENTIFIER OFF

CREATE TABLE [assignable_permission_set_item](
   [assignable_permission_set_id]   [BIGINT]            NOT NULL,
   [function_privilege_id]          [NVARCHAR](36)      NOT NULL,
   CONSTRAINT pk_aps_item           PRIMARY KEY CLUSTERED (assignable_permission_set_id, function_privilege_id),
   CONSTRAINT fk_apsi2aps           FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id)
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
   CONSTRAINT fk_apsi2afp          FOREIGN KEY (function_privilege_id) REFERENCES applicable_function_privilege (id)
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
   INDEX ix_aps_item_01 (function_privilege_id)
);

GO

CREATE TABLE [legal_entity] (
  [id]                          [NVARCHAR](36)                   NOT NULL,
  [external_id]                 [NVARCHAR](64)                   NOT NULL,
  [name]                        [NVARCHAR](128)                  NOT NULL,
  [parent_id]                   [NVARCHAR](36),
  [type]                        [NVARCHAR](8) DEFAULT 'CUSTOMER' NOT NULL,
  CONSTRAINT pk_legal_entity    PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_legal_entity_01 UNIQUE (external_id),
  CONSTRAINT fk_le2le           FOREIGN KEY (parent_id) REFERENCES legal_entity (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_legal_entity_02 (name),
  INDEX ix_legal_entity_03 (parent_id)
);

GO

CREATE TABLE [legal_entity_ancestor] (
  [descendent_id]                       [NVARCHAR](36) NOT NULL,
  [ancestor_id]                         [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_legal_entity_ancestor   PRIMARY KEY CLUSTERED (ancestor_id, descendent_id),
  CONSTRAINT fk_lea2le_01               FOREIGN KEY (descendent_id) REFERENCES legal_entity (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_lea2le_02               FOREIGN KEY (ancestor_id) REFERENCES legal_entity (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_legal_entity_ancestor_02 (descendent_id)
);

GO

CREATE TABLE [add_prop_legal_entity] (
  [add_prop_legal_entity_id]            [NVARCHAR](36) NOT NULL,
  [property_key]                        [NVARCHAR](50) NOT NULL,
  [property_value]                      [NVARCHAR](500),
  CONSTRAINT pk_add_prop_legal_entity   PRIMARY KEY CLUSTERED (add_prop_legal_entity_id, property_key),
  CONSTRAINT fk_aple2le                 FOREIGN KEY (add_prop_legal_entity_id) REFERENCES legal_entity (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

GO

CREATE TABLE [service_agreement] (
  [id]                              [NVARCHAR](36)  NOT NULL,
  [external_id]                     [NVARCHAR](64),
  [name]                            [NVARCHAR](128) NOT NULL,
  [description]                     [NVARCHAR](255) NOT NULL,
  [is_master]                       [TINYINT]       NOT NULL,
  [creator_legal_entity_id]         [NVARCHAR](36)  NOT NULL,
  [state]                           [NVARCHAR](16)  NOT NULL DEFAULT ('ENABLED'),
  [start_date]                      DATETIME,
  [end_date]                        DATETIME,
  [state_changed_at]                DATETIME,
  CONSTRAINT pk_service_agreement   PRIMARY KEY CLUSTERED (id),
  CONSTRAINT fk_sa2le               FOREIGN KEY (creator_legal_entity_id) REFERENCES legal_entity (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_service_agreement_03 (creator_legal_entity_id)
);

GO

CREATE UNIQUE INDEX ix_service_agreement_02
    ON service_agreement (external_id)
    WHERE external_id IS NOT NULL;

GO

CREATE UNIQUE INDEX ix_service_agreement_04
    ON service_agreement (creator_legal_entity_id, is_master)
    WHERE is_master = 1;

GO

CREATE TABLE [add_prop_service_agreement] (
  [add_prop_service_agreement_id]           [NVARCHAR](36) NOT NULL,
  [property_key]                            [NVARCHAR](50) NOT NULL,
  [property_value]                          [NVARCHAR](500),
  CONSTRAINT pk_add_prop_service_agreement  PRIMARY KEY CLUSTERED (add_prop_service_agreement_id, property_key),
  CONSTRAINT fk_apsa2sa                     FOREIGN KEY (add_prop_service_agreement_id) REFERENCES service_agreement (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

GO

CREATE TABLE [service_agreement_aps](
   [service_agreement_id]           [NVARCHAR](36)  NOT NULL,
   [assignable_permission_set_id]   [BIGINT]        NOT NULL,
   [type]                           [TINYINT]       NOT NULL,
   CONSTRAINT pk_sa_aps             PRIMARY KEY CLUSTERED (service_agreement_id, assignable_permission_set_id, type),
   CONSTRAINT fk_saapsd2sa          FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
   CONSTRAINT fk_saapsd2aps         FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id)
       ON UPDATE NO ACTION
       ON DELETE NO ACTION,
   INDEX ix_sa_aps_01 (assignable_permission_set_id)
);

GO

CREATE TABLE [participant] (
  [id]                          [NVARCHAR](36) NOT NULL,
  [legal_entity_id]             [NVARCHAR](36) NOT NULL,
  [service_agreement_id]        [NVARCHAR](36) NOT NULL,
  [share_users]                 [TINYINT]      NOT NULL,
  [share_accounts]              [TINYINT]      NOT NULL,
  CONSTRAINT pk_participant     PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_participant_01  UNIQUE (legal_entity_id, service_agreement_id),
  CONSTRAINT fk_prtc2le         FOREIGN KEY (legal_entity_id) REFERENCES legal_entity (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_prtc2sa         FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_participant_02 (service_agreement_id)
);

GO

CREATE TABLE [participant_user] (
  [id]                              [NVARCHAR](36) NOT NULL,
  [user_id]                         [NVARCHAR](36) NOT NULL,
  [participant_id]                  [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_participant_user    PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_participant_user_01 UNIQUE (user_id, participant_id),
  CONSTRAINT fk_pu2prtc             FOREIGN KEY (participant_id) REFERENCES participant (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_participant_user_02 (participant_id)
);

GO

CREATE TABLE [sa_admin] (
  [id]                      [NVARCHAR](36) NOT NULL,
  [user_id]                 [NVARCHAR](36) NOT NULL,
  [participant_id]          [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_sa_admin    PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_sa_admin_01 UNIQUE (user_id, participant_id),
  CONSTRAINT fk_adm2prtc    FOREIGN KEY (participant_id) REFERENCES participant (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_sa_admin_02 (participant_id)
);

GO

CREATE TABLE [function_group] (
  [id]                              [NVARCHAR](36)      NOT NULL,
  [name]                            [NVARCHAR](128)     NOT NULL,
  [description]                     [NVARCHAR](255)     NOT NULL,
  [type]                            [TINYINT] DEFAULT 0 NOT NULL,
  [service_agreement_id]            [NVARCHAR](36)      NOT NULL,
  [start_date]                      DATETIME,
  [end_date]                        DATETIME,
  [aps_id]                          [BIGINT],
  CONSTRAINT pk_function_group      PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_function_group_01   UNIQUE (service_agreement_id, name),
  CONSTRAINT fk_fg2sa               FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_fg2aps              FOREIGN KEY (aps_id) REFERENCES assignable_permission_set (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_function_group_01 (aps_id)
);

GO

CREATE TABLE [function_group_item] (
  [function_group_id]       [NVARCHAR](36) NOT NULL,
  [afp_id]                  [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_fgi         PRIMARY KEY CLUSTERED (function_group_id, afp_id),
  CONSTRAINT fk_fgi2afp     FOREIGN KEY (afp_id) REFERENCES applicable_function_privilege (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_fgi2fg      FOREIGN KEY (function_group_id) REFERENCES function_group (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_fgi_01 (afp_id)
);

GO

CREATE TABLE [data_group] (
  [id]                          [NVARCHAR](36)  NOT NULL,
  [name]                        [NVARCHAR](128) NOT NULL,
  [description]                 [NVARCHAR](255) NOT NULL,
  [type]                        [NVARCHAR](36)  NOT NULL,
  [service_agreement_id]        [NVARCHAR](36)  NOT NULL,
  CONSTRAINT pk_data_group      PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_data_group_01   UNIQUE (service_agreement_id, name),
  CONSTRAINT fk_dg2sa           FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

GO

CREATE TABLE [data_group_item] (
  [data_group_id]                   [NVARCHAR](36) NOT NULL,
  [data_item_id]                    [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_data_group_item     PRIMARY KEY CLUSTERED (data_group_id, data_item_id),
  CONSTRAINT fk_dgi2dg              FOREIGN KEY (data_group_id) REFERENCES data_group (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_data_group_item_01 (data_item_id)
);

GO

CREATE TABLE [user_context] (
  [id]                          [BIGINT]       NOT NULL,
  [service_agreement_id]        [NVARCHAR](36) NOT NULL,
  [user_id]                     [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_user_context    PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_user_context_01 UNIQUE (user_id, service_agreement_id),
  CONSTRAINT fk_uc2sa           FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_user_context_01 (service_agreement_id)
);

GO

CREATE TABLE [user_assigned_function_group] (
  [id]                      [BIGINT]       NOT NULL,
  [user_context_id]         [BIGINT]       NOT NULL,
  [function_group_id]       [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_uafg        PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_uafg_01     UNIQUE (user_context_id, function_group_id),
  CONSTRAINT fk_uafg2ua2    FOREIGN KEY (user_context_id) REFERENCES user_context (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_uafg2fg     FOREIGN KEY (function_group_id) REFERENCES function_group (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  INDEX ix_uafg_03 (function_group_id)
);

GO

CREATE TABLE [user_assigned_fg_combination] (
    [user_assigned_fg_id]       [BIGINT]       NOT NULL,
    [id]                        [BIGINT]       NOT NULL,
    CONSTRAINT pk_uafgc         PRIMARY KEY CLUSTERED (user_assigned_fg_id, id),
    CONSTRAINT uq_uafgc_01      UNIQUE (id),
    CONSTRAINT fk_uafgc2uafg    FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
);

GO

CREATE TABLE [user_assigned_combination_dg] (
    [ua_fg_combination_id]          [BIGINT]       NOT NULL,
    [data_group_id]                 [NVARCHAR](36) NOT NULL,
    CONSTRAINT pk_uacdg             PRIMARY KEY CLUSTERED (ua_fg_combination_id, data_group_id),
    CONSTRAINT fk_uacdg2uafgc       FOREIGN KEY (ua_fg_combination_id) REFERENCES user_assigned_fg_combination (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT fk_uacdg2dg          FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    INDEX ix_uacdg_01 (data_group_id)
);

GO

CREATE TABLE [access_control_approval] (
  [id]                                  [BIGINT]       NOT NULL,
  [approval_id]                         [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_access_control_approval PRIMARY KEY CLUSTERED (id)
);

GO

CREATE TABLE [approval_user_context] (
  [id]                                  [BIGINT]       NOT NULL,
  [user_id]                             [NVARCHAR](36) NOT NULL,
  [service_agreement_id]                [NVARCHAR](36) NOT NULL,
  [legal_entity_id]                     [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_approval_user_context   PRIMARY KEY CLUSTERED (id),
  CONSTRAINT fk_auc2aca                 FOREIGN KEY (id) REFERENCES access_control_approval (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

GO

CREATE TABLE [approval_uc_assign_fg] (
  [id]                                  [BIGINT]       NOT NULL,
  [approval_user_context_id]            [BIGINT]       NOT NULL,
  [function_group_id]                   [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_approval_uc_assign_fg   PRIMARY KEY CLUSTERED (id),
  CONSTRAINT fk_aucafg2auc              FOREIGN KEY (approval_user_context_id) REFERENCES approval_user_context (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_aucafg2fg               FOREIGN KEY (function_group_id) REFERENCES function_group (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    INDEX ix_aucafg_01 (approval_user_context_id),
    INDEX ix_aucafg_02 (function_group_id)
);

GO

CREATE TABLE [approval_uc_assign_fg_dg] (
  [approval_uc_assign_fg_id]                [BIGINT]       NOT NULL,
  [data_group_id]                           [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_approval_uc_assign_fg_dg    PRIMARY KEY CLUSTERED (approval_uc_assign_fg_id, data_group_id),
  CONSTRAINT fk_aucafgdg2aucafg             FOREIGN KEY (approval_uc_assign_fg_id) REFERENCES approval_uc_assign_fg (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_aucafgdg2dg                 FOREIGN KEY (data_group_id) REFERENCES data_group (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
    INDEX ix_aucafgdg_02 (data_group_id)
);

GO

CREATE TABLE [approval_data_group] (
  [id]                                  [BIGINT]      NOT NULL,
  [data_group_id]                       [NVARCHAR](36),
  CONSTRAINT pk_approval_data_group     PRIMARY KEY CLUSTERED (id),
  CONSTRAINT fk_adg2aca                 FOREIGN KEY (id) REFERENCES access_control_approval (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_data_group_detail] (
  [id]                                      [BIGINT]           NOT NULL,
  [service_agreement_id]                    [NVARCHAR](36)     NOT NULL,
  [name]                                    [NVARCHAR](128)    NOT NULL,
  [description]                             [NVARCHAR](255)    NOT NULL,
  [type]                                    [NVARCHAR](36)     NOT NULL,
  CONSTRAINT pk_approval_data_group_detail  PRIMARY KEY CLUSTERED (id),
  CONSTRAINT uq_adgd_01                     UNIQUE (service_agreement_id, name),
  CONSTRAINT fk_adgd2adg                    FOREIGN KEY (id) REFERENCES approval_data_group (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_data_group_item](
  [approval_data_group_id]                  [BIGINT]        NOT NULL,
  [data_item_id]                            [NVARCHAR](36)  NOT NULL,
  CONSTRAINT pk_approval_data_group_item    PRIMARY KEY CLUSTERED (approval_data_group_id, data_item_id),
  CONSTRAINT fk_adgi2adgd                   FOREIGN KEY (approval_data_group_id) REFERENCES approval_data_group_detail (id)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_function_group_ref](
    [id]                                        [BIGINT]       NOT NULL,
    [function_group_id]                         [NVARCHAR](36) NULL,
    CONSTRAINT pk_approval_function_group_ref   PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_afgr2aca                      FOREIGN KEY (id) REFERENCES access_control_approval(id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_function_group](
    [id]                                    [BIGINT]        NOT NULL,
    [name]                                  [NVARCHAR](128) NOT NULL,
    [description]                           [NVARCHAR](255) NOT NULL,
    [service_agreement_id]                  [NVARCHAR](36)  NOT NULL,
    [start_date]                            DATETIME        NULL,
    [end_date]                              DATETIME        NULL,
    [approval_type_id]                      [NVARCHAR](36)  NULL,
    CONSTRAINT pk_approval_function_group   PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_afg2afgr                  FOREIGN KEY (id) REFERENCES approval_function_group_ref(id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_function_group_item](
    [id]                                        [BIGINT]       NOT NULL,
    [afp_id]                                    [NVARCHAR](36) NOT NULL,
    CONSTRAINT pk_afgi                          PRIMARY KEY CLUSTERED (id, afp_id),
    CONSTRAINT fk_afgi2afg                      FOREIGN KEY (id) REFERENCES approval_function_group(id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_service_agreement_ref](
    [id]                                           [BIGINT]       NOT NULL,
    [service_agreement_id]                         [NVARCHAR](36) NULL,
    CONSTRAINT pk_approval_sa_ref                  PRIMARY KEY CLUSTERED (id),
    CONSTRAINT fk_asar2aca                         FOREIGN KEY (id) REFERENCES access_control_approval(id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_service_agreement] (
  [id]                                      [BIGINT]        NOT NULL,
  [external_id]                             [NVARCHAR](64),
  [name]                                    [NVARCHAR](128) NOT NULL,
  [description]                             [NVARCHAR](255) NOT NULL,
  [is_master]                               [TINYINT]       NOT NULL,
  [creator_legal_entity_id]                 [NVARCHAR](36)  NOT NULL,
  [state]                                   [NVARCHAR](16)  NOT NULL DEFAULT ('ENABLED'),
  [start_date]                               DATETIME,
  [end_date]                                 DATETIME,
  CONSTRAINT pk_approval_service_agreement   PRIMARY KEY CLUSTERED (id),
  CONSTRAINT fk_asa2asar                     FOREIGN KEY (id) REFERENCES approval_service_agreement_ref (id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

GO

CREATE UNIQUE INDEX uq_approval_sa_01
    ON approval_service_agreement (external_id)
    WHERE external_id IS NOT NULL;

GO

CREATE TABLE [approval_add_prop_sa] (
  [id]                                               [BIGINT]        NOT NULL,
  [property_key]                                     [NVARCHAR](50)  NOT NULL,
  [property_value]                                   [NVARCHAR](500),
  CONSTRAINT pk_approval_add_prop_sa                 PRIMARY KEY CLUSTERED (id,property_key),
  CONSTRAINT fk_aapsa2asa                            FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

GO

CREATE TABLE [approval_service_agreement_aps](
  [id]                             [BIGINT]        NOT NULL,
  [assignable_permission_set_id]   [BIGINT]        NOT NULL,
  [type]                           [TINYINT]       NOT NULL,
  CONSTRAINT pk_asa_aps            PRIMARY KEY CLUSTERED (id, assignable_permission_set_id, type),
  CONSTRAINT fk_asaa2asa           FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
     ON UPDATE NO ACTION
     ON DELETE NO ACTION
);

GO

CREATE TABLE [approval_sa_participant] (
  [id]                                                     [BIGINT]       NOT NULL,
  [legal_entity_id]                                        [NVARCHAR](36) NOT NULL,
  [share_users]                                            [TINYINT]      NOT NULL,
  [share_accounts]                                         [TINYINT]      NOT NULL,
  CONSTRAINT pk_approval_sa_participant                    PRIMARY KEY CLUSTERED (id,legal_entity_id),
  CONSTRAINT fk_asap2asa                                   FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
     ON DELETE NO ACTION
     ON UPDATE NO ACTION
);

GO

CREATE TABLE [approval_sa_admins] (
  [id]                                               [BIGINT]       NOT NULL,
  [legal_entity_id]                                  [NVARCHAR](36) NOT NULL,
  [user_id]                                          [NVARCHAR](36) NOT NULL,
  CONSTRAINT pk_approval_sa_admins                   PRIMARY KEY CLUSTERED (id,legal_entity_id,user_id),
  CONSTRAINT fk_asaa2asap                            FOREIGN KEY (id,legal_entity_id) REFERENCES approval_sa_participant(id,legal_entity_id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

GO
-- END OF TABLES CREATION