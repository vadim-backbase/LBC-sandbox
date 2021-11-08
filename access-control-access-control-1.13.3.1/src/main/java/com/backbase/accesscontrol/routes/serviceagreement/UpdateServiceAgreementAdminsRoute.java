package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_UPDATE_ADMINS;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_UPDATE_ADMINS;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceAgreementAdminsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateServiceAgreementAdminsRoute";

    /**
     * Route for updating admins in service agreement.
     */
    public UpdateServiceAgreementAdminsRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_UPDATE_ADMINS, DIRECT_DEFAULT_UPDATE_ADMINS);
    }
}
