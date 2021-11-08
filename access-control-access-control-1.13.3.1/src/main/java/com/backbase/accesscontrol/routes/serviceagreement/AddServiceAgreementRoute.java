package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_ADD_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_ADD_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AddServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "AddServiceAgreementRoute";

    /**
     * Route for adding service agreement.
     */
    public AddServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_ADD_SERVICE_AGREEMENT, DIRECT_DEFAULT_ADD_SERVICE_AGREEMENT);
    }
}
