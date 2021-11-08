package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Validates service agreement for the user id Route Proxy.
 */
public interface ValidateServiceAgreementRouteProxy {

    InternalRequest<String> validate(
        @Body InternalRequest<Void> internalRequest,
        @Header("userId") String externalUserId,
        @Header("serviceAgreementId") String serviceAgreementId);
}
