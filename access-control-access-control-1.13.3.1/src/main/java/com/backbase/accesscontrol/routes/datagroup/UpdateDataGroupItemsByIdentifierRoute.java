package com.backbase.accesscontrol.routes.datagroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_START_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER_PERSIST;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER_VALIDATE;

import com.backbase.buildingblocks.backend.communication.extension.ExtensibleRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateDataGroupItemsByIdentifierRoute extends ExtensibleRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroupItemsByIdentifierRoute.class);

    public UpdateDataGroupItemsByIdentifierRoute() {
        super(DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER);
        LOGGER.info("Created route with id: {}", DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER);
    }

    @Override
    public void configure() {
        from(DIRECT_START_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER)
            .to(DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER_VALIDATE)
            .to(DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER_PERSIST);
    }
}
