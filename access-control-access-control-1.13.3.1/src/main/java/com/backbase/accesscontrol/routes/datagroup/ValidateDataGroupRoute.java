package com.backbase.accesscontrol.routes.datagroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GROUP_VALIDATE;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_DATA_GROUP_VALIDATE;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ValidateDataGroupRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ValidateDataGroup";

    public ValidateDataGroupRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_GROUP_VALIDATE, DIRECT_DEFAULT_DATA_GROUP_VALIDATE);
    }
}
