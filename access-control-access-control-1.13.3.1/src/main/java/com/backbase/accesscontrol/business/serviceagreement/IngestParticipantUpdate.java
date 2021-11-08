package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_INGEST_PARTICIPANT_UPDATE;

import com.backbase.accesscontrol.business.serviceagreement.participant.IngestParticipantUpdateProcessor;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateParticipantsRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IngestParticipantUpdate implements UpdateParticipantsRouteProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestParticipantUpdate.class);

    private IngestParticipantUpdateProcessor ingestParticipantUpdateProcessor;

    /**
     * Ingest update participants.
     *
     * @param request internal request of {@link PresentationParticipantsPut}
     * @return internal request of list of {@link BatchResponseItemExtended}
     */
    @Override
    @Consume(value = DIRECT_DEFAULT_INGEST_PARTICIPANT_UPDATE)
    public InternalRequest<List<BatchResponseItemExtended>> updateParticipants(
        @Body InternalRequest<PresentationParticipantsPut> request) {
        LOGGER.info("Ingest, update service agreement participants {}", request);
        return getInternalRequest(ingestParticipantUpdateProcessor.processParticipantUpdate(request.getData()),
            request.getInternalRequestContext());
    }

}
