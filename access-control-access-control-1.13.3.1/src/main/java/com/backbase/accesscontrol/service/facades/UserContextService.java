package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;

/**
 * Interface for interacting with user contexts.
 */
public interface UserContextService {

    /**
     * Get list of user context by user provider id including master service agreement. The master service agreement is
     * added as first element of the list in the first page.
     *
     * @param userId User provider id
     * @param query Filter by service agreement name
     * @param from Beginning pagination
     * @param cursor Pagination cursor
     * @param size Pagination size
     * @return List of context users and total number of elements
     */
    ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> getUserContextByUserId(String userId, String query,
        Integer from, String cursor, Integer size);

    /**
     * Validates if the service agreement id is valid for the userId.
     *
     * @param externalUserId External user provider Id
     * @param serviceAgreementId Service Agreement Id
     * @return if the arguments are validate, a encrypted token will be returned.
     */
    String validate(String externalUserId, String serviceAgreementId);
}
