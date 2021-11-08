package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on business.legalentities.GetLegalEntitiesByExternalIds endpoint
 * and forwards the exchange to getLegalEntitiesByExternalIdsRequestedInternal endpoint.
 */
@Component
public class GetLegalEntitiesByExternalIdsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetLegalEntitiesByExternalIds";

    /**
     * Route for listing legal entities by list of external ids.
     */
    public GetLegalEntitiesByExternalIdsRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITIES_BY_EXTERNAL_IDS,
            EndpointConstants.DIRECT_DEFAULT_GET_LEGAL_ENTITIES_BY_EXTERNAL_IDS);
    }
}
