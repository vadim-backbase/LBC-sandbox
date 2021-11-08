package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Route Builder that configures route listening on business.legalentities.GetMasterServiceAgreementByLegalEntityId
 * endpoint and forwards the exchange to getMasterServiceAgreementByLegalEntityIdRequestedInternal endpoint.
 */
@Component
public class GetMasterServiceAgreementByLegalEntityIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetMasterServiceAgreementByLegalEntityId";

    /**
     * Route for getting master SA for legal entity with given internal ID.
     */
    public GetMasterServiceAgreementByLegalEntityIdRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_GET_MASTER_SERVICE_AGREEMENT_BY_LEGAL_ENTITY_ID,
            EndpointConstants.DIRECT_DEFAULT_GET_MASTER_SERVICE_AGREEMENT_BY_LEGAL_ENTITY_ID);
    }
}
