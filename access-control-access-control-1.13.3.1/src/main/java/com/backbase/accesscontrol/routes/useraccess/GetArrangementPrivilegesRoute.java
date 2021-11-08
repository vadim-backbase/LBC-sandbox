package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.GetArrangementPrivileges endpoint. This route
 * forwards the received exchange to direct:getArrangementPrivilegesInternal.
 */
@Component
public class GetArrangementPrivilegesRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetArrangementPrivileges";

    /**
     * Route for listing arrangement privileges.
     */
    public GetArrangementPrivilegesRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_LIST_ARRANGEMENT_PRIVILEGES,
            EndpointConstants.DIRECT_DEFAULT_LIST_ARRANGEMENT_PRIVILEGES);
    }
}
