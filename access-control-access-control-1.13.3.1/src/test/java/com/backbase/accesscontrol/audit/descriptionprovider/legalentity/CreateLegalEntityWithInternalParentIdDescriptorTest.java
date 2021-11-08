package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityItemId;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityType;
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

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityWithInternalParentIdDescriptorTest {

    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String PARENT_LEGAL_ENTITY_ID = "Parent Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";
    private static final String LEGAL_ENTITY_ID = "Legal Entity ID";

    @InjectMocks
    private CreateLegalEntityWithInternalParentIdDescriptor createLegalEntityWithInternalParentIdDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataList() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setParentInternalId("5643e686d3ae4216b3ff5d66a6ad897d");
        legalEntitiesPostRequestBody.setName("LeName");
        legalEntitiesPostRequestBody.setType(LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setExternalId("ExternalId");
        legalEntitiesPostRequestBody.setActivateSingleServiceAgreement(true);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(getDescription(Status.INITIATED, legalEntitiesPostRequestBody.getName(),
                legalEntitiesPostRequestBody.getType(), legalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentInternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = createLegalEntityWithInternalParentIdDescriptor
            .getInitEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getSuccessEventDataList() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setParentInternalId("5643e686d3ae4216b3ff5d66a6ad897d");
        legalEntitiesPostRequestBody.setName("LeName");
        legalEntitiesPostRequestBody.setType(LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setExternalId("ExternalId");
        legalEntitiesPostRequestBody.setActivateSingleServiceAgreement(true);

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
            .withEventMetaDatum(PARENT_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentInternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = createLegalEntityWithInternalParentIdDescriptor
            .getSuccessEventDataList(joinPoint, getResponseEntity(legalEntitiesPostResponseBody, HttpStatus.OK));

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataList() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setParentInternalId("5643e686d3ae4216b3ff5d66a6ad897d");
        legalEntitiesPostRequestBody.setName("LeName");
        legalEntitiesPostRequestBody.setType(LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setExternalId("ExternalId");
        legalEntitiesPostRequestBody.setActivateSingleServiceAgreement(true);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getDescription(Status.FAILED, legalEntitiesPostRequestBody.getName(),
                legalEntitiesPostRequestBody.getType(), legalEntitiesPostRequestBody.getExternalId()))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentInternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntitiesPostRequestBody.getType().toString());

        List<AuditMessage> actualEventList = createLegalEntityWithInternalParentIdDescriptor
            .getFailedEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    private String getDescription(Status status, String legalEntityName, LegalEntityType legalEntityType,
        String externalId) {
        return "Create | Legal Entity | " + status + " | name " + legalEntityName + ", type " + legalEntityType
            + ", external ID " + externalId;
    }
}
