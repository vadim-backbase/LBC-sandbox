package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on business.legalentities.GetLegalEntityForCurrentUser endpoint
 * and forwards the exchange to getLegalEntityForCurrentUserRequestedInternal endpoint.
 */
@Component
public class GetLegalEntityForCurrentUserRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetLegalEntityForCurrentUser";

    /**
     * Route for getting legal entity for the logged in user.
     */
    public GetLegalEntityForCurrentUserRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITY_FOR_CURRENT_USER,
            EndpointConstants.DIRECT_DEFAULT_GET_LEGAL_ENTITY_FOR_CURRENT_USER);
    }
}
