package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GetMasterServiceAgreementByExternalLegalEntityIdRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetMasterServiceAgreementByExternalLegalEntityId";

    /**
     * Route for getting master SA for legal entity with given external ID.
     */
    public GetMasterServiceAgreementByExternalLegalEntityIdRoute() {
        super(ROUTE_ID,
            EndpointConstants.DIRECT_BUSINESS_GET_MASTER_SERVICE_AGREEMENT_BY_EXTERNAL_LEGAL_ENTITY_ID,
            EndpointConstants.DIRECT_DEFAULT_GET_MASTER_SERVICE_AGREEMENT_BY_EXTERNAL_LEGAL_ENTITY_ID);
    }
}
