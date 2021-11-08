package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.BUSINESS_FUNCTION_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PRIVILEGES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.Functiongroupupdate;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifierNameIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionFunctionGroupUpdate;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFunctionGroupBatchDescriptorTest {

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @InjectMocks
    private UpdateFunctionGroupBatchDescriptor updateFunctionGroupBatchDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataListTest() {
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody1 = new BatchResponseItemExtended()
            .resourceId("DGId1")
            .status(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody2 = new BatchResponseItemExtended()
            .resourceId("DGId2")
            .externalServiceAgreementId("ex")
            .status(StatusEnum.HTTP_STATUS_OK);

        List<BatchResponseItemExtended> response = asList(
            serviceAgreementIngestPostResponseBody1,
            serviceAgreementIngestPostResponseBody2
        );

        List<PresentationPermissionFunctionGroupUpdate> permissions = new ArrayList<>();
        List<String> privileges = new ArrayList<>();
        privileges.add("View");
        privileges.add("Edit");
        privileges.add("Create");
        PresentationPermissionFunctionGroupUpdate p = new PresentationPermissionFunctionGroupUpdate()
            .privileges(privileges)
            .functionName("FunctionName");
        permissions.add(p);
        Functiongroupupdate fg = new Functiongroupupdate()
            .name("FGName")
            .description("desc")
            .permissions(permissions)
            .validFromDate("01-08-2019")
            .validFromTime("08:00")
            .validUntilDate("30-08-2019")
            .validUntilTime("10:00");

        PresentationIdentifier presentationIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);
        PresentationFunctionGroupPutRequestBody body1 = new PresentationFunctionGroupPutRequestBody()
            .functionGroup(fg)
            .identifier(presentationIdentifier);
        PresentationFunctionGroupPutRequestBody body2 = new PresentationFunctionGroupPutRequestBody()
            .functionGroup(fg)
            .identifier(nameIdentifier);

        List<PresentationFunctionGroupPutRequestBody> request = asList(body1, body2);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Function Group | Successful | ID 123456")
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "123456")
            .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME, "FGName")
            .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, "desc")
            .withEventMetaDatum(BUSINESS_FUNCTION_NAME_FIELD_NAME, "FunctionName")
            .withEventMetaDatum(PRIVILEGES_FIELD_NAME, "View,Edit,Create")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, "01-08-2019 08:00")
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, "30-08-2019 10:00");
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(
                "Update | Function Group | Successful | name NameId, external service agreement ID ExSa")
            .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, "NameId")
            .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, "desc")
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, "ExSa")
            .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME, "FGName")
            .withEventMetaDatum(BUSINESS_FUNCTION_NAME_FIELD_NAME, "FunctionName")
            .withEventMetaDatum(PRIVILEGES_FIELD_NAME, "View,Edit,Create")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, "01-08-2019 08:00")
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, "30-08-2019 10:00");
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = updateFunctionGroupBatchDescriptor
            .getSuccessEventDataList(joinPoint, new ResponseEntity<>(response, HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getSuccessEventDataListWithBothFailedAndSuccessfulEventsTest() {
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody1 = new BatchResponseItemExtended()
            .resourceId("DGId1")
            .status(StatusEnum.HTTP_STATUS_OK);
        List<String> errors = new ArrayList<>();
        errors.add("bad request");
        errors.add("something went wrong");
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody2 = new BatchResponseItemExtended()
            .resourceId("DGId2")
            .externalServiceAgreementId("ex")
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
            .errors(errors);

        List<BatchResponseItemExtended> response = asList(
            serviceAgreementIngestPostResponseBody1,
            serviceAgreementIngestPostResponseBody2
        );

        List<PresentationPermissionFunctionGroupUpdate> permissions = new ArrayList<>();
        List<String> privileges = new ArrayList<>();
        privileges.add("View");
        privileges.add("Edit");
        privileges.add("Create");
        PresentationPermissionFunctionGroupUpdate p = new PresentationPermissionFunctionGroupUpdate()
            .privileges(privileges)
            .functionName("FunctionName");
        permissions.add(p);
        Functiongroupupdate fg = new Functiongroupupdate()
            .name("FGName")
            .description("desc")
            .permissions(permissions);
        PresentationIdentifier presentationIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);
        PresentationFunctionGroupPutRequestBody body1 = new PresentationFunctionGroupPutRequestBody()
            .functionGroup(fg)
            .identifier(presentationIdentifier);
        PresentationFunctionGroupPutRequestBody body2 = new PresentationFunctionGroupPutRequestBody()
            .functionGroup(fg)
            .identifier(nameIdentifier);

        List<PresentationFunctionGroupPutRequestBody> request = asList(body1, body2);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Function Group | Successful | ID 123456")
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "123456")
            .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME, "FGName")
            .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, "desc")
            .withEventMetaDatum(BUSINESS_FUNCTION_NAME_FIELD_NAME, "FunctionName")
            .withEventMetaDatum(PRIVILEGES_FIELD_NAME, "View,Edit,Create")
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, "")
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, "")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription("Update | Function Group | Failed | name NameId, external service agreement ID ExSa")
            .withStatus(Status.FAILED)
            .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, "NameId")
            .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME, "FGName")
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, "ExSa")
            .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, "desc")
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, "")
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, "")
            .withEventMetaDatum(ERROR_MESSAGE, "bad request")
            .withEventMetaDatum(ERROR_CODE, "400");
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = updateFunctionGroupBatchDescriptor
            .getSuccessEventDataList(joinPoint, new ResponseEntity<>(response, HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getInitEventDataList() {
        List<PresentationPermissionFunctionGroupUpdate> permissions = new ArrayList<>();
        List<String> privileges = new ArrayList<>();
        privileges.add("View");
        privileges.add("Edit");
        privileges.add("Create");
        PresentationPermissionFunctionGroupUpdate p = new PresentationPermissionFunctionGroupUpdate()
            .privileges(privileges)
            .functionName("FunctionName");
        permissions.add(p);
        Functiongroupupdate fg = new Functiongroupupdate()
            .name("FGName")
            .description("desc")
            .permissions(permissions);
        PresentationIdentifier presentationIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);
        PresentationFunctionGroupPutRequestBody body1 = new PresentationFunctionGroupPutRequestBody()
            .functionGroup(fg)
            .identifier(presentationIdentifier);
        PresentationFunctionGroupPutRequestBody body2 = new PresentationFunctionGroupPutRequestBody()
            .functionGroup(fg)
            .identifier(nameIdentifier);

        List<PresentationFunctionGroupPutRequestBody> request = asList(body1, body2);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Function Group | Initiated | ID 123456")
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "123456")
            .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME, "FGName")
            .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, "desc")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum(BUSINESS_FUNCTION_NAME_FIELD_NAME, "FunctionName")
            .withEventMetaDatum(PRIVILEGES_FIELD_NAME, "View,Edit,Create")
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, "")
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, "");
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(
                "Update | Function Group | Initiated | name NameId, external service agreement ID ExSa")
            .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, "NameId")
            .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, "desc")
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, "ExSa")
            .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME, "FGName")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum(BUSINESS_FUNCTION_NAME_FIELD_NAME, "FunctionName")
            .withEventMetaDatum(PRIVILEGES_FIELD_NAME, "View,Edit,Create")
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, "")
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, "");
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = updateFunctionGroupBatchDescriptor
            .getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }


}
