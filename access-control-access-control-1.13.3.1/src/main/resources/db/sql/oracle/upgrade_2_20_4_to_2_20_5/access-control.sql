-- new business functions
INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1096', 'manage.ach.positive.pay', 'Manage ACH Positive Pay', 'payments', 'Payments');

INSERT INTO BUSINESS_FUNCTION(ID, FUNCTION_CODE, FUNCTION_NAME, RESOURCE_CODE, RESOURCE_NAME)
VALUES
('1097', 'manage.account.reconciliation', 'Manage Account Reconciliation', 'payments', 'Payments');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('357', 'Manage ACH Positive Pay', 'Payments', 'view', 0, '1096', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('358', 'Manage ACH Positive Pay', 'Payments', 'create', 0, '1096', '3');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('359', 'Manage ACH Positive Pay', 'Payments', 'edit', 0, '1096', '4');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('360', 'Manage ACH Positive Pay', 'Payments', 'delete', 0, '1096', '5');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('361', 'Manage ACH Positive Pay', 'Payments', 'cancel', 0, '1096', '7');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('362', 'Manage Account Reconciliation', 'Payments', 'view', 0, '1097', '2');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('363', 'Manage Account Reconciliation', 'Payments', 'create', 0, '1097', '3');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('364', 'Manage Account Reconciliation', 'Payments', 'edit', 0, '1097', '4');

INSERT INTO APPLICABLE_FUNCTION_PRIVILEGE
(ID, BUSINESS_FUNCTION_NAME, FUNCTION_RESOURCE_NAME, PRIVILEGE_NAME, SUPPORTS_LIMIT, BUSINESS_FUNCTION_ID, PRIVILEGE_ID)
VALUES
('365', 'Manage Account Reconciliation', 'Payments', 'delete', 0, '1097', '5');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '357');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '358');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '359');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '360');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '361');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '362');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '363');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '364');

INSERT INTO ASSIGNABLE_PERMISSION_SET_ITEM
(ASSIGNABLE_PERMISSION_SET_ID, FUNCTION_PRIVILEGE_ID)
VALUES
(1, '365');

commit;
