-- START OF THE SCRIPT
-- TABLES CREATION

CREATE TABLE sequence_table (
  sequence_name                VARCHAR2(255) NOT NULL,
  next_val                     NUMBER(38, 0),
  CONSTRAINT pk_sequence_table PRIMARY KEY (sequence_name)
);

CREATE TABLE privilege
(
  id                         VARCHAR2(36) NOT NULL,
  code                       VARCHAR2(8)  NOT NULL,
  name                       VARCHAR2(16) NOT NULL,
  CONSTRAINT pk_privilege    PRIMARY KEY (id),
  CONSTRAINT uq_privilege_01 UNIQUE (name),
  CONSTRAINT uq_privilege_02 UNIQUE (code)
);

CREATE TABLE business_function
(
  id                                 VARCHAR2(36) NOT NULL,
  function_code                      VARCHAR2(32) NOT NULL,
  function_name                      VARCHAR2(32) NOT NULL,
  resource_code                      VARCHAR2(32) NOT NULL,
  resource_name                      VARCHAR2(32) NOT NULL,
  CONSTRAINT pk_business_function    PRIMARY KEY (id),
  CONSTRAINT uq_business_function_01 UNIQUE (function_name)
);

CREATE TABLE applicable_function_privilege
(
  id                     VARCHAR2(36) NOT NULL,
  business_function_name VARCHAR2(32) NOT NULL,
  function_resource_name VARCHAR2(32) NOT NULL,
  privilege_name         VARCHAR2(16) NOT NULL,
  supports_limit         NUMBER(3)    NOT NULL,
  business_function_id   VARCHAR2(36) NOT NULL,
  privilege_id           VARCHAR2(36) NOT NULL,
  CONSTRAINT pk_afp      PRIMARY KEY (id),
  CONSTRAINT fk_afp2bf   FOREIGN KEY (business_function_id) REFERENCES business_function (id),
  CONSTRAINT fk_afp2priv FOREIGN KEY (privilege_id)         REFERENCES privilege (id)
);
CREATE INDEX ix_afp_01 ON applicable_function_privilege (business_function_id);
CREATE INDEX ix_afp_02 ON applicable_function_privilege (privilege_id);

CREATE TABLE assignable_permission_set
(
  id                                      NUMBER(38, 0)                  NOT NULL,
  name                                    VARCHAR2(128)                  NOT NULL,
  description                             VARCHAR2(255)                  NOT NULL,
  type                                    NUMBER(3)         DEFAULT 2    NOT NULL,
  CONSTRAINT pk_assignable_permission_set PRIMARY KEY (id),
  CONSTRAINT uq_aps_01                    UNIQUE (name)
);
CREATE UNIQUE INDEX ix_aps_01
    ON assignable_permission_set (case when TYPE = 0 then TYPE when TYPE = 1 THEN TYPE ELSE NULL  end);

CREATE TABLE assignable_permission_set_item
(
  assignable_permission_set_id  NUMBER(38, 0)                 NOT NULL,
  function_privilege_id         VARCHAR2(36)                  NOT NULL,
  CONSTRAINT pk_aps_item        PRIMARY KEY (assignable_permission_set_id, function_privilege_id),
  CONSTRAINT fk_apsi2aps        FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id),
  CONSTRAINT fk_apsi2afp        FOREIGN KEY (function_privilege_id)        REFERENCES applicable_function_privilege (id)
);
CREATE INDEX ix_aps_item_01 ON assignable_permission_set_item (function_privilege_id);

CREATE TABLE legal_entity
(
  id                            VARCHAR2(36)                   NOT NULL,
  external_id                   VARCHAR2(64)                   NOT NULL,
  name                          VARCHAR2(128)                  NOT NULL,
  parent_id                     VARCHAR2(36),
  type                          VARCHAR2(8) DEFAULT 'CUSTOMER' NOT NULL,
  CONSTRAINT pk_legal_entity    PRIMARY KEY (id),
  CONSTRAINT uq_legal_entity_01 UNIQUE (external_id),
  CONSTRAINT fk_le2le           FOREIGN KEY (parent_id) REFERENCES legal_entity (id)
);
CREATE INDEX ix_legal_entity_02 ON legal_entity (name);
CREATE INDEX ix_legal_entity_03 ON legal_entity (parent_id);

