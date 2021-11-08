package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.CheckPermissions endpoint. This route forwards the
 * received exchange to direct:checkPermissionsRequestedInternal.
 */
@Component
public class ValidatePermissionsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ValidatePermissions";

    /**
     * Route for validating permissions.
     */
    public ValidatePermissionsRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_CHECK_PERMISSIONS,
            EndpointConstants.DIRECT_DEFAULT_CHECK_PERMISSIONS);
    }
}
