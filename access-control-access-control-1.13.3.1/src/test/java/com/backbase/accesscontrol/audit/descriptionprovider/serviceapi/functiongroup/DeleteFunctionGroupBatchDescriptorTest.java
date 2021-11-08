package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifierNameIdentifier;
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
public class DeleteFunctionGroupBatchDescriptorTest {

    @InjectMocks
    private DeleteFunctionGroupBatchDescriptor deleteFunctionGroupBatchDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;


    @Test
    public void shouldHaveSameSizeMessageIds() {
        PresentationIdentifier presentationIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);

        List<PresentationIdentifier> request = asList(presentationIdentifier, nameIdentifier);
        when(joinPoint.getArgs())
            .thenReturn(singletonList(request).toArray());
        List<String> messageIds = deleteFunctionGroupBatchDescriptor.getMessageIds(joinPoint);
        assertEquals(messageIds.size(), request.size());
    }

    @Test
    public void getInitEventMetaDataListTest() {
        PresentationIdentifier presentationIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);

        List<PresentationIdentifier> request = asList(presentationIdentifier, nameIdentifier);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(request).toArray());
        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Delete | Function Group | Initiated | ID 123456")
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "123456")
            .withStatus(Status.INITIATED);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(
                "Delete | Function Group | Initiated | name NameId, external service agreement ID ExSa")
            .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, "NameId")
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, "ExSa")
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedMessage = asList(eventData1, eventData2);

        List<AuditMessage> actualMessage = deleteFunctionGroupBatchDescriptor
            .getInitEventDataList(joinPoint);

        assertEquals(expectedMessage, actualMessage);
    }


    @Test
    public void getSuccessEventDataListTest() {
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody1 = new BatchResponseItemExtended()
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody2 = new BatchResponseItemExtended()
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
            .externalServiceAgreementId("externalServiceAgreementID")
            .errors(emptyList());

        List<BatchResponseItemExtended> response = asList(
            serviceAgreementIngestPostResponseBody1,
            serviceAgreementIngestPostResponseBody2
        );

        PresentationIdentifier presentationIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(presentationIdentifier, nameIdentifier)});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Delete | Function Group | Successful | ID 123456")
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "123456")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(
                "Delete | Function Group | Failed | name NameId, external service agreement ID ExSa")
            .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, "NameId")
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, "ExSa")
            .withEventMetaDatum(ERROR_CODE, "400")
            .withStatus(Status.FAILED);
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = deleteFunctionGroupBatchDescriptor
            .getSuccessEventDataList(joinPoint, getResponseEntity(response, HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }

}