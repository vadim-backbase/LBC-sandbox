package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface GetUnexposedUsersRouteProxy {

    InternalRequest<PaginationDto<UnexposedUsersGetResponseBody>> getUnexposedUsers(
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("from") Integer from,
        @Header("size") Integer size,
        @Header("query") String query,
        @Header("cursor") String cursor);
}