CREATE TABLE legal_entity_ancestor
(
  descendent_id                          VARCHAR2(36) NOT NULL,
  ancestor_id                            VARCHAR2(36) NOT NULL,
  CONSTRAINT pk_legal_entity_ancestor    PRIMARY KEY (ancestor_id, descendent_id),
  CONSTRAINT fk_lea2le_01                FOREIGN KEY (descendent_id) REFERENCES legal_entity (id),
  CONSTRAINT fk_lea2le_02                FOREIGN KEY (ancestor_id)   REFERENCES legal_entity (id)
);
CREATE INDEX ix_legal_entity_ancestor_02 ON legal_entity_ancestor (descendent_id);

CREATE TABLE add_prop_legal_entity (
  add_prop_legal_entity_id            VARCHAR2(36) NOT NULL,
  property_key                        VARCHAR2(50) NOT NULL,
  property_value                      VARCHAR2(500),
  CONSTRAINT pk_add_prop_legal_entity PRIMARY KEY (add_prop_legal_entity_id, property_key),
  CONSTRAINT fk_aple2le               FOREIGN KEY (add_prop_legal_entity_id) REFERENCES legal_entity (id)
);

CREATE TABLE service_agreement
(
  id                                    VARCHAR2(36)                   NOT NULL,
  external_id                           VARCHAR2(64),
  name                                  VARCHAR2(128)                  NOT NULL,
  description                           VARCHAR2(255)                  NOT NULL,
  is_master                             NUMBER(3)                      NOT NULL,
  creator_legal_entity_id               VARCHAR2(36)                   NOT NULL,
  state                                 VARCHAR2(16) DEFAULT 'ENABLED' NOT NULL,
  start_date                            TIMESTAMP,
  end_date                              TIMESTAMP,
  state_changed_at                      TIMESTAMP,
  CONSTRAINT pk_service_agreement       PRIMARY KEY (id),
  CONSTRAINT uq_service_agreement_02    UNIQUE (external_id),
  CONSTRAINT fk_sa2le                   FOREIGN KEY (creator_legal_entity_id) REFERENCES legal_entity (id)
);
CREATE INDEX ix_service_agreement_03 ON service_agreement (creator_legal_entity_id);
CREATE UNIQUE INDEX ix_service_agreement_04
    ON service_agreement (case when IS_MASTER = 1 then CREATOR_LEGAL_ENTITY_ID end);

CREATE TABLE add_prop_service_agreement (
  add_prop_service_agreement_id            VARCHAR2(36)   NOT NULL,
  property_key                             VARCHAR2(50)   NOT NULL,
  property_value                           VARCHAR2(500),
  CONSTRAINT pk_add_prop_service_agreement PRIMARY KEY (add_prop_service_agreement_id, property_key),
  CONSTRAINT fk_apsa2sa                    FOREIGN KEY (add_prop_service_agreement_id) REFERENCES service_agreement (id)
);

CREATE TABLE service_agreement_aps
(
  service_agreement_id           VARCHAR2(36)                NOT NULL,
  assignable_permission_set_id   NUMBER(38, 0)               NOT NULL,
  type                           NUMBER(3)                   NOT NULL,
  CONSTRAINT pk_sa_aps           PRIMARY KEY (service_agreement_id, assignable_permission_set_id, type),
  CONSTRAINT fk_saapsd2sa        FOREIGN KEY (service_agreement_id)         REFERENCES service_agreement (id),
  CONSTRAINT fk_saapsd2aps       FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id)
);
CREATE INDEX ix_sa_aps_01 ON service_agreement_aps (assignable_permission_set_id);

CREATE TABLE participant
(
  id                            VARCHAR2(36) NOT NULL,
  legal_entity_id               VARCHAR2(36) NOT NULL,
  service_agreement_id          VARCHAR2(36) NOT NULL,
  share_users                   NUMBER(3)    NOT NULL,
  share_accounts                NUMBER(3)    NOT NULL,
  CONSTRAINT pk_participant     PRIMARY KEY (id),
  CONSTRAINT uq_participant_01  UNIQUE (legal_entity_id, service_agreement_id),
  CONSTRAINT fk_prtc2le         FOREIGN KEY (legal_entity_id)      REFERENCES legal_entity (id),
  CONSTRAINT fk_prtc2sa         FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
);
CREATE INDEX ix_participant_02 ON participant (service_agreement_id);

CREATE TABLE participant_user
(
  id                                VARCHAR2(36) NOT NULL,
  user_id                           VARCHAR2(36) NOT NULL,
  participant_id                    VARCHAR2(36) NOT NULL,
  CONSTRAINT pk_participant_user    PRIMARY KEY (id),
  CONSTRAINT uq_participant_user_01 UNIQUE (user_id, participant_id),
  CONSTRAINT fk_pu2prtc             FOREIGN KEY (participant_id) REFERENCES participant (id)
);
CREATE INDEX ix_participant_user_02 ON participant_user (participant_id);

