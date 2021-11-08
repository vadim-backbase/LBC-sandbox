package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType;
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

/**
 * Tests for {@link UpdateLegalEntityBatchDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateLegalEntityBatchDescriptorTest {

    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String PARENT_EXTERNAL_LEGAL_ENTITY_ID = "Parent External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";
    private static final String ERROR_CODE = "Error code";
    private static final String ERROR_MESSAGE = "Error message";

    @InjectMocks
    private UpdateLegalEntityBatchDescriptor updateLegalEntityBatchDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataListTest() {
        LegalEntityCreateItem legalEntity1 = new LegalEntityCreateItem();
        legalEntity1.setParentExternalId("p1");
        legalEntity1.setExternalId("ex1");
        legalEntity1.setName("someName1");
        legalEntity1.setType(LegalEntityType.BANK);
        LegalEntityCreateItem legalEntity2 = new LegalEntityCreateItem();
        legalEntity2.setParentExternalId("p2");
        legalEntity2.setExternalId("ex2");
        legalEntity2.setName("someName2");
        legalEntity2.setType(LegalEntityType.CUSTOMER);
        LegalEntityPut body1 = new LegalEntityPut();
        body1.setExternalId("ex1");
        body1.setLegalEntity(legalEntity1);
        LegalEntityPut body2 = new LegalEntityPut();
        body2.setExternalId("ex2");
        body2.setLegalEntity(legalEntity2);

        when(joinPoint.getArgs()).thenReturn(singletonList(asList(body1, body2)).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, body1.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body1.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body1.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body1.getLegalEntity().getType().toString())
            .withStatus(Status.INITIATED)
            .withEventDescription(
                getDescription(Status.INITIATED, body1.getLegalEntity().getName(), body1.getLegalEntity().getType(),
                    body1.getLegalEntity().getExternalId()));
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, body2.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body2.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body2.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body2.getLegalEntity().getType().toString())
            .withStatus(Status.INITIATED)
            .withEventDescription(
                getDescription(Status.INITIATED, body2.getLegalEntity().getName(), body2.getLegalEntity().getType(),
                    body2.getLegalEntity().getExternalId()));
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2);

        List<AuditMessage> auditMessages = updateLegalEntityBatchDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getSuccessEventDataListTest() {
        BatchResponseItem batchResponseItem1 = new BatchResponseItem();
        batchResponseItem1.setResourceId("ex1");
        batchResponseItem1.setStatus(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItem batchResponseItem2 = new BatchResponseItem();
        batchResponseItem2.setResourceId("ex2");
        batchResponseItem2.setStatus(StatusEnum.HTTP_STATUS_OK);

        LegalEntityCreateItem legalEntity1 = new LegalEntityCreateItem();
        legalEntity1.setParentExternalId("p1");
        legalEntity1.setExternalId("ex1");
        legalEntity1.setName("someName1");
        legalEntity1.setType(LegalEntityType.BANK);
        LegalEntityCreateItem legalEntity2 = new LegalEntityCreateItem();
        legalEntity2.setParentExternalId("p2");
        legalEntity2.setExternalId("ex2");
        legalEntity2.setName("someName2");
        legalEntity2.setType(LegalEntityType.CUSTOMER);
        LegalEntityPut body1 = new LegalEntityPut();
        body1.setExternalId("ex1");
        body1.setLegalEntity(legalEntity1);
        LegalEntityPut body2 = new LegalEntityPut();
        body2.setExternalId("ex2");
        body2.setLegalEntity(legalEntity2);

        when(joinPoint.getArgs()).thenReturn(singletonList(asList(body1, body2)).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, body1.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body1.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body1.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body1.getLegalEntity().getType().toString())
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                getDescription(Status.SUCCESSFUL, body1.getLegalEntity().getName(), body1.getLegalEntity().getType(),
                    body1.getLegalEntity().getExternalId()));
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, body2.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body2.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body2.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body2.getLegalEntity().getType().toString())
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                getDescription(Status.SUCCESSFUL, body2.getLegalEntity().getName(), body2.getLegalEntity().getType(),
                    body2.getLegalEntity().getExternalId()));
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2);

        List<AuditMessage> auditMessages = updateLegalEntityBatchDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(asList(batchResponseItem1, batchResponseItem2), HttpStatus.OK));

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getSuccessEventDataListWithBothFailedAndSuccessfulEventsTest() {
        BatchResponseItem batchResponseItem1 = new BatchResponseItem();
        batchResponseItem1.setResourceId("ex1");
        batchResponseItem1.setStatus(StatusEnum.HTTP_STATUS_OK);
        List<String> errors = new ArrayList<>();
        errors.add("bad request");
        BatchResponseItem batchResponseItem2 = new BatchResponseItem();
        batchResponseItem2.setResourceId("ex2");
        batchResponseItem2.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        batchResponseItem2.setErrors(errors);

        LegalEntityCreateItem legalEntity1 = new LegalEntityCreateItem();
        legalEntity1.setParentExternalId("p1");
        legalEntity1.setExternalId("ex1");
        legalEntity1.setName("someName1");
        legalEntity1.setType(LegalEntityType.BANK);
        LegalEntityCreateItem legalEntity2 = new LegalEntityCreateItem();
        legalEntity2.setParentExternalId("p2");
        legalEntity2.setExternalId("ex2");
        legalEntity2.setName("someName2");
        legalEntity2.setType(LegalEntityType.CUSTOMER);
        LegalEntityPut body1 = new LegalEntityPut();
        body1.setExternalId("ex1");
        body1.setLegalEntity(legalEntity1);
        LegalEntityPut body2 = new LegalEntityPut();
        body2.setExternalId("ex2");
        body2.setLegalEntity(legalEntity2);

        when(joinPoint.getArgs()).thenReturn(singletonList(asList(body1, body2)).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, body1.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body1.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body1.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body1.getLegalEntity().getType().toString())
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                getDescription(Status.SUCCESSFUL, body1.getLegalEntity().getName(), body1.getLegalEntity().getType(),
                    body1.getLegalEntity().getExternalId()));
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, body2.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body2.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body2.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body2.getLegalEntity().getType().toString())
            .withEventMetaDatum(ERROR_CODE, batchResponseItem2.getStatus().toString())
            .withEventMetaDatum(ERROR_MESSAGE, batchResponseItem2.getErrors().get(0))
            .withStatus(Status.FAILED)
            .withEventDescription(
                getDescription(Status.FAILED, body2.getLegalEntity().getName(), body2.getLegalEntity().getType(),
                    body2.getLegalEntity().getExternalId()));
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2);

        List<AuditMessage> auditMessages = updateLegalEntityBatchDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(asList(batchResponseItem1, batchResponseItem2), HttpStatus.OK));

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getSuccessEventDataListWithBothFailedAndSuccessfulAndOverwrittenEventsTest() {
        BatchResponseItem batchResponseItem1 = new BatchResponseItem();
        batchResponseItem1.setResourceId("Le1");
        batchResponseItem1.setStatus(StatusEnum.HTTP_STATUS_OK);

        BatchResponseItem batchResponseItem2 = new BatchResponseItem();
        batchResponseItem2.setResourceId("Le2");
        batchResponseItem2.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        BatchResponseItem batchResponseItem3 = new BatchResponseItem();
        batchResponseItem3.setResourceId("Le1");
        batchResponseItem3.setStatus(StatusEnum.HTTP_STATUS_OK);

        LegalEntityCreateItem legalEntity1 = new LegalEntityCreateItem();
        legalEntity1.setParentExternalId("BANK0001");
        legalEntity1.setExternalId("HxyBBAjQ");
        legalEntity1.setName("sInuWldY");
        legalEntity1.setType(LegalEntityType.BANK);
        LegalEntityCreateItem legalEntity2 = new LegalEntityCreateItem();
        legalEntity2.setParentExternalId("invalidLeParent");
        legalEntity2.setExternalId("CPUUJJcW");
        legalEntity2.setName("uhWfJPwD");
        legalEntity2.setType(LegalEntityType.CUSTOMER);
        LegalEntityCreateItem legalEntityOverwritten = new LegalEntityCreateItem();
        legalEntityOverwritten.setParentExternalId("BANK0001");
        legalEntityOverwritten.setExternalId("cKdpGbsk");
        legalEntityOverwritten.setName("YHMzTsWh");
        legalEntityOverwritten.setType(LegalEntityType.BANK);

        LegalEntityPut body1 = new LegalEntityPut();
        body1.setExternalId("HxyBBAjQ");
        body1.setLegalEntity(legalEntity1);
        LegalEntityPut body2 = new LegalEntityPut();
        body2.setExternalId("oAlsUekZ");
        body2.setLegalEntity(legalEntity2);
        LegalEntityPut body3 = new LegalEntityPut();
        body3.setExternalId("HxyBBAjQ");
        body3.setLegalEntity(legalEntityOverwritten);

        when(joinPoint.getArgs()).thenReturn(singletonList(asList(body1, body2, body3)).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntity1.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body1.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body1.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body1.getLegalEntity().getType().toString())
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                getDescription(Status.SUCCESSFUL, body1.getLegalEntity().getName(), body1.getLegalEntity().getType(),
                    body1.getLegalEntity().getExternalId()));
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntity2.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body2.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body2.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body2.getLegalEntity().getType().toString())
            .withEventMetaDatum(ERROR_CODE, batchResponseItem2.getStatus().toString())
            .withEventMetaDatum(ERROR_MESSAGE, "")
            .withStatus(Status.FAILED)
            .withEventDescription(
                getDescription(Status.FAILED, body2.getLegalEntity().getName(), body2.getLegalEntity().getType(),
                    body2.getLegalEntity().getExternalId()));
        AuditMessage eventData3 = new AuditMessage()
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntityOverwritten.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, body3.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, body3.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, body3.getLegalEntity().getType().toString())
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                getDescription(Status.SUCCESSFUL, body3.getLegalEntity().getName(), body3.getLegalEntity().getType(),
                    body3.getLegalEntity().getExternalId()));
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> auditMessages = updateLegalEntityBatchDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(asList(batchResponseItem1, batchResponseItem2, batchResponseItem3), HttpStatus.OK));

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void shouldGetMessageIdsEqualToNumberOfElementsInBatch() {
        LegalEntityCreateItem legalEntity1 = new LegalEntityCreateItem();
        legalEntity1.setParentExternalId("BANK0001");
        legalEntity1.setExternalId("HxyBBAjQ");
        legalEntity1.setName("sInuWldY");
        legalEntity1.setType(LegalEntityType.BANK);
        LegalEntityCreateItem legalEntity2 = new LegalEntityCreateItem();
        legalEntity2.setParentExternalId("invalidLeParent");
        legalEntity2.setExternalId("CPUUJJcW");
        legalEntity2.setName("uhWfJPwD");
        legalEntity2.setType(LegalEntityType.CUSTOMER);
        LegalEntityCreateItem legalEntityOverwritten = new LegalEntityCreateItem();
        legalEntityOverwritten.setParentExternalId("BANK0001");
        legalEntityOverwritten.setExternalId("cKdpGbsk");
        legalEntityOverwritten.setName("YHMzTsWh");
        legalEntityOverwritten.setType(LegalEntityType.BANK);

        LegalEntityPut body1 = new LegalEntityPut();
        body1.setExternalId("HxyBBAjQ");
        body1.setLegalEntity(legalEntity1);
        LegalEntityPut body2 = new LegalEntityPut();
        body2.setExternalId("oAlsUekZ");
        body2.setLegalEntity(legalEntity2);
        LegalEntityPut body3 = new LegalEntityPut();
        body3.setExternalId("HxyBBAjQ");
        body3.setLegalEntity(legalEntityOverwritten);

        List<LegalEntityPut> legalEntityPuts = asList(body1, body2, body3);

        when(joinPoint.getArgs()).thenReturn(singletonList(legalEntityPuts).toArray());

        List<String> messageIds = updateLegalEntityBatchDescriptor.getMessageIds(joinPoint);

        assertEquals(legalEntityPuts.size(), messageIds.size());
    }

    private String getDescription(Status status, String legalEntityName, LegalEntityType legalEntityType,
        String externalId) {
        return "Update | Legal Entity | " + status + " | name " + legalEntityName + ", type " + legalEntityType
            + ", external ID " + externalId;
    }
}
