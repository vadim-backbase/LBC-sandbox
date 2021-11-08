package com.backbase.accesscontrol.routes.approval;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.rejectApprovalRequest endpoint. This route forwards
 * the received exchange to direct:rejectApprovalRequest.
 */
@Component
public class RejectApprovalRequestRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "RejectApprovalRequest";

    /**
     * Route for rejecting approval request.
     */
    public RejectApprovalRequestRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_REJECT_APPROVAL_REQUEST,
            EndpointConstants.DIRECT_DEFAULT_REJECT_APPROVAL_REQUEST);
    }
}
