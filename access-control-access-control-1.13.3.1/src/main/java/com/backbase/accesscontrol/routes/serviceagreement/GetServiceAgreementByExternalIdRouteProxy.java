package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface GetServiceAgreementByExternalIdRouteProxy {

    InternalRequest<ServiceAgreementExternalIdGetResponseBody> getServiceAgreementByExternalId(
        @Body InternalRequest<Void> request,
        @Header("externalId") String externalId);
}
