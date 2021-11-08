package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Get Legal entity method specification that will be used to automatically inject a Camel Producer.
 */
public interface GetLegalEntityByIdRouteProxy {

    InternalRequest<LegalEntityByIdGetResponseBody> getLegalEntity(@Body InternalRequest<Void> request,
        @Header("legalEntityId") String legalEntityId);
}
