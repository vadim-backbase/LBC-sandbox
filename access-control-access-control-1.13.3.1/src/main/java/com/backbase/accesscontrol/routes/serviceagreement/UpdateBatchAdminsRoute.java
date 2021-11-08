package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_INGEST_ADMINS_UPDATE;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_INGEST_ADMINS_UPDATE;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateBatchAdminsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "IngestAdminsUpdateRoute";

    /**
     * Route for batch updating admins in service agreement.
     */
    public UpdateBatchAdminsRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_INGEST_ADMINS_UPDATE, DIRECT_DEFAULT_INGEST_ADMINS_UPDATE);
    }
}
