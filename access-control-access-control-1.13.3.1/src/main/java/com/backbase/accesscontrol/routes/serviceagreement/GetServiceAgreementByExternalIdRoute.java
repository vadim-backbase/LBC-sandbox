package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.getServiceAgreementByExternalId endpoint.This route
 * forwards the received exchange to direct:getServiceAgreementByExternalIdRequestedInternal.
 */
@Component
public class GetServiceAgreementByExternalIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetServiceAgreementByExternalIdRoute";

    /**
     * Route for getting service agreement by external id.
     */
    public GetServiceAgreementByExternalIdRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID,
            DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID);
    }

}
