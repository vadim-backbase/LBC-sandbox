package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface ListPrivilegesRouteProxy {

    InternalRequest<List<PrivilegesGetResponseBody>> getPrivileges(
        @Body InternalRequest<Void> internalRequest,
        @Header("userId") String userId,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("functionName") String functionName,
        @Header("resourceName") String resourceName);
}
