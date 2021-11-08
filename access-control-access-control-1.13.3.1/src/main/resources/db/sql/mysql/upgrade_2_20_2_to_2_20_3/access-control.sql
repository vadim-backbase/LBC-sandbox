-- new business functions
INSERT INTO business_function (id, function_code, function_name, resource_code, resource_name)
VALUES
('1089', 'manage.cards', 'Manage Cards', 'personal.finance.management', 'Personal Finance Management'),
('1090', 'places', 'Places', 'personal.finance.management', 'Personal Finance Management'),
('1091', 'remote.deposit.capture', 'Remote Deposit Capture', 'payments', 'Payments'),
('1092', 'audit.emulation', 'Audit Emulation', 'audit', 'Audit'),
('1093', 'identity.impersonation', 'Identity Impersonation', 'identities', 'Identities');

UPDATE business_function
SET resource_code = 'batch', resource_name = 'Batch'
WHERE id = '1079';

INSERT INTO applicable_function_privilege (id,
                                           business_function_name,
                                           function_resource_name,
                                           privilege_name,
                                           supports_limit,
                                           business_function_id,
                                           privilege_id)
VALUES ('335', 'Manage Pockets', 'Personal Finance Management', 'edit', 0, '1083', '4'),
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
       ('346', 'Identity Impersonation', 'Identities', 'execute', 0, '1093', '1');

UPDATE applicable_function_privilege
SET function_resource_name = 'Batch'
WHERE id IN ('297', '298', '299', '300', '301', '302');

INSERT INTO assignable_permission_set_item (assignable_permission_set_id,
                                            function_privilege_id)
VALUES (1, '335'),
       (1, '336'),
       (1, '337'),
       (1, '338'),
       (1, '339'),
       (1, '340'),
       (1, '341'),
       (1, '342'),
       (1, '343'),
       (1, '344'),
       (1, '345'),
       (1, '346');
commit;

