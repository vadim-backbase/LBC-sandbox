package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on business.legalentities.GetLegalEntityByExternalId endpoint and
 * forwards the exchange to getLegalEntityByExternalIdRequestedInternal endpoint.
 */

@Component
public class GetLegalEntityByExternalIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetLegalEntityByExternalId";

    /**
     * Route for getting legal entity by it's external ID.
     */
    public GetLegalEntityByExternalIdRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITY_BY_EXTERNAL_ID,
            EndpointConstants.DIRECT_DEFAULT_GET_LEGAL_ENTITY_BY_EXTERNAL_ID);
    }
}
