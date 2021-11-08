package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import org.apache.camel.Body;

public interface AddLegalEntityRouteProxy {

    InternalRequest<CreateLegalEntitiesPostResponseBody> createLegalEntity(
        @Body InternalRequest<CreateLegalEntitiesPostRequestBody> request);
}
