CREATE TABLE sa_admin
(
    id             VARCHAR(36) NOT NULL,
    user_id        VARCHAR(36) NOT NULL,
    participant_id VARCHAR(36) NOT NULL
);

CREATE TABLE applicable_function_privilege
(
    id                     VARCHAR(36) NOT NULL,
    business_function_name VARCHAR(32) NOT NULL,
    function_resource_name VARCHAR(32) NOT NULL,
    privilege_name         VARCHAR(16) NOT NULL,
    supports_limit         TINYINT     NOT NULL,
    business_function_id   VARCHAR(36) NOT NULL,
    privilege_id           VARCHAR(36) NOT NULL
);

CREATE TABLE business_function
(
    id            VARCHAR(36) NOT NULL,
    function_code VARCHAR(32) NOT NULL,
    function_name VARCHAR(32) NOT NULL,
    resource_code VARCHAR(32) NOT NULL,
    resource_name VARCHAR(32) NOT NULL
);

CREATE TABLE data_group
(
    id                   VARCHAR(36)  NOT NULL,
    type                 VARCHAR(36)  NOT NULL,
    description          VARCHAR(255) NOT NULL,
    name                 VARCHAR(128) NOT NULL,
    service_agreement_id VARCHAR(36)  NOT NULL
);

CREATE TABLE data_group_item
(
    data_item_id  VARCHAR(36) NOT NULL,
    data_group_id VARCHAR(36) NOT NULL
);

CREATE TABLE function_group
(
    id                   VARCHAR(36)  NOT NULL,
    description          VARCHAR(255) NOT NULL,
    name                 VARCHAR(128) NOT NULL,
    type                 TINYINT      NOT NULL DEFAULT 0,
    service_agreement_id VARCHAR(36)  NOT NULL,
    start_date           DATETIME     NULL,
    end_date             DATETIME     NULL,
    aps_id               BIGINT       NULL
);

CREATE TABLE function_group_item
(
    afp_id VARCHAR(36) NOT NULL,
    function_group_id             VARCHAR(36) NOT NULL
);

