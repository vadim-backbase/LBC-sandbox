package com.backbase.accesscontrol.routes.approval;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.GetPermissionsApprovalById endpoint. This route
 * forwards the received exchange to direct:getPermissionsApprovalByIdRequestedInternal.
 */
@Component
public class GetPermissionsApprovalDetailsByIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetPermissionsApprovalDetailsById";

    /**
     * Route for retrieving approval by id.
     */
    public GetPermissionsApprovalDetailsByIdRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_GET_PERMISSIONS_APPROVAL_BY_ID,
            EndpointConstants.DIRECT_DEFAULT_GET_PERMISSIONS_APPROVAL_BY_ID);
    }

}
