package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GET_UNEXPOSED_USERS;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_GET_UNEXPOSED_USERS;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.getUnexposedUsers endpoint.This route forwards the
 * received exchange to direct:getUnexposedUsersRequestedInternal.
 */
@Component
public class GetUnexposedUsersRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetUnexposedUsersRoute";

    /**
     * Route for getting unexposed users in service agreement.
     */
    public GetUnexposedUsersRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_GET_UNEXPOSED_USERS, DIRECT_DEFAULT_GET_UNEXPOSED_USERS);
    }

}
