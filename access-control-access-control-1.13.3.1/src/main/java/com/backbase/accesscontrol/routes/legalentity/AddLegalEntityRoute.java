package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.legalentities.CreateLegalEntity
 * endpoint and forwards the exchange to createLegalEntityRequestedInternal endpoint.
 */
@Component
public class AddLegalEntityRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "AddLegalEntity";

    /**
     * Route for adding new legal entity.
     */
    public AddLegalEntityRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_ADD_LEGAL_ENTITY,
            EndpointConstants.DIRECT_DEFAULT_ADD_LEGAL_ENTITY);
    }
}
