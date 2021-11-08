package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_UPDATE_FUNCTION_GROUP_BY_ID;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP_BY_ID;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateFunctionGroupByIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateFunctionGroupByIdRoute";

    /**
     * Route for updating function group by id.
     */
    public UpdateFunctionGroupByIdRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_UPDATE_FUNCTION_GROUP_BY_ID, DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP_BY_ID);
    }
}
