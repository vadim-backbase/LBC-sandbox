package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RemoveParticipantSharingAccountRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "RemoveParticipantSharingAccount";

    /**
     * Route for removing participants that share users from service agreement.
     */
    public RemoveParticipantSharingAccountRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE,
            DIRECT_DEFAULT_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE);
    }
}
