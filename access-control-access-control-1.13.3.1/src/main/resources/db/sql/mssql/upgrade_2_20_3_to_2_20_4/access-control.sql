-- new business functions
INSERT INTO [business_function] ([id], [function_code], [function_name], [resource_code], [resource_name])
VALUES
    ('1094', 'manage.aggregate.portfolio', 'Manage Aggregate Portfolio', 'portfolio', 'Portfolio'),
    ('1095', 'batch.templates', 'Batch Templates', 'batch', 'Batch');
GO

INSERT INTO [applicable_function_privilege] ([id],
                                             [business_function_name],
                                             [function_resource_name],
                                             [privilege_name],
                                             [supports_limit],
                                             [business_function_id],
                                             [privilege_id])
VALUES ('347', 'Manage Aggregate Portfolio', 'Portfolio', 'view', 0, '1094', '2'),
       ('348', 'Manage Aggregate Portfolio', 'Portfolio', 'create', 0, '1094', '3'),
       ('349', 'Manage Aggregate Portfolio', 'Portfolio', 'edit', 0, '1094', '4'),
       ('350', 'Manage Aggregate Portfolio', 'Portfolio', 'delete', 0, '1094', '5'),
       ('351', 'Manage Aggregate Portfolio', 'Portfolio', 'approve', 0, '1094', '6'),
       ('352', 'Batch Templates', 'Batch', 'view', 0, '1095', '2'),
       ('353', 'Batch Templates', 'Batch', 'create', 0, '1095', '3'),
       ('354', 'Batch Templates', 'Batch', 'edit', 0, '1095', '4'),
       ('355', 'Batch Templates', 'Batch', 'delete', 0, '1095', '5'),
       ('356', 'Batch Templates', 'Batch', 'approve', 0, '1095', '6');
GO

INSERT INTO [assignable_permission_set_item] ([assignable_permission_set_id],
[function_privilege_id])
VALUES (1, '347'),
       (1, '348'),
       (1, '349'),
       (1, '350'),
       (1, '351'),
       (1, '352'),
       (1, '353'),
       (1, '354'),
       (1, '355'),
       (1, '356');
GO
