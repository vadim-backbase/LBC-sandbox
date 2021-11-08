package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Get Legalentities method specification that will be used to automatically inject a Camel Producer.
 */
public interface ListLegalentitiesRouteProxy {

    InternalRequest<List<LegalEntitiesGetResponseBody>> getLegalentites(@Body InternalRequest<Void> request,
        @Header("parentEntityId") String parentEntityId);
}
