package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_ADD_FUNCTION_GROUP;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_ADD_FUNCTION_GROUP;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AddFunctionGroupRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "AddFunctionGroupRoute";

    /**
     * Route for adding function group.
     */
    public AddFunctionGroupRoute() {
        super(ROUTE_ID,
            DIRECT_BUSINESS_ADD_FUNCTION_GROUP,
            DIRECT_DEFAULT_ADD_FUNCTION_GROUP);
    }
}
