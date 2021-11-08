package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface ListServiceAgreementsRouteProxy {

    InternalRequest<PaginationDto<ServiceAgreementGetResponseBody>> getServiceAgreements(
        @Body InternalRequest<Void> request,
        @Header("creatorId") String creatorId,
        @Header("query") String query,
        @Header("from") Integer from,
        @Header("size") Integer size,
        @Header("cursor") String cursor);
}
