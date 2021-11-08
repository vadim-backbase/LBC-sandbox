package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.legalentities.GetLegalEntityById
 * endpoint and forwards the exchange to getLegalEntityByIdRequestedInternal endpoint.
 */
@Component
public class GetLegalEntityByIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetLegalEntityById";

    /**
     * Route for getting legal entity by it's internal ID.
     */
    public GetLegalEntityByIdRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITY_BY_ID,
            EndpointConstants.DIRECT_DEFAULT_GET_LEGAL_ENTITY_BY_ID);
    }
}
