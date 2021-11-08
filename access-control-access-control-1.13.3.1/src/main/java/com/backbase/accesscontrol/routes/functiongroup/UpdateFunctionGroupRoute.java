package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_UPDATE_FUNCTION_GROUP;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateFunctionGroupRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateFunctionGroupRoute";

    /**
     * Route for updating function group by id.
     */
    public UpdateFunctionGroupRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_UPDATE_FUNCTION_GROUP, DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP);
    }
}
