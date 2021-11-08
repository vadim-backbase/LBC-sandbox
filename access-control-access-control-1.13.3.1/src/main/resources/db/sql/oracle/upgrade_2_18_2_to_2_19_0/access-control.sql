-- assignable_permission_set_item
CREATE INDEX ix_aps_item_01 ON assignable_permission_set_item (function_privilege_id);

-- legal_entity
ALTER TABLE legal_entity
    DROP CONSTRAINT uq_legal_entity_01;
DROP INDEX ix_legal_entity_01;
ALTER TABLE legal_entity
    ADD CONSTRAINT uq_legal_entity_01 UNIQUE (external_id);

-- legal_entity_ancestor
-- Replace unique constraint with PK constraint in legal_entity_ancestor
ALTER TABLE legal_entity_ancestor
    DROP CONSTRAINT uq_legal_entity_ancestor_01;

ALTER TABLE legal_entity_ancestor
    ADD CONSTRAINT pk_legal_entity_ancestor PRIMARY KEY (ancestor_id, descendent_id);

-- service_agreement
ALTER TABLE service_agreement
    DROP CONSTRAINT uq_service_agreement_02;
DROP INDEX ix_service_agreement_02;
ALTER TABLE service_agreement
    ADD CONSTRAINT uq_service_agreement_02 UNIQUE (external_id);

--service_agreement_aps
CREATE INDEX ix_sa_aps_01 ON service_agreement_aps (assignable_permission_set_id);

-- participant
ALTER TABLE participant
    DROP CONSTRAINT uq_participant_01;
DROP INDEX ix_participant_01;
ALTER TABLE participant
    ADD CONSTRAINT uq_participant_01 UNIQUE (legal_entity_id, service_agreement_id);

-- participant_user
ALTER TABLE participant_user
    DROP CONSTRAINT uq_participant_user_01;
DROP INDEX ix_participant_user_01;
ALTER TABLE participant_user
    ADD CONSTRAINT uq_participant_user_01 UNIQUE (user_id, participant_id);

-- sa_admin
ALTER TABLE sa_admin
    DROP CONSTRAINT uq_sa_admin_01;
DROP INDEX ix_sa_admin_01;
ALTER TABLE sa_admin
    ADD CONSTRAINT uq_sa_admin_01 UNIQUE (user_id, participant_id);

-- function_group
-- Remove legal_entity_id from function_group
ALTER TABLE function_group_item DROP CONSTRAINT fk_fgi2fg;
ALTER TABLE user_assigned_function_group DROP CONSTRAINT fk_uafg2fg;
ALTER TABLE approval_uc_assign_fg DROP CONSTRAINT fk_aucafg2fg;

ALTER TABLE function_group DROP CONSTRAINT fk_fg2le;
ALTER TABLE function_group DROP CONSTRAINT fk_fg2sa;
ALTER TABLE function_group DROP CONSTRAINT fk_fg2aps;

ALTER TABLE function_group RENAME CONSTRAINT pk_function_group TO pk_function_group_old;
ALTER INDEX pk_function_group RENAME TO pk_function_group_old;
ALTER TABLE function_group RENAME CONSTRAINT uq_function_group_01 TO uq_function_group_01_old;
ALTER INDEX uq_function_group_01 RENAME TO uq_function_group_01_old;

ALTER TABLE function_group RENAME TO function_group_old;

CREATE TABLE function_group
(
  id                              VARCHAR2(36)        NOT NULL,
  name                            VARCHAR2(128)       NOT NULL,
  description                     VARCHAR2(255)       NOT NULL,
  type                            NUMBER(3) DEFAULT 0 NOT NULL,
  service_agreement_id            VARCHAR2(36)        NOT NULL,
  start_date                      TIMESTAMP,
  end_date                        TIMESTAMP,
  aps_id                          NUMBER(38, 0)
);

INSERT INTO function_group
(
    id,
    name,
    description,
    type,
    service_agreement_id,
    start_date,
    end_date,
    aps_id
)
SELECT
    fg_old.id,
    fg_old.name,
    fg_old.description,
    fg_old.type,
    fg_old.service_agreement_id,
    fg_old.start_date,
    fg_old.end_date,
    fg_old.aps_id
 FROM
    function_group_old fg_old;

COMMIT;

