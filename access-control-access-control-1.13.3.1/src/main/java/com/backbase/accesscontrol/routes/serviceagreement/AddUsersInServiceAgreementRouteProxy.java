package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer to route message for adding new user
 * in service agreement.
 */
public interface AddUsersInServiceAgreementRouteProxy {

    InternalRequest<Void> addUsersInServiceAgreement(
        @Body InternalRequest<PresentationUsersForServiceAgreementRequestBody> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId);
}
