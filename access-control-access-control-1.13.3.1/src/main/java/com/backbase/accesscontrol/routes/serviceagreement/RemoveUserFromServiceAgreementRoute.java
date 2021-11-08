package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_REMOVE_USERS_FROM_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_REMOVE_USER_FROM_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.removeUsersFromServiceAgreement endpoint. This
 * route forwards the received exchange to direct:removeUserInServiceAgreementRequestedInternal.
 */
@Component
public class RemoveUserFromServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "RemoveUserFromServiceAgreementRoute";

    /**
     * Route for removing users from service agreement.
     */
    public RemoveUserFromServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_REMOVE_USERS_FROM_SERVICE_AGREEMENT,
            DIRECT_DEFAULT_REMOVE_USER_FROM_SERVICE_AGREEMENT);
    }
}
