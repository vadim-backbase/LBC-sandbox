package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.DeleteBatchLegalEntity endpoint and
 * forwards the exchange to direct:deleteBatchLegalEntityRequestedInternal endpoint.
 */
@Component
public class DeleteBatchLegalEntityRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "DeleteBatchLegalEntity";

    /**
     * Route for deleting batch legal entity.
     */
    public DeleteBatchLegalEntityRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_DELETE_BATCH_LEGAL_ENTITY,
            EndpointConstants.DIRECT_DEFAULT_DELETE_BATCH_LEGAL_ENTITY);
    }
}