CREATE TABLE sa_admin
(
  id                        VARCHAR2(36) NOT NULL,
  user_id                   VARCHAR2(36) NOT NULL,
  participant_id            VARCHAR2(36) NOT NULL,
  CONSTRAINT pk_sa_admin    PRIMARY KEY (id),
  CONSTRAINT uq_sa_admin_01 UNIQUE (user_id, participant_id),
  CONSTRAINT fk_adm2prtc    FOREIGN KEY (participant_id) REFERENCES participant (id)
);
CREATE INDEX ix_sa_admin_02 ON sa_admin (participant_id);

CREATE TABLE function_group
(
  id                              VARCHAR2(36)        NOT NULL,
  name                            VARCHAR2(128)       NOT NULL,
  description                     VARCHAR2(255)       NOT NULL,
  type                            NUMBER(3) DEFAULT 0 NOT NULL,
  service_agreement_id            VARCHAR2(36)        NOT NULL,
  start_date                      TIMESTAMP,
  end_date                        TIMESTAMP,
  aps_id                          NUMBER(38, 0),
  CONSTRAINT pk_function_group    PRIMARY KEY (id),
  CONSTRAINT uq_function_group_01 UNIQUE (service_agreement_id, name),
  CONSTRAINT fk_fg2sa             FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id),
  CONSTRAINT fk_fg2aps            FOREIGN KEY (aps_id)               REFERENCES assignable_permission_set (id)
);
CREATE INDEX ix_function_group_01 ON function_group (aps_id);

CREATE TABLE function_group_item
(
  function_group_id      VARCHAR2(36) NOT NULL,
  afp_id                 VARCHAR2(36) NOT NULL,
  CONSTRAINT pk_fgi      PRIMARY KEY (function_group_id, afp_id),
  CONSTRAINT fk_fgi2fg   FOREIGN KEY (function_group_id) REFERENCES function_group (id),
  CONSTRAINT fk_fgi2afp  FOREIGN KEY (afp_id)            REFERENCES applicable_function_privilege (id)
);
CREATE INDEX ix_fgi_01 ON function_group_item (afp_id);

CREATE TABLE data_group
(
  id                          VARCHAR2(36)  NOT NULL,
  name                        VARCHAR2(128) NOT NULL,
  description                 VARCHAR2(255) NOT NULL,
  type                        VARCHAR2(36)  NOT NULL,
  service_agreement_id        VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_data_group    PRIMARY KEY (id),
  CONSTRAINT uq_data_group_01 UNIQUE (service_agreement_id, name),
  CONSTRAINT fk_dg2sa         FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
);

CREATE TABLE data_group_item
(
  data_group_id                 VARCHAR2(36) NOT NULL,
  data_item_id                  VARCHAR2(36) NOT NULL,
  CONSTRAINT pk_data_group_item PRIMARY KEY (data_group_id, data_item_id),
  CONSTRAINT fk_dgi2dg          FOREIGN KEY (data_group_id) REFERENCES data_group (id)
);
CREATE INDEX ix_data_group_item_01 ON data_group_item (data_item_id);

CREATE TABLE user_context
(
  id                            NUMBER(38, 0) NOT NULL,
  service_agreement_id          VARCHAR2(36)  NOT NULL,
  user_id                       VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_user_context    PRIMARY KEY (id),
  CONSTRAINT uq_user_context_01 UNIQUE (user_id, service_agreement_id),
  CONSTRAINT fk_uc2sa           FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
);
CREATE INDEX ix_user_context_01 ON user_context (service_agreement_id);

CREATE TABLE user_assigned_function_group
(
  id                     NUMBER(38, 0) NOT NULL,
  user_context_id        NUMBER(38, 0) NOT NULL,
  function_group_id      VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_uafg     PRIMARY KEY (id),
  CONSTRAINT uq_uafg_01  UNIQUE (user_context_id, function_group_id),
  CONSTRAINT fk_uafg2ua2 FOREIGN KEY (user_context_id)   REFERENCES user_context (id),
  CONSTRAINT fk_uafg2fg  FOREIGN KEY (function_group_id) REFERENCES function_group (id)
);
CREATE INDEX ix_uafg_03 ON user_assigned_function_group (function_group_id);

