package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_INGEST_FUNCTION_GROUP;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_INGEST_FUNCTION_GROUP;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class IngestFunctionGroupRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "IngestFunctionGroupRoute";

    /**
     * Route for ingesting function groups.
     */
    public IngestFunctionGroupRoute() {
        super(ROUTE_ID,
            DIRECT_BUSINESS_INGEST_FUNCTION_GROUP,
            DIRECT_DEFAULT_INGEST_FUNCTION_GROUP);
    }
}