ALTER TABLE function_group ADD CONSTRAINT pk_function_group PRIMARY KEY (id);
ALTER TABLE function_group ADD CONSTRAINT uq_function_group_01 UNIQUE (service_agreement_id, name);
ALTER TABLE function_group ADD CONSTRAINT fk_fg2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id);
ALTER TABLE function_group ADD CONSTRAINT fk_fg2aps FOREIGN KEY (aps_id) REFERENCES assignable_permission_set (id);
CREATE INDEX ix_function_group_01 ON function_group (aps_id);

ALTER TABLE function_group_item
  ADD CONSTRAINT fk_fgi2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id);
ALTER TABLE user_assigned_function_group
  ADD CONSTRAINT fk_uafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id);
ALTER TABLE approval_uc_assign_fg
  ADD CONSTRAINT fk_aucafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id);

-- function_group_item
CREATE INDEX ix_fgi_01 ON function_group_item (afp_id);

-- user_context
DROP INDEX ix_user_context_01;

ALTER TABLE user_context DROP CONSTRAINT uq_user_context_01;
ALTER TABLE user_context ADD CONSTRAINT uq_user_context_01 UNIQUE (user_id, service_agreement_id);

ALTER TABLE user_context
  ADD CONSTRAINT fk_uc2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id);

CREATE INDEX ix_user_context_01 ON user_context (service_agreement_id);

-- Normalize user_assigned_function_group
ALTER TABLE user_assigned_fg_dg DROP CONSTRAINT fk_uafgdg2uafg;

ALTER TABLE user_assigned_function_group DROP CONSTRAINT fk_uafg2ua;
ALTER TABLE user_assigned_function_group DROP CONSTRAINT fk_uafg2ua2;
ALTER TABLE user_assigned_function_group DROP CONSTRAINT fk_uafg2sa;
ALTER TABLE user_assigned_function_group DROP CONSTRAINT fk_uafg2fg;

ALTER TABLE user_assigned_function_group RENAME CONSTRAINT pk_uafg TO pk_uafg_old;
ALTER INDEX pk_uafg RENAME TO pk_uafg_old;
ALTER TABLE user_assigned_function_group RENAME CONSTRAINT uq_uafg_01 TO uq_uafg_01_old;
ALTER INDEX uq_uafg_01 RENAME TO uq_uafg_01_old;

ALTER INDEX ix_uafg_02 RENAME TO ix_uafg_02_old;
ALTER INDEX ix_uafg_03 RENAME TO ix_uafg_03_old;
ALTER INDEX ix_uafg_04 RENAME TO ix_uafg_04_old;
ALTER INDEX ix_uafg_05 RENAME TO ix_uafg_05_old;

ALTER TABLE user_assigned_function_group RENAME TO uafg_old;

CREATE TABLE user_assigned_function_group
(
  id                     NUMBER(38, 0) NOT NULL,
  user_context_id        NUMBER(38, 0) NOT NULL,
  function_group_id      VARCHAR2(36)  NOT NULL
);

INSERT INTO user_assigned_function_group
(
    id,
    user_context_id,
    function_group_id
)
SELECT
    id,
    user_context_id,
    function_group_id
 FROM
    uafg_old;

COMMIT;

ALTER TABLE user_assigned_function_group ADD CONSTRAINT pk_uafg PRIMARY KEY (id);
ALTER TABLE user_assigned_function_group ADD CONSTRAINT uq_uafg_01 UNIQUE (user_context_id, function_group_id);
ALTER TABLE user_assigned_function_group ADD CONSTRAINT fk_uafg2ua2 FOREIGN KEY (user_context_id)   REFERENCES user_context (id);
ALTER TABLE user_assigned_function_group ADD CONSTRAINT fk_uafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id);
CREATE INDEX ix_uafg_03 ON user_assigned_function_group (function_group_id);

ALTER TABLE user_assigned_fg_dg
  ADD CONSTRAINT fk_uafgdg2uafg FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id);

-- Normalize user_assigned_fg_dg
ALTER TABLE user_assigned_fg_dg DROP CONSTRAINT fk_uafgdg2dg;
ALTER TABLE user_assigned_fg_dg DROP CONSTRAINT fk_uafgdg2uafg;

