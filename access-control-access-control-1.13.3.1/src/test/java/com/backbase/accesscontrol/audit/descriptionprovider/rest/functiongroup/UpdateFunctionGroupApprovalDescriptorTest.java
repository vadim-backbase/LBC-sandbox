package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.BUSINESS_FUNCTION_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PRIVILEGES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.matchers.MatcherUtil.getAuditMessageMatcherWithDescription;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFunctionGroupApprovalDescriptorTest {

    @InjectMocks
    private UpdateFunctionGroupApprovalDescriptor updateFunctionGroupApprovalDescriptor;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getMessageIdsTest() {
        String fgId = "id";
        String approvalId = "approvalId";

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId("serviceAgreementId")
            .withDescription("DE-01")
            .withName("DGName");

        when(joinPoint.getArgs())
            .thenReturn(asList(functionGroupByIdPutRequestBody, fgId, approvalId).toArray());

        List<String> messageIds = updateFunctionGroupApprovalDescriptor
            .getMessageIds(joinPoint);

        assertEquals(singletonList(approvalId), messageIds);
    }

    @Test
    public void getInitEventDataList() {
        String serviceAgreementId = "service_agreement";
        String description = "description";
        String fgName = "Name";
        String approvalId = "approvalId";
        String fgId = "fgId";

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate("01-08-2019")
            .withValidFromTime("08:00")
            .withValidUntilDate("30-08-2019");

        when(joinPoint.getArgs())
            .thenReturn(asList(functionGroupByIdPutRequestBody, fgId, approvalId).toArray());

        AuditMessage auditMessage1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                "Request Update | Function Group | Initiated | ID " + fgId)
            .withEventMetaDatum("Function Group Name", functionGroupByIdPutRequestBody.getName())
            .withEventMetaDatum("Function Group Description", functionGroupByIdPutRequestBody.getDescription())
            .withEventMetaDatum("Service Agreement ID", functionGroupByIdPutRequestBody.getServiceAgreementId())
            .withEventMetaDatum("Start DateTime",
                functionGroupByIdPutRequestBody.getValidFromDate() + " " + functionGroupByIdPutRequestBody
                    .getValidFromTime())
            .withEventMetaDatum("End DateTime",
                functionGroupByIdPutRequestBody.getValidUntilDate());
        List<AuditMessage> actualEventList = updateFunctionGroupApprovalDescriptor
            .getInitEventDataList(joinPoint);
        assertEquals(auditMessage1, actualEventList.get(0));
    }

    @Test
    public void getSuccessEventDataList() {
        String id = "FG-01";
        String approvalId = "approvalId";
        String url = "/v2/accessgroups/function-groups/" + id;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("id", id);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest("putFunctionGroupById", url);
        httpServletRequest.setAttribute("org.springframework.web.servlet.View.pathVariables", attributes);

        String serviceAgreementId = "SA-01";
        String description = "description";
        String fgName = "Name";
        String function1 = "function1";
        String function2 = "function2";
        String validFromDate = "01-08-2019";
        String validFromTime = "08:00";
        String validUntilDate = "30-08-2019";
        String validUntilTime = "10:00";

        Privilege view = new Privilege()
            .withPrivilege("view");
        Privilege edit = new Privilege()
            .withPrivilege("edit");
        Privilege create = new Privilege()
            .withPrivilege("create");

        List<Permission> permissions = Lists.newArrayList(
            new Permission()
                .withFunctionId(function1)
                .withAssignedPrivileges(Lists.newArrayList(view, edit)),
            new Permission()
                .withFunctionId(function2)
                .withAssignedPrivileges(Lists.newArrayList(view, create))
        );

        FunctionGroupByIdPutRequestBody functionGroupBase = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate(validFromDate)
            .withValidFromTime(validFromTime)
            .withValidUntilDate(validUntilDate)
            .withValidUntilTime(validUntilTime)
            .withPermissions(permissions);

        when(joinPoint.getArgs())
            .thenReturn(Lists.newArrayList(functionGroupBase, id, approvalId).toArray());

        List<AuditMessage> actualEventList = updateFunctionGroupApprovalDescriptor
            .getSuccessEventDataList(joinPoint, null);

        assertThat(actualEventList, hasItems(
            getAuditMessageMatcherWithDescription(is(Status.SUCCESSFUL),
                is("Request Update | Function Group | Successful | ID " + id), allOf(
                    hasEntry(FUNCTION_GROUP_ID_FIELD_NAME, id),
                    hasEntry(FUNCTION_GROUP_NAME_FIELD_NAME, fgName),
                    hasEntry(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, description),
                    hasEntry(SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId),
                    hasEntry(BUSINESS_FUNCTION_ID_FIELD_NAME, function1),
                    hasEntry(PRIVILEGES_FIELD_NAME, "view,edit"),
                    hasEntry(START_DATE_TIME_FIELD_NAME,
                        functionGroupBase.getValidFromDate() + " " + functionGroupBase.getValidFromTime()),
                    hasEntry(END_DATE_TIME_FIELD_NAME,
                        functionGroupBase.getValidUntilDate() + " " + functionGroupBase.getValidUntilTime())
                )),
            getAuditMessageMatcherWithDescription(is(Status.SUCCESSFUL),
                is("Request Update | Function Group | Successful | ID " + id), allOf(
                    hasEntry(FUNCTION_GROUP_ID_FIELD_NAME, id),
                    hasEntry(FUNCTION_GROUP_NAME_FIELD_NAME, fgName),
                    hasEntry(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, description),
                    hasEntry(SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId),
                    hasEntry(BUSINESS_FUNCTION_ID_FIELD_NAME, function2),
                    hasEntry(PRIVILEGES_FIELD_NAME, "view,create"),
                    hasEntry(START_DATE_TIME_FIELD_NAME,
                        functionGroupBase.getValidFromDate() + " " + functionGroupBase.getValidFromTime()),
                    hasEntry(END_DATE_TIME_FIELD_NAME,
                        functionGroupBase.getValidUntilDate() + " " + functionGroupBase.getValidUntilTime())
                ))
        ));
    }

    @Test
    public void getFailedEventDataList() {
        String serviceAgreementId = "service_agreement";
        String description = "description";
        String fgName = "Name";
        String approvalId = "approvalId";
        String fgId = "fgId";

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate("01-08-2019")
            .withValidFromTime("08:00")
            .withValidUntilDate("30-08-2019");

        when(joinPoint.getArgs())
            .thenReturn(asList(functionGroupByIdPutRequestBody, fgId, approvalId).toArray());

        AuditMessage auditMessage1 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(
                "Request Update | Function Group | Failed | ID fgId")
            .withEventMetaDatum("Function Group Name", functionGroupByIdPutRequestBody.getName())
            .withEventMetaDatum("Function Group Description", functionGroupByIdPutRequestBody.getDescription())
            .withEventMetaDatum("Service Agreement ID", functionGroupByIdPutRequestBody.getServiceAgreementId())
            .withEventMetaDatum("Start DateTime",
                functionGroupByIdPutRequestBody.getValidFromDate() + " " + functionGroupByIdPutRequestBody
                    .getValidFromTime())
            .withEventMetaDatum("End DateTime",
                functionGroupByIdPutRequestBody.getValidUntilDate());
        List<AuditMessage> actualEventList = updateFunctionGroupApprovalDescriptor
            .getFailedEventDataList(joinPoint);
        assertEquals(auditMessage1, actualEventList.get(0));
    }
}
