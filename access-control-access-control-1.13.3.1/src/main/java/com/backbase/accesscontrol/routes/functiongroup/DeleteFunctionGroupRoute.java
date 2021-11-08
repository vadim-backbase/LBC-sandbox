package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_DELETE_FUNCTION_GROUP;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_DELETE_FUNCTION_GROUP;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DeleteFunctionGroupRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "DeleteFunctionGroupRoute";

    /**
     * Route for deleting function group by id.
     */
    public DeleteFunctionGroupRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_DELETE_FUNCTION_GROUP, DIRECT_DEFAULT_DELETE_FUNCTION_GROUP);
    }
}
