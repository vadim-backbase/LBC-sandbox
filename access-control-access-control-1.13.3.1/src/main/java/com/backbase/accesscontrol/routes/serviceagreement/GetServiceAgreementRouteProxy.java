package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface GetServiceAgreementRouteProxy {

    InternalRequest<ServiceAgreementItemGetResponseBody> getServiceAgreementById(@Body InternalRequest<Void> request,
        @Header("serviceAgreementId") String serviceAgreementId);
}
