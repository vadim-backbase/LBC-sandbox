package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AssignUserPermissionsBatchRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "AssignUserPermissionsBatch";

    /**
     * Route for assigning function/data group pair to user access.
     */
    public AssignUserPermissionsBatchRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_ASSIGN_USER_PERMISSIONS_BATCH,
            EndpointConstants.DIRECT_DEFAULT_BUSINESS_ASSIGN_USER_PERMISSIONS);
    }
}
