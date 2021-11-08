package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface ValidatePermissionsRouteProxy {

    InternalRequest<Void> getUserPermissionCheck(@Body InternalRequest<Void> internalRequest,
        @Header("userId") String userId,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("resourceName") String resourceName,
        @Header("functionName") String functionName,
        @Header("privileges") String privileges);
}
