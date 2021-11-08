package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_LIST_ADMIN_USERS_FOR_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_LIST_ADMIN_USERS_FOR_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.listAdminUsersForServiceAgreement endpoint. This
 * route forwards the received exchange to direct:listAdminUsersForServiceAgreementRequestedInternal.
 */
@Component
public class ListAdminsForServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ListAdminsForServiceAgreementRoute";

    /**
     * Route for listing admins in service agreement.
     */
    public ListAdminsForServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_LIST_ADMIN_USERS_FOR_SERVICE_AGREEMENT,
            DIRECT_DEFAULT_LIST_ADMIN_USERS_FOR_SERVICE_AGREEMENT);
    }
}
