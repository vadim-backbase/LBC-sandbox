package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateAssignUsersPermissionsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateAssignUsersPermissionsRoute";

    /**
     * Create a {@code SimpleExtensibleRouteBuilder} instance.
     */
    public UpdateAssignUsersPermissionsRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS,
            DIRECT_DEFAULT_DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS);
    }
}
