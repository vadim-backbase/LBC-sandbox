package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface GetServiceAgreementParticipantsRouteProxy {

    InternalRequest<List<ServiceAgreementParticipantsGetResponseBody>> getServiceAgreementParticipants(@Body
        InternalRequest<Void> request,
        @Header("serviceAgreementId") String serviceAgreementId);
}
