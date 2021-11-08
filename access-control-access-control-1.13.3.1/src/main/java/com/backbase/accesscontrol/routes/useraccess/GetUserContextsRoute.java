package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Get user context by userIds route.
 */
@Component
public class GetUserContextsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "getUserContexts";

    /**
     * Route for getting user context.
     */
    public GetUserContextsRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_GET_USER_CONTEXT,
            EndpointConstants.DIRECT_DEFAULT_GET_USER_CONTEXT);
    }

}
