package com.backbase.accesscontrol.audit;

public enum AuditObjectType {
    DATA_GROUP(Constants.DATA_GROUP_STRING),
    DATA_GROUP_APPROVAL(Constants.DATA_GROUP_STRING),
    DATA_GROUP_SERVICE(Constants.DATA_GROUP_STRING),
    DATA_GROUP_BATCH_SERVICE(Constants.DATA_GROUP_STRING),
    DATA_GROUP_INGEST(Constants.DATA_GROUP_STRING),
    DATA_GROUP_ITEMS(Constants.DATA_GROUP_STRING),
    FUNCTION_GROUP(Constants.FUNCTION_GROUP_STRING),
    FUNCTION_GROUP_APPROVAL(Constants.FUNCTION_GROUP_STRING),
    FUNCTION_GROUP_BATCH(Constants.FUNCTION_GROUP_STRING),
    FUNCTION_GROUP_INGEST(Constants.FUNCTION_GROUP_STRING),
    SERVICE_AGREEMENT(Constants.AGREEMENT),
    SERVICE_AGREEMENT_STATE(Constants.AGREEMENT),
    SERVICE_AGREEMENT_APPROVAL(Constants.AGREEMENT),
    SAVE_SERVICE_AGREEMENT(Constants.AGREEMENT),
    SERVICE_AGREEMENT_SERVICE(Constants.AGREEMENT),
    SERVICE_AGREEMENT_ADMINS(Constants.AGREEMENT),
    SERVICE_AGREEMENT_USERS_ADD(Constants.AGREEMENT),
    SERVICE_AGREEMENTS_USERS_ADD(Constants.AGREEMENT),
    SERVICE_AGREEMENT_USERS_REMOVE(Constants.AGREEMENT),
    SERVICE_AGREEMENTS_USERS_REMOVE(Constants.AGREEMENT),
    UPDATE_USER_PERMISSIONS(Constants.USER_PERMISSIONS_STRING),
    UPDATE_USER_PERMISSIONS_APPROVAL(Constants.USER_PERMISSIONS_STRING),
    ASSIGN_USER_PERMISSIONS(Constants.USER_PERMISSIONS_STRING),
    SERVICE_AGREEMENT_PARTICIPANTS_UPDATE(Constants.AGREEMENT),
    SERVICE_AGREEMENT_ADMINS_UPDATE_BATCH(Constants.AGREEMENT),
    SERVICE_AGREEMENT_USERS_UPDATE_BATCH(Constants.AGREEMENT),
    SERVICE_AGREEMENT_BATCH_SERVICE(Constants.AGREEMENT),
    APPROVAL(Constants.APPROVAL_STRING),
    PERMISSION_SET(Constants.PERMISSION_SET),
    LEGAL_ENTITY(Constants.LEGAL_ENTITY),
    LEGAL_ENTITY_CREATE_AS_PARTICIPANT(Constants.LEGAL_ENTITY),
    LEGAL_ENTITY_CREATE(Constants.LEGAL_ENTITY),
    LEGAL_ENTITY_ADD(Constants.LEGAL_ENTITY),
    LEGAL_ENTITY_BATCH(Constants.LEGAL_ENTITY);

    private String objectTypeName;

    AuditObjectType(String objectTypeName) {
        this.objectTypeName = objectTypeName;
    }

    public String getObjectTypeName() {
        return objectTypeName;
    }

    private static class Constants {

        private static final String FUNCTION_GROUP_STRING = "Function Group";
        private static final String AGREEMENT = "Service Agreement";
        private static final String DATA_GROUP_STRING = "Data Group";
        private static final String USER_PERMISSIONS_STRING = "User Permissions";
        private static final String APPROVAL_STRING = "Approval";
        private static final String PERMISSION_SET = "Permission Set";
        private static final String LEGAL_ENTITY = "Legal Entity";
    }
}
