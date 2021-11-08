-- new business functions
INSERT INTO [business_function] ([id], [function_code], [function_name], [resource_code], [resource_name])
VALUES
    ('1098', 'batch.list.confidential', 'Batch - List Confidential', 'batch', 'Batch'),
    ('1099', 'batch.manage.confidential', 'Batch - Manage Confidential', 'batch', 'Batch');
GO

UPDATE business_function
SET resource_code = 'product.summary', resource_name = 'Product Summary'
WHERE id = '1082';
GO

INSERT INTO [applicable_function_privilege] ([id],
                                             [business_function_name],
                                             [function_resource_name],
                                             [privilege_name],
                                             [supports_limit],
                                             [business_function_id],
                                             [privilege_id])
VALUES ('366', 'Batch - List Confidential', 'Batch', 'view', 0, '1098', '2'),
       ('367', 'Batch - Manage Confidential', 'Batch', 'execute', 0, '1099', '1');
GO

UPDATE applicable_function_privilege
SET function_resource_name = 'Product Summary'
WHERE id = '312';
GO

INSERT INTO [assignable_permission_set_item] ([assignable_permission_set_id],
[function_privilege_id])
VALUES (1, '366'),
       (1, '367');
GO