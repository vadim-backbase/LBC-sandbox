package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.getUserPrivilegesSummary endpoint.This route
 * forwards the received exchange to direct:getUserPrivilegesSummaryRequestedInternal.
 */
@Component
public class GetUserPrivilegesSummaryRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "getUserPrivilegesSummary";

    /**
     * Route for getting user privileges summary.
     */
    public GetUserPrivilegesSummaryRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_LIST_USER_PRIVILEGES_SUMMARY,
            EndpointConstants.DIRECT_DEFAULT_LIST_USER_PRIVILEGES_SUMMARY);
    }
}
