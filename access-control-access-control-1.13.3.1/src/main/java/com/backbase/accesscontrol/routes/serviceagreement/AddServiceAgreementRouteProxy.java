package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;

/**
 * Method specification that will be used to automatically inject a Camel Producer to route message for adding new
 * service agreement.
 */
public interface AddServiceAgreementRouteProxy {

    InternalRequest<ServiceAgreementPostResponseBody> addServiceAgreement(
        InternalRequest<ServiceAgreementPostRequestBody>
            internalRequest);
}
