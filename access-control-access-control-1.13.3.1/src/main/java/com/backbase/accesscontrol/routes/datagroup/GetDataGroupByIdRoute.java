package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Camel route that routes exchanges from "direct:business.GetDataGroupById" to all consumers listening on route
 * "direct:getDataGroupByIdRequestedInternal";
 */
@Component
public class GetDataGroupByIdRoute extends SimpleExtensibleRouteBuilder {

    public static final String ROUTE_ID = "GetDataGroupById";

    public GetDataGroupByIdRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_GET_DATA_GROUP_BY_ID,
            EndpointConstants.DIRECT_DEFAULT_GET_DATA_GROUP_BY_ID);
    }
}
