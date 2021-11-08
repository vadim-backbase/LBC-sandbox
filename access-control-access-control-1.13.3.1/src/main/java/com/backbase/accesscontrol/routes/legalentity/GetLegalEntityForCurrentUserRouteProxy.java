package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import org.apache.camel.Body;

/**
 * Get Legal entity method for current user specification that will be used to automatically inject a Camel Producer.
 */
public interface GetLegalEntityForCurrentUserRouteProxy {

    InternalRequest<LegalEntityForUserGetResponseBody> getLegalEntityForCurrentUser(
        @Body InternalRequest<Void> request);
}
