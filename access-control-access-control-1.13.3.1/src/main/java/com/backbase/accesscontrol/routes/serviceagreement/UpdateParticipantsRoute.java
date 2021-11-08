package com.backbase.accesscontrol.routes.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_INGEST_PARTICIPANT_UPDATE;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_INGEST_PARTICIPANT_UPDATE;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateParticipantsRoute extends SimpleExtensibleRouteBuilder {

    private static final String ROUTE_ID = "UpdateParticipantsRoute";

    /**
     * Route for batch updating participants in service agreement.
     */
    public UpdateParticipantsRoute() {
        super(ROUTE_ID, DIRECT_BUSINESS_INGEST_PARTICIPANT_UPDATE, DIRECT_DEFAULT_INGEST_PARTICIPANT_UPDATE);
    }
}
