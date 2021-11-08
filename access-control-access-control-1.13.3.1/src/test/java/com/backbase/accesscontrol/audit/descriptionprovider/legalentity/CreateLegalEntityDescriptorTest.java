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
 * Tests for {@link CreateLegalEntityDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityDescriptorTest {

    private static final String LEGAL_ENTITY_ID = "Legal Entity ID";
    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String PARENT_EXTERNAL_LEGAL_ENTITY_ID = "Parent External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";

    @InjectMocks
    private CreateLegalEntityDescriptor createLegalEntityDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataList() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setParentExternalId("ParentEx");
        legalEntitiesPostRequestBody.setName("LeName");
        legalEntitiesPostRequestBody.setType(com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setExternalId("ExternalId");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(getDescription(Status.INITIATED, legalEntitiesPostRequestBody.getName(),
                legalEntitiesPostRequestBody.getType(), legalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = createLegalEntityDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getSuccessEventDataList() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setParentExternalId("ParentEx");
        legalEntitiesPostRequestBody.setName("LeName");
        legalEntitiesPostRequestBody.setType(LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setExternalId("ExternalId");

        LegalEntityItemId legalEntitiesPostResponseBody = new LegalEntityItemId();
        legalEntitiesPostResponseBody.setId("id");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getDescription(Status.SUCCESSFUL, legalEntitiesPostRequestBody.getName(),
                legalEntitiesPostRequestBody.getType(), legalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(LEGAL_ENTITY_ID, legalEntitiesPostResponseBody.getId())
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = createLegalEntityDescriptor
            .getSuccessEventDataList(joinPoint, getResponseEntity(legalEntitiesPostResponseBody, HttpStatus.OK));

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataList() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setParentExternalId("ParentEx");
        legalEntitiesPostRequestBody.setName("LeName");
        legalEntitiesPostRequestBody.setType(LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setExternalId("ExternalId");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getDescription(Status.FAILED, legalEntitiesPostRequestBody.getName(),
                legalEntitiesPostRequestBody.getType(), legalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = createLegalEntityDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    private String getDescription(Status status, String legalEntityName, LegalEntityType legalEntityType,
        String externalId) {
        return "Create | Legal Entity | " + status + " | name " + legalEntityName + ", type " + legalEntityType
            + ", external ID " + externalId;
    }
}
