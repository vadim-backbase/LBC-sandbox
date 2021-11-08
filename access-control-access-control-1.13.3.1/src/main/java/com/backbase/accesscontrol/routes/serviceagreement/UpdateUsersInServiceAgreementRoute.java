package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_UPDATE_USERS_IN_SA;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_UPDATE_USERS_IN_SA;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateUsersInServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateUsersInServiceAgreementRoute";

    /**
     * Route for batch updating users in service agreement.
     */
    public UpdateUsersInServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_UPDATE_USERS_IN_SA,
            DIRECT_DEFAULT_UPDATE_USERS_IN_SA);
    }
}
