package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementBatchDelete;
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
public class DeleteServiceAgreementBatchDescriptorTest {

    @Mock
    private ProceedingJoinPoint joinPoint;
    @InjectMocks
    private DeleteServiceAgreementBatchDescriptor deleteServiceAgreementBatchDescriptor;

    @Test
    public void getInitEventMetaDataListTest() {
        PresentationServiceAgreementIdentifier item1 = new PresentationServiceAgreementIdentifier()
            .idIdentifier("IdIdentifier");
        PresentationServiceAgreementIdentifier item2 = new PresentationServiceAgreementIdentifier()
            .nameIdentifier("NameIdentifier");
        PresentationServiceAgreementIdentifier item3 = new PresentationServiceAgreementIdentifier()
            .externalIdIdentifier("ExternalIdIdentifier");
        List<PresentationServiceAgreementIdentifier> serviceAgreementsIdentifiers = asList(item1, item2, item3);

        ServiceAgreementBatchDelete presentationDeleteServiceAgreements = new ServiceAgreementBatchDelete()
            .serviceAgreementIdentifiers(serviceAgreementsIdentifiers);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationDeleteServiceAgreements).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum("Service Agreement ID", "IdIdentifier")
            .withEventDescription("Delete | Service Agreement | Initiated | ID IdIdentifier")
            .withStatus(Status.INITIATED);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription("Delete | Service Agreement | Initiated | name NameIdentifier")
            .withEventMetaDatum("Service Agreement Name", "NameIdentifier")
            .withStatus(Status.INITIATED);
        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Delete | Service Agreement | Initiated | external ID ExternalIdIdentifier")
            .withEventMetaDatum("External Service Agreement ID", "ExternalIdIdentifier")
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedMetaData = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> actualMetaData = deleteServiceAgreementBatchDescriptor
            .getInitEventDataList(joinPoint);

        assertEquals(expectedMetaData, actualMetaData);
    }

    @Test
    public void getMessageIdsTest() {
        PresentationServiceAgreementIdentifier item1 = new PresentationServiceAgreementIdentifier()
            .idIdentifier("IdIdentifier");
        PresentationServiceAgreementIdentifier item2 = new PresentationServiceAgreementIdentifier()
            .nameIdentifier("NameIdentifier");
        PresentationServiceAgreementIdentifier item3 = new PresentationServiceAgreementIdentifier()
            .externalIdIdentifier("ExternalIdIdentifier");
        List<PresentationServiceAgreementIdentifier> serviceAgreementsIdentifiers = asList(item1, item2, item3);

        ServiceAgreementBatchDelete presentationDeleteServiceAgreements = new ServiceAgreementBatchDelete()
            .serviceAgreementIdentifiers(serviceAgreementsIdentifiers);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationDeleteServiceAgreements).toArray());

        List<String> actualMessageIds = deleteServiceAgreementBatchDescriptor
            .getMessageIds(joinPoint);

        assertEquals(3, actualMessageIds.size());
    }


    @Test
    public void getSuccessEventDataListTest() {
        BatchResponseItem batchResponseItem1 = new BatchResponseItem()
            .resourceId("IdIdentifier")
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItem batchResponseItem2 = new BatchResponseItem()
            .resourceId("NameIdentifier")
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItem batchResponseItem3 = new BatchResponseItem()
            .resourceId("ExternalIdIdentifier")
            .status(StatusEnum.HTTP_STATUS_OK);

        PresentationServiceAgreementIdentifier item1 = new PresentationServiceAgreementIdentifier()
            .idIdentifier("IdIdentifier");
        PresentationServiceAgreementIdentifier item2 = new PresentationServiceAgreementIdentifier()
            .nameIdentifier("NameIdentifier");
        PresentationServiceAgreementIdentifier item3 = new PresentationServiceAgreementIdentifier()
            .externalIdIdentifier("ExternalIdIdentifier");
        List<PresentationServiceAgreementIdentifier> serviceAgreementsIdentifiers = asList(item1, item2, item3);

        ServiceAgreementBatchDelete presentationDeleteServiceAgreements = new ServiceAgreementBatchDelete()
            .serviceAgreementIdentifiers(serviceAgreementsIdentifiers);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationDeleteServiceAgreements).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum("Service Agreement ID", "IdIdentifier")
            .withEventDescription("Delete | Service Agreement | Successful | ID IdIdentifier")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription("Delete | Service Agreement | Successful | name NameIdentifier")
            .withEventMetaDatum("Service Agreement Name", "NameIdentifier")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Delete | Service Agreement | Successful | external ID ExternalIdIdentifier")
            .withEventMetaDatum("External Service Agreement ID", "ExternalIdIdentifier")
            .withStatus(Status.SUCCESSFUL);
        List<AuditMessage> expectedMetaData = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> actualMetaData = deleteServiceAgreementBatchDescriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(asList(batchResponseItem1, batchResponseItem2, batchResponseItem3),
                HttpStatus.MULTI_STATUS));

        assertEquals(expectedMetaData, actualMetaData);
    }

    @Test
    public void getSuccessEventDataListWithBothFailedAndSuccessfulEventsTest() {
        BatchResponseItem batchResponseItem1 = new BatchResponseItem()
            .resourceId("IdIdentifier")
            .status(StatusEnum.HTTP_STATUS_OK);
        List<String> errors = new ArrayList<>();
        errors.add("bad request");
        BatchResponseItem batchResponseItem2 = new BatchResponseItem()
            .resourceId("NameIdentifier")
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
            .errors(errors);

        PresentationServiceAgreementIdentifier item1 = new PresentationServiceAgreementIdentifier()
            .idIdentifier("IdIdentifier");
        PresentationServiceAgreementIdentifier item2 = new PresentationServiceAgreementIdentifier()
            .nameIdentifier("NameIdentifier");
        List<PresentationServiceAgreementIdentifier> serviceAgreementsIdentifiers = asList(item1, item2);

        ServiceAgreementBatchDelete presentationDeleteServiceAgreements = new ServiceAgreementBatchDelete()
            .serviceAgreementIdentifiers(serviceAgreementsIdentifiers);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationDeleteServiceAgreements).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum("Service Agreement ID", "IdIdentifier")
            .withEventDescription("Delete | Service Agreement | Successful | ID IdIdentifier")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription("Delete | Service Agreement | Failed | name NameIdentifier")
            .withEventMetaDatum("Service Agreement Name", "NameIdentifier")
            .withEventMetaDatum("Error code", "400")
            .withEventMetaDatum("Error message", "bad request")
            .withStatus(Status.FAILED);
        List<AuditMessage> expectedEventMetaData = asList(eventData1, eventData2);

        List<AuditMessage> actualEventMetaData = deleteServiceAgreementBatchDescriptor
            .getSuccessEventDataList(joinPoint,
                new ResponseEntity<>(asList(batchResponseItem1, batchResponseItem2), HttpStatus.MULTI_STATUS));

        assertEquals(expectedEventMetaData, actualEventMetaData);
    }

}