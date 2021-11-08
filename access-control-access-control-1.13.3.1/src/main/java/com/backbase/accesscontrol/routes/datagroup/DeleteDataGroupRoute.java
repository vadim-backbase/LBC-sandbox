package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;


/**
 * Camel route that routes exchanges from "direct:business.DeleteDataGroup" to all consumers listening on route
 * "direct:deleteDataGroupRequestedInternal";
 */
@Component
public class DeleteDataGroupRoute extends SimpleExtensibleRouteBuilder {

    public static final String ROUTE_ID = "DeleteDataGroup";

    public DeleteDataGroupRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_DELETE_DATA_GROUP,
            EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUP);
    }
}
