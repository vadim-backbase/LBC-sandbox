-- START OF THE SCRIPT
-- TABLES CREATION

CREATE TABLE sequence_table
(
    sequence_name                   VARCHAR(255) NOT NULL,
    next_val                        BIGINT,
    CONSTRAINT pk_sequence_table    PRIMARY KEY (sequence_name)
);

CREATE TABLE privilege
(
    id                          VARCHAR(36) NOT NULL,
    code                        VARCHAR(8)  NOT NULL,
    name                        VARCHAR(16) NOT NULL,
    CONSTRAINT pk_privilege     PRIMARY KEY (id),
    CONSTRAINT uq_privilege_01  UNIQUE (name),
    CONSTRAINT uq_privilege_02  UNIQUE (code)
);

CREATE TABLE business_function
(
    id                                  VARCHAR(36) NOT NULL,
    function_code                       VARCHAR(32) NOT NULL,
    function_name                       VARCHAR(32) NOT NULL,
    resource_code                       VARCHAR(32) NOT NULL,
    resource_name                       VARCHAR(32) NOT NULL,
    CONSTRAINT pk_business_function     PRIMARY KEY (id),
    CONSTRAINT uq_business_function_01  UNIQUE (function_name)
);

CREATE TABLE applicable_function_privilege
(
    id                      VARCHAR(36) NOT NULL,
    business_function_name  VARCHAR(32) NOT NULL,
    function_resource_name  VARCHAR(32) NOT NULL,
    privilege_name          VARCHAR(16) NOT NULL,
    supports_limit          TINYINT     NOT NULL,
    business_function_id    VARCHAR(36) NOT NULL,
    privilege_id            VARCHAR(36) NOT NULL,
    CONSTRAINT pk_afp       PRIMARY KEY (id),
    CONSTRAINT fk_afp2bf    FOREIGN KEY (business_function_id) REFERENCES business_function (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_afp2priv  FOREIGN KEY (privilege_id) REFERENCES privilege (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_afp_01 (business_function_id),
    KEY ix_afp_02 (privilege_id)
);

CREATE TABLE assignable_permission_set
(
    id                                      BIGINT                                                   NOT NULL,
    name                                    VARCHAR(128)                                             NOT NULL,
    description                             VARCHAR(255)                                             NOT NULL,
    type                                    TINYINT DEFAULT 2                                        NOT NULL,
    type_unique                             TINYINT as (if(type = 0 or type = 1, type, null)) STORED,
    CONSTRAINT pk_assignable_permission_set PRIMARY KEY (id),
    CONSTRAINT uq_aps_01                    UNIQUE (name),
    CONSTRAINT uq_aps_02                    UNIQUE (type, type_unique)
);

CREATE TABLE assignable_permission_set_item
(
    assignable_permission_set_id    BIGINT      NOT NULL,
    function_privilege_id           VARCHAR(36) NOT NULL,
    CONSTRAINT pk_aps_item          PRIMARY KEY (assignable_permission_set_id, function_privilege_id),
    CONSTRAINT fk_apsi2aps          FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_apsi2afp          FOREIGN KEY (function_privilege_id) REFERENCES applicable_function_privilege (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_aps_item_01 (function_privilege_id)
);

CREATE TABLE legal_entity
(
    id                              VARCHAR(36)  NOT NULL,
    external_id                     VARCHAR(64)  NOT NULL,
    name                            VARCHAR(128) NOT NULL,
    parent_id                       VARCHAR(36)  NULL,
    type                            VARCHAR(8)   NOT NULL DEFAULT 'CUSTOMER',
    CONSTRAINT pk_legal_entity      PRIMARY KEY (id),
    CONSTRAINT uq_legal_entity_01   UNIQUE (external_id),
    CONSTRAINT fk_le2le             FOREIGN KEY (parent_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_legal_entity_02 (name),
    KEY ix_legal_entity_03 (parent_id)
);

CREATE TABLE legal_entity_ancestor
(
    descendent_id                       VARCHAR(36) NOT NULL,
    ancestor_id                         VARCHAR(36) NOT NULL,
    CONSTRAINT pk_legal_entity_ancestor PRIMARY KEY (ancestor_id, descendent_id),
    CONSTRAINT fk_lea2le_01             FOREIGN KEY (descendent_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_lea2le_02             FOREIGN KEY (ancestor_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_legal_entity_ancestor_02 (descendent_id)
);

CREATE TABLE add_prop_legal_entity
(
    add_prop_legal_entity_id            VARCHAR(36) NOT NULL,
    property_key                        VARCHAR(50) NOT NULL,
    property_value                      VARCHAR(500),
    CONSTRAINT pk_add_prop_legal_entity PRIMARY KEY (add_prop_legal_entity_id, property_key),
    CONSTRAINT fk_aple2le               FOREIGN KEY (add_prop_legal_entity_id) REFERENCES legal_entity (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE service_agreement
(
    id                                  VARCHAR(36)                                    NOT NULL,
    external_id                         VARCHAR(64)                                    NULL,
    name                                VARCHAR(128)                                   NOT NULL,
    description                         VARCHAR(255)                                   NOT NULL,
    is_master                           TINYINT                                        NOT NULL,
    creator_legal_entity_id             VARCHAR(36)                                    NOT NULL,
    state                               VARCHAR(16)                                    NOT NULL DEFAULT 'ENABLED',
    start_date                          DATETIME                                       NULL,
    end_date                            DATETIME                                       NULL,
    state_changed_at                    TIMESTAMP                                      NULL,
    is_master_unique                    TINYINT as (if(is_master = 1, 1, null)) stored,
    CONSTRAINT pk_service_agreement     PRIMARY KEY (id),
    CONSTRAINT uq_service_agreement_02  UNIQUE (external_id),
    CONSTRAINT uq_service_agreement_03  UNIQUE (creator_legal_entity_id, is_master_unique),
    CONSTRAINT fk_sa2le                 FOREIGN KEY (creator_legal_entity_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_service_agreement_03 (creator_legal_entity_id)
);

CREATE TABLE add_prop_service_agreement
(
    add_prop_service_agreement_id               VARCHAR(36) NOT NULL,
    property_key                                VARCHAR(50) NOT NULL,
    property_value                              VARCHAR(500),
    CONSTRAINT pk_add_prop_service_agreement    PRIMARY KEY (add_prop_service_agreement_id, property_key),
    CONSTRAINT fk_apsa2sa                       FOREIGN KEY (add_prop_service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE service_agreement_aps
(
    service_agreement_id            VARCHAR(36) NOT NULL,
    assignable_permission_set_id    BIGINT      NOT NULL,
    type                            TINYINT     NOT NULL,
    CONSTRAINT pk_sa_aps            PRIMARY KEY (service_agreement_id, assignable_permission_set_id, type),
    CONSTRAINT fk_saapsd2sa         FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_saapsd2aps        FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_sa_aps_01 (assignable_permission_set_id)
);

CREATE TABLE participant
(
    id                              VARCHAR(36) NOT NULL,
    legal_entity_id                 VARCHAR(36) NOT NULL,
    service_agreement_id            VARCHAR(36) NOT NULL,
    share_users                     TINYINT     NOT NULL,
    share_accounts                  TINYINT     NOT NULL,
    CONSTRAINT pk_participant       PRIMARY KEY (id),
    CONSTRAINT uq_participant_01    UNIQUE (legal_entity_id, service_agreement_id),
    CONSTRAINT fk_prtc2le           FOREIGN KEY (legal_entity_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_prtc2sa           FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_participant_02 (service_agreement_id)
);

CREATE TABLE participant_user
(
    id                                  VARCHAR(36) NOT NULL,
    user_id                             VARCHAR(36) NOT NULL,
    participant_id                      VARCHAR(36) NOT NULL,
    CONSTRAINT pk_participant           PRIMARY KEY (id),
    CONSTRAINT uq_participant_user_01   UNIQUE (user_id, participant_id),
    CONSTRAINT fk_pu2prtc               FOREIGN KEY (participant_id) REFERENCES participant (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_participant_user_02 (participant_id)
);

CREATE TABLE sa_admin
(
    id                          VARCHAR(36) NOT NULL,
    user_id                     VARCHAR(36) NOT NULL,
    participant_id              VARCHAR(36) NOT NULL,
    CONSTRAINT pk_sa_admin      PRIMARY KEY (id),
    CONSTRAINT uq_sa_admin_01   UNIQUE (user_id, participant_id),
    CONSTRAINT fk_adm2prtc      FOREIGN KEY (participant_id) REFERENCES participant (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_sa_admin_02 (participant_id)
);

CREATE TABLE function_group
(
    id                                  VARCHAR(36)  NOT NULL,
    name                                VARCHAR(128) NOT NULL,
    description                         VARCHAR(255) NOT NULL,
    type                                TINYINT      NOT NULL DEFAULT 0,
    service_agreement_id                VARCHAR(36)  NOT NULL,
    start_date                          DATETIME     NULL,
    end_date                            DATETIME     NULL,
    aps_id                              BIGINT       NULL,
    CONSTRAINT pk_function_group        PRIMARY KEY (id),
    CONSTRAINT uq_function_group_01     UNIQUE (service_agreement_id, name),
    CONSTRAINT fk_fg2sa                 FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_fg2aps                FOREIGN KEY (aps_id) REFERENCES assignable_permission_set (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_function_group_01 (aps_id)
);

CREATE TABLE function_group_item
(
    function_group_id       VARCHAR(36) NOT NULL,
    afp_id                  VARCHAR(36) NOT NULL,
    CONSTRAINT pk_fgi       PRIMARY KEY (function_group_id, afp_id),
    CONSTRAINT fk_fgi2fg    FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_fgi2afp   FOREIGN KEY (afp_id) REFERENCES applicable_function_privilege (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_fgi_01 (afp_id)
);

CREATE TABLE data_group
(
    id                          VARCHAR(36)  NOT NULL,
    name                        VARCHAR(128) NOT NULL,
    description                 VARCHAR(255) NOT NULL,
    type                        VARCHAR(36)  NOT NULL,
    service_agreement_id        VARCHAR(36)  NOT NULL,
    CONSTRAINT pk_data_group    PRIMARY KEY (id),
    CONSTRAINT uq_data_group_01 UNIQUE (service_agreement_id, name),
    CONSTRAINT fk_dg2sa         FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE data_group_item
(
    data_group_id                   VARCHAR(36) NOT NULL,
    data_item_id                    VARCHAR(36) NOT NULL,
    CONSTRAINT pk_data_group_item   PRIMARY KEY (data_group_id, data_item_id),
    CONSTRAINT fk_dgi2dg            FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_data_group_item_01 (data_item_id)
);

CREATE TABLE user_context
(
    id                              BIGINT      NOT NULL,
    service_agreement_id            VARCHAR(36) NOT NULL,
    user_id                         VARCHAR(36) NOT NULL,
    CONSTRAINT pk_user_context      PRIMARY KEY (id),
    CONSTRAINT uq_user_context_01   UNIQUE (user_id, service_agreement_id),
    CONSTRAINT fk_uc2sa             FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_user_context_01 (service_agreement_id)
);

CREATE TABLE user_assigned_function_group
(
    id                      BIGINT      NOT NULL,
    user_context_id         BIGINT      NOT NULL,
    function_group_id       VARCHAR(36) NOT NULL,
    CONSTRAINT pk_uafg      PRIMARY KEY (id),
    CONSTRAINT uq_uafg_01   UNIQUE (user_context_id, function_group_id),
    CONSTRAINT fk_uafg2ua2  FOREIGN KEY (user_context_id) REFERENCES user_context (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_uafg2fg   FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_uafg_03 (function_group_id)
);

CREATE TABLE user_assigned_fg_combination
(
    user_assigned_fg_id         BIGINT      NOT NULL,
    id                          BIGINT      NOT NULL,
    CONSTRAINT pk_uafgc         PRIMARY KEY (user_assigned_fg_id, id),
    CONSTRAINT uq_uafgc_01      UNIQUE (id),
    CONSTRAINT fk_uafgc2uafg    FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE user_assigned_combination_dg
(
    ua_fg_combination_id            BIGINT      NOT NULL,
    data_group_id                   VARCHAR(36) NOT NULL,
    CONSTRAINT pk_uacdg             PRIMARY KEY (ua_fg_combination_id, data_group_id),
    CONSTRAINT fk_uacdg2uafgc       FOREIGN KEY (ua_fg_combination_id) REFERENCES user_assigned_fg_combination (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_uacdg2dg          FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_uacdg_01 (data_group_id)
);

CREATE TABLE access_control_approval
(
    id                                      BIGINT      NOT NULL,
    approval_id                             VARCHAR(36) NOT NULL,
    CONSTRAINT pk_access_control_approval   PRIMARY KEY (id)
);

CREATE TABLE approval_user_context
(
    id                                  BIGINT      NOT NULL,
    user_id                             VARCHAR(36) NOT NULL,
    service_agreement_id                VARCHAR(36) NOT NULL,
    legal_entity_id                     VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_user_context PRIMARY KEY (id),
    CONSTRAINT fk_auc2aca               FOREIGN KEY (id) REFERENCES access_control_approval (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_uc_assign_fg
(
    id                                  BIGINT      NOT NULL,
    approval_user_context_id            BIGINT      NOT NULL,
    function_group_id                   VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_uc_assign_fg PRIMARY KEY (id),
    CONSTRAINT fk_aucafg2auc            FOREIGN KEY (approval_user_context_id) REFERENCES approval_user_context (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_aucafg2fg             FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_aucafg_01 (approval_user_context_id),
    KEY ix_aucafg_02 (function_group_id)
);

CREATE TABLE approval_uc_assign_fg_dg
(
    approval_uc_assign_fg_id                BIGINT      NOT NULL,
    data_group_id                           VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_uc_assign_fg_dg  PRIMARY KEY (approval_uc_assign_fg_id, data_group_id),
    CONSTRAINT fk_aucafgdg2aucafg           FOREIGN KEY (approval_uc_assign_fg_id) REFERENCES approval_uc_assign_fg (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_aucafgdg2dg               FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    KEY ix_aucafgdg_02 (data_group_id)
);

CREATE TABLE approval_data_group
(
    id                                  BIGINT NOT NULL,
    data_group_id                       VARCHAR(36),
    CONSTRAINT pk_approval_data_group   PRIMARY KEY (id),
    CONSTRAINT fk_adg2aca               FOREIGN KEY (id) REFERENCES access_control_approval (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_data_group_detail
(
    id                                          BIGINT       NOT NULL,
    service_agreement_id                        VARCHAR(36)  NOT NULL,
    name                                        VARCHAR(128) NOT NULL,
    description                                 VARCHAR(255) NOT NULL,
    type                                        VARCHAR(36)  NOT NULL,
    CONSTRAINT pk_approval_data_group_detail    PRIMARY KEY (id),
    CONSTRAINT uq_adgd_01                       UNIQUE (service_agreement_id, name),
    CONSTRAINT fk_adgd2adg                      FOREIGN KEY (id) REFERENCES approval_data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_data_group_item
(
    approval_data_group_id                  BIGINT      NOT NULL,
    data_item_id                            VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_data_group_item  PRIMARY KEY (approval_data_group_id, data_item_id),
    CONSTRAINT fk_adgi2adgd                 FOREIGN KEY (approval_data_group_id) REFERENCES approval_data_group_detail (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_function_group_ref
(
    id                                          BIGINT NOT NULL,
    function_group_id                           VARCHAR(36) NULL,
    CONSTRAINT pk_approval_function_group_ref   PRIMARY KEY (id),
    CONSTRAINT fk_afgr2aca                      FOREIGN KEY (id) REFERENCES access_control_approval(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_function_group
(
    id                                      BIGINT NOT NULL,
    name                                    VARCHAR(128) NOT NULL,
    description                             VARCHAR(255) NOT NULL,
    service_agreement_id                    VARCHAR(36) NOT NULL,
    start_date                              DATETIME NULL,
    end_date                                DATETIME NULL,
    approval_type_id                        VARCHAR(36) NULL,
    CONSTRAINT pk_approval_function_group   PRIMARY KEY (id),
    CONSTRAINT fk_afg2afgr                  FOREIGN KEY (id) REFERENCES approval_function_group_ref(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_function_group_item
(
    id                                          BIGINT NOT NULL,
    afp_id                                      VARCHAR(36) NOT NULL,
    CONSTRAINT pk_afgi  PRIMARY KEY (id, afp_id),
    CONSTRAINT fk_afgi2afg                      FOREIGN KEY (id) REFERENCES approval_function_group(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_service_agreement_ref
(
    id                                             BIGINT           NOT NULL,
    service_agreement_id                           VARCHAR(36)      NULL,
    CONSTRAINT pk_approval_sa_ref                  PRIMARY KEY (id),
    CONSTRAINT fk_asar2aca                         FOREIGN KEY (id) REFERENCES access_control_approval(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_service_agreement
(
    id                                           BIGINT                                         NOT NULL,
    external_id                                  VARCHAR(64)                                    NULL,
    name                                         VARCHAR(128)                                   NOT NULL,
    description                                  VARCHAR(255)                                   NOT NULL,
    is_master                                    TINYINT                                        NOT NULL,
    creator_legal_entity_id                      VARCHAR(36)                                    NOT NULL,
    state                                        VARCHAR(16)                                    NOT NULL DEFAULT 'ENABLED',
    start_date                                   DATETIME                                       NULL,
    end_date                                     DATETIME                                       NULL,
    CONSTRAINT pk_approval_service_agreement     PRIMARY KEY (id),
    CONSTRAINT uq_approval_sa_01                 UNIQUE (external_id),
    CONSTRAINT fk_asa2asar                       FOREIGN KEY (id) REFERENCES approval_service_agreement_ref(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_add_prop_sa
(
    id                                                   BIGINT         NOT NULL,
    property_key                                         VARCHAR(50)    NOT NULL,
    property_value                                       VARCHAR(500),
    CONSTRAINT pk_approval_add_prop_sa                   PRIMARY KEY (id, property_key),
    CONSTRAINT fk_aapsa2asa                              FOREIGN KEY (id) REFERENCES approval_service_agreement(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
);

CREATE TABLE approval_service_agreement_aps
(
    id                              BIGINT      NOT NULL,
    assignable_permission_set_id    BIGINT      NOT NULL,
    type                            TINYINT     NOT NULL,
    CONSTRAINT pk_asa_aps           PRIMARY KEY (id, assignable_permission_set_id, type),
    CONSTRAINT fk_asaa2asa          FOREIGN KEY (id) REFERENCES approval_service_agreement(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_sa_participant
(
    id                                                         BIGINT      NOT NULL,
    legal_entity_id                                            VARCHAR(36) NOT NULL,
    share_users                                                TINYINT     NOT NULL,
    share_accounts                                             TINYINT     NOT NULL,
    CONSTRAINT pk_approval_sa_participant                      PRIMARY KEY (id,legal_entity_id),
    CONSTRAINT fk_asap2asa                                     FOREIGN KEY (id) REFERENCES approval_service_agreement(id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE approval_sa_admins
(
    id                                                  BIGINT      NOT NULL,
    legal_entity_id                                     VARCHAR(36) NOT NULL,
    user_id                                             VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_sa_admins                    PRIMARY KEY (id,legal_entity_id,user_id),
    CONSTRAINT fk_asaa2asap                             FOREIGN KEY (id,legal_entity_id) REFERENCES approval_sa_participant(id,legal_entity_id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- END OF TABLES CREATION