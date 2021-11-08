package com.backbase.accesscontrol.routes.approval;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.AcceptApprovalRequest endpoint.This route forwards
 * the received exchange to direct:acceptApprovalRequestedInternal.
 */
@Component
public class AcceptApprovalRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "AcceptApprovalRequest";

    /**
     * Route for accepting approval by id.
     */
    public AcceptApprovalRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_ACCEPT_APPROVAL_REQUEST,
            EndpointConstants.DIRECT_DEFAULT_ACCEPT_APPROVAL_REQUEST);
    }
}
