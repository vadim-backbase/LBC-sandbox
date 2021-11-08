-- new business functions
INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1098', 'batch.list.confidential', 'Batch - List Confidential', 'batch', 'Batch');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1099', 'batch.manage.confidential', 'Batch - Manage Confidential', 'batch', 'Batch');

UPDATE BUSINESS_FUNCTION
SET RESOURCE_CODE = 'product.summary', RESOURCE_NAME = 'Product Summary'
WHERE id = '1082';

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('366', 'Batch - List Confidential', 'Batch', 'view', 0, '1098', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('367', 'Batch - Manage Confidential', 'Batch', 'execute', 0, '1099', '1');

UPDATE APPLICABLE_FUNCTION_PRIVILEGE
SET FUNCTION_RESOURCE_NAME = 'Product Summary'
WHERE id = '312';

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '366');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '367');

commit;
