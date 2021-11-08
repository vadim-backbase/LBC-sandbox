package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.CheckUserArrangementItemPermission endpoint. This
 * route forwards the received exchange to direct:CheckPermissionsForArrangementIdInternal.
 */
@Component
public class CheckUserArrangementItemPermissionRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "CheckUserArrangementItemPermission";

    /**
     * Route for checking arrangement item permissions for arrangement id.
     */
    public CheckUserArrangementItemPermissionRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_CHECK_PERMISSIONS_FOR_ARRANGEMENT_ID,
            EndpointConstants.DIRECT_DEFAULT_CHECK_PERMISSIONS_FOR_ARRANGEMENT_ID);
    }
}
