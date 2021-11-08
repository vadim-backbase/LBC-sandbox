package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Get users context by user providerId route proxy.
 */
public interface GetUserContextsRouteProxy {

    InternalRequest<ListElementsWrapper<UserContextServiceAgreementsGetResponseBody>> getUserContextsByUserId(
        @Body InternalRequest<Void> request,
        @Header("userId") String userId,
        @Header("query") String query,
        @Header("from") Integer from,
        @Header("cursor") String cursor,
        @Header("size") Integer size);
}
