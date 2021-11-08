package com.backbase.accesscontrol.auth;

import java.util.List;

/**
 * The implementation of this interface should hold the Access Control PnP client and send the request for checking
 * permissions.
 */
public interface AccessControlValidator {

    /**
     * Returns true if the user has no access to modify the resource belonging to @param resourceLegalEntityId.
     *
     * @param resourceLegalEntityId legalEntityId of the resource which we want to modify
     * @param accessResourceType    the type of the resource that needs to be accessed (example USER for accessing
     *                              users)
     * @return true if user has no access to modify the resource
     */
    boolean userHasNoAccessToEntitlementResource(String resourceLegalEntityId,
        AccessResourceType accessResourceType);

    /**
     * Returns true if the user has no access to modify an resource belonging to @param legalEntities.
     *
     * @param legalEntities      list of legal entities of the resource which we want to modify
     * @param accessResourceType the type of the resource that needs to be accessed (example USER for accessing users)
     * @return true if user has no access to modify the resource
     */
    boolean userHasNoAccessToEntitlementResource(List<String> legalEntities,
        AccessResourceType accessResourceType);

    /**
     * Returns true if the user has no access to modify the resource belonging to @param serviceAgreementId.
     *
     * @param serviceAgreementId serviceAgreementId of the resource which we want to modify
     * @param accessResourceType the type of the resource that needs to be accessed (example USER for accessing users)
     * @return true if user has no access to modify the resource
     */
    boolean userHasNoAccessToServiceAgreement(String serviceAgreementId,
        AccessResourceType accessResourceType);

    /**
     * Returns true if the user has no permissions under given item.
     *
     * @param businessFunction - name of the business function
     * @param privilege        - privilege name
     * @param dataType         - type of data group
     * @param itemId           - internal id of the item for which the check is made for the specific user
     * @return true if user has no business function/privilege assigned for given item id
     */
    boolean userHasNoAccessToDataItem(String businessFunction, String privilege, String dataType, String itemId);
}
