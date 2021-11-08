package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityUpdateItem;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link UpdateLegalEntityDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateLegalEntityDescriptorTest {

    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";

    @InjectMocks
    private UpdateLegalEntityDescriptor updateLegalEntityDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataList() {
        LegalEntityUpdateItem legalEntityByExternalIdPutRequestBody = new LegalEntityUpdateItem();
        legalEntityByExternalIdPutRequestBody.setType(LegalEntityType.BANK);

        String externalId = "externalId";
        when(joinPoint.getArgs())
            .thenReturn(asList(legalEntityByExternalIdPutRequestBody, externalId).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                getDescription(Status.INITIATED, legalEntityByExternalIdPutRequestBody.getType(), externalId))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId)
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntityByExternalIdPutRequestBody.getType().toString());

        List<AuditMessage> actualEventList = updateLegalEntityDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getSuccessEventDataList() {
        LegalEntityUpdateItem legalEntityByExternalIdPutRequestBody = new LegalEntityUpdateItem();
        legalEntityByExternalIdPutRequestBody.setType(LegalEntityType.BANK);

        String externalId = "externalId";
        when(joinPoint.getArgs())
            .thenReturn(asList(legalEntityByExternalIdPutRequestBody, externalId).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                getDescription(Status.SUCCESSFUL, legalEntityByExternalIdPutRequestBody.getType(), externalId))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId)
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntityByExternalIdPutRequestBody.getType().toString());

        List<AuditMessage> actualEventList = updateLegalEntityDescriptor.getSuccessEventDataList(joinPoint, null);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataList() {

        LegalEntityUpdateItem legalEntityByExternalIdPutRequestBody = new LegalEntityUpdateItem();
        legalEntityByExternalIdPutRequestBody.setType(LegalEntityType.BANK);

        String externalId = "externalId";
        when(joinPoint.getArgs())
            .thenReturn(asList(legalEntityByExternalIdPutRequestBody, externalId).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(
                getDescription(Status.FAILED, legalEntityByExternalIdPutRequestBody.getType(), externalId))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId)
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntityByExternalIdPutRequestBody.getType().toString());

        List<AuditMessage> actualEventList = updateLegalEntityDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    private String getDescription(Status status, LegalEntityType legalEntityType, String externalId) {
        return "Update | Legal Entity | " + status + " | type " + legalEntityType + ", external ID " + externalId;
    }
}
