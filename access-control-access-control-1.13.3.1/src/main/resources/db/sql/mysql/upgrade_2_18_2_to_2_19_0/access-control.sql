-- privilege
ALTER TABLE privilege
    DROP INDEX ix_privilege_01,
    DROP INDEX ix_privilege_02;

-- assignable_permission_set_item
CREATE INDEX ix_aps_item_01 ON assignable_permission_set_item (function_privilege_id);

-- legal_entity
ALTER TABLE legal_entity DROP INDEX ix_legal_entity_01;

-- legal_entity_ancestor
-- Replace unique constraint with PK constraint in legal_entity_ancestor
ALTER TABLE legal_entity_ancestor
    DROP FOREIGN KEY fk_lea2le_02,
    DROP INDEX ix_legal_entity_ancestor_01,
    DROP INDEX uq_legal_entity_ancestor_01;

ALTER TABLE legal_entity_ancestor
    ADD PRIMARY KEY (ancestor_id, descendent_id),
    ADD CONSTRAINT fk_lea2le_02 FOREIGN KEY (ancestor_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

-- service_agreement
ALTER TABLE service_agreement DROP INDEX ix_service_agreement_02;

-- service_agreement_aps
CREATE INDEX ix_sa_aps_01 ON service_agreement_aps (assignable_permission_set_id);

-- participant
ALTER TABLE participant DROP INDEX ix_participant_01;

-- participant_user
ALTER TABLE participant_user DROP INDEX ix_participant_user_01;

-- sa_admin
ALTER TABLE sa_admin DROP INDEX ix_sa_admin_01;

-- function_group
ALTER TABLE user_assigned_function_group DROP FOREIGN KEY fk_uafg2fg;
ALTER TABLE function_group_item DROP FOREIGN KEY fk_fgi2fg;
ALTER TABLE approval_uc_assign_fg DROP FOREIGN KEY fk_aucafg2fg;

ALTER TABLE function_group DROP FOREIGN KEY fk_fg2le;
ALTER TABLE function_group DROP FOREIGN KEY fk_fg2sa;
ALTER TABLE function_group DROP FOREIGN KEY fk_fg2aps;

ALTER TABLE function_group RENAME INDEX fk_fg2le TO fk_fg2le_old;
ALTER TABLE function_group RENAME INDEX uq_function_group_01 TO uq_function_group_01_old;
ALTER TABLE function_group RENAME TO function_group_old;

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
    CONSTRAINT pk_function_group        PRIMARY KEY (id)
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
    function_group_old      fg_old;

COMMIT;

CREATE INDEX ix_function_group_01 ON function_group (aps_id);

ALTER TABLE function_group
  ADD CONSTRAINT uq_function_group_01 UNIQUE (service_agreement_id, name);

ALTER TABLE function_group
    ADD CONSTRAINT fk_fg2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE function_group
    ADD CONSTRAINT fk_fg2aps FOREIGN KEY (aps_id) REFERENCES assignable_permission_set (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE user_assigned_function_group
    ADD CONSTRAINT fk_uafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;
ALTER TABLE function_group_item
    ADD CONSTRAINT fk_fgi2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;
alter table approval_uc_assign_fg
    add constraint fk_aucafg2fg foreign key (function_group_id) references function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

-- function_group_item
CREATE INDEX ix_fgi_01 ON function_group_item (afp_id);

-- data_group
ALTER TABLE data_group DROP INDEX ix_data_group_01;

-- user_context
-- Remove user_id index
ALTER TABLE user_context DROP INDEX ix_user_context_01;

ALTER TABLE user_context DROP INDEX uq_user_context_01;
ALTER TABLE user_context ADD CONSTRAINT uq_user_context_01 UNIQUE (user_id, service_agreement_id);

-- Create service_agreement_id index
CREATE INDEX ix_user_context_01 ON user_context (service_agreement_id);

-- Add FK constraint for service_agreement_id
ALTER TABLE user_context
    ADD CONSTRAINT fk_uc2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

-- Normalize user_assigned_function_group
ALTER TABLE user_assigned_function_group
  DROP FOREIGN KEY fk_uafg2sa;
ALTER TABLE user_assigned_function_group
  DROP FOREIGN KEY fk_uafg2fg;
ALTER TABLE user_assigned_function_group
  DROP FOREIGN KEY fk_uafg2ua;
ALTER TABLE user_assigned_function_group
  DROP FOREIGN KEY fk_uafg2ua2;

ALTER TABLE user_assigned_function_group RENAME INDEX uq_uafg_01 TO uq_uafg_01_old;

ALTER TABLE user_assigned_function_group RENAME TO uafg_old;

CREATE TABLE user_assigned_function_group
(
    id                      BIGINT      NOT NULL,
    user_context_id         BIGINT      NOT NULL,
    function_group_id       VARCHAR(36) NOT NULL,
    CONSTRAINT pk_uafg      PRIMARY KEY (id)
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

CREATE INDEX ix_uafg_03 ON user_assigned_function_group (function_group_id);

ALTER TABLE user_assigned_function_group
  ADD CONSTRAINT uq_uafg_01 UNIQUE (user_context_id, function_group_id);

ALTER TABLE user_assigned_function_group
    ADD CONSTRAINT fk_uafg2ua2  FOREIGN KEY (user_context_id) REFERENCES user_context (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;
ALTER TABLE user_assigned_function_group
    ADD CONSTRAINT fk_uafg2fg   FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

-- Normalize user_assigned_fg_dg
ALTER TABLE user_assigned_fg_dg
  DROP FOREIGN KEY fk_uafgdg2dg,
  DROP FOREIGN KEY fk_uafgdg2uafg;

ALTER TABLE user_assigned_fg_dg RENAME INDEX uq_user_assigned_fg_dg_01 TO uq_user_assigned_fg_dg_01_old;
ALTER TABLE user_assigned_fg_dg RENAME TO user_assigned_fg_dg_old;

CREATE TABLE user_assigned_fg_dg
(
  user_assigned_fg_id  BIGINT NOT NULL,
  data_group_id        VARCHAR(36) NOT NULL,
  PRIMARY KEY (user_assigned_fg_id, data_group_id)
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

commit;

CREATE INDEX ix_user_assigned_fg_dg_02 ON user_assigned_fg_dg (data_group_id);

ALTER TABLE user_assigned_fg_dg
    ADD CONSTRAINT fk_uafgdg2uafg FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE user_assigned_fg_dg
    ADD CONSTRAINT fk_uafgdg2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

-- approval_uc_assign_fg
CREATE INDEX ix_aucafg_01 ON approval_uc_assign_fg (approval_user_context_id);
CREATE INDEX ix_aucafg_02 ON approval_uc_assign_fg (function_group_id);

-- approval_uc_assign_fg_dg
CREATE INDEX ix_aucafgdg_02 ON approval_uc_assign_fg_dg (data_group_id);

commit;

-- new business functions
INSERT INTO business_function (id, function_code, function_name, resource_code, resource_name)
VALUES ('1070','p2p.transfer', 'P2P Transfer', 'payments', 'Payments'),
       ('1071','payment.templates', 'Payment Templates', 'payments', 'Payments'),
       ('1072','flow.task.statistics', 'Access Task Statistics', 'flow', 'Flow'),
       ('1073','uk.chaps', 'UK CHAPS', 'payments', 'Payments'),
       ('1074','uk.faster.payments', 'UK Faster Payments', 'payments', 'Payments'),
       ('1075','emulate', 'Emulate', 'employee', 'Employee'),
	   ('1076','act.on.behalf.of', 'Act on behalf of', 'employee', 'Employee'),
	   ('1077','flow.collection', 'Access Collections', 'flow', 'Flow');

INSERT INTO applicable_function_privilege (id,
                                           business_function_name,
                                           function_resource_name,
                                           privilege_name,
                                           supports_limit,
                                           business_function_id,
                                           privilege_id)
VALUES ('266', 'P2P Transfer', 'Payments', 'view', 1, '1070', '2'),
       ('267', 'P2P Transfer', 'Payments', 'create', 1, '1070', '3'),
       ('268', 'P2P Transfer', 'Payments', 'edit', 1, '1070', '4'),
       ('269', 'P2P Transfer', 'Payments', 'delete', 1, '1070', '5'),
       ('270', 'P2P Transfer', 'Payments', 'approve', 1, '1070', '6'),
       ('271', 'P2P Transfer', 'Payments', 'cancel', 1, '1070', '7'),
       ('272', 'Payment Templates', 'Payments', 'view', 0, '1071', '2'),
       ('273', 'Payment Templates', 'Payments', 'create', 0, '1071', '3'),
       ('274', 'Payment Templates', 'Payments', 'edit', 0, '1071', '4'),
       ('275', 'Payment Templates', 'Payments', 'delete', 0, '1071', '5'),
       ('276', 'Payment Templates', 'Payments', 'approve', 0, '1071', '6'),
       ('277', 'Access Task Statistics', 'Flow', 'view', 0, '1072', '2'),
       ('278', 'UK CHAPS', 'Payments', 'view', 1, '1073', '2'),
       ('279', 'UK CHAPS', 'Payments', 'create', 1, '1073', '3'),
       ('280', 'UK CHAPS', 'Payments', 'edit', 1, '1073', '4'),
       ('281', 'UK CHAPS', 'Payments', 'delete', 1, '1073', '5'),
       ('282', 'UK CHAPS', 'Payments', 'approve', 1, '1073', '6'),
       ('283', 'UK CHAPS', 'Payments', 'cancel', 1, '1073', '7'),
       ('284', 'UK Faster Payments', 'Payments', 'view', 1, '1074', '2'),
       ('285', 'UK Faster Payments', 'Payments', 'create', 1, '1074', '3'),
       ('286', 'UK Faster Payments', 'Payments', 'edit', 1, '1074', '4'),
       ('287', 'UK Faster Payments', 'Payments', 'delete', 1, '1074', '5'),
       ('288', 'UK Faster Payments', 'Payments', 'approve', 1, '1074', '6'),
       ('289', 'UK Faster Payments', 'Payments', 'cancel', 1, '1074', '7'),
       ('290', 'Emulate', 'Employee', 'view', 0, '1075', '2'),
       ('291', 'Emulate', 'Employee', 'execute', 0, '1075', '1'),
       ('292', 'Act on behalf of', 'Employee', 'execute', 0, '1076', '1'),
       ('293', 'Access Collections', 'Flow', 'view', 0, '1077', '2');

INSERT INTO assignable_permission_set_item (assignable_permission_set_id, function_privilege_id)
VALUES (1, '266'),
       (1, '267'),
       (1, '268'),
       (1, '269'),
       (1, '270'),
       (1, '271'),
       (1, '272'),
       (1, '273'),
       (1, '274'),
       (1, '275'),
       (1, '276'),
       (1, '277'),
       (1, '278'),
       (1, '279'),
       (1, '280'),
       (1, '281'),
       (1, '282'),
       (1, '283'),
       (1, '284'),
       (1, '285'),
       (1, '286'),
       (1, '287'),
       (1, '288'),
       (1, '289'),
       (1, '290'),
       (1, '291'),
       (1, '292'),
       (1, '293');

commit;

-- Cleanup
-- DROP TABLE user_ac;
-- DROP TABLE user_assigned_function_priv;
-- DROP TABLE user_assigned_function_priv_dg;
-- DROP TABLE function_group_old;
-- DROP TABLE uafg_old;
-- DROP TABLE user_assigned_fg_dg_old;


-- IMPORTANT!!!
-- If the environment is migrated from versions older then 2.18.0 then most probably there are tables and sequences
-- in the databases which we don't use them anymore and we delete them with the statements below.

-- DROP TABLE user_access;
-- DROP TABLE tbl_uuid_user_context;
-- DROP TABLE tbl_uuid_uafg;
