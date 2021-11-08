package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static com.backbase.accesscontrol.matchers.MatcherUtil.getAuditMessageMatcherWithDescription;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import com.google.common.collect.Lists;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateFunctionGroupApprovalDescriptorTest {

    @InjectMocks
    private CreateFunctionGroupApprovalDescriptor createFunctionGroupDescriptor;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataList() {
        String serviceAgreementId = "service_agreement";
        String description = "description";
        String fgName = "Name";
        String approvalId = "approvalId";

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate("01-08-2019")
            .withValidFromTime("08:00")
            .withValidUntilDate("30-08-2019")
            .withType(Type.TEMPLATE);

        when(joinPoint.getArgs())
            .thenReturn(asList(functionGroupBase, approvalId).toArray());

        AuditMessage auditMessage1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                "Request Create | Function Group Template | Initiated | name Name, service agreement ID service_agreement")
            .withEventMetaDatum("Function Group Name", functionGroupBase.getName())
            .withEventMetaDatum("Function Group Description", functionGroupBase.getDescription())
            .withEventMetaDatum("Service Agreement ID", functionGroupBase.getServiceAgreementId())
            .withEventMetaDatum("Start DateTime",
                functionGroupBase.getValidFromDate() + " " + functionGroupBase.getValidFromTime())
            .withEventMetaDatum("End DateTime",
                functionGroupBase.getValidUntilDate())
            .withEventMetaDatum("Function Group Type", functionGroupBase.getType().toString());
        List<AuditMessage> actualEventList = createFunctionGroupDescriptor
            .getInitEventDataList(joinPoint);
        List<String> messageIds = createFunctionGroupDescriptor
            .getMessageIds(joinPoint);
        assertTrue(actualEventList.contains(auditMessage1));
        assertEquals(singletonList(approvalId), messageIds);

    }

    @Test
    public void getSuccessEventDataList() {
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

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate(validFromDate)
            .withValidFromTime(validFromTime)
            .withValidUntilDate(validUntilDate)
            .withValidUntilTime(validUntilTime)
            .withPermissions(permissions)
            .withType(Type.REGULAR);

        FunctionGroupsPostResponseBody response =
            new FunctionGroupsPostResponseBody()
                .withId("id");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(functionGroupBase).toArray());

        List<AuditMessage> actualEventList = createFunctionGroupDescriptor
            .getSuccessEventDataList(joinPoint, response);

        assertThat(actualEventList, hasItems(
            getAuditMessageMatcherWithDescription(is(Status.SUCCESSFUL),
                is("Request Create | Function Group Regular | Successful | name Name, service agreement ID SA-01"),
                allOf(
                    hasEntry("Function Group Name", fgName),
                    hasEntry("Function Group Description", description),
                    hasEntry("Service Agreement ID", serviceAgreementId),
                    hasEntry("Business Function ID", function1),
                    hasEntry("Start DateTime", validFromDate + " " + validFromTime),
                    hasEntry("End DateTime", validUntilDate + " " + validUntilTime),
                    hasEntry("Privileges", "view,edit"),
                    hasEntry("Function Group Type", functionGroupBase.getType().toString())
                )),
            getAuditMessageMatcherWithDescription(is(Status.SUCCESSFUL),
                is("Request Create | Function Group Regular | Successful | name Name, service agreement ID SA-01"),
                allOf(
                    hasEntry("Function Group Name", fgName),
                    hasEntry("Function Group Description", description),
                    hasEntry("Service Agreement ID", serviceAgreementId),
                    hasEntry("Business Function ID", function2),
                    hasEntry("Start DateTime", validFromDate + " " + validFromTime),
                    hasEntry("End DateTime", validUntilDate + " " + validUntilTime),
                    hasEntry("Privileges", "view,create"),
                    hasEntry("Function Group Type", functionGroupBase.getType().toString())
                ))
        ));
    }

    @Test
    public void getFailedEventData() {
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

        FunctionGroupBase functionGroupBase = new FunctionGroupBase()
            .withServiceAgreementId(serviceAgreementId)
            .withDescription(description)
            .withName(fgName)
            .withValidFromDate(validFromDate)
            .withValidFromTime(validFromTime)
            .withValidUntilDate(validUntilDate)
            .withValidUntilTime(validUntilTime)
            .withPermissions(permissions)
            .withType(Type.TEMPLATE);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(functionGroupBase).toArray());

        List<AuditMessage> actualEventList = createFunctionGroupDescriptor
            .getFailedEventDataList(joinPoint);

        assertThat(actualEventList, hasItems(
            getAuditMessageMatcherWithDescription(is(Status.FAILED),
                is("Request Create | Function Group Template | Failed | name Name, service agreement ID SA-01"), allOf(
                    hasEntry("Function Group Name", fgName),
                    hasEntry("Function Group Description", description),
                    hasEntry("Service Agreement ID", serviceAgreementId),
                    hasEntry("Start DateTime", validFromDate + " " + validFromTime),
                    hasEntry("End DateTime", validUntilDate + " " + validUntilTime)
                ))
        ));
    }
}
