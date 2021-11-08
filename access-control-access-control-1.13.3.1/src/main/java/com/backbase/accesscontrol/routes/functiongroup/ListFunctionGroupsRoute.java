package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_LIST_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_LIST_FUNCTION_GROUPS;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ListFunctionGroupsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ListFunctionGroupsRoute";

    /**
     * Route for listing function groups.
     */
    public ListFunctionGroupsRoute() {
        super(ROUTE_ID,
            DIRECT_BUSINESS_LIST_FUNCTION_GROUPS,
            DIRECT_DEFAULT_LIST_FUNCTION_GROUPS);
    }
}
