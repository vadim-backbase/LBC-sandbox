-- migrate user_assigned_fg_dg to user_assigned_fg_combination and user_assigned_combination_dg
ALTER TABLE [user_assigned_fg_dg] DROP CONSTRAINT [fk_uafgdg2uafg];

GO

ALTER TABLE [user_assigned_fg_dg] DROP CONSTRAINT [fk_uafgdg2dg];

GO

EXEC sp_rename N'user_assigned_fg_dg.ix_user_assigned_fg_dg_02', N'ix_user_assigned_fg_dg_02_old', N'INDEX';

GO

EXEC sp_rename N'pk_user_assigned_fg_dg', N'pk_user_assigned_fg_dg_old', N'OBJECT';

GO

EXEC sp_rename 'user_assigned_fg_dg', 'user_assigned_fg_dg_old';

GO

-- create sequence to generate Integer values for PK
CREATE SEQUENCE seq_user_assigned_combination START WITH 1 INCREMENT BY 1;

GO

CREATE TABLE [user_assigned_fg_combination] (
    [user_assigned_fg_id]       [BIGINT]       NOT NULL,
    [id]                        [BIGINT]       NOT NULL,
    CONSTRAINT pk_uafgc         PRIMARY KEY CLUSTERED (user_assigned_fg_id, id)
);

GO

CREATE TABLE [user_assigned_combination_dg] (
    [ua_fg_combination_id]          [BIGINT]       NOT NULL,
    [data_group_id]                 [NVARCHAR](36) NOT NULL,
    CONSTRAINT pk_uacdg             PRIMARY KEY CLUSTERED (ua_fg_combination_id, data_group_id)
);

GO

INSERT INTO [user_assigned_fg_combination]
(
    [user_assigned_fg_id],
    [id]
)
SELECT
    [m].[user_assigned_fg_id],
    NEXT VALUE FOR [seq_user_assigned_combination]
FROM
    (
	    SELECT DISTINCT
            [user_assigned_fg_id]
	    FROM
            [user_assigned_fg_dg_old]
    ) [m];

GO

INSERT INTO [user_assigned_combination_dg]
(
    [ua_fg_combination_id],
    [data_group_id]
)
SELECT
    [t].[id],
    [m].[data_group_id]
FROM
    [user_assigned_fg_dg_old] [m]
    INNER JOIN [user_assigned_fg_combination] [t]
    ON [m].[user_assigned_fg_id] = [t].[user_assigned_fg_id];

GO

ALTER TABLE [user_assigned_fg_combination] ADD CONSTRAINT [uq_uafgc_01] UNIQUE ([id]);

GO

ALTER TABLE [user_assigned_fg_combination]
    ADD CONSTRAINT fk_uafgc2uafg FOREIGN KEY (user_assigned_fg_id) REFERENCES user_assigned_function_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

CREATE INDEX [ix_uacdg_01]
    ON [user_assigned_combination_dg] ([data_group_id]);

GO

ALTER TABLE [user_assigned_combination_dg]
    ADD CONSTRAINT fk_uacdg2uafgc FOREIGN KEY (ua_fg_combination_id) REFERENCES user_assigned_fg_combination (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

ALTER TABLE [user_assigned_combination_dg]
    ADD CONSTRAINT fk_uacdg2dg FOREIGN KEY (data_group_id) REFERENCES data_group (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;

GO

-- Update sequence generator table
INSERT INTO [sequence_table] ([sequence_name], [next_val])
    VALUES('SEQ_USER_ASSIGNED_FG_COMBINATION', NEXT VALUE FOR [seq_user_assigned_combination]);

GO

-- new business functions
INSERT INTO [business_function] ([id], [function_code], [function_name], [resource_code], [resource_name])
VALUES
    ('1079','batch.intracompany.payments', 'Batch - Intracompany Payments', 'payments', 'Payments'),
    ('1080','manage.employee.comments', 'Manage Employee Comments', 'comments', 'Comments'),
    ('1081','manage.positive.pay', 'Manage Positive Pay', 'payments', 'Payments'),
    ('1082','manage.arrangement.alias', 'Manage Arrangement Alias', 'arrangements', 'Arrangements');


GO

INSERT INTO [applicable_function_privilege] ([id],
                                             [business_function_name],
                                             [function_resource_name],
                                             [privilege_name],
                                             [supports_limit],
                                             [business_function_id],
                                             [privilege_id])
VALUES ('297', 'Batch - Intracompany Payments', 'Payments', 'view', 0, '1079', '2'),
       ('298', 'Batch - Intracompany Payments', 'Payments', 'create', 1, '1079', '3'),
       ('299', 'Batch - Intracompany Payments', 'Payments', 'edit', 0, '1079', '4'),
       ('300', 'Batch - Intracompany Payments', 'Payments', 'delete', 0, '1079', '5'),
       ('301', 'Batch - Intracompany Payments', 'Payments', 'approve', 1, '1079', '6'),
       ('302', 'Batch - Intracompany Payments', 'Payments', 'cancel', 0, '1079', '7'),
       ('303', 'Manage Employee Comments', 'Comments', 'view', 0, '1080', '2'),
       ('304', 'Manage Employee Comments', 'Comments', 'create', 0, '1080', '3'),
       ('305', 'Manage Employee Comments', 'Comments', 'edit', 0, '1080', '4'),
       ('306', 'Manage Employee Comments', 'Comments', 'delete', 0, '1080', '5'),
       ('307', 'Manage Positive Pay', 'Payments', 'view', 0, '1081', '2'),
       ('308', 'Manage Positive Pay', 'Payments', 'create', 0, '1081', '3'),
       ('309', 'Manage Positive Pay', 'Payments', 'edit', 0, '1081', '4'),
       ('310', 'Manage Positive Pay', 'Payments', 'delete', 0, '1081', '5'),
       ('311', 'Manage Positive Pay', 'Payments', 'cancel', 0, '1081', '7'),
       ('312', 'Manage Arrangement Alias', 'Arrangements', 'edit', 0, '1082', '4');


GO

INSERT INTO [assignable_permission_set_item] ([assignable_permission_set_id],
                                              [function_privilege_id])
VALUES (1, '297'),
       (1, '298'),
       (1, '299'),
       (1, '300'),
       (1, '301'),
       (1, '302'),
       (1, '303'),
       (1, '304'),
       (1, '305'),
       (1, '306'),
       (1, '307'),
       (1, '308'),
       (1, '309'),
       (1, '310'),
       (1, '311'),
       (1, '312');
GO

-- Cleanup
-- DROP TABLE [user_assigned_fg_dg_old];
-- GO
-- DROP SEQUENCE [seq_user_assigned_combination];
-- GO
