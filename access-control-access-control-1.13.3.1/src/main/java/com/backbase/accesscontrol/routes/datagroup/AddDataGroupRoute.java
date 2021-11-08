package com.backbase.accesscontrol.routes.datagroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.ADD_DATA_GROUP_ROUTE_ID;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_ADD_DATA_GROUP_PERSIST;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_ADD_DATA_GROUP_VALIDATE;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_START_ADD_DATA_GROUP;

import com.backbase.buildingblocks.backend.communication.extension.ExtensibleRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Camel route that routes exchanges from "direct:business.AddDataGroup" to all consumers listening on route
 * "direct:addDataGroupRequestedInternal";
 */
@Component
public class AddDataGroupRoute extends ExtensibleRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddDataGroupRoute.class);

    public AddDataGroupRoute() {
        super(ADD_DATA_GROUP_ROUTE_ID);
        LOGGER.info("Created route with id: {}", ADD_DATA_GROUP_ROUTE_ID);
    }

    @Override
    public void configure() {
        from(DIRECT_START_ADD_DATA_GROUP)
            .to(DIRECT_ADD_DATA_GROUP_VALIDATE)
            .to(DIRECT_ADD_DATA_GROUP_PERSIST);
    }
}
