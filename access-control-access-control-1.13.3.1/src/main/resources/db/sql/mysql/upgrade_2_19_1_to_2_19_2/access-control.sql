-- new business functions
INSERT INTO business_function (id, function_code, function_name, resource_code, resource_name)
VALUES ('1078','manage.authorized.users', 'Manage Authorized Users', 'user', 'User');

INSERT INTO applicable_function_privilege (id,
                                           business_function_name,
                                           function_resource_name,
                                           privilege_name,
                                           supports_limit,
                                           business_function_id,
                                           privilege_id)
VALUES ('294', 'Manage Authorized Users', 'User', 'view', 1, '1078', '2'),
       ('295', 'Manage Authorized Users', 'User', 'create', 1, '1078', '3'),
       ('296', 'Manage Authorized Users', 'User', 'edit', 1, '1078', '4');

INSERT INTO assignable_permission_set_item (assignable_permission_set_id, function_privilege_id)
VALUES (1, '294'),
       (1, '295'),
       (1, '296');

commit;