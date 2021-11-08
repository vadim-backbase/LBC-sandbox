package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_UPDATE_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_UPDATE_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateServiceAgreementRoute";

    /**
     * Route for updating service agreements.
     */
    public UpdateServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_UPDATE_SERVICE_AGREEMENT, DIRECT_DEFAULT_UPDATE_SERVICE_AGREEMENT);
    }
}
