package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Get Legal Entity method specification that will be used to automatically inject a Camel Producer.
 */
public interface GetLegalEntityByExternalIdRouteProxy {

    InternalRequest<LegalEntityByExternalIdGetResponseBody> getLegalEntity(@Body InternalRequest<Void> request,
        @Header("externalId") String externalId);
}