CREATE TABLE user_assigned_fg_combination
(
  user_assigned_fg_id           NUMBER(38, 0) NOT NULL,
  id                            NUMBER(38, 0) NOT NULL,
  CONSTRAINT pk_uafgc           PRIMARY KEY (user_assigned_fg_id, id),
  CONSTRAINT uq_uafgc_01        UNIQUE (id),
  CONSTRAINT fk_uafgc2uafg      FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id)
);

CREATE TABLE user_assigned_combination_dg
(
  ua_fg_combination_id          NUMBER(38, 0) NOT NULL,
  data_group_id                 VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_uacdg           PRIMARY KEY (ua_fg_combination_id, data_group_id),
  CONSTRAINT fk_uacdg2uafgc     FOREIGN KEY (ua_fg_combination_id) REFERENCES user_assigned_fg_combination (id),
  CONSTRAINT fk_uacdg2dg        FOREIGN KEY (data_group_id) REFERENCES data_group (id)
);
CREATE INDEX ix_uacdg_01 ON user_assigned_combination_dg (data_group_id);

CREATE TABLE access_control_approval (
  id                                    NUMBER(38, 0) NOT NULL,
  approval_id                           VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_access_control_approval PRIMARY KEY (id)
);

CREATE TABLE approval_user_context (
  id                                     NUMBER(38, 0) NOT NULL,
  user_id                                VARCHAR2(36)  NOT NULL,
  service_agreement_id                   VARCHAR2(36)  NOT NULL,
  legal_entity_id                        VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_approval_user_context    PRIMARY KEY (id),
  CONSTRAINT fk_auc2aca                  FOREIGN KEY (id) REFERENCES access_control_approval (id)
);

CREATE TABLE approval_uc_assign_fg (
  id                                  NUMBER(38, 0) NOT NULL,
  approval_user_context_id            NUMBER(38, 0) NOT NULL,
  function_group_id                   VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_approval_uc_assign_fg PRIMARY KEY (id),
  CONSTRAINT fk_aucafg2auc            FOREIGN KEY (approval_user_context_id) REFERENCES approval_user_context (id),
  CONSTRAINT fk_aucafg2fg             FOREIGN KEY (function_group_id)        REFERENCES function_group (id)
);
CREATE INDEX ix_aucafg_01 ON approval_uc_assign_fg (approval_user_context_id);
CREATE INDEX ix_aucafg_02 ON approval_uc_assign_fg (function_group_id);

CREATE TABLE approval_uc_assign_fg_dg (
  approval_uc_assign_fg_id                NUMBER(38, 0) NOT NULL,
  data_group_id                           VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_approval_uc_assign_fg_dg  PRIMARY KEY (approval_uc_assign_fg_id, data_group_id),
  CONSTRAINT fk_aucafgdg2aucafg           FOREIGN KEY (approval_uc_assign_fg_id) REFERENCES approval_uc_assign_fg (id),
  CONSTRAINT fk_aucafgdg2dg               FOREIGN KEY (data_group_id)            REFERENCES data_group (id)
);
CREATE INDEX ix_aucafgdg_02 ON approval_uc_assign_fg_dg (data_group_id);

CREATE TABLE approval_data_group (
  id                                NUMBER(38, 0) NOT NULL,
  data_group_id                     VARCHAR2(36),
  CONSTRAINT pk_approval_data_group PRIMARY KEY (id),
  CONSTRAINT fk_adg2aca             FOREIGN KEY (id) REFERENCES access_control_approval (id)
);

CREATE TABLE approval_data_group_detail (
  id                                        NUMBER(38, 0) NOT NULL,
  service_agreement_id                      VARCHAR2(36)  NOT NULL,
  name                                      VARCHAR2(128) NOT NULL,
  description                               VARCHAR2(255) NOT NULL,
  type                                      VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_approval_data_group_detail  PRIMARY KEY (id),
  CONSTRAINT uq_adgd_01                     UNIQUE (service_agreement_id, name),
  CONSTRAINT fk_adgd2adg                    FOREIGN KEY (id) REFERENCES approval_data_group (id)
);

CREATE TABLE  approval_data_group_item(
  approval_data_group_id                  NUMBER(38, 0) NOT NULL,
  data_item_id                            VARCHAR2(36)  NOT NULL,
  CONSTRAINT pk_approval_data_group_item  PRIMARY KEY (approval_data_group_id, data_item_id),
  CONSTRAINT fk_adgi2adgd                 FOREIGN KEY (approval_data_group_id) REFERENCES approval_data_group_detail (id)
);

CREATE TABLE approval_function_group_ref
(
    id                                          NUMBER(38, 0)     NOT NULL,
    function_group_id                           VARCHAR2(36)      NULL,
    CONSTRAINT pk_approval_function_group_ref   PRIMARY KEY (id),
    CONSTRAINT fk_afgr2aca                      FOREIGN KEY (id) REFERENCES access_control_approval(id)
);

