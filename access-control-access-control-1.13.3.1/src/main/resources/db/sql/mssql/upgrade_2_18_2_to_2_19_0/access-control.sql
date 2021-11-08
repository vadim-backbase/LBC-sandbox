-- assignable_permission_set_item
CREATE INDEX [ix_aps_item_01] ON [assignable_permission_set_item] ([function_privilege_id]);

GO

-- legal_entity
CREATE INDEX [ix_legal_entity_02] ON [legal_entity] ([name]);

GO

-- legal_entity_ancestor
-- Replace unique constraint with PK constraint in legal_entity_ancestor
ALTER TABLE [legal_entity_ancestor]
    DROP CONSTRAINT [uq_legal_entity_ancestor_01];

GO

ALTER TABLE [legal_entity_ancestor]
    ADD CONSTRAINT [pk_legal_entity_ancestor] PRIMARY KEY ([ancestor_id], [descendent_id]);

GO

-- service_agreement_aps
CREATE INDEX [ix_sa_aps_01] ON [service_agreement_aps] ([assignable_permission_set_id]);

GO

-- function_group
ALTER TABLE [function_group_item] DROP CONSTRAINT [fk_fgi2fg];

GO

ALTER TABLE [user_assigned_function_group] DROP CONSTRAINT [fk_uafg2fg];

GO

ALTER TABLE [approval_uc_assign_fg] DROP CONSTRAINT [fk_aucfg2fg];

GO

ALTER TABLE [function_group] DROP CONSTRAINT [fk_fg2le];

GO

ALTER TABLE [function_group] DROP CONSTRAINT [fk_fg2sa];

GO

ALTER TABLE [function_group] DROP CONSTRAINT [fk_fg2aps];

GO

EXEC sp_rename N'pk_function_group', N'pk_function_group_old', N'OBJECT';

GO

EXEC sp_rename N'uq_function_group_01', N'uq_function_group_01_old', N'OBJECT';

GO

EXEC sp_rename 'function_group', N'function_group_old';

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
    CONSTRAINT pk_function_group      PRIMARY KEY CLUSTERED (id)
);

GO

INSERT INTO [function_group]
(
    [id],
    [name],
    [description],
    [type],
    [service_agreement_id],
    [start_date],
    [end_date],
    [aps_id]
)
SELECT
    [fg_old].[id],
    [fg_old].[name],
    [fg_old].[description],
    [fg_old].[type],
    [fg_old].[service_agreement_id],
    [fg_old].[start_date],
    [fg_old].[end_date],
    [fg_old].[aps_id]
FROM
    [function_group_old]      [fg_old];

GO

CREATE INDEX [ix_function_group_01] ON [function_group] ([aps_id]);

GO

ALTER TABLE [function_group] ADD CONSTRAINT [uq_function_group_01] UNIQUE ([service_agreement_id], [name]);

GO

ALTER TABLE [function_group]
    ADD CONSTRAINT [fk_fg2sa] FOREIGN KEY ([service_agreement_id]) REFERENCES [service_agreement] ([id])
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

GO

ALTER TABLE [function_group]
    ADD CONSTRAINT [fk_fg2aps] FOREIGN KEY ([aps_id]) REFERENCES [assignable_permission_set] ([id])
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

GO

ALTER TABLE [function_group_item]
    ADD CONSTRAINT fk_fgi2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

ALTER TABLE [user_assigned_function_group]
    ADD CONSTRAINT fk_uafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

ALTER TABLE [approval_uc_assign_fg]
    ADD CONSTRAINT fk_aucfg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

-- function_group_item
CREATE INDEX [ix_fgi_01] ON [function_group_item] ([afp_id]);

GO

-- user_context
-- Remove user_id index
DROP INDEX [user_context].[ix_user_context_01];

GO

ALTER TABLE [user_context] DROP CONSTRAINT [uq_user_context_01];

GO

ALTER TABLE [user_context] ADD CONSTRAINT [uq_user_context_01] UNIQUE ([user_id], [service_agreement_id]);

