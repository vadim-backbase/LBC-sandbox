package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import java.util.List;
import org.apache.camel.Body;

public interface UpdateParticipantsRouteProxy {

    InternalRequest<List<BatchResponseItemExtended>> updateParticipants(
        @Body InternalRequest<PresentationParticipantsPut> request);
}
