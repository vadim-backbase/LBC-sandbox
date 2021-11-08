package com.backbase.accesscontrol.audit.descriptionprovider.permissions;

import static java.util.Arrays.asList;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Bound;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy;
import java.math.BigDecimal;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdatePermissionsApprovalDescriptorTest {

    @InjectMocks
    private UpdatePermissionsApprovalDescriptor updatePermissionsApprovalDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private static final String UPDATE_PERMISSIONS_PREFIX = "Request Permissions Update";
    private static final String INITIATE_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Initiated | for user %s in service agreement %s";
    private static final String SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Successful | for user %s in service agreement %s";
    private static final String FAILED_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Failed | for user %s in service agreement %s";

    @Test
    void shouldGetInitEventDataList() {
        List args = Lists.newArrayList(new PresentationFunctionDataGroupItems(), "SA1", "user1", "le1", "approval1");

        when(joinPoint.getArgs()).thenReturn(args.toArray());

        List<AuditMessage> messages = updatePermissionsApprovalDescriptor.getInitEventDataList(joinPoint);

        assertEquals(1, messages.size());
        AuditMessage message = messages.get(0);
        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals(2, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());
    }

    @Test
    void shouldGetMultipleAuditMessagesInitEventDataList() {
        PresentationFunctionDataGroupItems presentationFunctionDataGroupItems = new PresentationFunctionDataGroupItems()
            .withItems(asList(
                new PresentationFunctionDataGroup().withFunctionGroupId("fgId1").withDataGroupIds(
                    asList(new PresentationGenericObjectId().withId("dgId1"),
                        new PresentationGenericObjectId().withId("dgId2"))),
                new PresentationFunctionDataGroup().withFunctionGroupId("fgId2")));

        List args = Lists.newArrayList(presentationFunctionDataGroupItems, "SA1", "user1", "le1", "approval1");

        when(joinPoint.getArgs()).thenReturn(args.toArray());

        List<AuditMessage> messages = updatePermissionsApprovalDescriptor.getInitEventDataList(joinPoint);

        assertEquals(3, messages.size());

        AuditMessage message = messages.get(0);
        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals(4, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fgId1", message.getEventMetaData().get("Function Group ID"));
        assertEquals("dgId1", message.getEventMetaData().get("Data Group ID"));
        assertEquals(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());

        message = messages.get(1);
        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals(4, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fgId1", message.getEventMetaData().get("Function Group ID"));
        assertEquals("dgId2", message.getEventMetaData().get("Data Group ID"));
        assertEquals(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());

        message = messages.get(2);
        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals(3, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fgId2", message.getEventMetaData().get("Function Group ID"));
        assertEquals(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());
    }

    @Test
    void shouldGetSuccessEventDataList() {
        PresentationFunctionDataGroupItems requestBody = new PresentationFunctionDataGroupItems();

        requestBody.getItems()
            .add(new PresentationFunctionDataGroup().withFunctionGroupId("fn1"));

        requestBody.getItems()
            .add(new PresentationFunctionDataGroup()
                .withFunctionGroupId("fn2")
                .withDataGroupIds(Lists.newArrayList(new PresentationGenericObjectId().withId("dg1"),
                    new PresentationGenericObjectId().withId("dg2"))));

        List args = Lists.newArrayList(requestBody, "SA1", "user1", "le1", "approval1");

        when(joinPoint.getArgs()).thenReturn(args.toArray());

        List<AuditMessage> messages = updatePermissionsApprovalDescriptor.getSuccessEventDataList(joinPoint, null);

        assertEquals(3, messages.size());
        AuditMessage message = messages.get(0);
        assertEquals(Status.SUCCESSFUL, message.getStatus());
        assertEquals(3, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID",
            "Function Group ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fn1", message.getEventMetaData().get("Function Group ID"));
        assertEquals(String.format(SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());

        message = messages.get(1);
        assertEquals(Status.SUCCESSFUL, message.getStatus());
        assertEquals(4, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID",
            "Function Group ID", "Data Group ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fn2", message.getEventMetaData().get("Function Group ID"));
        assertEquals("dg1", message.getEventMetaData().get("Data Group ID"));
        assertEquals(String.format(SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());

        message = messages.get(2);
        assertEquals(Status.SUCCESSFUL, message.getStatus());
        assertEquals(4, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID",
            "Function Group ID", "Data Group ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fn2", message.getEventMetaData().get("Function Group ID"));
        assertEquals("dg2", message.getEventMetaData().get("Data Group ID"));
        assertEquals(String.format(SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());
    }

    @Test
    void shouldGetFailedEventDataList() {
        PresentationFunctionDataGroupItems requestBody = new PresentationFunctionDataGroupItems();

        requestBody.getItems()
            .add(new PresentationFunctionDataGroup().withFunctionGroupId("fn1"));

        requestBody.getItems()
            .add(new PresentationFunctionDataGroup()
                .withFunctionGroupId("fn2")
                .withDataGroupIds(newArrayList(new PresentationGenericObjectId().withId("dg1"),
                    new PresentationGenericObjectId().withId("dg2"))));

        List args = Lists.newArrayList(requestBody, "SA1", "user1", "le1", "approval1");

        when(joinPoint.getArgs()).thenReturn(args.toArray());

        List<AuditMessage> messages = updatePermissionsApprovalDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(3, messages.size());
        AuditMessage message = messages.get(0);
        assertEquals(Status.FAILED, message.getStatus());
        assertEquals(3, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fn1", message.getEventMetaData().get("Function Group ID"));
        assertEquals(String.format(FAILED_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());

        message = messages.get(1);
        assertEquals(Status.FAILED, message.getStatus());
        assertEquals(4, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fn2", message.getEventMetaData().get("Function Group ID"));
        assertEquals("dg1", message.getEventMetaData().get("Data Group ID"));
        assertEquals(String.format(FAILED_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());

        message = messages.get(2);
        assertEquals(Status.FAILED, message.getStatus());
        assertEquals(4, message.getEventMetaData().size());
        assertThat(message.getEventMetaData().keySet(), hasItems("Service Agreement ID", "User ID"));
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals("fn2", message.getEventMetaData().get("Function Group ID"));
        assertEquals("dg2", message.getEventMetaData().get("Data Group ID"));
        assertEquals(String.format(FAILED_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());
    }

    @Test
    void shouldGetApprovalIdAsMessageId() {
        List args = Lists.newArrayList(new PresentationFunctionDataGroupItems(), "SA1", "user1", "le1", "approval1");

        when(joinPoint.getArgs()).thenReturn(args.toArray());

        List<String> messageIds = updatePermissionsApprovalDescriptor.getMessageIds(joinPoint);

        assertEquals(1, messageIds.size());
        assertEquals("approval1", messageIds.get(0));
    }

    @Test
    void shouldGetInitEventDataListWithSAP() {
        PresentationFunctionDataGroupItems presentationFunctionDataGroupItems =
            new PresentationFunctionDataGroupItems();

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("SEPA_CT");
        selfApprovalPolicy.setCanSelfApprove(true);

        Bound bound1 = new Bound();
        bound1.setAmount(new BigDecimal(100));
        bound1.setCurrencyCode("EUR");

        Bound bound2 = new Bound();
        bound2.setAmount(new BigDecimal(1000));
        bound2.setCurrencyCode("USD");

        selfApprovalPolicy.setBounds(List.of(bound1, bound2));

        presentationFunctionDataGroupItems.setItems(List.of(new PresentationFunctionDataGroup()
            .withSelfApprovalPolicies(List.of(selfApprovalPolicy))));

        when(joinPoint.getArgs())
            .thenReturn(List.of(presentationFunctionDataGroupItems, "SA1", "user1", "le1", "approval1").toArray());

        List<AuditMessage> messages = updatePermissionsApprovalDescriptor.getInitEventDataList(joinPoint);

        assertEquals(1, messages.size());
        AuditMessage message = messages.get(0);

        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals(8, message.getEventMetaData().size());
        assertEquals("SA1", message.getEventMetaData().get("Service Agreement ID"));
        assertEquals("user1", message.getEventMetaData().get("User ID"));
        assertEquals(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "user1", "SA1"),
            message.getEventDescription());
        assertEquals("SEPA_CT",
            message.getEventMetaData().get(String.format(DescriptorFieldNames.SAP_BF_NAME, 1)));
        assertEquals("100",
            message.getEventMetaData().get(String.format(DescriptorFieldNames.SAP_BOUND_CURRENCY_AMOUNT, 1, 1)));
        assertEquals("1000",
            message.getEventMetaData().get(String.format(DescriptorFieldNames.SAP_BOUND_CURRENCY_AMOUNT, 1, 2)));
        assertEquals("EUR",
            message.getEventMetaData().get(String.format(DescriptorFieldNames.SAP_BOUND_CURRENCY_CODE, 1, 1)));
        assertEquals("USD",
            message.getEventMetaData().get(String.format(DescriptorFieldNames.SAP_BOUND_CURRENCY_CODE, 1, 2)));
    }

}