CREATE TABLE approval_function_group
(
    id                                      NUMBER(38, 0)     NOT NULL,
    name                                    VARCHAR2(128)     NOT NULL,
    description                             VARCHAR2(255)     NOT NULL,
    service_agreement_id                    VARCHAR2(36)      NOT NULL,
    start_date                              TIMESTAMP         NULL,
    end_date                                TIMESTAMP         NULL,
    approval_type_id                        VARCHAR2(36)      NULL,
    CONSTRAINT pk_approval_function_group   PRIMARY KEY (id),
    CONSTRAINT fk_afg2afgr                  FOREIGN KEY (id) REFERENCES approval_function_group_ref(id)
);

CREATE TABLE approval_function_group_item
(
    id                     NUMBER(38, 0)   NOT NULL,
    afp_id                 VARCHAR2(36)    NOT NULL,
    CONSTRAINT pk_afgi     PRIMARY KEY (id, afp_id),
    CONSTRAINT fk_afgi2afg FOREIGN KEY (id) REFERENCES approval_function_group(id)
);

CREATE TABLE approval_service_agreement_ref
(
    id                                             NUMBER(38, 0)     NOT NULL,
    service_agreement_id                           VARCHAR2(36)      NULL,
    CONSTRAINT pk_approval_sa_ref                  PRIMARY KEY (id),
    CONSTRAINT fk_asar2aca                         FOREIGN KEY (id) REFERENCES access_control_approval(id)
);

CREATE TABLE approval_service_agreement
(
    id                                             NUMBER(38, 0)                  NOT NULL,
    external_id                                    VARCHAR2(64),
    name                                           VARCHAR2(128)                  NOT NULL,
    description                                    VARCHAR2(255)                  NOT NULL,
    is_master                                      NUMBER(3)                      NOT NULL,
    creator_legal_entity_id                        VARCHAR2(36)                   NOT NULL,
    state                                          VARCHAR2(16) DEFAULT 'ENABLED' NOT NULL,
    start_date                                     TIMESTAMP,
    end_date                                       TIMESTAMP,
    CONSTRAINT pk_approval_service_agreement       PRIMARY KEY (id),
    CONSTRAINT uq_approval_sa_01                   UNIQUE (external_id),
    CONSTRAINT fk_asa2asar                         FOREIGN KEY (id) REFERENCES approval_service_agreement_ref (id)
);

CREATE TABLE approval_add_prop_sa (
    id                                                NUMBER(38, 0)  NOT NULL,
    property_key                                      VARCHAR2(50)   NOT NULL,
    property_value                                    VARCHAR2(500),
    CONSTRAINT pk_approval_add_prop_sa                PRIMARY KEY (id,property_key),
    CONSTRAINT fk_aapsa2asa                           FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
    );

CREATE TABLE approval_service_agreement_aps
(
    id                             NUMBER(38, 0)               NOT NULL,
    assignable_permission_set_id   NUMBER(38, 0)               NOT NULL,
    type                           NUMBER(3)                   NOT NULL,
    CONSTRAINT pk_asa_aps          PRIMARY KEY (id, assignable_permission_set_id, type),
    CONSTRAINT fk_asaa2asa         FOREIGN KEY (id)   REFERENCES approval_service_agreement (id)
);

CREATE TABLE approval_sa_participant
(
    id                                                       NUMBER(38, 0) NOT NULL,
    legal_entity_id                                          VARCHAR2(36)  NOT NULL,
    share_users                                              NUMBER(3)     NOT NULL,
    share_accounts                                           NUMBER(3)     NOT NULL,
    CONSTRAINT pk_approval_sa_participant                    PRIMARY KEY (id,legal_entity_id),
    CONSTRAINT fk_asap2asa                                   FOREIGN KEY (id) REFERENCES approval_service_agreement (id)
);

CREATE TABLE approval_sa_admins
(
    id                                                 NUMBER(38, 0) NOT NULL,
    legal_entity_id                                    VARCHAR2(36)  NOT NULL,
    user_id                                            VARCHAR2(36)  NOT NULL,
    CONSTRAINT pk_approval_sa_admins                   PRIMARY KEY (id,legal_entity_id,user_id),
    CONSTRAINT fk_asaa2asap                            FOREIGN KEY (id,legal_entity_id) REFERENCES approval_sa_participant(id,legal_entity_id)
);

-- END OF TABLES CREATION