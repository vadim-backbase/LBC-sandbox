package com.backbase.accesscontrol.routes.datagroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_START_UPDATE_DATA_GROUP;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_UPDATE_DATA_GROUP_PERSIST;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_UPDATE_DATA_GROUP_VALIDATE;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.UPDATE_DATA_GROUP_ROUTE_ID;

import com.backbase.buildingblocks.backend.communication.extension.ExtensibleRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Camel route that routes exchanges from "direct:business.UpdateDataGroup" to all consumers listening on endpoint
 * "direct:updateDataGroupRequestedInternal";
 */
@Component
public class UpdateDataGroupByIdGroupRoute extends ExtensibleRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroupByIdGroupRoute.class);

    public UpdateDataGroupByIdGroupRoute() {
        super(UPDATE_DATA_GROUP_ROUTE_ID);
        LOGGER.info("Created route with id: {}", UPDATE_DATA_GROUP_ROUTE_ID);
    }

    @Override
    public void configure() {
        from(DIRECT_START_UPDATE_DATA_GROUP)
            .to(DIRECT_UPDATE_DATA_GROUP_VALIDATE)
            .to(DIRECT_UPDATE_DATA_GROUP_PERSIST);
    }
}
