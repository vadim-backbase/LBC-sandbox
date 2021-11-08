package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_EDIT_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_EDIT_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EditServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "EditServiceAgreementRoute";

    /**
     * Route for updating service agreement.
     */
    public EditServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_EDIT_SERVICE_AGREEMENT, DIRECT_DEFAULT_EDIT_SERVICE_AGREEMENT);
    }
}