ALTER INDEX ix_user_assigned_fg_dg_02 RENAME TO ix_user_assigned_fg_dg_02_old;
ALTER TABLE user_assigned_fg_dg RENAME CONSTRAINT pk_user_assigned_fg_dg TO pk_user_assigned_fg_dg_old;
ALTER INDEX pk_user_assigned_fg_dg RENAME TO pk_user_assigned_fg_dg_old;
ALTER TABLE user_assigned_fg_dg RENAME CONSTRAINT uq_user_assigned_fg_dg_01 TO uq_user_assigned_fg_dg_01_old;
ALTER INDEX uq_user_assigned_fg_dg_01 RENAME TO uq_user_assigned_fg_dg_01_old;
ALTER TABLE user_assigned_fg_dg RENAME TO user_assigned_fg_dg_old;

CREATE TABLE user_assigned_fg_dg
(
  user_assigned_fg_id               NUMBER(38, 0) NOT NULL,
  data_group_id                     VARCHAR2(36) NOT NULL
);

INSERT INTO user_assigned_fg_dg
(
    user_assigned_fg_id,
    data_group_id
)
SELECT
    uafgdg.user_assigned_fg_id,
    uafgdg.data_group_id
 FROM
    user_assigned_fg_dg_old uafgdg;

COMMIT;

ALTER TABLE user_assigned_fg_dg
    ADD CONSTRAINT pk_user_assigned_fg_dg PRIMARY KEY (user_assigned_fg_id, data_group_id);
ALTER TABLE user_assigned_fg_dg
    ADD CONSTRAINT fk_uafgdg2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id);
ALTER TABLE user_assigned_fg_dg
    ADD CONSTRAINT fk_uafgdg2uafg FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id);

CREATE INDEX ix_user_assigned_fg_dg_02
  ON user_assigned_fg_dg (data_group_id);

-- approval_uc_assign_fg_dg
ALTER TABLE approval_uc_assign_fg_dg DROP CONSTRAINT fk_auc_afgdg2dg;
DROP INDEX ix_aucafgdg_01;
ALTER TABLE approval_uc_assign_fg_dg
    ADD CONSTRAINT fk_aucafgdg2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id);

-- approval_data_group_detail
ALTER TABLE approval_data_group_detail
    DROP CONSTRAINT uq_adgd_01;
DROP INDEX ix_adgd_01;
ALTER TABLE approval_data_group_detail
    ADD CONSTRAINT uq_adgd_01 UNIQUE (service_agreement_id, name);

-- approval_function_group_ref
ALTER TABLE approval_function_group_ref RENAME CONSTRAINT pk_afgr TO pk_approval_function_group_ref;
ALTER INDEX pk_afgr RENAME TO pk_approval_function_group_ref;

-- approval_function_group
ALTER TABLE approval_function_group
  DROP CONSTRAINT fk_afg2afgr;

ALTER TABLE approval_function_group RENAME CONSTRAINT pk_afg TO pk_afg_old;
ALTER INDEX pk_afg RENAME TO pk_afg_old;
ALTER TABLE approval_function_group RENAME TO approval_function_group_old;

CREATE TABLE approval_function_group
(
    id                     NUMBER(38, 0)     NOT NULL,
    name                   VARCHAR2(128)     NOT NULL,
    description            VARCHAR2(255)     NOT NULL,
    service_agreement_id   VARCHAR2(36)      NOT NULL,
    start_date             TIMESTAMP         NULL,
    end_date               TIMESTAMP         NULL,
    approval_type_id       VARCHAR2(36)      NULL
);

INSERT INTO approval_function_group
(
    id,
    name,
    description,
    service_agreement_id,
    start_date,
    end_date,
    approval_type_id
)
SELECT
    afg_old.id,
    afg_old.name,
    afg_old.description,
    afg_old.service_agreement_id,
    afg_old.start_date,
    afg_old.end_date,
    afg_old.approval_type_id
 FROM
    approval_function_group_old afg_old;

COMMIT;

ALTER TABLE approval_function_group
    ADD CONSTRAINT pk_approval_function_group PRIMARY KEY (id);
ALTER TABLE approval_function_group
    ADD CONSTRAINT fk_afg2afgr FOREIGN KEY (id) REFERENCES approval_function_group_ref(id);

-- approval_function_group_item
ALTER TABLE approval_function_group_item
  DROP CONSTRAINT fk_afgi2afg;

