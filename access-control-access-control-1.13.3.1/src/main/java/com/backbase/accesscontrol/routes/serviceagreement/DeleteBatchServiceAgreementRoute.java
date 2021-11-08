package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures Camel route listening on direct component business.DeleteBatchServiceAgreement endpoint
 * and forwards the exchange to direct:deleteBatchServiceAgreement endpoint.
 */
@Component
public class DeleteBatchServiceAgreementRoute extends SimpleExtensibleRouteBuilder {
    private static final String ROUTE_ID = "DeleteBatchServiceAgreement";

    /**
     * Route for deleting batch service agreement.
     */
    public DeleteBatchServiceAgreementRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_DELETE_BATCH_SERVICE_AGREEMENT,
            EndpointConstants.DIRECT_DEFAULT_DELETE_BATCH_SERVICE_AGREEMENT);
    }
}
