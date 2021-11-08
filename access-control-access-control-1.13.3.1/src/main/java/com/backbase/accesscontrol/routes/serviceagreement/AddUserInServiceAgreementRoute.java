package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_ADD_USERS_IN_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_ADD_USER_IN_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.addUserInServiceAgreement endpoint. This route
 * forwards the received exchange to direct:addUserInServiceAgreementRequestedInternal.
 */
@Component
public class AddUserInServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "AddUserInServiceAgreementRoute";

    /**
     * Route for batch adding users in service agreement.
     */
    public AddUserInServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_ADD_USERS_IN_SERVICE_AGREEMENT,
            DIRECT_DEFAULT_ADD_USER_IN_SERVICE_AGREEMENT);
    }
}
