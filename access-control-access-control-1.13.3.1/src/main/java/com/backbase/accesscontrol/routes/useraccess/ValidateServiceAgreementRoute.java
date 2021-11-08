package com.backbase.accesscontrol.routes.useraccess;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Validate Service agreement id from user context.
 */
@Component
public class ValidateServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "ValidateServiceAgreementRoute";

    /**
     * Route for validating service agreement from user context.
     */
    public ValidateServiceAgreementRoute() {
        super(ROUTE_ID, EndpointConstants.DIRECT_BUSINESS_VALIDATE_SERVICE_AGREEMENT,
            EndpointConstants.DIRECT_DEFAULT_VALIDATE_SERVICE_AGREEMENT);
    }
}
