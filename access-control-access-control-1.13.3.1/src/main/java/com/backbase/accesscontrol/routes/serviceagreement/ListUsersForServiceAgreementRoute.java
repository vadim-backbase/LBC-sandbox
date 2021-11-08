package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_LIST_USERS_FOR_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_LIST_USERS_FOR_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.listUsersForServiceAgreement endpoint. This route
 * forwards the received exchange to direct:listUsersForServiceAgreementRequestedInternal.
 */
@Component
public class ListUsersForServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ListUsersForServiceAgreementRoute";

    /**
     * Route for listing users in service agreement.
     */
    public ListUsersForServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_LIST_USERS_FOR_SERVICE_AGREEMENT,
            DIRECT_DEFAULT_LIST_USERS_FOR_SERVICE_AGREEMENT);
    }
}
