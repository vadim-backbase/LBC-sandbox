package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_PARTICIPANTS;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_PARTICIPANTS;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Configures the Camel route listening on direct component business.getServiceAgreementParticipants endpoint.This route
 * forwards the received exchange to direct:getServiceAgreementByIdRequestedInternal.
 */
@Component
public class GetServiceAgreementParticipantsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "GetServiceAgreementParticipantsRoute";

    /**
     * Route for getting service agreement participants.
     */
    public GetServiceAgreementParticipantsRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_GET_SERVICE_AGREEMENT_PARTICIPANTS,
            DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_PARTICIPANTS);
    }

}
