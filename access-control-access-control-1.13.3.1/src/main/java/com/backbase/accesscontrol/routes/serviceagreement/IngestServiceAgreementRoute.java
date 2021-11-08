package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_INGEST_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_INGEST_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class IngestServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "IngestServiceAgreementRoute";

    /**
     * Route for ingesting service agreement.
     */
    public IngestServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_INGEST_SERVICE_AGREEMENT, DIRECT_DEFAULT_INGEST_SERVICE_AGREEMENT);
    }
}

