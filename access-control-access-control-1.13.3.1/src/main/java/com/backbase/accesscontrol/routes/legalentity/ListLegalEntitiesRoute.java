package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.ListLegalEntitiesRoute endpoint and
 * forwards the exchange to direct:listLegalEntitiesRequestedInternal endpoint.
 */
@Component
public class ListLegalEntitiesRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ListLegalEntities";

    /**
     * Route for listing legal entities.
     */
    public ListLegalEntitiesRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_LIST_LEGAL_ENTITIES,
            EndpointConstants.DIRECT_DEFAULT_LIST_LEGAL_ENTITIES);
    }
}
