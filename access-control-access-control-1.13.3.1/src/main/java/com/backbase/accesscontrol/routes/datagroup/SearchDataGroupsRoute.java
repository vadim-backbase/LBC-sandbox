package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SearchDataGroupsRoute extends SimpleExtensibleRouteBuilder {

    public static final String ROUTE_ID = "SearchDataGroupsRoute";

    public SearchDataGroupsRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_SEARCH_DATA_GROUPS,
            EndpointConstants.DIRECT_DEFAULT_SEARCH_DATA_GROUPS);
    }
}
