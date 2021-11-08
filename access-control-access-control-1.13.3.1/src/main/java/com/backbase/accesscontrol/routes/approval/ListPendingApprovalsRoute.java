package com.backbase.accesscontrol.routes.approval;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.ListPendingApprovals endpoint. This route forwards
 * the received exchange to direct:listPrivilegesRequestedInternal.
 */
@Component
public class ListPendingApprovalsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ListPendingApprovals";

    /**
     * Route for listing pending approvals.
     */
    public ListPendingApprovalsRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_LIST_PENDING_APPROVALS,
            EndpointConstants.DIRECT_DEFAULT_LIST_PENDING_APPROVALS);
    }
}
