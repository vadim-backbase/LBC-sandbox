package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Camel route that routes exchanges from "direct:business.ListDataAccessGroups" to all consumers listening on route
 * "direct:listDataAccessGroupRequestedInternal";
 */
@Component
public class ListDataGroupsRoute extends SimpleExtensibleRouteBuilder {

    public static final String ROUTE_ID = "ListDataGroups";

    public ListDataGroupsRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_LIST_DATA_GROUPS,
            EndpointConstants.DIRECT_DEFAULT_LIST_DATA_GROUPS);
    }
}
