package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import org.apache.camel.Body;

public interface IngestServiceAgreementRouteProxy {

    InternalRequest<ServiceAgreementIngestPostResponseBody> ingestServiceAgreement(@Body
        InternalRequest<ServiceAgreementIngestPostRequestBody> internalRequest);
}
