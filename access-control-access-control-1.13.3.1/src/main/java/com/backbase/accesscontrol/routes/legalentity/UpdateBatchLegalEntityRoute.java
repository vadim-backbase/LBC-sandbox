package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.UpdateBatchLegalEntity endpoint and
 * forwards the exchange to direct:updateBatchLegalEntityRequestedInternal endpoint.
 */
@Component
public class UpdateBatchLegalEntityRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateBatchLegalEntity";

    /**
     * Route for updating batch legal entities.
     */
    public UpdateBatchLegalEntityRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_UPDATE_BATCH_LEGAL_ENTITY,
            EndpointConstants.DIRECT_DEFAULT_UPDATE_BATCH_LEGAL_ENTITY);
    }
}
