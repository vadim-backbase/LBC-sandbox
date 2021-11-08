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
import java.util.ArrayList;
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

/**
 * Tests for {@link UpdateFunctionGroupDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateFunctionGroupDescriptorTest {

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @InjectMocks
    private UpdateFunctionGroupDescriptor updateFunctionGroupDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataList() {
        String id = "FG-01";
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
            .thenReturn(Lists.newArrayList(functionGroupBase, id).toArray());

        List<AuditMessage> actualEventList = updateFunctionGroupDescriptor
            .getSuccessEventDataList(joinPoint, null);

        assertThat(actualEventList, hasItems(
            getAuditMessageMatcherWithDescription(is(Status.SUCCESSFUL),
                is("Update | Function Group | Successful | ID FG-01"), allOf(
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
                is("Update | Function Group | Successful | ID FG-01"), allOf(
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
    public void getSuccessEventDataListWhenPermissionListIsNull() {
        String id = "FG-01";
        String url = "/v2/accessgroups/function-groups/" + id;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("id", id);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest("putFunctionGroupById", url);
        httpServletRequest.setAttribute("org.springframework.web.servlet.View.pathVariables", attributes);

        String serviceAgreementId = "SA-01";
        String description = "description";
        String fgName = "Name";
        String validFromDate = "01-08-2019";
        String validUntilDate = "30-08-2019";

        FunctionGroupByIdPutRequestBody functionGroupBase = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate(validFromDate)
            .withValidUntilDate(validUntilDate);

        when(joinPoint.getArgs())
            .thenReturn(Lists.newArrayList(functionGroupBase, id).toArray());

        List<AuditMessage> actualEventList = updateFunctionGroupDescriptor
            .getSuccessEventDataList(joinPoint, null);

        List<AuditMessage> expectedEventList = new ArrayList<>();
        AuditMessage auditMessage = new AuditMessage();
        auditMessage.withStatus(Status.SUCCESSFUL);
        auditMessage.withEventDescription("Update | Function Group | Successful | ID FG-01");
        auditMessage.withEventMetaDatum("Function Group ID", id);
        auditMessage.withEventMetaDatum("Function Group Name", fgName);
        auditMessage.withEventMetaDatum("Function Group Description", description);
        auditMessage.withEventMetaDatum("Service Agreement ID", serviceAgreementId);
        auditMessage.withEventMetaDatum("Start DateTime", functionGroupBase.getValidFromDate());
        auditMessage.withEventMetaDatum("End DateTime", functionGroupBase.getValidUntilDate());
        expectedEventList.add(auditMessage);
        assertEquals(expectedEventList, actualEventList);
    }

    @Test
    public void getFailedOnTimeWithNoEventDataList() {
        String id = "FG-01";
        String url = "/v2/accessgroups/function-groups/" + id;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("id", id);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest("putFunctionGroupById", url);
        httpServletRequest.setAttribute("org.springframework.web.servlet.View.pathVariables", attributes);

        String serviceAgreementId = "SA-01";
        String description = "description";
        String fgName = "Name";
        String validFromDate = "01-08-2019";
        String validFromTime = "08:00";
        String validUntilTime = "10:00";

        FunctionGroupByIdPutRequestBody functionGroupBase = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate(validFromDate)
            .withValidFromTime(validFromTime)
            .withValidUntilTime(validUntilTime);

        when(joinPoint.getArgs())
            .thenReturn(Lists.newArrayList(functionGroupBase, id).toArray());

        List<AuditMessage> actualEventList = updateFunctionGroupDescriptor
            .getFailedEventDataList(joinPoint);
        List<AuditMessage> expectedEventList = new ArrayList<>();
        AuditMessage auditMessage = new AuditMessage();
        auditMessage.withStatus(Status.FAILED);
        expectedEventList.add(auditMessage);
        assertEquals(expectedEventList, actualEventList);
    }

    @Test
    public void getInitEventDataList() {
        String id = "FG-01";
        String url = "/v2/accessgroups/function-groups/" + id;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("id", id);
        HttpServletRequest httpServletRequest = new MockHttpServletRequest("putFunctionGroupById", url);
        httpServletRequest.setAttribute("org.springframework.web.servlet.View.pathVariables", attributes);

        String serviceAgreementId = "SA-01";
        String description = "description";
        String fgName = "Name";
        String validFromDate = "01-08-2019";
        String validFromTime = "08:00";
        String validUntilDate = "30-08-2019";
        String validUntilTime = "10:00";

        FunctionGroupByIdPutRequestBody functionGroupBase = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromTime(validFromTime)
            .withValidFromDate(validFromDate)
            .withValidUntilDate(validUntilDate)
            .withValidUntilTime(validUntilTime);

        when(joinPoint.getArgs())
            .thenReturn(Lists.newArrayList(functionGroupBase, id).toArray());

        List<AuditMessage> actualEventList = updateFunctionGroupDescriptor
            .getInitEventDataList(joinPoint);
        List<AuditMessage> expectedEventList = new ArrayList<>();
        AuditMessage auditMessage = new AuditMessage();
        auditMessage.withStatus(Status.INITIATED);
        auditMessage.withEventDescription("Update | Function Group | Initiated | ID FG-01");
        auditMessage.withEventMetaDatum("Function Group Name", fgName);
        auditMessage.withEventMetaDatum("Function Group Description", description);
        auditMessage.withEventMetaDatum("Service Agreement ID", serviceAgreementId);
        auditMessage.withEventMetaDatum("Start DateTime",
            functionGroupBase.getValidFromDate() + " " + functionGroupBase.getValidFromTime());
        auditMessage.withEventMetaDatum("End DateTime",
            functionGroupBase.getValidUntilDate() + " " + functionGroupBase.getValidUntilTime());
        expectedEventList.add(auditMessage);
        assertEquals(expectedEventList, actualEventList);
    }
}
