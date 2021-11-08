package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_LIST_SERVICE_AGREEMENTS_HIERARCHY;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_LIST_SERVICE_AGREEMENTS_HIERARCHY;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.listServiceAgreementsHierarchy endpoint. This route
 * forwards the received exchange to direct:listServiceAgreementsHierarchyRequestedInternal.
 */
@Component
public class ListServiceAgreementsHierarchyRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ListServiceAgreementsRouteHierarchy";

    /**
     * Route for listing service agreement hierarchy.
     */
    public ListServiceAgreementsHierarchyRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_LIST_SERVICE_AGREEMENTS_HIERARCHY,
            DIRECT_DEFAULT_LIST_SERVICE_AGREEMENTS_HIERARCHY);
    }
}
