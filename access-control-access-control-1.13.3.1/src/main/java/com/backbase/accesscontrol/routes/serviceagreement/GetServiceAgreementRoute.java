package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_ID;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_ID;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.getServiceAgreementById endpoint.This route
 * forwards the received exchange to direct:getServiceAgreementByIdRequestedInternal.
 */
@Component
public class GetServiceAgreementRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetServiceAgreementRoute";

    /**
     * Route for getting service agreement by id.
     */
    public GetServiceAgreementRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_ID, DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_ID);
    }

}
