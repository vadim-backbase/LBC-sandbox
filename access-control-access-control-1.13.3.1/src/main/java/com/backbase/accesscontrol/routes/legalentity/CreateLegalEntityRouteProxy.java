package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import org.apache.camel.Body;

public interface CreateLegalEntityRouteProxy {

    InternalRequest<LegalEntitiesPostResponseBody> createLegalEntity(
        @Body InternalRequest<LegalEntitiesPostRequestBody> request);
}
