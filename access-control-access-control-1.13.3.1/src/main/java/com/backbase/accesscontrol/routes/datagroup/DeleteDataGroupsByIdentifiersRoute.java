package com.backbase.accesscontrol.routes.datagroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_DELETE_DATA_GROUPS_BY_IDENTIFIERS;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUPS_BY_IDENTIFIERS;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DeleteDataGroupsByIdentifiersRoute extends SimpleExtensibleRouteBuilder {

    public static final String ROUTE_ID = "DeleteDataGroupByIdentifiers";

    public DeleteDataGroupsByIdentifiersRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_DELETE_DATA_GROUPS_BY_IDENTIFIERS,
            DIRECT_DEFAULT_DELETE_DATA_GROUPS_BY_IDENTIFIERS);
    }
}
