package com.backbase.accesscontrol.util.errorcodes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LegalEntityErrorCodes {

    ERR_LE_003("User does not have access to perform this action", "entitlements.access.error.message.E_NOT_ACCESS"),
    ERR_LE_004("Creating Legal Entity failed.", "legalEntity.save.error.message.E_CREATION_LE_FAILED"),
    ERR_LE_007("Updating Legal Entity failed.", "legalEntity.update.error.message.E_UPDATE_LE_FAILED"),
    ERR_LE_009("Invalid from parameter", "legalEntity.get.error.message.E_INVALID_FROM_PARAMETER"),
    ERR_LE_010("Invalid size parameter", "legalEntity.get.error.message.E_INVALID_SIZE_PARAMETER"),
    ERR_LE_011("Invalid from or size parameter", "legalEntity.get.error.message.E_INVALID_FROM_OR_SIZE_PARAMETER"),
    ERR_LE_012("Invalid query parameters", "legalEntity.get.error.message.E_INVALID_QUERY_PARAMETER"),
    ERR_AG_013("User does not have access to Manage Entitlements", "entitlements.access.error.message.E_NOT_ACCESS"),
    ERR_AG_014("Audit Data can not be extracted from request", "audit.error.message.E_INVALID_PARAMETERS"),
    ERR_AG_015("Descriptor is not provided", "audit.error.message.E_DESCRIPTOR_NOT_PROVIDED"),
    ERR_AG_016("No User Context.", "context.E_MISSING_USER_CONTEXT"),
    ERR_AG_017("Error extracting user id from JWT", "context.E_MISSING_USER_ID"),
    ERR_AG_018("Descriptor is not provided", "audit.error.message.E_DESCRIPTOR_NOT_PROVIDED"),
    ERR_LE_019("Batch delete Legal Entity failed.", "legalEntity.batch.delete.error.message.E_BATCH_DELETE_LE_FAILED"),
    ERR_LE_020("External Id not provided.", "legalEntity.create.error.message.E_CREATE_LE_FAILED"),
    ERR_LE_021("Action can only be triggered from the master service agreement", "legalEntity.create.error.message.E_CREATE_LE_AS_PARTICIPANT_FAILED");


    private String errorMessage;
    private String errorCode;

}
