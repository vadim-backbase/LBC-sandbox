package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.legalentities.CreateLegalEntity
 * endpoint and forwards the exchange to createLegalEntityRequestedInternal endpoint.
 */
@Component
public class CreateLegalEntityRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "CreateLegalEntity";

    /**
     * Route for creating new legal entity.
     */
    public CreateLegalEntityRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_CREATE_LEGAL_ENTITY,
            EndpointConstants.DIRECT_DEFAULT_CREATE_LEGAL_ENTITY);
    }
}
