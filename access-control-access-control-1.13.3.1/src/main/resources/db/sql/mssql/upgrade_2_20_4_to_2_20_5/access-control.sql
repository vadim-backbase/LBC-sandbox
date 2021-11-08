-- new business functions
INSERT INTO [business_function] ([id], [function_code], [function_name], [resource_code], [resource_name])
VALUES
    ('1096', 'manage.ach.positive.pay', 'Manage ACH Positive Pay', 'payments', 'Payments'),
    ('1097', 'manage.account.reconciliation', 'Manage Account Reconciliation', 'payments', 'Payments');
GO

INSERT INTO [applicable_function_privilege] ([id],
                                             [business_function_name],
                                             [function_resource_name],
                                             [privilege_name],
                                             [supports_limit],
                                             [business_function_id],
                                             [privilege_id])
VALUES ('357', 'Manage ACH Positive Pay', 'Payments', 'view', 0, '1096', '2'),
       ('358', 'Manage ACH Positive Pay', 'Payments', 'create', 0, '1096', '3'),
       ('359', 'Manage ACH Positive Pay', 'Payments', 'edit', 0, '1096', '4'),
       ('360', 'Manage ACH Positive Pay', 'Payments', 'delete', 0, '1096', '5'),
       ('361', 'Manage ACH Positive Pay', 'Payments', 'cancel', 0, '1096', '7'),
       ('362', 'Manage Account Reconciliation', 'Payments', 'view', 0, '1097', '2'),
       ('363', 'Manage Account Reconciliation', 'Payments', 'create', 0, '1097', '3'),
       ('364', 'Manage Account Reconciliation', 'Payments', 'edit', 0, '1097', '4'),
       ('365', 'Manage Account Reconciliation', 'Payments', 'delete', 0, '1097', '5');
GO

INSERT INTO [assignable_permission_set_item] ([assignable_permission_set_id],
[function_privilege_id])
VALUES (1, '357'),
       (1, '358'),
       (1, '359'),
       (1, '360'),
       (1, '361'),
       (1, '362'),
       (1, '363'),
       (1, '364'),
       (1, '365');
GO