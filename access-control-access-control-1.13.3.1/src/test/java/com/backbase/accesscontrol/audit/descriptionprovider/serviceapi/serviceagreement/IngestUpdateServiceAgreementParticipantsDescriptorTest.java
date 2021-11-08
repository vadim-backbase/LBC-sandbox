package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationAction;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantBatchUpdate;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantPutBody;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class IngestUpdateServiceAgreementParticipantsDescriptorTest {

    @InjectMocks
    private IngestUpdateServiceAgreementParticipantsDescriptor descriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void shouldReturnSuccessfulEventMetaData() {

        List<PresentationParticipantPutBody> participants = new ArrayList<>();
        PresentationParticipantPutBody presentationParticipantPutBody = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId1")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ExSa1")
            .sharingAccounts(true)
            .sharingUsers(true);
        PresentationParticipantPutBody presentationParticipantPutBody2 = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId2")
            .action(PresentationAction.REMOVE)
            .externalServiceAgreementId("ExSa2")
            .sharingAccounts(true)
            .sharingUsers(false);
        PresentationParticipantPutBody presentationParticipantPutBody3 = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId3")
            .action(PresentationAction.REMOVE)
            .externalServiceAgreementId("ExSa3");
        PresentationParticipantPutBody presentationParticipantPutBody4 = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId4")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ExSa4")
            .sharingAccounts(false)
            .sharingUsers(false);

        participants.add(presentationParticipantPutBody);
        participants.add(presentationParticipantPutBody2);
        participants.add(presentationParticipantPutBody3);
        participants.add(presentationParticipantPutBody4);
        PresentationParticipantBatchUpdate request1 = new PresentationParticipantBatchUpdate()
            .participants(participants);

        BatchResponseItemExtended response4 = new BatchResponseItemExtended()
            .resourceId("ExParticipantId4")
            .externalServiceAgreementId("ExSa4")
            .action(PresentationAction.ADD)
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItemExtended response3 = new BatchResponseItemExtended()
            .resourceId("ExParticipantId3")
            .externalServiceAgreementId("ExSa3")
            .action(PresentationAction.REMOVE)
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItemExtended response2 = new BatchResponseItemExtended()
            .resourceId("ExParticipantId2")
            .externalServiceAgreementId("ExSa2")
            .action(PresentationAction.REMOVE)
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItemExtended response1 = new BatchResponseItemExtended()
            .resourceId("ExParticipantId1")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ExSa1")
            .status(StatusEnum.HTTP_STATUS_OK);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request1});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                "Add Participant | Service Agreement | Successful | external service agreement ID ExSa1, external participant ID ExParticipantId1")
            .withEventMetaDatum("Add External Participant ID",
                presentationParticipantPutBody.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody.getExternalServiceAgreementId())
            .withEventMetaDatum("Participant is sharing users",
                presentationParticipantPutBody.getSharingUsers().toString())
            .withEventMetaDatum("Participant is sharing accounts",
                presentationParticipantPutBody.getSharingAccounts().toString());

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                "Remove Participant | Service Agreement | Successful | external service agreement ID ExSa2, external participant ID ExParticipantId2")
            .withEventMetaDatum("Remove External Participant ID",
                presentationParticipantPutBody2.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody2.getExternalServiceAgreementId());

        AuditMessage eventData3 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                "Remove Participant | Service Agreement | Successful | external service agreement ID ExSa3, external participant ID ExParticipantId3")
            .withEventMetaDatum("Remove External Participant ID",
                presentationParticipantPutBody3.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody3.getExternalServiceAgreementId());

        AuditMessage eventData4 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                "Add Participant | Service Agreement | Successful | external service agreement ID ExSa4, external participant ID ExParticipantId4")
            .withEventMetaDatum("Add External Participant ID",
                presentationParticipantPutBody4.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody4.getExternalServiceAgreementId())
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Participant is sharing users", "false");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3, eventData4);

        List<AuditMessage> initDescription = descriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(asList(response1, response2, response3, response4), HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnSuccessfulAndFailedEventMetaData() {

        List<PresentationParticipantPutBody> participants = new ArrayList<>();
        PresentationParticipantPutBody presentationParticipantPutBody = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId1")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ExSa1")
            .sharingAccounts(true)
            .sharingUsers(true);
        PresentationParticipantPutBody presentationParticipantPutBody2 = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId2")
            .action(PresentationAction.REMOVE)
            .externalServiceAgreementId("ExSa2")
            .sharingAccounts(true)
            .sharingUsers(false);
        PresentationParticipantPutBody presentationParticipantPutBody3 = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId3")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ExSa3")
            .sharingAccounts(false)
            .sharingUsers(false);

        participants.add(presentationParticipantPutBody);
        participants.add(presentationParticipantPutBody2);
        participants.add(presentationParticipantPutBody3);
        PresentationParticipantBatchUpdate request1 = new PresentationParticipantBatchUpdate()
            .participants(participants);

        List<String> errors = new ArrayList<>();
        errors.add("Bad Request");
        BatchResponseItemExtended response3 = new BatchResponseItemExtended()
            .resourceId("ExParticipantId3")
            .externalServiceAgreementId("ExSa3")
            .errors(errors)
            .action(PresentationAction.ADD)
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        BatchResponseItemExtended response2 = new BatchResponseItemExtended()
            .resourceId("ExParticipantId2")
            .externalServiceAgreementId("ExSa2")
            .errors(errors)
            .action(PresentationAction.REMOVE)
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        BatchResponseItemExtended response1 = new BatchResponseItemExtended()
            .resourceId("ExParticipantId1")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ExSa1")
            .status(StatusEnum.HTTP_STATUS_OK);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request1});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                "Add Participant | Service Agreement | Successful | external service agreement ID ExSa1, external participant ID ExParticipantId1")
            .withEventMetaDatum("Add External Participant ID",
                presentationParticipantPutBody.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody.getExternalServiceAgreementId())
            .withEventMetaDatum("Participant is sharing users",
                presentationParticipantPutBody.getSharingUsers().toString())
            .withEventMetaDatum("Participant is sharing accounts",
                presentationParticipantPutBody.getSharingAccounts().toString());

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(
                "Remove Participant | Service Agreement | Failed | external service agreement ID ExSa2, external participant ID ExParticipantId2")
            .withEventMetaDatum("Remove External Participant ID",
                presentationParticipantPutBody2.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody2.getExternalServiceAgreementId())
            .withEventMetaDatum("Error code", "400")
            .withEventMetaDatum("Error message", "Bad Request");

        AuditMessage eventData3 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(
                "Add Participant | Service Agreement | Failed | external service agreement ID ExSa3, external participant ID ExParticipantId3")
            .withEventMetaDatum("Add External Participant ID",
                presentationParticipantPutBody3.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody3.getExternalServiceAgreementId())
            .withEventMetaDatum("Error code", "400")
            .withEventMetaDatum("Error message", "Bad Request")
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Participant is sharing users", "false");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = descriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(asList(response1, response2, response3), HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnInititatedEventMetaData() {

        List<PresentationParticipantPutBody> participants = new ArrayList<>();
        PresentationParticipantPutBody presentationParticipantPutBody = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId1")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ExSa1")
            .sharingAccounts(true)
            .sharingUsers(true);
        PresentationParticipantPutBody presentationParticipantPutBody2 = new PresentationParticipantPutBody()
            .externalParticipantId("ExParticipantId2")
            .action(PresentationAction.REMOVE)
            .externalServiceAgreementId("ExSa2")
            .sharingAccounts(true)
            .sharingUsers(false);
        participants.add(presentationParticipantPutBody);
        participants.add(presentationParticipantPutBody2);
        PresentationParticipantBatchUpdate request1 = new PresentationParticipantBatchUpdate()
            .participants(participants);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request1});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                "Add Participant | Service Agreement | Initiated | external service agreement ID ExSa1, external participant ID ExParticipantId1")
            .withEventMetaDatum("Add External Participant ID",
                presentationParticipantPutBody.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody.getExternalServiceAgreementId())
            .withEventMetaDatum("Participant is sharing users",
                presentationParticipantPutBody.getSharingUsers().toString())
            .withEventMetaDatum("Participant is sharing accounts",
                presentationParticipantPutBody.getSharingAccounts().toString());

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                "Remove Participant | Service Agreement | Initiated | external service agreement ID ExSa2, external participant ID ExParticipantId2")
            .withEventMetaDatum("Remove External Participant ID",
                presentationParticipantPutBody2.getExternalParticipantId())
            .withEventMetaDatum("External Service Agreement ID",
                presentationParticipantPutBody2.getExternalServiceAgreementId());

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = descriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

}