CREATE TABLE legal_entity
(
    id            VARCHAR(36)  NOT NULL,
    external_id   VARCHAR(64)  NOT NULL,
    name          VARCHAR(128) NOT NULL,
    parent_id     VARCHAR(36)  NULL,
    type          VARCHAR(8)   NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE legal_entity_ancestor
(
    descendent_id VARCHAR(36) NOT NULL,
    ancestor_id   VARCHAR(36) NOT NULL
);

CREATE TABLE participant
(
    id                   VARCHAR(36) NOT NULL,
    legal_entity_id      VARCHAR(36) NOT NULL,
    service_agreement_id VARCHAR(36) NOT NULL,
    share_users          TINYINT     NOT NULL,
    share_accounts       TINYINT     NOT NULL
);

CREATE TABLE privilege
(
    id   VARCHAR(36) NOT NULL,
    code VARCHAR(8)  NOT NULL,
    name VARCHAR(16) NOT NULL
);

CREATE TABLE participant_user
(
    id             VARCHAR(36) NOT NULL,
    user_id        VARCHAR(36) NOT NULL,
    participant_id VARCHAR(36) NOT NULL
);

CREATE TABLE service_agreement
(
    id                      VARCHAR(36)                                    NOT NULL,
    description             VARCHAR(255)                                   NOT NULL,
    is_master               TINYINT                                        NOT NULL,
    name                    VARCHAR(128)                                   NOT NULL,
    creator_legal_entity_id VARCHAR(36)                                    NOT NULL,
    external_id             VARCHAR(64)                                    NULL,
    state                   VARCHAR(16)                                    NOT NULL DEFAULT 'ENABLED',
    start_date              DATETIME                                       NULL,
    end_date                DATETIME                                       NULL,
    state_changed_at        TIMESTAMP                                      NULL,
    additional_properties   CLOB                    NULL
);

CREATE TABLE user_context
(
    id                   BIGINT      NOT NULL,
    service_agreement_id VARCHAR(36) NOT NULL,
    user_id              VARCHAR(36) NOT NULL
);

CREATE TABLE user_assigned_function_group
(
  id                   BIGINT      NOT NULL,
  function_group_id    VARCHAR(36) NOT NULL,
  user_context_id      BIGINT      NOT NULL
);

CREATE TABLE user_assigned_fg_combination (
    user_assigned_fg_id     BIGINT      NOT NULL,
    id                      BIGINT      NOT NULL,
);

CREATE TABLE user_assigned_combination_dg (
    ua_fg_combination_id            BIGINT      NOT NULL,
    data_group_id                   VARCHAR(36) NOT NULL,
);

CREATE TABLE add_prop_service_agreement
(
    add_prop_service_agreement_id VARCHAR(36) NOT NULL,
    property_value                VARCHAR(500),
    property_key                  VARCHAR(50) NOT NULL
);

CREATE TABLE add_prop_legal_entity
(
    add_prop_legal_entity_id VARCHAR(36) NOT NULL,
    property_value           VARCHAR(500),
    property_key             VARCHAR(50) NOT NULL
);

CREATE TABLE sequence_table
(
    sequence_name VARCHAR(255) NOT NULL,
    next_val      BIGINT
);

CREATE TABLE access_control_approval
(
    id          BIGINT      NOT NULL,
    approval_id VARCHAR(36) NOT NULL
);

CREATE TABLE approval_user_context
(
    id                   BIGINT      NOT NULL,
    user_id              VARCHAR(36) NOT NULL,
    service_agreement_id VARCHAR(36) NOT NULL,
    legal_entity_id      VARCHAR(36) NOT NULL
);

CREATE TABLE approval_uc_assign_fg
(
    id                       BIGINT      NOT NULL,
    approval_user_context_id BIGINT      NOT NULL,
    function_group_id        VARCHAR(36) NOT NULL
);

CREATE TABLE approval_uc_assign_fg_dg
(
    approval_uc_assign_fg_id BIGINT      NOT NULL,
    data_group_id            VARCHAR(36) NOT NULL
);

CREATE TABLE approval_data_group
(
    id            BIGINT NOT NULL,
    data_group_id VARCHAR(36)
);

CREATE TABLE approval_data_group_detail
(
    id                   BIGINT       NOT NULL,
    service_agreement_id VARCHAR(36)  NOT NULL,
    name                 VARCHAR(128) NOT NULL,
    description          VARCHAR(255) NOT NULL,
    type                 VARCHAR(36)  NOT NULL
);

CREATE TABLE approval_data_group_item
(
    approval_data_group_id BIGINT      NOT NULL,
    data_item_id           VARCHAR(36) NOT NULL
);

CREATE TABLE assignable_permission_set
(
    id          BIGINT                                                   NOT NULL,
    description VARCHAR(255)                                             NOT NULL,
    name        VARCHAR(128)                                             NOT NULL,
    type        TINYINT DEFAULT 2                                        NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE assignable_permission_set_item
(
    assignable_permission_set_id BIGINT      NOT NULL,
    function_privilege_id        VARCHAR(36) NOT NULL,
    PRIMARY KEY (assignable_permission_set_id, function_privilege_id)
);

CREATE TABLE service_agreement_aps
(
    service_agreement_id         VARCHAR(36) NOT NULL,
    assignable_permission_set_id BIGINT      NOT NULL,
    type                         TINYINT     NOT NULL,
    PRIMARY KEY (service_agreement_id, assignable_permission_set_id, type)
);

ALTER TABLE sequence_table
    ADD PRIMARY KEY (sequence_name);

ALTER TABLE access_control_approval
    ADD PRIMARY KEY (id);

ALTER TABLE approval_user_context
    ADD PRIMARY KEY (id);

ALTER TABLE approval_uc_assign_fg
    ADD PRIMARY KEY (id);

ALTER TABLE approval_uc_assign_fg_dg
    ADD PRIMARY KEY (approval_uc_assign_fg_id, data_group_id);

ALTER TABLE approval_data_group
    ADD PRIMARY KEY (id);

ALTER TABLE approval_data_group_detail
    ADD PRIMARY KEY (id);

ALTER TABLE approval_data_group_item
    ADD PRIMARY KEY (approval_data_group_id, data_item_id);

ALTER TABLE sa_admin
    ADD PRIMARY KEY (id);

ALTER TABLE applicable_function_privilege
    ADD PRIMARY KEY (id);

ALTER TABLE business_function
    ADD PRIMARY KEY (id);

ALTER TABLE data_group
    ADD PRIMARY KEY (id);

ALTER TABLE data_group_item
    ADD PRIMARY KEY (data_group_id, data_item_id);

ALTER TABLE function_group
    ADD PRIMARY KEY (id);

ALTER TABLE function_group_item
    ADD PRIMARY KEY (function_group_id, afp_id);

ALTER TABLE legal_entity
    ADD PRIMARY KEY (id);

ALTER TABLE participant
    ADD PRIMARY KEY (id);

ALTER TABLE privilege
    ADD PRIMARY KEY (id);

ALTER TABLE participant_user
    ADD PRIMARY KEY (id);

ALTER TABLE service_agreement
    ADD PRIMARY KEY (id);

ALTER TABLE user_context
    ADD PRIMARY KEY (id);

ALTER TABLE user_assigned_function_group
    ADD PRIMARY KEY (id);

ALTER TABLE user_assigned_fg_combination
    ADD PRIMARY KEY (user_assigned_fg_id, id);

ALTER TABLE user_assigned_combination_dg
    ADD PRIMARY KEY (ua_fg_combination_id, data_group_id);

ALTER TABLE add_prop_service_agreement
    ADD PRIMARY KEY (add_prop_service_agreement_id, property_key);

ALTER TABLE add_prop_legal_entity
    ADD PRIMARY KEY (add_prop_legal_entity_id, property_key);


ALTER TABLE privilege
    ADD CONSTRAINT uq_privilege_01 UNIQUE (name);

ALTER TABLE privilege
    ADD CONSTRAINT uq_privilege_02 UNIQUE (code);

ALTER TABLE business_function
    ADD CONSTRAINT uq_business_function_01 UNIQUE (function_name);

ALTER TABLE legal_entity_ancestor
    ADD CONSTRAINT uq_legal_entity_ancestor_01 UNIQUE (ancestor_id, descendent_id);

ALTER TABLE participant
    ADD CONSTRAINT uq_participant_01 UNIQUE (legal_entity_id, service_agreement_id);

ALTER TABLE legal_entity
    ADD CONSTRAINT uq_legal_entity_01 UNIQUE (external_id);

ALTER TABLE service_agreement
    ADD CONSTRAINT uq_service_agreement_02 UNIQUE (external_id);

ALTER TABLE sa_admin
    ADD CONSTRAINT uq_sa_admin_01 UNIQUE (user_id, participant_id);

ALTER TABLE participant_user
    ADD CONSTRAINT uq_participant_user_01 UNIQUE (user_id, participant_id);

ALTER TABLE user_assigned_function_group
    ADD CONSTRAINT uq_uafg_01 UNIQUE (user_context_id, function_group_id);

ALTER TABLE user_assigned_fg_combination
    ADD CONSTRAINT uq_uafgc_01 UNIQUE (id);

ALTER TABLE user_context
    ADD CONSTRAINT uq_user_context_01 UNIQUE (user_id, service_agreement_id);

ALTER TABLE function_group
  ADD CONSTRAINT uq_function_group_01 UNIQUE (service_agreement_id, name);

ALTER TABLE data_group
    ADD CONSTRAINT uq_data_group_01 UNIQUE (service_agreement_id, name);

ALTER TABLE approval_data_group_detail
    ADD CONSTRAINT uq_adgd_01 UNIQUE (service_agreement_id, name);

ALTER TABLE assignable_permission_set
    ADD CONSTRAINT uq_aps_01 UNIQUE (name);

CREATE INDEX ix_participant_user_01
    ON participant_user (user_id, participant_id);

CREATE INDEX ix_participant_user_02
    ON participant_user (participant_id);

CREATE INDEX ix_data_group_01
    ON data_group (service_agreement_id);

CREATE INDEX ix_afp_01
    ON applicable_function_privilege (business_function_id);

CREATE INDEX ix_data_group_item_01
    ON data_group_item (data_item_id);

CREATE INDEX ix_participant_01
    ON participant (legal_entity_id, service_agreement_id);

CREATE INDEX ix_participant_02
    ON participant (service_agreement_id);

CREATE INDEX ix_afp_02
    ON applicable_function_privilege (privilege_id);

CREATE INDEX ix_service_agreement_02
    ON service_agreement (external_id);

CREATE INDEX ix_service_agreement_03
    ON service_agreement (creator_legal_entity_id);

CREATE INDEX ix_legal_entity_ancestor_01
    ON legal_entity_ancestor (ancestor_id, descendent_id);

CREATE INDEX ix_legal_entity_ancestor_02
    ON legal_entity_ancestor (descendent_id);

CREATE INDEX ix_legal_entity_01
    ON legal_entity (external_id);

CREATE INDEX ix_legal_entity_02
    ON legal_entity (name);

CREATE INDEX ix_legal_entity_03
    ON legal_entity (parent_id);

CREATE INDEX ix_privilege_01
    ON privilege (name);

CREATE INDEX ix_privilege_02
    ON privilege (code);

CREATE INDEX ix_sa_admin_01
    ON sa_admin (user_id, participant_id);

CREATE INDEX ix_uacdg_01
    ON user_assigned_combination_dg(data_group_id);


ALTER TABLE approval_user_context
    ADD CONSTRAINT fk_auc2aca FOREIGN KEY (id) REFERENCES access_control_approval (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE approval_uc_assign_fg
    ADD CONSTRAINT fk_aucafg2auc FOREIGN KEY (approval_user_context_id) REFERENCES approval_user_context (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

alter table approval_uc_assign_fg
    add constraint fk_aucafg2fg foreign key (function_group_id) references function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE approval_uc_assign_fg_dg
    ADD CONSTRAINT fk_aucafgdg2aucafg FOREIGN KEY (approval_uc_assign_fg_id) REFERENCES approval_uc_assign_fg (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

alter table approval_uc_assign_fg_dg
    add constraint fk_aucafgdg2dg foreign key (data_group_id) references data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE approval_data_group
    ADD CONSTRAINT fk_adg2aca FOREIGN KEY (id) REFERENCES access_control_approval (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE approval_data_group_detail
    ADD CONSTRAINT fk_adgd2adg FOREIGN KEY (id) REFERENCES approval_data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE approval_data_group_item
    ADD CONSTRAINT fk_adgi2adgd FOREIGN KEY (approval_data_group_id) REFERENCES approval_data_group_detail (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE function_group_item
    ADD CONSTRAINT fk_fgi2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE participant_user
    ADD CONSTRAINT fk_pu2prtc FOREIGN KEY (participant_id) REFERENCES participant (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE participant
    ADD CONSTRAINT fk_prtc2le FOREIGN KEY (legal_entity_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE applicable_function_privilege
    ADD CONSTRAINT fk_afp2bf FOREIGN KEY (business_function_id) REFERENCES business_function (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE user_assigned_function_group
    ADD CONSTRAINT fk_uafg2ua2 FOREIGN KEY (user_context_id) REFERENCES user_context (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE data_group_item
    ADD CONSTRAINT fk_dgi2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE participant
    ADD CONSTRAINT fk_prtc2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE user_assigned_function_group
    ADD CONSTRAINT fk_uafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE user_assigned_fg_combination
    ADD CONSTRAINT fk_uafgc2uafg  FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE user_assigned_combination_dg
    ADD CONSTRAINT fk_uacdg2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE user_assigned_combination_dg
    ADD CONSTRAINT fk_uacdg2uafgc FOREIGN KEY (ua_fg_combination_id) REFERENCES user_assigned_fg_combination (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE applicable_function_privilege
    ADD CONSTRAINT fk_afp2priv FOREIGN KEY (privilege_id) REFERENCES privilege (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE data_group
    ADD CONSTRAINT fk_dg2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE service_agreement
    ADD CONSTRAINT fk_sa2le FOREIGN KEY (creator_legal_entity_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE sa_admin
    ADD CONSTRAINT fk_adm2prtc FOREIGN KEY (participant_id) REFERENCES participant (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE function_group
    ADD CONSTRAINT fk_fg2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE function_group
    ADD CONSTRAINT fk_fg2aps FOREIGN KEY (aps_id) REFERENCES assignable_permission_set (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE function_group_item
    ADD CONSTRAINT fk_fgi2afp FOREIGN KEY (afp_id) REFERENCES applicable_function_privilege (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE legal_entity_ancestor
    ADD CONSTRAINT fk_lea2le_01 FOREIGN KEY (descendent_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE legal_entity_ancestor
    ADD CONSTRAINT fk_lea2le_02 FOREIGN KEY (ancestor_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE legal_entity
    ADD CONSTRAINT fk_le2le FOREIGN KEY (parent_id) REFERENCES legal_entity (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE add_prop_service_agreement
    ADD CONSTRAINT fk_apsa2sa FOREIGN KEY (add_prop_service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE add_prop_legal_entity
    ADD CONSTRAINT fk_aple2le FOREIGN KEY (add_prop_legal_entity_id) REFERENCES legal_entity (id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT;

ALTER TABLE assignable_permission_set_item
    ADD CONSTRAINT fk_apsi2aps FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE assignable_permission_set_item
    ADD CONSTRAINT fk_apsi2afp FOREIGN KEY (function_privilege_id) REFERENCES applicable_function_privilege (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE service_agreement_aps
    ADD CONSTRAINT fk_saapsd2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

ALTER TABLE service_agreement_aps
    ADD CONSTRAINT fk_saapsd2aps FOREIGN KEY (assignable_permission_set_id) REFERENCES assignable_permission_set (id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

INSERT INTO privilege (id, code, name)
VALUES ('1', 'execute', 'execute'),
       ('2', 'view', 'view'),
       ('3', 'create', 'create'),
       ('4', 'edit', 'edit'),
       ('5', 'delete', 'delete'),
       ('6', 'approve', 'approve'),
       ('7', 'cancel', 'cancel');

INSERT INTO business_function (id, function_code, function_name, resource_code, resource_name)
VALUES ('1002', 'payments.sepa', 'SEPA CT', 'payments', 'Payments'),
       ('1003', 'transactions', 'Transactions', 'transactions', 'Transactions'),
       ('1005', 'contacts', 'Contacts', 'contacts', 'Contacts'),
       ('1006', 'product.summary', 'Product Summary', 'product.summary', 'Product Summary'),
       ('1007', 'assign.users', 'Assign Users', 'service.agreement', 'Service Agreement'),
       ('1009', 'assign.permissions', 'Assign Permissions', 'service.agreement', 'Service Agreement'),
       ('1010', 'manage.users', 'Manage Users', 'user', 'User'),
       ('1011', 'manage.legalentities', 'Manage Legal Entities', 'legalentity', 'Legal Entity'),
       ('1012', 'manage.limits', 'Manage Limits', 'limits', 'Limits'),
       ('1013', 'audit', 'Audit', 'audit', 'Audit'),
       ('1014', 'manage.shadow.limits', 'Manage Shadow Limits', 'limits', 'Limits'),
       ('1015', 'intra.company.payments', 'Intra Company Payments', 'payments', 'Payments'),
       ('1016', 'manage.statements', 'Manage Statements', 'account.statements', 'Account Statements'),
       ('1017', 'us.domestic.wire', 'US Domestic Wire', 'payments', 'Payments'),
       ('1018', 'us.foreign.wire', 'US Foreign Wire', 'payments', 'Payments'),
       ('1019', 'manage.data.groups', 'Manage Data Groups', 'entitlements', 'Entitlements'),
       ('1020', 'manage.function.groups', 'Manage Function Groups', 'entitlements', 'Entitlements'),
       ('1021', 'us.billpay.payments', 'US Billpay Payments', 'payments', 'Payments'),
       ('1022', 'us.billpay.payees', 'US Billpay Payees', 'contacts', 'Contacts'),
       ('1023', 'us.billpay.accounts', 'US Billpay Accounts', 'arrangements', 'Arrangements'),
       ('1024', 'us.billpay.payees.search', 'US Billpay Payees-Search', 'contacts', 'Contacts'),
       ('1025', 'us.billpay.payees.summary', 'US Billpay Payees-Summary', 'contacts', 'Contacts'),
       ('1026', 'us.billpay.enrolment', 'US Billpay Enrolment', 'billpay', 'Billpay'),
       ('1027', 'access.actions.history', 'Access Actions History', 'actions', 'Actions'),
       ('1028', 'manage.service.agreements', 'Manage Service Agreements', 'service.agreement', 'Service Agreement'),
       ('1029', 'manage.actions.recipes', 'Manage Action Recipes', 'actions', 'Actions'),
       ('1030', 'manage.notifications', 'Manage Notifications', 'notifications', 'Notifications'),
       ('1031', 'manage.topics', 'Manage Topics', 'message.center', 'Message Center'),
       ('1032', 'assign.approval.policies', 'Assign Approval Policies', 'approvals', 'Approvals'),
       ('1033', 'manage.approval.policy.level', 'Manage Approval Policy and Level', 'approvals', 'Approvals'),
       ('1034', 'manage.identities', 'Manage Identities', 'identities', 'Identities'),
       ('1035', 'manage.user.profiles', 'Manage User Profiles', 'user.profiles', 'User Profiles'),
       ('1036', 'support.access.payments', 'Support Access for Payments', 'support.access', 'Support Access'),
       ('1037', 'batch.sepa', 'Batch - SEPA CT', 'batch', 'Batch'),
       ('1038', 'manage.messages', 'Manage Messages', 'message.center', 'Message Center'),
       ('1039', 'supervise.messages', 'Supervise Messages', 'message.center', 'Message Center'),
       ('1040', 'manage.global.limits', 'Manage Global Limits', 'limits', 'Limits'),
       ('1041', 'ach.credit.transfer', 'ACH Credit Transfer', 'payments', 'Payments'),
       ('1042', 'ach.credit.intc', 'ACH Credit - Intracompany', 'payments', 'Payments'),
       ('1043', 'sepa.credit.transfer.intc', 'SEPA CT - Intracompany', 'payments', 'Payments'),
       ('1044', 'us.domestic.wire.intc', 'US Domestic Wire - Intracompany', 'payments', 'Payments'),
       ('1045', 'us.foreign.wire.intc', 'US Foreign Wire - Intracompany', 'payments', 'Payments'),
       ('1046', 'ach.debit', 'ACH Debit', 'payments', 'Payments'),
       ('1047', 'manage.budgets', 'Manage Budgets', 'personal.finance.management', 'Personal Finance Management'),
       ('1048', 'manage.saving.goals', 'Manage Saving Goals', 'personal.finance.management',
        'Personal Finance Management'),
       ('1049', 'lock.user', 'Lock User', 'identities', 'Identities'),
       ('1050', 'unlock.user', 'Unlock User', 'identities', 'Identities'),
       ('1051', 'manage.devices', 'Manage Devices', 'device', 'Device'),
       ('1052', 'sepa.credit.transfer.closed', 'SEPA CT - closed', 'payments', 'Payments'),
       ('1053', 'a2a.transfer', 'A2A Transfer', 'payments', 'Payments'),
       ('1054', 'flow.case', 'Manage Case', 'flow', 'Flow'),
       ('1055', 'flow.case.archive', 'Archive Case', 'flow', 'Flow'),
       ('1056', 'flow.case.document', 'Manage Case Documents', 'flow', 'Flow'),
       ('1057', 'flow.case.comment', 'Manage Case Comments', 'flow', 'Flow'),
       ('1058', 'flow.case.changelog', 'Access Case Changelog', 'flow', 'Flow'),
       ('1059', 'flow.case.statistics', 'Access Case Statistics', 'flow', 'Flow'),
       ('1060', 'flow.journey.statistics', 'Access Journey Statistics', 'flow', 'Flow'),
       ('1061', 'flow.journey.definitions', 'Access Journey Definitions', 'flow', 'Flow'),
       ('1062', 'flow.task.assign', 'Assign Task', 'flow', 'Flow'),
       ('1063', 'flow.task.dates', 'Manage Task Dates', 'flow', 'Flow'),
       ('1064', 'flow.task', 'Manage Task', 'flow', 'Flow'),
       ('1065', 'stop.checks', 'Stop Checks', 'payments', 'Payments'),
       ('1066', 'manage.other.users.devices', 'Manage Other User''s Devices', 'device', 'Device'),
       ('1067', 'batch.ach.credit', 'Batch - ACH Credit', 'batch', 'Batch'),
       ('1068', 'cash.flow.forecasting', 'Cash Flow Forecasting', 'cash.flow', 'Cash Flow'),
       ('1069', 'batch.ach.debit', 'Batch - ACH Debit', 'batch', 'Batch'),
       ('1070','p2p.transfer', 'P2P Transfer', 'payments', 'Payments'),
       ('1071','payment.templates', 'Payment Templates', 'payments', 'Payments'),
       ('1072','flow.task.statistics', 'Access Task Statistics', 'flow', 'Flow'),
       ('1073','uk.chaps', 'UK CHAPS', 'payments', 'Payments'),
       ('1074','uk.faster.payments', 'UK Faster Payments', 'payments', 'Payments'),
       ('1075', 'emulate', 'Emulate', 'employee', 'Employee'),
       ('1076', 'act.on.behalf.of', 'Act on behalf of', 'employee', 'Employee'),
       ('1077', 'flow.collection', 'Access Collections', 'flow', 'Flow'),
       ('1078', 'manage.authorized.users', 'Manage Authorized Users', 'user', 'User'),
       ('1079', 'batch.intracompany.payments', 'Batch - Intracompany Payments', 'batch', 'Batch'),
       ('1080', 'manage.employee.comments', 'Manage Employee Comments', 'comments', 'Comments'),
       ('1081', 'manage.positive.pay', 'Manage Positive Pay', 'payments', 'Payments'),
       ('1082', 'manage.arrangement.alias', 'Manage Arrangement Alias', 'product.summary', 'Product Summary'),
       ('1083', 'manage.pockets', 'Manage Pockets', 'personal.finance.management', 'Personal Finance Management'),
       ('1084', 'revoke.access', 'Revoke access', 'identities', 'Identities'),
       ('1085', 'product.summary.limited.view', 'Product Summary Limited View', 'product.summary', 'Product Summary'),
       ('1086', 'manage.engagement.events', 'Manage Engagement Events', 'actions', 'Actions'),
       ('1087', 'manage.portfolio', 'Manage Portfolio', 'portfolio', 'Portfolio'),
       ('1088', 'manage.order', 'Manage Order', 'portfolio', 'Portfolio'),
       ('1089', 'manage.cards', 'Manage Cards', 'personal.finance.management', 'Personal Finance Management'),
       ('1090', 'places', 'Places', 'personal.finance.management', 'Personal Finance Management'),
       ('1091', 'remote.deposit.capture', 'Remote Deposit Capture', 'payments', 'Payments'),
       ('1092', 'audit.emulation', 'Audit Emulation', 'audit', 'Audit'),
       ('1093', 'identity.impersonation', 'Identity Impersonation', 'identities', 'Identities'),
       ('1094', 'manage.aggregate.portfolio', 'Manage Aggregate Portfolio', 'portfolio', 'Portfolio'),
       ('1095', 'batch.templates', 'Batch Templates', 'batch', 'Batch'),
       ('1096', 'manage.ach.positive.pay', 'Manage ACH Positive Pay', 'payments', 'Payments'),
       ('1097', 'manage.account.reconciliation', 'Manage Account Reconciliation', 'payments', 'Payments'),
       ('1098', 'batch.list.confidential', 'Batch - List Confidential', 'batch', 'Batch'),
       ('1099', 'batch.manage.confidential', 'Batch - Manage Confidential', 'batch', 'Batch'),
       ('1100', 'manage.content', 'Manage Content', 'content', 'Content'),
       ('1101', 'loans.payment', 'Loans Payment', 'payments', 'Payments'),
       ('1102', 'loans.advance', 'Loans Advance', 'payments', 'Payments');



INSERT INTO applicable_function_privilege (id,
                                           business_function_name,
                                           function_resource_name,
                                           privilege_name,
                                           supports_limit,
                                           business_function_id,
                                           privilege_id)
VALUES ('3', 'SEPA CT', 'Payments', 'create', 1, '1002', '3'),
       ('4', 'Transactions', 'Transactions', 'view', 0, '1003', '2'),
       ('5', 'Transactions', 'Transactions', 'edit', 0, '1003', '4'),
       ('7', 'Contacts', 'Contacts', 'view', 0, '1005', '2'),
       ('8', 'Contacts', 'Contacts', 'create', 0, '1005', '3'),
       ('9', 'Contacts', 'Contacts', 'edit', 0, '1005', '4'),
       ('10', 'Contacts', 'Contacts', 'delete', 0, '1005', '5'),
       ('11', 'Product Summary', 'Product Summary', 'view', 0, '1006', '2'),
       ('12', 'Product Summary', 'Product Summary', 'edit', 0, '1006', '4'),
       ('13', 'Assign Users', 'Service Agreement', 'view', 0, '1007', '2'),
       ('14', 'Assign Users', 'Service Agreement', 'create', 0, '1007', '3'),
       ('15', 'Assign Users', 'Service Agreement', 'edit', 0, '1007', '4'),
       ('19', 'Assign Permissions', 'Service Agreement', 'view', 0, '1009', '2'),
       ('20', 'Assign Permissions', 'Service Agreement', 'create', 0, '1009', '3'),
       ('21', 'Assign Permissions', 'Service Agreement', 'edit', 0, '1009', '4'),
       ('22', 'Manage Users', 'User', 'view', 0, '1010', '2'),
       ('23', 'Manage Legal Entities', 'Legal Entity', 'view', 0, '1011', '2'),
       ('24', 'SEPA CT', 'Payments', 'view', 0, '1002', '2'),
       ('25', 'SEPA CT', 'Payments', 'edit', 0, '1002', '4'),
       ('26', 'SEPA CT', 'Payments', 'delete', 0, '1002', '5'),
       ('27', 'Manage Limits', 'Limits', 'view', 0, '1012', '2'),
       ('28', 'Manage Limits', 'Limits', 'create', 0, '1012', '3'),
       ('29', 'Manage Limits', 'Limits', 'edit', 0, '1012', '4'),
       ('30', 'Manage Limits', 'Limits', 'delete', 0, '1012', '5'),
       ('31', 'Audit', 'Audit', 'view', 0, '1013', '2'),
       ('32', 'Audit', 'Audit', 'create', 0, '1013', '3'),
       ('33', 'Manage Shadow Limits', 'Limits', 'view', 0, '1014', '2'),
       ('34', 'Manage Shadow Limits', 'Limits', 'create', 0, '1014', '3'),
       ('35', 'Manage Shadow Limits', 'Limits', 'edit', 0, '1014', '4'),
       ('36', 'Manage Shadow Limits', 'Limits', 'delete', 0, '1014', '5'),
       ('37', 'Intra Company Payments', 'Payments', 'view', 0, '1015', '2'),
       ('38', 'Intra Company Payments', 'Payments', 'create', 1, '1015', '3'),
       ('39', 'Intra Company Payments', 'Payments', 'edit', 0, '1015', '4'),
       ('40', 'Intra Company Payments', 'Payments', 'delete', 0, '1015', '5'),
       ('41', 'Intra Company Payments', 'Payments', 'approve', 0, '1015', '6'),
       ('42', 'Manage Statements', 'Account Statements', 'view', 0, '1016', '2'),
       ('43', 'US Domestic Wire', 'Payments', 'view', 0, '1017', '2'),
       ('44', 'US Domestic Wire', 'Payments', 'create', 1, '1017', '3'),
       ('45', 'US Domestic Wire', 'Payments', 'edit', 0, '1017', '4'),
       ('46', 'US Domestic Wire', 'Payments', 'delete', 0, '1017', '5'),
       ('47', 'US Domestic Wire', 'Payments', 'approve', 0, '1017', '6'),
       ('48', 'US Foreign Wire', 'Payments', 'view', 0, '1018', '2'),
       ('49', 'US Foreign Wire', 'Payments', 'create', 1, '1018', '3'),
       ('50', 'US Foreign Wire', 'Payments', 'edit', 0, '1018', '4'),
       ('51', 'US Foreign Wire', 'Payments', 'delete', 0, '1018', '5'),
       ('52', 'US Foreign Wire', 'Payments', 'approve', 0, '1018', '6'),
       ('53', 'SEPA CT', 'Payments', 'approve', 0, '1002', '6'),
       ('54', 'Manage Data Groups', 'Entitlements', 'view', 0, '1019', '2'),
       ('55', 'Manage Data Groups', 'Entitlements', 'create', 0, '1019', '3'),
       ('56', 'Manage Data Groups', 'Entitlements', 'edit', 0, '1019', '4'),
       ('57', 'Manage Data Groups', 'Entitlements', 'delete', 0, '1019', '5'),
       ('58', 'Manage Data Groups', 'Entitlements', 'approve', 0, '1019', '6'),
       ('59', 'Manage Function Groups', 'Entitlements', 'view', 0, '1020', '2'),
       ('60', 'Manage Function Groups', 'Entitlements', 'create', 0, '1020', '3'),
       ('61', 'Manage Function Groups', 'Entitlements', 'edit', 0, '1020', '4'),
       ('62', 'Manage Function Groups', 'Entitlements', 'delete', 0, '1020', '5'),
       ('63', 'Manage Function Groups', 'Entitlements', 'approve', 0, '1020', '6'),
       ('64', 'US Billpay Payments', 'Payments', 'view', 0, '1021', '2'),
       ('65', 'US Billpay Payments', 'Payments', 'create', 0, '1021', '3'),
       ('66', 'US Billpay Payments', 'Payments', 'edit', 0, '1021', '4'),
       ('67', 'US Billpay Payments', 'Payments', 'delete', 0, '1021', '5'),
       ('68', 'US Billpay Payees', 'Contacts', 'view', 0, '1022', '2'),
       ('69', 'US Billpay Payees', 'Contacts', 'create', 0, '1022', '3'),
       ('70', 'US Billpay Payees', 'Contacts', 'edit', 0, '1022', '4'),
       ('71', 'US Billpay Payees', 'Contacts', 'delete', 0, '1022', '5'),
       ('72', 'US Billpay Accounts', 'Arrangements', 'view', 0, '1023', '2'),
       ('73', 'US Billpay Payees-Search', 'Contacts', 'execute', 0, '1024', '1'),
       ('74', 'US Billpay Payees-Summary', 'Contacts', 'view', 0, '1025', '2'),
       ('75', 'US Billpay Enrolment', 'Billpay', 'execute', 0, '1026', '1'),
       ('76', 'US Billpay Enrolment', 'Billpay', 'view', 0, '1026', '2'),
       ('77', 'Access Actions History', 'Actions', 'execute', 0, '1027', '1'),
       ('78', 'Access Actions History', 'Actions', 'view', 0, '1027', '2'),
       ('79', 'Intra Company Payments', 'Payments', 'cancel', 0, '1015', '7'),
       ('80', 'US Domestic Wire', 'Payments', 'cancel', 0, '1017', '7'),
       ('81', 'US Foreign Wire', 'Payments', 'cancel', 0, '1018', '7'),
       ('82', 'SEPA CT', 'Payments', 'cancel', 0, '1002', '7'),
       ('83', 'Manage Service Agreements', 'Service Agreement', 'view', 0, '1028', '2'),
       ('84', 'Manage Service Agreements', 'Service Agreement', 'create', 0, '1028', '3'),
       ('85', 'Manage Service Agreements', 'Service Agreement', 'edit', 0, '1028', '4'),
       ('86', 'Manage Service Agreements', 'Service Agreement', 'delete', 0, '1028', '5'),
       ('87', 'Manage Service Agreements', 'Service Agreement', 'approve', 0, '1028', '6'),
       ('88', 'Manage Action Recipes', 'Actions', 'execute', 0, '1029', '1'),
       ('89', 'Manage Action Recipes', 'Actions', 'view', 0, '1029', '2'),
       ('90', 'Manage Action Recipes', 'Actions', 'create', 0, '1029', '3'),
       ('91', 'Manage Action Recipes', 'Actions', 'edit', 0, '1029', '4'),
       ('92', 'Manage Action Recipes', 'Actions', 'delete', 0, '1029', '5'),
       ('93', 'Manage Notifications', 'Notifications', 'view', 0, '1030', '2'),
       ('94', 'Manage Notifications', 'Notifications', 'create', 0, '1030', '3'),
       ('95', 'Manage Notifications', 'Notifications', 'delete', 0, '1030', '5'),
       ('96', 'Contacts', 'Contacts', 'approve', 0, '1005', '6'),
       ('97', 'Manage Topics', 'Message Center', 'execute', 0, '1031', '1'),
       ('98', 'Manage Topics', 'Message Center', 'view', 0, '1031', '2'),
       ('99', 'Manage Topics', 'Message Center', 'create', 0, '1031', '3'),
       ('100', 'Manage Topics', 'Message Center', 'edit', 0, '1031', '4'),
       ('101', 'Manage Topics', 'Message Center', 'delete', 0, '1031', '5'),
       ('102', 'Assign Approval Policies', 'Approvals', 'view', 0, '1032', '2'),
       ('103', 'Assign Approval Policies', 'Approvals', 'create', 0, '1032', '3'),
       ('104', 'Assign Approval Policies', 'Approvals', 'edit', 0, '1032', '4'),
       ('105', 'Assign Approval Policies', 'Approvals', 'delete', 0, '1032', '5'),
       ('106', 'Assign Approval Policies', 'Approvals', 'approve', 0, '1032', '6'),
       ('107', 'Manage Approval Policy and Level', 'Approvals', 'view', 0, '1033', '2'),
       ('108', 'Manage Approval Policy and Level', 'Approvals', 'create', 0, '1033', '3'),
       ('109', 'Manage Approval Policy and Level', 'Approvals', 'edit', 0, '1033', '4'),
       ('110', 'Manage Approval Policy and Level', 'Approvals', 'delete', 0, '1033', '5'),
       ('111', 'Manage Approval Policy and Level', 'Approvals', 'approve', 0, '1033', '6'),
       ('112', 'Manage Identities', 'Identities', 'view', 0, '1034', '2'),
       ('113', 'Manage Identities', 'Identities', 'create', 0, '1034', '3'),
       ('114', 'Manage Identities', 'Identities', 'edit', 0, '1034', '4'),
       ('115', 'Assign Permissions', 'Service Agreement', 'approve', 0, '1009', '6'),
       ('116', 'Manage User Profiles', 'User Profiles', 'view', 0, '1035', '2'),
       ('117', 'Manage User Profiles', 'User Profiles', 'edit', 0, '1035', '4'),
       ('118', 'Support Access for Payments', 'Support Access', 'view', 0, '1036', '2'),
       ('119', 'Batch - SEPA CT', 'Batch', 'view', 1, '1037', '2'),
       ('120', 'Batch - SEPA CT', 'Batch', 'create', 1, '1037', '3'),
       ('121', 'Batch - SEPA CT', 'Batch', 'edit', 1, '1037', '4'),
       ('122', 'Batch - SEPA CT', 'Batch', 'delete', 1, '1037', '5'),
       ('123', 'Batch - SEPA CT', 'Batch', 'approve', 1, '1037', '6'),
       ('124', 'Batch - SEPA CT', 'Batch', 'cancel', 1, '1037', '7'),
       ('125', 'Manage Messages', 'Message Center', 'view', 0, '1038', '2'),
       ('126', 'Manage Messages', 'Message Center', 'create', 0, '1038', '3'),
       ('127', 'Manage Messages', 'Message Center', 'edit', 0, '1038', '4'),
       ('128', 'Manage Messages', 'Message Center', 'delete', 0, '1038', '5'),
       ('129', 'Manage Messages', 'Message Center', 'approve', 0, '1038', '6'),
       ('130', 'Supervise Messages', 'Message Center', 'view', 0, '1039', '2'),
       ('131', 'Supervise Messages', 'Message Center', 'create', 0, '1039', '3'),
       ('132', 'Supervise Messages', 'Message Center', 'edit', 0, '1039', '4'),
       ('133', 'Supervise Messages', 'Message Center', 'delete', 0, '1039', '5'),
       ('134', 'Supervise Messages', 'Message Center', 'approve', 0, '1039', '6'),
       ('135', 'Manage Global Limits', 'Limits', 'view', 0, '1040', '2'),
       ('136', 'Manage Global Limits', 'Limits', 'create', 0, '1040', '3'),
       ('137', 'Manage Global Limits', 'Limits', 'edit', 0, '1040', '4'),
       ('138', 'Manage Global Limits', 'Limits', 'delete', 0, '1040', '5'),
       ('139', 'Manage Global Limits', 'Limits', 'approve', 0, '1040', '6'),
       ('140', 'Manage Notifications', 'Notifications', 'approve', '0', '1030', '6'),
       ('141', 'Manage Notifications', 'Notifications', 'edit', '0', '1030', '4'),
       ('142', 'ACH Credit Transfer', 'Payments', 'view', 1, '1041', '2'),
       ('143', 'ACH Credit Transfer', 'Payments', 'create', 1, '1041', '3'),
       ('144', 'ACH Credit Transfer', 'Payments', 'edit', 1, '1041', '4'),
       ('145', 'ACH Credit Transfer', 'Payments', 'delete', 1, '1041', '5'),
       ('146', 'ACH Credit Transfer', 'Payments', 'approve', 1, '1041', '6'),
       ('147', 'ACH Credit Transfer', 'Payments', 'cancel', 1, '1041', '7'),
       ('148', 'ACH Credit - Intracompany', 'Payments', 'view', 1, '1042', '2'),
       ('149', 'ACH Credit - Intracompany', 'Payments', 'create', 1, '1042', '3'),
       ('150', 'ACH Credit - Intracompany', 'Payments', 'edit', 1, '1042', '4'),
       ('151', 'ACH Credit - Intracompany', 'Payments', 'delete', 1, '1042', '5'),
       ('152', 'ACH Credit - Intracompany', 'Payments', 'approve', 1, '1042', '6'),
       ('153', 'ACH Credit - Intracompany', 'Payments', 'cancel', 1, '1042', '7'),
       ('154', 'SEPA CT - Intracompany', 'Payments', 'view', 1, '1043', '2'),
       ('155', 'SEPA CT - Intracompany', 'Payments', 'create', 1, '1043', '3'),
       ('156', 'SEPA CT - Intracompany', 'Payments', 'edit', 1, '1043', '4'),
       ('157', 'SEPA CT - Intracompany', 'Payments', 'delete', 1, '1043', '5'),
       ('158', 'SEPA CT - Intracompany', 'Payments', 'approve', 1, '1043', '6'),
       ('159', 'SEPA CT - Intracompany', 'Payments', 'cancel', 1, '1043', '7'),
       ('160', 'US Domestic Wire - Intracompany', 'Payments', 'view', 1, '1044', '2'),
       ('161', 'US Domestic Wire - Intracompany', 'Payments', 'create', 1, '1044', '3'),
       ('162', 'US Domestic Wire - Intracompany', 'Payments', 'edit', 1, '1044', '4'),
       ('163', 'US Domestic Wire - Intracompany', 'Payments', 'delete', 1, '1044', '5'),
       ('164', 'US Domestic Wire - Intracompany', 'Payments', 'approve', 1, '1044', '6'),
       ('165', 'US Domestic Wire - Intracompany', 'Payments', 'cancel', 1, '1044', '7'),
       ('166', 'US Foreign Wire - Intracompany', 'Payments', 'view', 1, '1045', '2'),
       ('167', 'US Foreign Wire - Intracompany', 'Payments', 'create', 1, '1045', '3'),
       ('168', 'US Foreign Wire - Intracompany', 'Payments', 'edit', 1, '1045', '4'),
       ('169', 'US Foreign Wire - Intracompany', 'Payments', 'delete', 1, '1045', '5'),
       ('170', 'US Foreign Wire - Intracompany', 'Payments', 'approve', 1, '1045', '6'),
       ('171', 'US Foreign Wire - Intracompany', 'Payments', 'cancel', 1, '1045', '7'),
       ('172', 'Manage Legal Entities', 'Legal Entity', 'create', '0', '1011', '3'),
       ('173', 'Manage Legal Entities', 'Legal Entity', 'edit', '0', '1011', '4'),
       ('174', 'Manage Legal Entities', 'Legal Entity', 'delete', '0', '1011', '5'),
       ('175', 'Manage Legal Entities', 'Legal Entity', 'approve', '0', '1011', '6'),
       ('176', 'ACH Debit', 'Payments', 'view', 1, '1046', '2'),
       ('177', 'ACH Debit', 'Payments', 'create', 1, '1046', '3'),
       ('178', 'ACH Debit', 'Payments', 'edit', 1, '1046', '4'),
       ('179', 'ACH Debit', 'Payments', 'delete', 1, '1046', '5'),
       ('180', 'ACH Debit', 'Payments', 'approve', 1, '1046', '6'),
       ('181', 'ACH Debit', 'Payments', 'cancel', 1, '1046', '7'),
       ('182', 'Manage Budgets', 'Personal Finance Management', 'view', 0, '1047', '2'),
       ('183', 'Manage Budgets', 'Personal Finance Management', 'create', 0, '1047', '3'),
       ('184', 'Manage Budgets', 'Personal Finance Management', 'edit', 0, '1047', '4'),
       ('185', 'Manage Budgets', 'Personal Finance Management', 'delete', 0, '1047', '5'),
       ('186', 'Manage Saving Goals', 'Personal Finance Management', 'view', 0, '1048', '2'),
       ('187', 'Manage Saving Goals', 'Personal Finance Management', 'create', 0, '1048', '3'),
       ('188', 'Manage Saving Goals', 'Personal Finance Management', 'edit', 0, '1048', '4'),
       ('189', 'Manage Saving Goals', 'Personal Finance Management', 'delete', 0, '1048', '5'),
       ('190', 'Manage Shadow Limits', 'Limits', 'approve', 0, '1014', '6'),
       ('191', 'Manage Limits', 'Limits', 'approve', 0, '1012', '6'),
       ('192', 'Lock User', 'Identities', 'view', 0, '1049', '2'),
       ('193', 'Lock User', 'Identities', 'create', 0, '1049', '3'),
       ('194', 'Lock User', 'Identities', 'edit', 0, '1049', '4'),
       ('195', 'Lock User', 'Identities', 'approve', 0, '1049', '6'),
       ('196', 'Unlock User', 'Identities', 'view', 0, '1050', '2'),
       ('197', 'Unlock User', 'Identities', 'create', 0, '1050', '3'),
       ('198', 'Unlock User', 'Identities', 'edit', 0, '1050', '4'),
       ('199', 'Unlock User', 'Identities', 'approve', 0, '1050', '6'),
       ('200', 'Manage Identities', 'Identities', 'approve', 0, '1034', '6'),
       ('201', 'Manage Devices', 'Device', 'view', 0, '1051', '2'),
       ('202', 'Manage Devices', 'Device', 'edit', 0, '1051', '4'),
       ('203', 'SEPA CT - closed', 'Payments', 'view', 1, '1052', '2'),
       ('204', 'SEPA CT - closed', 'Payments', 'create', 1, '1052', '3'),
       ('205', 'SEPA CT - closed', 'Payments', 'edit', 1, '1052', '4'),
       ('206', 'SEPA CT - closed', 'Payments', 'delete', 1, '1052', '5'),
       ('207', 'SEPA CT - closed', 'Payments', 'approve', 1, '1052', '6'),
       ('208', 'SEPA CT - closed', 'Payments', 'cancel', 1, '1052', '7'),
       ('209', 'A2A Transfer', 'Payments', 'view', 1, '1053', '2'),
       ('210', 'A2A Transfer', 'Payments', 'create', 1, '1053', '3'),
       ('211', 'A2A Transfer', 'Payments', 'edit', 1, '1053', '4'),
       ('212', 'A2A Transfer', 'Payments', 'delete', 1, '1053', '5'),
       ('213', 'A2A Transfer', 'Payments', 'approve', 1, '1053', '6'),
       ('214', 'A2A Transfer', 'Payments', 'cancel', 1, '1053', '7'),
       ('215', 'Manage Case', 'Flow', 'view', 0, '1054', '2'),
       ('216', 'Manage Case', 'Flow', 'create', 0, '1054', '3'),
       ('217', 'Manage Case', 'Flow', 'edit', 0, '1054', '4'),
       ('218', 'Manage Case', 'Flow', 'delete', 0, '1054', '5'),
       ('219', 'Archive Case', 'Flow', 'execute', 0, '1055', '1'),
       ('220', 'Manage Case Documents', 'Flow', 'view', 0, '1056', '2'),
       ('221', 'Manage Case Documents', 'Flow', 'create', 0, '1056', '3'),
       ('222', 'Manage Case Documents', 'Flow', 'delete', 0, '1056', '5'),
       ('223', 'Manage Case Comments', 'Flow', 'view', 0, '1057', '2'),
       ('224', 'Manage Case Comments', 'Flow', 'create', 0, '1057', '3'),
       ('225', 'Manage Case Comments', 'Flow', 'edit', 0, '1057', '4'),
       ('226', 'Manage Case Comments', 'Flow', 'delete', 0, '1057', '5'),
       ('227', 'Access Case Changelog', 'Flow', 'view', 0, '1058', '2'),
       ('228', 'Access Case Statistics', 'Flow', 'view', 0, '1059', '2'),
       ('229', 'Access Journey Statistics', 'Flow', 'view', 0, '1060', '2'),
       ('230', 'Access Journey Definitions', 'Flow', 'view', 0, '1061', '2'),
       ('231', 'Assign Task', 'Flow', 'execute', 0, '1062', '1'),
       ('232', 'Manage Task Dates', 'Flow', 'view', 0, '1063', '2'),
       ('233', 'Manage Task Dates', 'Flow', 'create', 0, '1063', '3'),
       ('234', 'Manage Task Dates', 'Flow', 'edit', 0, '1063', '4'),
       ('235', 'Manage Task Dates', 'Flow', 'delete', 0, '1063', '5'),
       ('236', 'Manage Task', 'Flow', 'execute', 0, '1064', '1'),
       ('237', 'Manage Task', 'Flow', 'view', 0, '1064', '2'),
       ('238', 'Stop Checks', 'Payments', 'view', 0, '1065', '2'),
       ('239', 'Stop Checks', 'Payments', 'create', 0, '1065', '3'),
       ('240', 'Stop Checks', 'Payments', 'edit', 0, '1065', '4'),
       ('241', 'Stop Checks', 'Payments', 'delete', 0, '1065', '5'),
       ('242', 'Stop Checks', 'Payments', 'approve', 0, '1065', '6'),
       ('243', 'Stop Checks', 'Payments', 'cancel', 0, '1065', '7'),
       ('244', 'Manage Devices', 'Device', 'delete', 0, '1051', '5'),
       ('245', 'Manage Other User''s Devices', 'Device', 'view', 0, '1066', '2'),
       ('246', 'Manage Other User''s Devices', 'Device', 'edit', 0, '1066', '4'),
       ('247', 'Manage Other User''s Devices', 'Device', 'delete', 0, '1066', '5'),
       ('248', 'Batch - ACH Credit', 'Batch', 'view', 1, '1067', '2'),
       ('249', 'Batch - ACH Credit', 'Batch', 'create', 1, '1067', '3'),
       ('250', 'Batch - ACH Credit', 'Batch', 'edit', 1, '1067', '4'),
       ('251', 'Batch - ACH Credit', 'Batch', 'delete', 1, '1067', '5'),
       ('252', 'Batch - ACH Credit', 'Batch', 'approve', 1, '1067', '6'),
       ('253', 'Batch - ACH Credit', 'Batch', 'cancel', 1, '1067', '7'),
       ('254', 'Cash Flow Forecasting', 'Cash Flow', 'view', 1, '1068', '2'),
       ('255', 'Cash Flow Forecasting', 'Cash Flow', 'create', 1, '1068', '3'),
       ('256', 'Cash Flow Forecasting', 'Cash Flow', 'edit', 1, '1068', '4'),
       ('257', 'Cash Flow Forecasting', 'Cash Flow', 'delete', 1, '1068', '5'),
       ('258', 'Manage User Profiles', 'User Profiles', 'create', 0, '1035', '3'),
       ('259', 'Manage User Profiles', 'User Profiles', 'delete', 0, '1035', '5'),
       ('260', 'Batch - ACH Debit', 'Batch', 'view', 1, '1069', '2'),
       ('261', 'Batch - ACH Debit', 'Batch', 'create', 1, '1069', '3'),
       ('262', 'Batch - ACH Debit', 'Batch', 'edit', 1, '1069', '4'),
       ('263', 'Batch - ACH Debit', 'Batch', 'delete', 1, '1069', '5'),
       ('264', 'Batch - ACH Debit', 'Batch', 'approve', 1, '1069', '6'),
       ('265', 'Batch - ACH Debit', 'Batch', 'cancel', 1, '1069', '7'),
       ('266', 'P2P Transfer', 'Payments', 'view', 1, '1070', '2'),
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
       ('293', 'Access Collections', 'Flow', 'view', 0, '1077', '2'),
       ('294', 'Manage Authorized Users', 'User', 'view', 1, '1078', '2'),
       ('295', 'Manage Authorized Users', 'User', 'create', 1, '1078', '3'),
       ('296', 'Manage Authorized Users', 'User', 'edit', 1, '1078', '4'),
       ('297', 'Batch - Intracompany Payments', 'Batch', 'view', 0, '1079', '2'),
       ('298', 'Batch - Intracompany Payments', 'Batch', 'create', 1, '1079', '3'),
       ('299', 'Batch - Intracompany Payments', 'Batch', 'edit', 0, '1079', '4'),
       ('300', 'Batch - Intracompany Payments', 'Batch', 'delete', 0, '1079', '5'),
       ('301', 'Batch - Intracompany Payments', 'Batch', 'approve', 1, '1079', '6'),
       ('302', 'Batch - Intracompany Payments', 'Batch', 'cancel', 0, '1079', '7'),
       ('303', 'Manage Employee Comments', 'Comments', 'view', 0, '1080', '2'),
       ('304', 'Manage Employee Comments', 'Comments', 'create', 0, '1080', '3'),
       ('305', 'Manage Employee Comments', 'Comments', 'edit', 0, '1080', '4'),
       ('306', 'Manage Employee Comments', 'Comments', 'delete', 0, '1080', '5'),
       ('307', 'Manage Positive Pay', 'Payments', 'view', 0, '1081', '2'),
       ('308', 'Manage Positive Pay', 'Payments', 'create', 0, '1081', '3'),
       ('309', 'Manage Positive Pay', 'Payments', 'edit', 0, '1081', '4'),
       ('310', 'Manage Positive Pay', 'Payments', 'delete', 0, '1081', '5'),
       ('311', 'Manage Positive Pay', 'Payments', 'cancel', 0, '1081', '7'),
       ('312', 'Manage Arrangement Alias', 'Product Summary', 'edit', 0, '1082', '4'),
       ('313', 'Manage Pockets', 'Personal Finance Management', 'create', 0, '1083', '3'),
       ('314', 'Manage Pockets', 'Personal Finance Management', 'view', 0, '1083', '2'),
       ('315', 'Manage Pockets', 'Personal Finance Management', 'delete', 0, '1083', '5'),
       ('316', 'Manage Pockets', 'Personal Finance Management', 'execute', 0, '1083', '1'),
       ('317', 'Revoke access', 'Identities', 'create', 0, '1084', '3'),
       ('318', 'Product Summary Limited View', 'Product Summary', 'view', 0, '1085', '2'),
       ('319', 'Product Summary Limited View', 'Product Summary', 'edit', 0, '1085', '4'),
       ('320', 'Manage Engagement Events', 'Actions', 'view', 0, '1086','2'),
       ('321', 'Manage Engagement Events', 'Actions', 'create', 0, '1086','3'),
       ('322', 'Manage Engagement Events', 'Actions', 'edit', 0, '1086','4'),
       ('323', 'Manage Engagement Events', 'Actions', 'delete', 0, '1086','5'),
       ('324', 'Manage Engagement Events', 'Actions', 'approve', 0, '1086','6'),
       ('325', 'Manage Portfolio', 'Portfolio', 'view', 0, '1087','2'),
       ('326', 'Manage Portfolio', 'Portfolio', 'create', 0, '1087','3'),
       ('327', 'Manage Portfolio', 'Portfolio', 'edit', 0, '1087','4'),
       ('328', 'Manage Portfolio', 'Portfolio', 'delete', 0, '1087','5'),
       ('329', 'Manage Portfolio', 'Portfolio', 'approve', 0, '1087','6'),
       ('330', 'Manage Order', 'Portfolio', 'view', 0, '1088','2'),
       ('331', 'Manage Order', 'Portfolio', 'create', 0, '1088','3'),
       ('332', 'Manage Order', 'Portfolio', 'edit', 0, '1088','4'),
       ('333', 'Manage Order', 'Portfolio', 'delete', 0, '1088','5'),
       ('334', 'Manage Order', 'Portfolio', 'approve', 0, '1088','6'),
       ('335', 'Manage Pockets', 'Personal Finance Management', 'edit', 0, '1083', '4'),
       ('336', 'Manage Cards', 'Personal Finance Management', 'view', 1, '1089', '2'),
       ('337', 'Manage Cards', 'Personal Finance Management', 'create', 1, '1089', '3'),
       ('338', 'Manage Cards', 'Personal Finance Management', 'edit', 1, '1089', '4'),
       ('339', 'Manage Cards', 'Personal Finance Management', 'delete', 1, '1089', '5'),
       ('340', 'Places', 'Personal Finance Management', 'view', 0, '1090', '2'),
       ('341', 'Remote Deposit Capture', 'Payments', 'view', 1, '1091', '2'),
       ('342', 'Remote Deposit Capture', 'Payments', 'create', 1, '1091', '3'),
       ('343', 'Remote Deposit Capture', 'Payments', 'edit', 1, '1091', '4'),
       ('344', 'Remote Deposit Capture', 'Payments', 'delete', 1, '1091', '5'),
       ('345', 'Audit Emulation', 'Audit', 'view', 0, '1092', '2'),
       ('346', 'Identity Impersonation', 'Identities', 'execute', 0, '1093', '1'),
       ('347', 'Manage Aggregate Portfolio', 'Portfolio', 'view', 0, '1094', '2'),
       ('348', 'Manage Aggregate Portfolio', 'Portfolio', 'create', 0, '1094', '3'),
       ('349', 'Manage Aggregate Portfolio', 'Portfolio', 'edit', 0, '1094', '4'),
       ('350', 'Manage Aggregate Portfolio', 'Portfolio', 'delete', 0, '1094', '5'),
       ('351', 'Manage Aggregate Portfolio', 'Portfolio', 'approve', 0, '1094', '6'),
       ('352', 'Batch Templates', 'Batch', 'view', 0, '1095', '2'),
       ('353', 'Batch Templates', 'Batch', 'create', 0, '1095', '3'),
       ('354', 'Batch Templates', 'Batch', 'edit', 0, '1095', '4'),
       ('355', 'Batch Templates', 'Batch', 'delete', 0, '1095', '5'),
       ('356', 'Batch Templates', 'Batch', 'approve', 0, '1095', '6'),
       ('357', 'Manage ACH Positive Pay', 'Payments', 'view', 0, '1096', '2'),
       ('358', 'Manage ACH Positive Pay', 'Payments', 'create', 0, '1096', '3'),
       ('359', 'Manage ACH Positive Pay', 'Payments', 'edit', 0, '1096', '4'),
       ('360', 'Manage ACH Positive Pay', 'Payments', 'delete', 0, '1096', '5'),
       ('361', 'Manage ACH Positive Pay', 'Payments', 'cancel', 0, '1096', '7'),
       ('362', 'Manage Account Reconciliation', 'Payments', 'view', 0, '1097', '2'),
       ('363', 'Manage Account Reconciliation', 'Payments', 'create', 0, '1097', '3'),
       ('364', 'Manage Account Reconciliation', 'Payments', 'edit', 0, '1097', '4'),
       ('365', 'Manage Account Reconciliation', 'Payments', 'delete', 0, '1097', '5'),
       ('366', 'Batch - List Confidential', 'Batch', 'view', 0, '1098', '2'),
       ('367', 'Batch - Manage Confidential', 'Batch', 'execute', 0, '1099', '1'),
       ('368', 'Manage Content', 'Content', 'view', 0, '1100', '2'),
       ('369', 'Manage Content', 'Content', 'create', 0, '1100', '3'),
       ('370', 'Manage Content', 'Content', 'edit', 0, '1100', '4'),
       ('371', 'Manage Content', 'Content', 'delete', 0, '1100', '5'),
       ('372', 'Loans Payment', 'Payments', 'view', 1, '1101', '2'),
       ('373', 'Loans Payment', 'Payments', 'create', 1, '1101', '3'),
       ('374', 'Loans Payment', 'Payments', 'edit', 1, '1101', '4'),
       ('375', 'Loans Payment', 'Payments', 'delete', 1, '1101', '5'),
       ('376', 'Loans Payment', 'Payments', 'approve', 1, '1101', '6'),
       ('377', 'Loans Payment', 'Payments', 'cancel', 1, '1101', '7'),
       ('378', 'Loans Advance', 'Payments', 'view', 1, '1102', '2'),
       ('379', 'Loans Advance', 'Payments', 'create', 1, '1102', '3'),
       ('380', 'Loans Advance', 'Payments', 'edit', 1, '1102', '4'),
       ('381', 'Loans Advance', 'Payments', 'delete', 1, '1102', '5'),
       ('382', 'Loans Advance', 'Payments', 'approve', 1, '1102', '6'),
       ('383', 'Loans Advance', 'Payments', 'cancel', 1, '1102', '7');


INSERT INTO assignable_permission_set (id, description, name, type)
VALUES (1,
        'This Assignable Permission Set (APS) contains all available Business Function/Privilege pairs',
        'Regular user APS',
        0);

INSERT INTO assignable_permission_set (id, description, name, type)
VALUES (2,
        'This Assignable Permission Set (APS) contains all available Entitlements Business Function/Privilege pairs.',
        'Admin user APS',
        1);

INSERT INTO assignable_permission_set_item (assignable_permission_set_id, function_privilege_id)
SELECT 2, afp.id
FROM applicable_function_privilege afp
WHERE afp.id IN
      (
       '13',
       '14',
       '15',
       '19',
       '20',
       '21',
       '22',
       '23',
       '54',
       '55',
       '56',
       '57',
       '59',
       '60',
       '61',
       '62',
       '83',
       '84',
       '85',
       '86',
       '172',
       '173',
       '174'
          );

INSERT INTO assignable_permission_set_item (assignable_permission_set_id, function_privilege_id)
SELECT 1, id
FROM applicable_function_privilege;

CREATE TABLE approval_function_group_ref
(
    id BIGINT NOT NULL,
    function_group_id VARCHAR(36) NULL,
    CONSTRAINT pk_approval_function_group_ref PRIMARY KEY (id),
    CONSTRAINT fk_afgr2aca FOREIGN KEY (id) REFERENCES access_control_approval(id)
);

CREATE TABLE approval_function_group
(
    id BIGINT NOT NULL ,
    description VARCHAR(255) NOT NULL,
    name VARCHAR(128) NOT NULL,
    service_agreement_id VARCHAR(36) NOT NULL,
    start_date DATETIME NULL,
    end_date DATETIME NULL,
    approval_type_id VARCHAR(36) NULL,
    CONSTRAINT pk_approval_function_group PRIMARY KEY (id),
    CONSTRAINT fk_afg2afgr FOREIGN KEY (id) REFERENCES approval_function_group_ref(id)
);

CREATE TABLE approval_function_group_item
(
    id BIGINT NOT NULL,
    afp_id VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_function_group_item PRIMARY KEY (id,afp_id),
    CONSTRAINT fk_afgi2afg FOREIGN KEY (id) REFERENCES approval_function_group(id)
);

CREATE INDEX ix_user_context_01
    ON user_context (user_id);

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
    user_id                                             VARCHAR(36) NOT NULL,
    legal_entity_id                                     VARCHAR(36) NOT NULL,
    CONSTRAINT pk_approval_sa_admins                    PRIMARY KEY (id,user_id,legal_entity_id),
    CONSTRAINT fk_asaa2asap                             FOREIGN KEY (id,legal_entity_id) REFERENCES approval_sa_participant(id,legal_entity_id)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE PUBLIC.self_appr_policy (
   id VARCHAR(36) NOT NULL,
    ua_fg_combination_id BIGINT NOT NULL,
    function_group_id VARCHAR(36) NOT NULL,
    afp_id VARCHAR(36) NOT NULL,
    can_self_approve BOOLEAN NOT NULL,
    CONSTRAINT pk_sap PRIMARY KEY (id),
    CONSTRAINT fk_sap2uafgc FOREIGN KEY (ua_fg_combination_id) REFERENCES PUBLIC.user_assigned_fg_combination(id),
    CONSTRAINT uq_sap_01 UNIQUE (ua_fg_combination_id, afp_id)
);

COMMENT ON TABLE PUBLIC.self_appr_policy IS
    'Contains self-approval policies telling whether users can approve actions or payments initiated by themeselves from specific account groups to specific payee groups in context of given service agreement and job role.';

COMMENT ON COLUMN PUBLIC.self_appr_policy.id IS
    'ID of the self-approval policy';

COMMENT ON COLUMN PUBLIC.self_appr_policy.ua_fg_combination_id IS
    'ID of the target combination of account groups plus payee groups for user';

COMMENT ON COLUMN PUBLIC.self_appr_policy.function_group_id IS
    'ID of the target job role';

COMMENT ON COLUMN PUBLIC.self_appr_policy.afp_id IS
    'ID of the target business function ''approve'' privilege inside the job role';

COMMENT ON COLUMN PUBLIC.self_appr_policy.can_self_approve IS
    'boolean flag telling whether user is allowed to self-approve';

ALTER TABLE PUBLIC.self_appr_policy
   ADD CONSTRAINT fk_sap2fgitem
   FOREIGN KEY (function_group_id, afp_id)
   REFERENCES PUBLIC.function_group_item (function_group_id, afp_id);

CREATE TABLE PUBLIC.self_appr_policy_bound (
   id VARCHAR(36) NOT NULL,
    self_appr_policy_id VARCHAR(36) NOT NULL,
    upper_bound DECIMAL(23, 5) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    CONSTRAINT pk_sapb PRIMARY KEY (id),
    CONSTRAINT fk_sapb2sap FOREIGN KEY (self_appr_policy_id) REFERENCES PUBLIC.self_appr_policy(id),
    CONSTRAINT uq_sapb_01 UNIQUE (self_appr_policy_id, currency_code)
);

COMMENT ON TABLE PUBLIC.self_appr_policy_bound IS
    'Optionally defines boundaries in different currecies for payments that can be self-approved by users';

COMMENT ON COLUMN PUBLIC.self_appr_policy_bound.id IS
    'ID of the self-approval policy bound';

COMMENT ON COLUMN PUBLIC.self_appr_policy_bound.self_appr_policy_id IS
    'ID of the target self-approval policy';

COMMENT ON COLUMN PUBLIC.self_appr_policy_bound.upper_bound IS
    'Max transaction amount that can be self-approved';

COMMENT ON COLUMN PUBLIC.self_appr_policy_bound.currency_code IS
    'Max transaction amount currency, e.g. ''EUR''';

CREATE TABLE PUBLIC.approval_self_appr_policy (
   id VARCHAR(36) NOT NULL,
    approval_uc_assign_fg_id BIGINT NOT NULL,
    function_group_id VARCHAR(36) NOT NULL,
    afp_id VARCHAR(36) NOT NULL,
    can_self_approve BOOLEAN NOT NULL,
    CONSTRAINT pk_asap PRIMARY KEY (id),
    CONSTRAINT fk_asap2aucafg FOREIGN KEY (approval_uc_assign_fg_id) REFERENCES PUBLIC.approval_uc_assign_fg(id),
    CONSTRAINT uq_asap_01 UNIQUE (approval_uc_assign_fg_id, afp_id)
);

COMMENT ON TABLE PUBLIC.approval_self_appr_policy IS
    'Contains self-approval policies that are pending user permissions assignment';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy.id IS
    'ID of the pending self-approval policy';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy.approval_uc_assign_fg_id IS
    'ID of the pending combination of account groups plus payee groups for user';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy.function_group_id IS
    'ID of the target job role';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy.afp_id IS
    'ID of the target business function ''approve'' privilege inside the job role';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy.can_self_approve IS
    'boolean flag telling whether user is allowed to self-approve';

ALTER TABLE PUBLIC.approval_self_appr_policy
   ADD CONSTRAINT fk_asap2fgitem
   FOREIGN KEY (function_group_id, afp_id)
   REFERENCES PUBLIC.function_group_item (function_group_id, afp_id);

CREATE TABLE PUBLIC.approval_self_appr_policy_bound (
   id VARCHAR(36) NOT NULL,
    approval_self_appr_policy_id VARCHAR(36) NOT NULL,
    upper_bound DECIMAL(23, 5) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    CONSTRAINT pk_asapb PRIMARY KEY (id),
    CONSTRAINT fk_asapb2asap FOREIGN KEY (approval_self_appr_policy_id) REFERENCES PUBLIC.approval_self_appr_policy(id),
    CONSTRAINT uq_asapb_01 UNIQUE (approval_self_appr_policy_id, currency_code)
);

COMMENT ON TABLE PUBLIC.approval_self_appr_policy_bound IS
    'Contains optional boundaries for pending self-approval policies';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy_bound.id IS
    'ID of the pending self-approval policy bound';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy_bound.approval_self_appr_policy_id IS
    'ID of the pending self-approval policy';

COMMENT ON COLUMN PUBLIC.approval_self_appr_policy_bound.upper_bound IS
    'Max transaction amount that can be self-approved';