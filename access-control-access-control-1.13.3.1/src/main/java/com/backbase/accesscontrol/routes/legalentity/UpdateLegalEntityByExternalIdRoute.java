package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.UpdateLegalEntityByExternalId
 * endpoint and forwards the exchange to direct:updateLegalEntityByExternalIdRequestedInternal endpoint.
 */
@Component
public class UpdateLegalEntityByExternalIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateLegalEntityByExternalId";

    /**
     * Route for updating legal entity by external ID.
     */
    public UpdateLegalEntityByExternalIdRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_UPDATE_LEGAL_ENTITY_BY_EXTERNAL_ID,
            EndpointConstants.DIRECT_DEFAULT_UPDATE_LEGAL_ENTITY_BY_EXTERNAL_ID);
    }
}
