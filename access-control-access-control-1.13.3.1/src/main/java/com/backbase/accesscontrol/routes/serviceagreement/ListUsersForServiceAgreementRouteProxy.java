package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface ListUsersForServiceAgreementRouteProxy {

    InternalRequest<ListElementsWrapper<ServiceAgreementUsersGetResponseBody>> getUsersForServiceAgreement(
        @Body InternalRequest<Void> internalRequest,
        @Header("id") String serviceAgreementId,
        @Header("query") String query,
        @Header("from") Integer from,
        @Header("size") Integer size,
        @Header("cursor") String cursor);
}
