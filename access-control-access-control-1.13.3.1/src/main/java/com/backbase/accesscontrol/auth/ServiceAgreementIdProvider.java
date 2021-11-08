package com.backbase.accesscontrol.auth;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import java.util.List;
import java.util.Optional;

/**
 * The implementation of this interface should provide service agreement context under which the user is working.
 */
public interface ServiceAgreementIdProvider {

    /**
     * Retrieve the service agreement id from the context.
     *
     * @return service agreement ID under whose context the logged in user is working.
     */
    Optional<String> getServiceAgreementId();

    /**
     * Retrieve the service agreement by id.
     *
     * @param serviceAgreementId - internal id of service agreement
     * @return service agreement {@link ServiceAgreementItem}
     */
    Optional<ServiceAgreementItem> getServiceAgreementById(
        String serviceAgreementId);

    /**
     * Retrieve the list of service agreement participants by given service agreement id.
     *
     * @param serviceAgreementId - internal id of service agreement
     * @return list of {@link Participant}
     */
    List<Participant> getServiceAgreementParticipants(
        String serviceAgreementId);

    /**
     * Get master service agreement for provided user.
     *
     * @param username - username.
     * @return master service agreement ID for the logged in user.
     */
    String getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(String username);

}
