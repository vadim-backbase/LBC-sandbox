-- new business functions
INSERT INTO [business_function] ([id], [function_code], [function_name], [resource_code], [resource_name])
VALUES
    ('1083', 'manage.pockets', 'Manage Pockets', 'personal.finance.management', 'Personal Finance Management'),
    ('1084', 'revoke.access', 'Revoke access', 'identities', 'Identities'),
    ('1085', 'product.summary.limited.view', 'Product Summary Limited View', 'product.summary', 'Product Summary'),
    ('1086', 'manage.engagement.events', 'Manage Engagement Events', 'actions', 'Actions'),
    ('1087', 'manage.portfolio', 'Manage Portfolio', 'portfolio', 'Portfolio'),
    ('1088', 'manage.order', 'Manage Order', 'portfolio', 'Portfolio');
GO

INSERT INTO [applicable_function_privilege] ([id],
                                             [business_function_name],
                                             [function_resource_name],
                                             [privilege_name],
                                             [supports_limit],
                                             [business_function_id],
                                             [privilege_id])
VALUES ('313', 'Manage Pockets', 'Personal Finance Management', 'create', 0, '1083', '3'),
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
    ('334', 'Manage Order', 'Portfolio', 'approve', 0, '1088','6');
GO

INSERT INTO [assignable_permission_set_item] ([assignable_permission_set_id],
[function_privilege_id])
VALUES (1, '313'),
       (1, '314'),
       (1, '315'),
       (1, '316'),
       (1, '317'),
       (1, '318'),
       (1, '319'),
       (1, '320'),
       (1, '321'),
       (1, '322'),
       (1, '323'),
       (1, '324'),
       (1, '325'),
       (1, '326'),
       (1, '327'),
       (1, '328'),
       (1, '329'),
       (1, '330'),
       (1, '331'),
       (1, '332'),
       (1, '333'),
       (1, '334');
GO
