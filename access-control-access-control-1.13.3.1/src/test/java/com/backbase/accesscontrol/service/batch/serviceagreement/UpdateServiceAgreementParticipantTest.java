package com.backbase.accesscontrol.service.batch.serviceagreement;

import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_BAD_REQUEST;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.PresentationActionDto;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.service.impl.ParticipantService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementParticipantTest {

    @InjectMocks
    private UpdateServiceAgreementParticipant updateServiceAgreementParticipant;
    @Spy
    private BatchResponseItemExtendedMapper batchResponseItemExtendedMapper =
        Mappers.getMapper(BatchResponseItemExtendedMapper.class);
    @Mock
    private ParticipantService participantService;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @Mock
    private EventBus eventBus;
    @Mock
    private Validator validator;

    @Test
    public void performBatchProcess() {
        ArgumentCaptor<PresentationParticipantPutBody> participant = ArgumentCaptor
            .forClass(PresentationParticipantPutBody.class);
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("extSa")
            .withAction(PresentationAction.ADD);

        when(participantService.updateServiceAgreementParticipants(any(PresentationParticipantPutBody.class)))
            .thenReturn("saId");

        updateServiceAgreementParticipant.processBatchItems(Lists.newArrayList(item));

        verify(participantService).updateServiceAgreementParticipants(participant.capture());
        verify(serviceAgreementBusinessRulesService).isServiceAgreementInPendingStateByExternalId("extSa");
        EnvelopedEvent<ServiceAgreementEvent> expectedEvent = new EnvelopedEvent<>();
        expectedEvent.setEvent(new ServiceAgreementEvent().withAction(UPDATE).withId("saId"));
        verify(eventBus).emitEvent(refEq(expectedEvent));
        assertEquals(item, participant.getValue());
        assertEquals(1, participant.getAllValues().size());
    }

    @Test
    public void getBatchResponseItemExtended() {
        PresentationParticipantPutBody item = new PresentationParticipantPutBody()
            .withExternalServiceAgreementId("exSAId")
            .withAction(PresentationAction.REMOVE)
            .withExternalParticipantId("exLEId");
        ResponseItemExtended expectedBean =
            new ResponseItemExtended(item.getExternalParticipantId(),
                item.getExternalServiceAgreementId(),
                HTTP_STATUS_BAD_REQUEST,
                PresentationActionDto.REMOVE
                , new ArrayList<>());

        ResponseItemExtended batchResponseItem = updateServiceAgreementParticipant.getBatchResponseItem(item,
            HTTP_STATUS_BAD_REQUEST,
            new ArrayList<>());

        assertThat(
            batchResponseItem,
            samePropertyValuesAs(expectedBean)
        );
    }

}