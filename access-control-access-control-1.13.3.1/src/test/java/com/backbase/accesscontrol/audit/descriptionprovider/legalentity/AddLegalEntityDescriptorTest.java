package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemId;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

/**
 * Tests for {@link AddLegalEntityDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class AddLegalEntityDescriptorTest {

    private static final String LEGAL_ENTITY_ID = "Legal Entity ID";
    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String PARENT_EXTERNAL_LEGAL_ENTITY_ID = "Parent External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";

    @InjectMocks
    private AddLegalEntityDescriptor addLegalEntityDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataList() {
        LegalEntityCreateItem createLegalEntitiesPostRequestBody = new LegalEntityCreateItem();
        createLegalEntitiesPostRequestBody.setParentExternalId("ParentEx");
        createLegalEntitiesPostRequestBody.setName("LeName");
        createLegalEntitiesPostRequestBody.setType(
            com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType.BANK);
        createLegalEntitiesPostRequestBody.setExternalId("ExternalId");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(createLegalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(getDescription(Status.INITIATED, createLegalEntitiesPostRequestBody.getName(),
                createLegalEntitiesPostRequestBody.getType(), createLegalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, createLegalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, createLegalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID,
                createLegalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, createLegalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = addLegalEntityDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getSuccessEventDataList() {
        LegalEntityCreateItem createLegalEntitiesPostRequestBody = new LegalEntityCreateItem();
        createLegalEntitiesPostRequestBody.setParentExternalId("ParentEx");
        createLegalEntitiesPostRequestBody.setName("LeName");
        createLegalEntitiesPostRequestBody.setType(LegalEntityType.BANK);
        createLegalEntitiesPostRequestBody.setExternalId("ExternalId");

        LegalEntityItemId createLegalEntitiesPostResponseBody = new LegalEntityItemId();
        createLegalEntitiesPostResponseBody.setId("id");

        when(joinPoint.getArgs())
            .thenReturn(
                singletonList(createLegalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getDescription(Status.SUCCESSFUL, createLegalEntitiesPostRequestBody.getName(),
                createLegalEntitiesPostRequestBody.getType(), createLegalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(LEGAL_ENTITY_ID, createLegalEntitiesPostResponseBody.getId())
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, createLegalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, createLegalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID,
                createLegalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, createLegalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = addLegalEntityDescriptor
            .getSuccessEventDataList(joinPoint, getResponseEntity(createLegalEntitiesPostResponseBody, HttpStatus.OK));

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataList() {
        LegalEntityCreateItem createLegalEntitiesPostRequestBody = new LegalEntityCreateItem();
        createLegalEntitiesPostRequestBody.setParentExternalId("ParentEx");
        createLegalEntitiesPostRequestBody.setName("LeName");
        createLegalEntitiesPostRequestBody.setType(LegalEntityType.BANK);
        createLegalEntitiesPostRequestBody.setExternalId("ExternalId");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(createLegalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getDescription(Status.FAILED, createLegalEntitiesPostRequestBody.getName(),
                createLegalEntitiesPostRequestBody.getType(), createLegalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, createLegalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, createLegalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID,
                createLegalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, createLegalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = addLegalEntityDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    private String getDescription(Status status, String legalEntityName, LegalEntityType legalEntityType,
        String externalId) {
        return "Create | Legal Entity | " + status + " | name " + legalEntityName + ", type " + legalEntityType
            + ", external ID " + externalId;
    }
}
