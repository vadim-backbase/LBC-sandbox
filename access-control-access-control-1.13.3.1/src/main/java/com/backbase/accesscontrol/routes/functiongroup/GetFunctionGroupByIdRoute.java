package com.backbase.accesscontrol.routes.functiongroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GET_FUNCTION_GROUP_BY_ID;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_GET_FUNCTION_GROUP_BY_ID;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.GetFunctionAccessGroup endpoint.This route forwards
 * the received exchange to direct:getFunctionAccessGroupRequestedInternal.
 */
@Component
public class GetFunctionGroupByIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetFunctionGroupRoute";

    /**
     * Route for getting function group by id.
     */
    public GetFunctionGroupByIdRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_GET_FUNCTION_GROUP_BY_ID, DIRECT_DEFAULT_GET_FUNCTION_GROUP_BY_ID);
    }
}