package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_DELETE_FUNCTION_GROUP_BY_ID;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_DELETE_FUNCTION_GROUP_BY_ID;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DeleteFunctionGroupByIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "DeleteFunctionGroupByIdRoute";

    /**
     * Route for deleting function group by id.
     */
    public DeleteFunctionGroupByIdRoute() {
        super(ROUTE_ID,
            DIRECT_BUSINESS_DELETE_FUNCTION_GROUP_BY_ID,
            DIRECT_DEFAULT_DELETE_FUNCTION_GROUP_BY_ID);
    }
}
