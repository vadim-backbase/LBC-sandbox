package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.ListPrivileges endpoint.This route forwards the
 * received exchange to direct:listPrivilegesRequestedInternal.
 */
@Component
public class ListDataItemPrivilegesRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ListDataItemPrivileges";

    /**
     * Route for listing data item privileges.
     */
    public ListDataItemPrivilegesRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_LIST_DATA_ITEM_PRIVILEGES,
            EndpointConstants.DIRECT_DEFAULT_LIST_DATA_ITEM_PRIVILEGES);
    }
}
