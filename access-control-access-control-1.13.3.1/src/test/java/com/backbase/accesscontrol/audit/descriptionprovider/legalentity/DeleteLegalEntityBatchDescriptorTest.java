package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntitiesBatchDelete;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class DeleteLegalEntityBatchDescriptorTest {

    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String ERROR_CODE = "Error code";
    private static final String ERROR_MESSAGE = "Error message";


    @Mock
    private ProceedingJoinPoint joinPoint;
    @InjectMocks
    private DeleteLegalEntityBatchDescriptor deleteLegalEntityBatchDescriptor;

    @Test
    public void getInitEventDataList() {
        List<String> ids = Arrays.asList("ex1", "ex2");
        LegalEntitiesBatchDelete legalEntityExternalIds = new LegalEntitiesBatchDelete();
        legalEntityExternalIds.setExternalIds(ids);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(legalEntityExternalIds).toArray());

        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, "ex2")
            .withStatus(Status.INITIATED)
            .withEventDescription(getDescription(Status.INITIATED, "ex2"));
        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, "ex1")
            .withStatus(Status.INITIATED)
            .withEventDescription(getDescription(Status.INITIATED, "ex1"));
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = deleteLegalEntityBatchDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getSuccessEventDataListTest() {
        BatchResponseItem batchResponseItem1 = new BatchResponseItem();
        batchResponseItem1.setResourceId("ex1");
        batchResponseItem1.setStatus(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItem batchResponseItem2 = new BatchResponseItem();
        batchResponseItem2.setResourceId("ex2");
        batchResponseItem2.setStatus(StatusEnum.HTTP_STATUS_OK);

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, "ex1")
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getDescription(Status.SUCCESSFUL, "ex1"));
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, "ex2")
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getDescription(Status.SUCCESSFUL, "ex2"));
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = deleteLegalEntityBatchDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(asList(batchResponseItem1, batchResponseItem2), HttpStatus.OK));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getSuccessEventDataListWithFailedAndSuccessfulEventsTest() {
        BatchResponseItem batchResponseItem1 = new BatchResponseItem();
        batchResponseItem1.setResourceId("ex1");
        batchResponseItem1.setStatus(StatusEnum.HTTP_STATUS_OK);
        List<String> errors = new ArrayList<>();
        errors.add("bad request");
        errors.add("something went wrong");
        BatchResponseItem batchResponseItem2 = new BatchResponseItem();
        batchResponseItem2.setResourceId("ex2");
        batchResponseItem2.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        batchResponseItem2.setErrors(errors);

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, "ex1")
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getDescription(Status.SUCCESSFUL, "ex1"));
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, "ex2")
            .withStatus(Status.FAILED)
            .withEventDescription(getDescription(Status.FAILED, "ex2"))
            .withEventMetaDatum(ERROR_CODE, batchResponseItem2.getStatus().toString())
            .withEventMetaDatum(ERROR_MESSAGE, batchResponseItem2.getErrors().get(0));
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = deleteLegalEntityBatchDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(asList(batchResponseItem1, batchResponseItem2), HttpStatus.OK));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldGetMessageIdsEqualToNumberOfElementsInBatch() {
        List<String> ids = Arrays.asList("ex1", "ex2");
        LegalEntitiesBatchDelete legalEntityExternalIds = new LegalEntitiesBatchDelete();
        legalEntityExternalIds.setExternalIds(ids);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(legalEntityExternalIds).toArray());

        List<String> messageIds = deleteLegalEntityBatchDescriptor.getMessageIds(joinPoint);

        assertEquals(ids.size(), messageIds.size());
    }

    private String getDescription(Status status, String externalId) {
        return "Delete | Legal Entity | " + status + " | external ID " + externalId;
    }
}