ALTER TABLE approval_function_group_item
  ADD CONSTRAINT fk_afgi2afg FOREIGN KEY (id ) REFERENCES approval_function_group(id);

COMMIT;

-- new business functions
INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1070','p2p.transfer', 'P2P Transfer', 'payments', 'Payments');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1071','payment.templates', 'Payment Templates', 'payments', 'Payments');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1072','flow.task.statistics', 'Access Task Statistics', 'flow', 'Flow');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1073','uk.chaps', 'UK CHAPS', 'payments', 'Payments');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1074','uk.faster.payments', 'UK Faster Payments', 'payments', 'Payments');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1075','emulate', 'Emulate', 'employee', 'Employee');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1076','act.on.behalf.of', 'Act on behalf of', 'employee', 'Employee');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1077','flow.collection', 'Access Collections', 'flow', 'Flow');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('266', 'P2P Transfer', 'Payments', 'view', 1, '1070', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('267', 'P2P Transfer', 'Payments', 'create', 1, '1070', '3');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('268', 'P2P Transfer', 'Payments', 'edit', 1, '1070', '4');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('269', 'P2P Transfer', 'Payments', 'delete', 1, '1070', '5');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('270', 'P2P Transfer', 'Payments', 'approve', 1, '1070', '6');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('271', 'P2P Transfer', 'Payments', 'cancel', 1, '1070', '7');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('272', 'Payment Templates', 'Payments', 'view', 0, '1071', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('273', 'Payment Templates', 'Payments', 'create', 0, '1071', '3');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('274', 'Payment Templates', 'Payments', 'edit', 0, '1071', '4');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('275', 'Payment Templates', 'Payments', 'delete', 0, '1071', '5');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('276', 'Payment Templates', 'Payments', 'approve', 0, '1071', '6');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('277', 'Access Task Statistics', 'Flow', 'view', 0, '1072', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('278', 'UK CHAPS', 'Payments', 'view', 1, '1073', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('279', 'UK CHAPS', 'Payments', 'create', 1, '1073', '3');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('280', 'UK CHAPS', 'Payments', 'edit', 1, '1073', '4');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('281', 'UK CHAPS', 'Payments', 'delete', 1, '1073', '5');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('282', 'UK CHAPS', 'Payments', 'approve', 1, '1073', '6');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('283', 'UK CHAPS', 'Payments', 'cancel', 1, '1073', '7');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('284', 'UK Faster Payments', 'Payments', 'view', 1, '1074', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('285', 'UK Faster Payments', 'Payments', 'create', 1, '1074', '3');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('286', 'UK Faster Payments', 'Payments', 'edit', 1, '1074', '4');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('287', 'UK Faster Payments', 'Payments', 'delete', 1, '1074', '5');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('288', 'UK Faster Payments', 'Payments', 'approve', 1, '1074', '6');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('289', 'UK Faster Payments', 'Payments', 'cancel', 1, '1074', '7');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('290', 'Emulate', 'Employee', 'view', 0, '1075', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
 ('291', 'Emulate', 'Employee', 'execute', 0, '1075', '1');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('292', 'Act on behalf of', 'Employee', 'execute', 0, '1076', '1');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('293', 'Access Collections', 'Flow', 'view', 0, '1077', '2');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '266');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '267');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '268');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '269');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '270');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '271');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '272');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '273');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '274');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '275');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '276');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '277');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '278');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '279');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '280');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '281');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '282');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '283');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '284');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '285');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '286');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '287');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '288');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '289');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '290');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '291');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '292');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '293');

COMMIT;

-- Cleanup
-- DROP TABLE user_ac;
-- DROP TABLE user_assigned_function_priv;
-- DROP TABLE user_assigned_function_priv_dg;
-- DROP TABLE function_group_old;
-- DROP TABLE uafg_old;
-- DROP TABLE user_assigned_fg_dg_old;
-- DROP TABLE approval_function_group_old;


-- IMPORTANT!!!
-- If the environment is migrated from versions older then 2.18.0 then most probably there are tables and sequences
-- in the databases which we don't use them anymore and we delete them with the statements below.

-- DROP TABLE user_access;
-- DROP TABLE tbl_uuid_user_context;
-- DROP TABLE tbl_uuid_uafg;
-- DROP SEQUENCE seq_user_context;
-- DROP SEQUENCE seq_uafg;
