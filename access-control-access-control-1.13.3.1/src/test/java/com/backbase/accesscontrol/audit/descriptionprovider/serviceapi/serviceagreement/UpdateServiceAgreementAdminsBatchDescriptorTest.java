package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationAction;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUserPair;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUsersBatchUpdate;
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
public class UpdateServiceAgreementAdminsBatchDescriptorTest {

    @InjectMocks
    private UpdateServiceAgreementAdminsBatchDescriptor descriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataListTest() {
        BatchResponseItemExtended response2 = new BatchResponseItemExtended()
            .resourceId("user2")
            .externalServiceAgreementId("ex2")
            .action(PresentationAction.REMOVE)
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItemExtended response1 = new BatchResponseItemExtended()
            .resourceId("user1")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ex1")
            .status(StatusEnum.HTTP_STATUS_OK);

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription(
                "Add Admin | Service Agreement | Successful | external service agreement ID ex1, external user ID user1")
            .withEventMetaDatum("External Service Agreement ID", response1.getExternalServiceAgreementId())
            .withEventMetaDatum("External User ID", response1.getResourceId())
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(
                "Remove Admin | Service Agreement | Successful | external service agreement ID ex2, external user ID user2")
            .withEventMetaDatum("External Service Agreement ID", response2.getExternalServiceAgreementId())
            .withEventMetaDatum("External User ID", response2.getResourceId())
            .withStatus(Status.SUCCESSFUL);
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = descriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(asList(response1, response2), HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getSuccessEventDataListWithBothFailedAndSuccessfulEventsTest() {
        List<String> errors = new ArrayList<>();
        errors.add("bad request");
        errors.add("something went wrong");
        BatchResponseItemExtended response2 = new BatchResponseItemExtended()
            .resourceId("user2")
            .externalServiceAgreementId("ex2")
            .action(PresentationAction.ADD)
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
            .errors(errors);
        BatchResponseItemExtended response1 = new BatchResponseItemExtended()
            .resourceId("user1")
            .action(PresentationAction.ADD)
            .externalServiceAgreementId("ex1")
            .status(StatusEnum.HTTP_STATUS_OK);

        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(
                "Add Admin | Service Agreement | Failed | external service agreement ID ex2, external user ID user2")
            .withEventMetaDatum("External Service Agreement ID", response2.getExternalServiceAgreementId())
            .withEventMetaDatum("External User ID", response2.getResourceId())
            .withEventMetaDatum("Error code", response2.getStatus().toString())
            .withEventMetaDatum("Error message", response2.getErrors().isEmpty() ? "" : response2.getErrors().get(0))
            .withStatus(Status.FAILED);
        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription(
                "Add Admin | Service Agreement | Successful | external service agreement ID ex1, external user ID user1")
            .withEventMetaDatum("External Service Agreement ID", response1.getExternalServiceAgreementId())
            .withEventMetaDatum("External User ID", response1.getResourceId())
            .withStatus(Status.SUCCESSFUL);
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = descriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(asList(response1, response2), HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getInitiatedEventDataListTest() {
        List<PresentationServiceAgreementUserPair> users = new ArrayList<>();
        PresentationServiceAgreementUserPair presentationServiceAgreementUserPair1 = new PresentationServiceAgreementUserPair();
        PresentationServiceAgreementUserPair presentationServiceAgreementUserPair2 = new PresentationServiceAgreementUserPair();
        presentationServiceAgreementUserPair1.externalServiceAgreementId("ex1").externalUserId("user1");
        presentationServiceAgreementUserPair2.externalServiceAgreementId("ex2").externalUserId("user2");
        users.add(presentationServiceAgreementUserPair1);
        users.add(presentationServiceAgreementUserPair2);

        PresentationServiceAgreementUsersBatchUpdate presentationServiceAgreementUsersUpdate =
            new PresentationServiceAgreementUsersBatchUpdate();
        presentationServiceAgreementUsersUpdate.action(PresentationAction.ADD).users(users);
        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationServiceAgreementUsersUpdate).toArray());

        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(
                "Add Admin | Service Agreement | Initiated | external service agreement ID ex2, external user ID user2")
            .withEventMetaDatum("External Service Agreement ID",
                presentationServiceAgreementUserPair2.getExternalServiceAgreementId())
            .withEventMetaDatum("External User ID", presentationServiceAgreementUserPair2.getExternalUserId())
            .withStatus(Status.INITIATED);
        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription(
                "Add Admin | Service Agreement | Initiated | external service agreement ID ex1, external user ID user1")
            .withEventMetaDatum("External Service Agreement ID",
                presentationServiceAgreementUserPair1.getExternalServiceAgreementId())
            .withEventMetaDatum("External User ID", presentationServiceAgreementUserPair1.getExternalUserId())
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = descriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

}
