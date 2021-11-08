package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GET_ASSIGNED_USERS_PERMISSIONS;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_GET_ASSIGNED_USERS_PERMISSIONS_INTERNAL;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GetAssignedUserPermissionsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetAssignedUserPermissionsRoute";

    /**
     * Route for listing users permissions for given service agreement.
     */
    public GetAssignedUserPermissionsRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_GET_ASSIGNED_USERS_PERMISSIONS,
            DIRECT_GET_ASSIGNED_USERS_PERMISSIONS_INTERNAL);
    }
}