GO

CREATE INDEX [ix_user_context_01] ON [user_context] ([service_agreement_id]);

GO

ALTER TABLE [user_context] ADD CONSTRAINT fk_uc2sa FOREIGN KEY (service_agreement_id) REFERENCES service_agreement (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

GO

-- Normalize user_assigned_function_group
ALTER TABLE [user_assigned_function_group] DROP CONSTRAINT [fk_uafg2fg];

GO

ALTER TABLE [user_assigned_function_group] DROP CONSTRAINT [fk_uafg2sa];

GO

ALTER TABLE [user_assigned_function_group] DROP CONSTRAINT [fk_uafg2ua];

GO

ALTER TABLE [user_assigned_function_group] DROP CONSTRAINT [fk_uafg2ua2];

GO

EXEC sp_rename N'user_assigned_function_group.ix_uafg_02', N'ix_uafg_02_old', N'INDEX';

GO

EXEC sp_rename N'user_assigned_function_group.ix_uafg_03', N'ix_uafg_03_old', N'INDEX';

GO

EXEC sp_rename N'user_assigned_function_group.ix_uafg_04', N'ix_uafg_04_old', N'INDEX';

GO

EXEC sp_rename N'user_assigned_function_group.ix_uafg_05', N'ix_uafg_05_old', N'INDEX';

GO

EXEC sp_rename N'pk_uafg', N'pk_uafg_old', N'OBJECT';

GO

EXEC sp_rename N'uq_uafg_01', N'uq_uafg_01_old', N'OBJECT';

GO

EXEC sp_rename 'user_assigned_function_group', N'uafg_old';

GO

CREATE TABLE [user_assigned_function_group] (
    [id]                      [BIGINT]       NOT NULL,
    [user_context_id]         [BIGINT]       NOT NULL,
    [function_group_id]       [NVARCHAR](36) NOT NULL,
    CONSTRAINT pk_uafg        PRIMARY KEY CLUSTERED (id)
);

GO

INSERT INTO [user_assigned_function_group]
(
    [id],
    [user_context_id],
    [function_group_id]
)
SELECT
    [uafg_old].[id],
    [uafg_old].[user_context_id],
    [uafg_old].[function_group_id]
FROM
    [uafg_old];

GO

CREATE INDEX [ix_uafg_03] ON [user_assigned_function_group] ([function_group_id]);

GO

ALTER TABLE [user_assigned_function_group] ADD CONSTRAINT uq_uafg_01 UNIQUE (user_context_id, function_group_id);

GO

ALTER TABLE [user_assigned_function_group]
    ADD CONSTRAINT fk_uafg2ua2 FOREIGN KEY (user_context_id) REFERENCES user_context (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

ALTER TABLE [user_assigned_function_group]
    ADD CONSTRAINT fk_uafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

-- Normalize user_assigned_fg_dg
ALTER TABLE [user_assigned_fg_dg] DROP CONSTRAINT [fk_uafgdg2dg];

GO

ALTER TABLE [user_assigned_fg_dg] DROP CONSTRAINT [fk_uafgdg2uafg];

GO

EXEC sp_rename N'user_assigned_fg_dg.ix_user_assigned_fg_dg_02', N'ix_user_assigned_fg_dg_02_old', N'INDEX';

GO

EXEC sp_rename N'pk_user_assigned_fg_dg', N'pk_user_assigned_fg_dg_old', N'OBJECT';

GO

EXEC sp_rename N'uq_user_assigned_fg_dg_01', N'uq_user_assigned_fg_dg_01_old', N'OBJECT';

GO

EXEC sp_rename 'user_assigned_fg_dg', 'user_assigned_fg_dg_old';

GO

CREATE TABLE [user_assigned_fg_dg]
(
    [data_group_id]        [NVARCHAR](36)     NOT NULL,
    [user_assigned_fg_id]  [BIGINT]           NOT NULL,
    CONSTRAINT pk_user_assigned_fg_dg PRIMARY KEY CLUSTERED (user_assigned_fg_id, data_group_id)
);

GO

INSERT INTO [user_assigned_fg_dg]
(
    [data_group_id],
    [user_assigned_fg_id]
)
SELECT
    [uafgdg].[data_group_id],
    [uafgdg].[user_assigned_fg_id]
FROM
    [user_assigned_fg_dg_old] [uafgdg];

GO

CREATE INDEX [ix_user_assigned_fg_dg_02] ON [user_assigned_fg_dg] ([data_group_id]);

GO

ALTER TABLE [user_assigned_fg_dg]
    ADD CONSTRAINT fk_uafgdg2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

ALTER TABLE [user_assigned_fg_dg]
    ADD CONSTRAINT fk_uafgdg2uafg FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

-- approval_uc_assign_fg
ALTER TABLE [approval_uc_assign_fg] DROP CONSTRAINT [fk_aucfg2fg];

GO

CREATE INDEX [ix_aucafg_01] ON [approval_uc_assign_fg] ([approval_user_context_id]);

GO

CREATE INDEX [ix_aucafg_02] ON [approval_uc_assign_fg] ([function_group_id]);

GO

ALTER TABLE [approval_uc_assign_fg]
    ADD CONSTRAINT fk_aucafg2fg FOREIGN KEY (function_group_id) REFERENCES function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

-- approval_uc_assign_fg_dg
ALTER TABLE [approval_uc_assign_fg_dg] DROP CONSTRAINT [fk_aucfgdg2dg];

GO

CREATE INDEX [ix_aucafgdg_02] ON [approval_uc_assign_fg_dg] ([data_group_id]);

GO

ALTER TABLE [approval_uc_assign_fg_dg]
    ADD CONSTRAINT fk_aucafgdg2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

-- approval_function_group_item
EXEC sp_rename N'pk_approval_function_group_item', N'pk_afgi', N'OBJECT';

GO

-- new business functions
INSERT INTO [business_function] ([id], [function_code], [function_name], [resource_code], [resource_name])
VALUES ('1070', 'p2p.transfer', 'P2P Transfer', 'payments', 'Payments'),
       ('1071', 'payment.templates', 'Payment Templates', 'payments', 'Payments'),
       ('1072','flow.task.statistics', 'Access Task Statistics', 'flow', 'Flow'),
       ('1073','uk.chaps', 'UK CHAPS', 'payments', 'Payments'),
       ('1074','uk.faster.payments', 'UK Faster Payments', 'payments', 'Payments'),
       ('1075','emulate', 'Emulate', 'employee', 'Employee'),
	   ('1076','act.on.behalf.of', 'Act on behalf of', 'employee', 'Employee'),
	   ('1077','flow.collection', 'Access Collections', 'flow', 'Flow');

GO

INSERT INTO [applicable_function_privilege] ([id],
                                             [business_function_name],
                                             [function_resource_name],
                                             [privilege_name],
                                             [supports_limit],
                                             [business_function_id],
                                             [privilege_id])
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

GO

INSERT INTO [assignable_permission_set_item] ([assignable_permission_set_id],
                                              [function_privilege_id])
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
GO

-- Cleanup
-- DROP TABLE [user_ac];
-- GO
-- DROP TABLE [user_assigned_function_priv];
-- GO
-- DROP TABLE [user_assigned_function_priv_dg];
-- GO
-- DROP TABLE [function_group_old];
-- GO
-- DROP TABLE [uafg_old];
-- GO
-- DROP TABLE [user_assigned_fg_dg_old];
-- GO


-- IMPORTANT!!!
-- If the environment is migrated from versions older then 2.18.0 then most probably there are tables and sequences
-- in the databases which we don't use them anymore and we delete them with the statements below.

-- DROP TABLE user_access;
-- GO
-- DROP TABLE tbl_uuid_user_context;
-- GO
-- DROP TABLE tbl_uuid_uafg;
-- GO
-- DROP SEQUENCE seq_user_context;
-- GO
-- DROP SEQUENCE seq_uafg;
-- GO
