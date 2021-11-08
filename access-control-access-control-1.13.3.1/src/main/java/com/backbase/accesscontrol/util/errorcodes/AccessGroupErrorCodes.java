package com.backbase.accesscontrol.util.errorcodes;

public enum AccessGroupErrorCodes {

    ERR_AG_001("Invalid data group type", "dataGroup.save.error.message.E_INVALID_DATA_GROUP_TYPE"),
    ERR_AG_005("Every LE participant should share at least users or accounts.",
        "serviceAgreement.create.error.message.E_INVALID_PARTICIPANT_PROPERTIES"),
    ERR_AG_006("At least one participant should share users.",
        "serviceAgreement.create.error.message.E_NOT_SHARE_USERS"),
    ERR_AG_007("Function group does not exist.", "functionAccessGroup.get.error.message.E_NOT_EXISTS"),
    ERR_AG_008("At least one participant should share accounts.",
        "serviceAgreement.create.error.message.E_NOT_SHARE_ACCOUNTS"),
    ERR_AG_028("Admins must belong to the participants of the service agreement",
        "serviceAgreement.create.error.message.E_NOT_VALID_ADMIN_FOR_PARTICIPANTS"),
    ERR_AG_030("Admins must be valid users.", "serviceAgreement.create.error.message.E_NOT_VALID_ADMINS"),
    ERR_AG_031("User with the specified external user id does not exist",
        "serviceAgreement.get.error.message.E_USER_DOES_NOT_EXIST"),
    ERR_AG_032("User does not have access to Manage Entitlements", "entitlements.access.error.message.E_NOT_ACCESS"),
    ERR_AG_033("There are no participants in this SA that share users",
        "serviceAgreement.get.error.message.E_NO_PARTICIPANTS_SHARING_USERS"),
    ERR_AG_043("User can not list Service Agreements created by other Legal Entity",
        "serviceAgreement.get.error.message.E_CREATOR_NOT_BELONGS_TO_LE"),
    ERR_AG_050("Communication with service failed",
        "entitlements.access.error.message.E_COMMUNICATION_WITH_SERVICE_FAILED"),
    ERR_AG_059("Not authenticated user.", "serviceAgreement.create.error.message.E_NOT_AUTHENTICATED"),
    ERR_AG_061("Service agreement does not exist", "serviceAgreements.get.error.message.E_NOT_EXISTS"),
    ERR_AG_062("Invalid search query parameter", "user.get.error.message.E_INVALID_SEARCH_PARAMETER"),
    ERR_AG_063("Invalid from parameter", "user.get.error.message.E_INVALID_FROM_PARAMETER"),
    ERR_AG_064("Invalid size parameter", "user.get.error.message.E_INVALID_SIZE_PARAMETER"),
    ERR_AG_065("Invalid query parameters", "user.get.error.message.E_INVALID_QUERY_PARAMETER"),
    ERR_AG_066("Invalid legal entity id", "user.get.error.message.E_INVALID_LEGAL_ENTITY_ID"),
    ERR_AG_067("Audit Data can not be extracted from request", "audit.error.message.E_INVALID_PARAMETERS"),
    ERR_AG_068("Descriptor is not provided", "audit.error.message.E_DESCRIPTOR_NOT_PROVIDED"),
    ERR_AG_069("Service agreement external id must be unique.",
        "serviceAgreement.create.error.message.E_EXTERNAL_ID_NOT_UNIQUE"),
    ERR_AG_070("Service agreement can not be disabled.", "serviceAgreement.update.error.message.E_NOT_VALID_STATUS"),
    ERR_AG_071("No User Context.", "context.E_MISSING_USER_CONTEXT"),
    ERR_AG_072("Error extracting user id from JWT", "context.E_MISSING_USER_ID"),
    ERR_AG_073("The service agreement and legal entities are not valid for the current user.",
        "context.E_NOT_VALID_SA"),
    ERR_AG_079("Users must belong on legal entities that are participants in the service agreement and share users.",
        "serviceAgreement.ingest.error.message.E_NOT_VALID_USERS"),
    ERR_AG_080("Duplicate Participant in service agreement.",
        "serviceAgreement.create.error.message.E_DUPLICATE_PARTICIPANT"),
    ERR_AG_081("Invalid Identifier. Either name or id identifier should be provided",
        "functionGroup.dataGroup.update.delete.error.message.E_INVALID_IDENTIFIER"),
    ERR_AG_082("Not existing user", "user.permissions.update.error.message.E_NOT_EXISTING_USER"),
    ERR_AG_083("Invalid function group/data group identifier.",
        "user.permissions.update.error.message.E_INVALID_FUNCTION_GROUP_IDENTIFIERS"),
    ERR_AG_085("Invalid data group items", "dataGroup.save.error.message.E_INVALID_DATA_GROUP_ITEMS"),
    ERR_AG_086("Unable to remove participant, please remove data groups from the service agreement",
        "participant.update.error.message.E_UNABLE_TO_REMOVE_PARTICIPANT"),
    ERR_AG_087("Invalid participant", "participant.update.error.message.E_INVALID_PARTICIPANT"),
    ERR_AG_089("Arrangement validation failed",
        "dataGroup.validation.error.message.ARRANGEMENT_VALIDATION_FAILED"),
    ERR_AG_090("Access is denied.", "usercontext.error.message.ACCESS_DENIED"),
    ERR_AG_092("User has no privilege to access function", "useraccess.get.error.message.E_USER_NO_PRIVILEGE"),
    ERR_AG_093("Approval must be on to use this method", "approval.get.error.message.E_APPROVAL_OFF"),
    ERR_AG_094("Wrong date/time format", "datetime.valid.period.INVALID_FORMAT"),
    ERR_AG_095("Invalid validity period.", "datetime.valid.period.INVALID_VALUE"),
    ERR_AG_097("Batch delete Service Agreement failed.",
        "serviceAgreement.batch.delete.error.message.E_BATCH_DELETE_SA_FAILED"),
    ERR_AG_098("No service agreement or data item identifier is provided", "dataGroups.search.request.invalid"),
    ERR_AG_099("One or more data items are invalid", "dataGroups.update.error.message.INVALID_DATA_ITEMS"),
    ERR_AG_100("Missing service agreement id or external service agreement id in the data group.",
        "dataGroup.create.error.message.E_MISSING_SERVICE_AGREEMENTS"),
    ERR_AG_101("Unable to remove participant, there is active and pending data groups to this service agreement",
        "participant.update.error.message.E_UNABLE_TO_REMOVE_PARTICIPANT_PENDING"),
    ERR_AG_102("Both or none of the identifiers sent for regular/admin user assignable permission set.",
        "permissionSet.identifier.INVALID_IDENTIFIERS"),
    ERR_AG_103("Data group type is not allowed", "dataGroup.save.error.message.E_NOT_ALLOWED_DATA_GROUP_TYPE"),
    ERR_AG_104("Data item validation failed",
        "dataGroup.validation.error.message.DATA_ITEM_VALIDATION_FAILED"),
    ERR_AG_105("Operation not allowed, there is pending modification on this service agreement.",
        "serviceAgreements.modify.error.message.PENDING_MODIFICATION"),
    ERR_AG_106("Operation not allowed, there is pending modification on service agreement with this external id.",
        "serviceAgreements.modify.error.message.EXTERNAL_ID_PENDING_MODIFICATION"),
    ERR_AG_107("This method does not support approval flow",
        "accesscontrol.general.error.NOT_SUPPOERTED_WITH_APPROVAL"),
    ERR_AG_108("You cannot manage this service agreement, while there is a pending function group",
        "serviceAgreements.modify.error.message.PENDING_MODIFICATION_JOB_ROLE"),
    ERR_AG_109("You cannot manage this service agreement, while there is a pending data group",
        "serviceAgreements.modify.error.message.PENDING_MODIFICATION_DATA_GROUP"),
    ERR_AG_110("You cannot manage this service agreement, while there is a pending permission assignment",
        "serviceAgreements.modify.error.message.PENDING_MODIFICATION_PERMISSION"),
    ERR_AG_111("You can not mix the same function group with and without data groups combinations, "
        + "during assigning permissions", "permissions.assign.error.invalidDataGroupCombinations"),
    ERR_AG_112("User does not have permission for provided data items",
        "permissions.dataItemsPermission.NO_PERMISSION"),
    ERR_AG_113("Data item id or type must be unique",
        "permissions.dataItemsPermission.UNIQUE_ID_AND_TYPE"),
    ERR_AG_114("Privilege not valid for business function group",
        "permissions.dataItemsPermission.INVALID_PRIVILEGE"),
    ERR_AG_115("Data items can not be null",
        "permissions.dataItemsPermission.INVALID_DATA_ITEM_NULL"),
    ERR_AG_116("Data groups cannot be duplicated in scope of a single function group during assigning permissions",
            "permissions.assign.error.dataGroupDuplicates"),
    ERR_AG_117("You cannot pass creator legal entity id for master service agreement",
        "serviceAgreements.ingest.error.message.INVALID_PARAMETER_FOR_MASTER_SA");

    private String errorCode;
    private String errorMessage;

    AccessGroupErrorCodes(String errorMessage, String errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

}