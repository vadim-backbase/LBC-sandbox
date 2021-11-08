package com.backbase.accesscontrol.util.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommandErrorCodes implements ErrorCode {

    ERR_ACC_001("Action cannot be completed because useraccess has no access to function access group",
        "dataAccessGroup.addOrRemoveFunction.error.message.E_NO_ACCESS"),
    ERR_ACC_002("Action cannot be completed because useraccess has no access to data access group",
        "dataAccessGroup"
            + ".addOrRemoveData.error.message.E_NO_ACCESS"),
    ERR_ACC_003("User already exists", "user.add.error.message.E_ALREADY_EXISTS"),
    ERR_ACC_004("Legal Entity with given external Id already exists",
        "legalEntity.save.error.message"
            + ".E_EX_ID_ALREADY_EXISTS"),
    ERR_ACC_006("Legal Entity with given name already exists",
        "legalEntity.save.error.message.E_NAME_ALREADY_EXISTS"),
    ERR_ACC_007("Invalid Parent Legal Entity",
        "legalEntity.save.error.message.E_PARENT_LE_INVALID"),
    ERR_ACC_008("Parent Legal Entity is not bank",
        "legalEntity.save.error.message.E_PARENT_LE_IS_NOT_BANK"),
    ERR_ACC_009("Legal Entity must be bank", "legalEntity.save.error.message.E_LE_MUST_BE_BANK"),
    ERR_ACC_010("Legal Entity not found", "legalEntity.update.error.message.E_NOT_FOUND"),
    ERR_ACC_011("Parent Legal Entity mast be BANK",
        "legalEntity.update.error.message.E_PARENT_LE_IS_NOT_BANK"),
    ERR_ACC_012("Legal Entity must have customer children", "legalEntity.save.error.message"
        + ".E_LE_MUST_HAVE_CUSTOMER_CHILDREN"),
    ERR_ACC_013("Data Access Group is assigned to a function access group",
        "dataAccessGroup.delete.error.message"
            + ".E_ASSIGNED"),
    ERR_ACC_014("Data Access Group is part of a service agreement",
        "dataAccessGroup.delete.error.message"
            + ".DATA_ACCESS_GROUP_IN_SERVICE_AGREEMENT"),
    ERR_ACC_015("Data Access Group doesn't exists.",
        "dataAccessGroup.delete.error.message.E_NOT_EXISTS"),
    ERR_ACC_027("If you want to delete this group you should first remove all users assigned to it.",
        "functionAccessGroup.delete.error.message.E_USER_ASSIGNED"),
    ERR_ACC_028("Data Access Group with given name already exists",
        "dataAccessGroup.save.error.message"
            + ".E_ALREADY_EXISTS"),
    ERR_ACC_029("Service agreement does not exist",
        "serviceAgreements.get.error.message.E_NOT_EXISTS"),
    ERR_ACC_030("At least one account should be included in a Data Group",
        "dataAccessGroup.save.error"
            + ".message.E_ACCOUNT_SHOULD_BE_INCLUDED"),
    ERR_ACC_031("Invalid service agreement",
        "dataGroup.update.error.message.E_INVALID_SERVICE_AGREEMENT"),
    ERR_ACC_032("Invalid delete data groups request body",
        "dataGroup.delete.error.message.E_INVALID_REQUEST_BODY"),
    ERR_ACC_033("User with id ? does not belong in service agreement", "user.put.error.message"
        + ".E_USER_NOT_BELONG_IN_SERVICE_AGREEMENT"),
    ERR_ACC_034("Duplicated function groups", "user.put.error.message.E_DUPLICATED_FG"),
    ERR_ACC_035("Invalid function group ids", "user.put.error.message.E_INVALID_FG_IDS"),
    ERR_ACC_036("Invalid data group ids", "user.put.error.message.E_INVALID_DG_IDS"),
    ERR_ACC_037("Legal Entity with external ID already exists.",
        "legalEntity.get.error.message.E_EX_ID_EXISTS"),
    ERR_ACC_038("Invalid Service Agreement or Participant.",
        "serviceAgreement.users.add.error.message"
            + ".E_INVALID_BODY"),
    ERR_ACC_039("User already exists in service agreement",
        "entitlements.access.error.message.E_USER_EXISTS_IN_SERVICE_AGREEMENT"),
    ERR_ACC_040("User does not exist in service agreement",
        "entitlements.access.error.message.E_USER_NOT_EXISTS_IN_SERVICE_AGREEMENT"),
    ERR_ACC_041("User can not be removed, FAG/DAG pair assigned",
        "entitlements.access.error.message"
            + ".E_USER_CAN_NOT_BE_REMOVED"),
    ERR_ACC_042("Legal entity is not a valid participant of service agreement",
        "serviceAgreement.users.add.error"
            + ".message.E_LE_NOT_VALID_PARTICIPANT"),
    ERR_ACC_043("LE Participant must share Users and/or Accounts",
        "serviceAgreement.participant.add.error.message.E_LE_NOT_VALID_PARTICIPANT"),
    ERR_ACC_044("Invalid Service Agreement or Participant.",
        "serviceAgreement.participant.add.error.message"
            + ".E_INVALID_BODY"),
    ERR_ACC_045("LE Participant exists in the Service Agreement.",
        "serviceAgreement.participant.add.error.message"
            + ".E_LE_EXISTS"),
    ERR_ACC_046("Can not add participant to the service agreement - "
        + "Legal entity does not belong to the creator's "
        + "hierarchy.",
        "serviceAgreement.participant.add.error.message.E_LE_INVALID_HIERARCHY"),
    ERR_ACC_047("Participant does not exist in service agreement",
        "serviceAgreement.admins.add.remove.error.message"
            + ".E_PARTICIPANT_NOT_EXISTS"),
    ERR_ACC_048("User is already admin in service agreement",
        "serviceAgreement.admins.add.remove.error.message"
            + ".E_USER_ALREADY_ADMIN"),
    ERR_ACC_049("User is not admin in service agreement and can not be removed",
        "serviceAgreement.admins.add.remove"
            + ".error.message.E_USER_IS_NOT_ADMIN"),
    ERR_ACC_050("Invalid or duplicate function group/data group identifiers",
        "user.permissions.update.error.message"
            + ".E_INVALID_RETRIEVED_FG_DG_IDENTIFIERS"),
    ERR_ACC_051("Invalid data group identifiers", "dataGroup.identifier.error.message"
        + ".E_INVALID_DATA_GROUP_IDENTIFIERS"),
    ERR_ACC_052("Invalid function group identifiers", "user.permissions.update.error.message"
        + ".E_INVALID_FUNCTION_GROUP_IDENTIFIERS"),
    ERR_ACC_053("Invalid data group type",
        "dataGroup.save.error.message.E_INVALID_DATA_GROUP_TYPE"),
    ERR_ACC_054("Participant can not be removed from the service agreement, "
        + "there are users with permissions in the "
        + "service agreement", "participant.update.error.message.E_PARTICIPANT_CAN_NOT_BE_REMOVED"),
    ERR_ACC_055("Participant can not be added to the service agreement",
        "participant.update.error.message"
            + ".E_PARTICIPANT_CAN_NOT_BE_ADDED"),
    ERR_ACC_056("Unable to remove Participant from Service Agreement",
        "participant.update.error.message"
            + ".E_PARTICIPANT_CAN_NOT_BE_REMOVED"),
    ERR_ACC_057("Users must belong on legal entities that are participants "
        + "in the service agreement and share users.",
        "serviceAgreement.ingest.error.message.E_NOT_VALID_USERS"),
    ERR_ACC_058("Admins must belong to the participants of the service agreement",
        "serviceAgreement.create.error.message.E_NOT_VALID_ADMIN_FOR_PARTICIPANTS"),
    ERR_ACC_059("Invalid legal entity id", "user.get.error.message.E_INVALID_LEGAL_ENTITY_ID"),
    ERR_ACC_060("Not all legal entities were found.",
        "legalentity.get.error.message.E_INVALID_LEGAL_ENTITY_EXTERNAL_IDS"),
    ERR_ACC_061("Duplicate Participant in service agreement.",
        "serviceAgreement.create.error.message.E_DUPLICATE_PARTICIPANT"),
    ERR_ACC_062("Legal entities hierarchy missing.",
        "legalentity.get.error.message.E_INVALID_LEGAL_HIERARCHY"),
    ERR_ACC_063("Participant can not expose users if sharing users is not available.",
        "serviceAgreement.ingest.error.message.INVALID_SHARING_USERS"),
    ERR_ACC_064("Unexpected number of participants in service agreement.",
        "serviceAgreement.ingest.error.message.INVALID_PARTICIPANTS_NUMBER"),
    ERR_ACC_065("Unexpected participants sharing options, "
        + "participant should share both user and accounts.",
        "serviceAgreement.ingest.error.message.INVALID_PARTICIPANTS_SHARING"),
    ERR_ACC_066("Unable to create service agreement of this type. Maximum 1 per legal entity is allowed.",
        "serviceAgreement.ingest.error.message.INVALID_MAXIMUM_SERVICE_AGREEMENTS"),
    ERR_ACC_067("You can not add/remove users from this Service Agreement.",
        "serviceAgreement.remove.batch.user.error.message.USER_CAN_NOT_BE_REMOVED"),
    ERR_ACC_068("Property isMaster can not be updated",
        "serviceAgreement.update.error.message.UPDATE_IS_MASTER_INVALID"),
    ERR_ACC_069("Unable to update participant in this service agreement",
        "serviceAgreement.update.error.message.UPDATE_PARTICIPANTS_INVALID"),
    ERR_ACC_070("Can not disable Service agreement of the root legal entity",
        "serviceAgreement.update.error.message.DISABLE_INVALID"),
    ERR_ACC_071("User does not belong to the specified service agreement.",
        "serviceAgreement.create.error.message.E_INVALID_SERVICE_AGREEMENT"),
    ERR_ACC_072("There is already pending permission assignment for the given user and service agreement.",
        "serviceAgreement.permissions.assign.approval.error.message.E_ASSIGNMENT_ALREADY_PENDING"),
    ERR_ACC_073("Function Group can not be deleted, "
        + "because it is assigned in a pending user permission assignment.",
        "functionAccessGroup.delete.error.message.E_PENDING_ASSIGNMENT"),
    ERR_ACC_074("Data Group can not be deleted, "
        + "because it is assigned in a pending user permission assignment.",
        "dataAccessGroup.delete.error.message.E_PENDING_ASSIGNMENT"),
    ERR_ACC_075("User can not be removed from Service Agreement, because it is assigned in a "
        + "pending user permission assignment.",
        "serviceAgreement.users.remove.error.message.E_PENDING_ASSIGNMENT"),
    ERR_ACC_076("Default Service Agreement of "
        + "root Legal Entity cannot be configured with time boundaries.",
        "serviceAgreement.create.error.message.E_INVALID_SERVICE_AGREEMENT_TIME_PERIOD_ROOT_LE"),
    ERR_ACC_077("Invalid validity period.",
        "serviceAgreement.create.error.message.E_INVALID_SERVICE_AGREEMENT_TIME_PERIOD"),
    ERR_ACC_078("Participant can not be removed from the service agreement, there are users "
        + "with pending permissions in the service agreement",
        "participant.update.error.message.E_PARTICIPANT_CAN_NOT_BE_REMOVED_PENDING_PERMISSIONS"),
    ERR_ACC_079("Data Item does not exist.",
        "dataGroup.update.dataItems.error.message.NON_EXISTING_DATA_ITEM"),
    ERR_ACC_080("You cannot remove all the items in a Data Group.",
        "dataGroup.update.dataItems.error.message.CANNOT_REMOVE_ALL_ITEMS"),
    ERR_ACC_081("Data Item already exist in the Data Group.",
        "dataGroup.update.dataItems.error.message.CANNOT_ADD_SAME_ITEM_TWICE"),
    ERR_ACC_082("There is pending creation of data group.",
        "dataGroup.create.approval.error.message.ALREADY_IN_PENDING_STATE"),
    ERR_ACC_083("There is pending action for data group.",
        "dataGroup.update.approval.error.message.ALREADY_IN_PENDING_STATE"),
    ERR_ACC_084("Data group with pending deletion request cannot be assigned.",
        "user.permissions.update.error"
            + ".message.E_PENDING_DATA_GROUP_DELETE"),
    ERR_ACC_085("Data group does not exist.",
        "dataGroup.update.error.message.NON_EXISTING_DATA_GROUP"),
    ERR_ACC_086("Invalid validity period.", "datetime.valid.period.INVALID_VALUE"),
    ERR_ACC_087("Permission set name already exists",
        "permissionSet.save.error.message.E_NAME_ALREADY_EXISTS"),
    ERR_ACC_088("Invalid business function privilege pair",
        "permissionSet.save.error.message.E_INVALID_PERMISSION"),
    ERR_ACC_089("Provided identifiers for the APS are invalid.",
        "delete.permissionSet.identifiers.INVALID_IDENTIFIER"),
    ERR_ACC_090("The APS doesn't exist.",
        "delete.permissionSet.identifiers.NOT_EXISTS"),
    ERR_ACC_091("The APS is system default and cannot be deleted.",
        "delete.permissionSet.identifiers.NOT_CUSTOM"),
    ERR_ACC_092("The APS is associated to at least one Service Agreement",
        "delete.permissionSet.identifiers.ASSIGNED_TO_SERVICE_AGREEMENT"),
    ERR_ACC_094("Invalid id or name identifier of assignable permission set.",
        "permissionSet.identifier.INVALID_VALUE"),
    ERR_ACC_095("Both or none of the identifiers sent for regular/admin user assignable permission set.",
        "permissionSet.identifier.INVALID_IDENTIFIERS"),
    ERR_ACC_096("At least one business function/privilege is contained in one or more function groups.",
        "permissionSet.aps.PRIVILEGE_CONTAINED_IN_FUNCTION_GROUP"),
    ERR_ACC_097("Operation not allowed on custom service agreement.",
        "serviceAgreement.custom.NOT_ALLOWED"),
    ERR_ACC_098(
        "The Legal Entity cannot be deleted, "
            + "because it is contained in at least one data group of type CUSTOMERS.",
        "legalEntity.delete.error.message.E_CONTAINED_IN_NOT_ALLOWED_DG_TYPE"),
    ERR_ACC_099("You may perform this action only for entities below in the hierarchy.",
        "legalEntity.error.message.E_NOT_IN_HIERARCHY"),
    ERR_ACC_100("There is a pending record of function group with same name.",
        "functionGroup.create.approval.error.message.ALREADY_IN_PENDING_STATE"),
    ERR_ACC_101("There is a pending record of function group.",
        "functionGroup.approval.error.message.ALREADY_IN_PENDING_STATE"),
    ERR_ACC_102("There is a pending record of function group with business function with privileges that "
        + "are not compatible with assignable permission set from the request.",
        "functionGroup.approval.error.message.ALREADY_IN_PENDING_STATE_WITH_DIFFERENT_APS"),
    ERR_ACC_103("Job role template can't be created for custom service agreement.",
        "functionGroup.create.error.message.JRT_NOT_ALLOWED_FOR_CUSTOM_SA"),
    ERR_ACC_104("Either apsId or apsName should be sent.",
        "functionGroup.create.error.message.NEITHER_OF_APS_IDENTIFIERS_PROVIDED"),
    ERR_ACC_105("Function group template delete operation is not supported.",
        "functionGroupTemplate.delete.UNSUPPORTED_OPERATION"),
    ERR_ACC_106("You are not able to update the permissions, while there is a pending change to the service agreement",
        "permission.put.error.CANNOT_UPDATE_WHILE_SERVICE_AGREEMENT_PENDING"),
    ERR_ACC_107("You cannot manage this entity, while the referenced service agreement has a pending change.",
        "serviceAgreement.error.in.pending.state"),
    ERR_ACC_108("You can not assign/unassign system job role.",
        "user.permissions.update.error.message.E_SJR_ASSIGN_PROHIBITED"),
    ERR_ACC_109("Business Function '%s' is not supported",
        "businessFunction.error.message.E_BUSINESS_FUNCTION_NOT_EXISTS"),
    ERR_ACC_110("Business Function '%s' does not support limits",
        "businessFunction.error.message.E_LIMITS_NOT_SUPPORTED"),
    ERR_ACC_111("You are not able to add bounds to SelfApprovalPolicy and set flag canSelfApprove to false",
        "selfApprovalPolicy.error.message.E_BOUNDS_PROVIDED"),
    ERR_ACC_112("You are not able to add more than one bound per policy",
        "selfApprovalPolicy.error.message.E_BOUNDS_NUMBER_EXCEEDED"),
    ERR_ACC_113(
        "Business Function '%s' is duplicated for SelfApprovalPolicies per combination of functionGroup and dataGroups",
        "selfApprovalPolicy.error.message.E_BUSINESS_FUNCTION_DUPLICATES_NOT_ALLOWED"),
    ERR_ACC_114(
        "Business Function '%s' does not support '%s' privilege",
        "businessFunction.error.message.E_PRIVILEGE_NOT_SUPPORTED"),
    ERR_ACC_115("Business Function '%s' with privilege '%s' not assigned to functionGroup",
        "functionGroup.error.message.E_BUSINESS_FUNCTION_NOT_ASSIGNED");



    private String errorMessage;
    private String errorCode;
}
