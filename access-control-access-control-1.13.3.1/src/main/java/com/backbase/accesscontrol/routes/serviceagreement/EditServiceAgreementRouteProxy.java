package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer to route message for adding new
 * service agreement.
 */
public interface EditServiceAgreementRouteProxy {

    InternalRequest<Void> editServiceAgreement(@Body InternalRequest<ServiceAgreementSave> internalRequest,
        @Header("id") String id);
}
