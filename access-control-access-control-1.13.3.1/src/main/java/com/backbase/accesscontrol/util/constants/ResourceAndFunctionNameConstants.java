package com.backbase.accesscontrol.util.constants;

/**
 * Resource, function and privileges names for different functionalities related to access group.
 */
public class ResourceAndFunctionNameConstants {

    private ResourceAndFunctionNameConstants() {
    }

    /**
     * (function, resource, list of privileges) triple for controlling entitlements.
     */
    public static final String ENTITLEMENTS_RESOURCE_NAME_FUNCTION_NAME = "Entitlements";
    public static final String ENTITLEMENTS_RESOURCE_NAME = "Entitlements";
    public static final String SERVICE_AGREEMENT_RESOURCE_NAME = "Service Agreement";
    public static final String ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME = "Assign Users";
    public static final String FUNCTION_ASSIGN_PERMISSONS = "Assign Permissions";
    public static final String FUNCTION_MANAGE_USERS = "Manage Users";
    public static final String RESOURCE_MANAGE_USERS = "User";
    public static final String PRIVILEGE_VIEW = "view";
    public static final String PRIVILEGE_EDIT = "edit";
    public static final String PRIVILEGE_CREATE = "create";
    public static final String PRIVILEGE_DELETE = "delete";
    public static final String PRIVILEGE_APPROVE = "approve";
    public static final String ENTITLEMENTS_MANAGE_FUNCTION_GROUPS = "Manage Function Groups";
    public static final String ENTITLEMENTS_MANAGE_DATA_GROUPS = "Manage Data Groups";
    public static final String MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME = "Manage Service Agreements";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_EDIT = "EDIT";
    public static final String ACTION_DELETE = "DELETE";
    public static final String MANAGE_LEGAL_ENTITY_FUNCTION_NAME = "Manage Legal Entities";
    public static final String MANAGE_LEGAL_ENTITY_RESOURCE_NAME = "Legal Entity";
    public static final String MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW = "view";
    public static final String MANAGE_LEGAL_ENTITY_PRIVILEGE_CREATE = "create";
}
