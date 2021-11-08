package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateServiceAgreementApprovalDescriptorTest {

    private static final String VALID_FROM_DATE = "08-08-2019";
    private static final String VALID_UNTIL_DATE = "11-11-2019";
    private static final String TIME = "11:11";

    @InjectMocks
    private CreateServiceAgreementApprovalDescriptor createServiceAgreementApprovalDescriptor;

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void shouldReturnInitAuditMessageForEveryParticipant() {

        String approvalId = "approvalId";
        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServiceAgreementPostRequestBody()
                .withStatus(CreateStatus.ENABLED)
                .withName("SA name")
                .withDescription("SA Description")
                .withValidFromDate(VALID_FROM_DATE)
                .withValidFromTime(TIME)
                .withValidUntilDate(VALID_UNTIL_DATE)
                .withValidUntilTime(TIME)
                .withParticipants(Lists.newArrayList(
                new Participant()
                    .withId("le1")
                    .withSharingAccounts(true)
                    .withSharingUsers(false)
                    .withAdmins(Sets.newHashSet("admin11", "admin12")),
                new Participant()
                    .withId("le2")
                    .withSharingAccounts(false)
                    .withSharingUsers(true)
                    .withAdmins(Sets.newHashSet("admin21")),
                new Participant()
                    .withId("le3")
                    .withSharingUsers(false)
                    .withSharingAccounts(true)
                    .withAdmins(new HashSet<>())
            )), null, approvalId});

        List<String> messageIds = createServiceAgreementApprovalDescriptor.getMessageIds(joinPoint);
        assertEquals(singletonList(approvalId), messageIds);

        List<AuditMessage> res = createServiceAgreementApprovalDescriptor.getInitEventDataList(joinPoint);

        assertEquals(3, res.size());
        AuditMessage message = res.get(0);
        assertEquals("Request Create | Service Agreement | Initiated | name SA name", message.getEventDescription());
        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le1", message.getEventMetaData().get("Participant ID"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" + " " + "11:11", message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019" + " " + "11:11", message.getEventMetaData().get("End DateTime"));

        message = res.get(1);
        assertEquals("Request Create | Service Agreement | Initiated | name SA name", message.getEventDescription());
        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le2", message.getEventMetaData().get("Participant ID"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" + " " + "11:11", message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019" + " " + "11:11", message.getEventMetaData().get("End DateTime"));

        message = res.get(2);
        assertEquals("Request Create | Service Agreement | Initiated | name SA name", message.getEventDescription());
        assertEquals(Status.INITIATED, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le3", message.getEventMetaData().get("Participant ID"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" + " " + "11:11", message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019" + " " + "11:11", message.getEventMetaData().get("End DateTime"));
    }


    @Test
    public void shouldReturnSuccessAuditMessageForEveryParticipant() {
        String approvalId = "approvalId";
        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServiceAgreementPostRequestBody()
                .withStatus(CreateStatus.ENABLED)
                .withName("SA name")
                .withDescription("SA Description")
                .withValidFromDate(VALID_FROM_DATE)
                .withValidUntilDate(VALID_UNTIL_DATE)

                .withParticipants(Lists.newArrayList(
                new Participant()
                    .withId("le1")
                    .withSharingAccounts(true)
                    .withSharingUsers(false)
                    .withAdmins(Sets.newHashSet("admin11", "admin12")),
                new Participant()
                    .withId("le2")
                    .withSharingAccounts(false)
                    .withSharingUsers(true)
                    .withAdmins(Sets.newHashSet("admin21")),
                new Participant()
                    .withId("le3")
                    .withSharingUsers(false)
                    .withSharingAccounts(true)
                    .withAdmins(new HashSet<>())
            )), null, approvalId});

        List<String> messageIds = createServiceAgreementApprovalDescriptor.getMessageIds(joinPoint);
        assertEquals(singletonList(approvalId), messageIds);

        List<AuditMessage> res = createServiceAgreementApprovalDescriptor.getSuccessEventDataList(joinPoint, null);

        assertEquals(3, res.size());
        AuditMessage message = res.get(0);
        assertEquals("Request Create | Service Agreement | Successful | name SA name", message.getEventDescription());
        assertEquals(Status.SUCCESSFUL, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le1", message.getEventMetaData().get("Participant ID"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" , message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019", message.getEventMetaData().get("End DateTime"));

        message = res.get(1);
        assertEquals("Request Create | Service Agreement | Successful | name SA name", message.getEventDescription());
        assertEquals(Status.SUCCESSFUL, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le2", message.getEventMetaData().get("Participant ID"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" , message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019", message.getEventMetaData().get("End DateTime"));

        message = res.get(2);
        assertEquals("Request Create | Service Agreement | Successful | name SA name", message.getEventDescription());
        assertEquals(Status.SUCCESSFUL, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le3", message.getEventMetaData().get("Participant ID"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" , message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019", message.getEventMetaData().get("End DateTime"));


    }

    @Test
    public void shouldReturnFailedAuditMessageForEveryParticipant() {

        String approvalId = "approvalId";
        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServiceAgreementPostRequestBody()
                .withStatus(CreateStatus.ENABLED)
                .withName("SA name")
                .withDescription("SA Description")
                .withValidFromDate(VALID_FROM_DATE)
                .withValidFromTime(TIME)
                .withValidUntilDate(VALID_UNTIL_DATE)
                .withParticipants(Lists.newArrayList(
                new Participant()
                    .withId("le1")
                    .withSharingAccounts(true)
                    .withSharingUsers(false)
                    .withAdmins(Sets.newHashSet("admin11", "admin12")),
                new Participant()
                    .withId("le2")
                    .withSharingAccounts(false)
                    .withSharingUsers(true)
                    .withAdmins(Sets.newHashSet("admin21")),
                new Participant()
                    .withId("le3")
                    .withSharingUsers(false)
                    .withSharingAccounts(true)
                    .withAdmins(new HashSet<>())
            )), null, approvalId});

        List<String> messageIds = createServiceAgreementApprovalDescriptor.getMessageIds(joinPoint);
        assertEquals(singletonList(approvalId), messageIds);

        List<AuditMessage> res = createServiceAgreementApprovalDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(3, res.size());
        AuditMessage message = res.get(0);
        assertEquals("Request Create | Service Agreement | Failed | name SA name", message.getEventDescription());
        assertEquals(Status.FAILED, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le1", message.getEventMetaData().get("Participant ID"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" + " " + "11:11", message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019", message.getEventMetaData().get("End DateTime"));

        message = res.get(1);
        assertEquals("Request Create | Service Agreement | Failed | name SA name", message.getEventDescription());
        assertEquals(Status.FAILED, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le2", message.getEventMetaData().get("Participant ID"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" + " " + "11:11", message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019", message.getEventMetaData().get("End DateTime"));

        message = res.get(2);
        assertEquals("Request Create | Service Agreement | Failed | name SA name", message.getEventDescription());
        assertEquals(Status.FAILED, message.getStatus());
        assertEquals("false", message.getEventMetaData().get("Master Service Agreement"));
        assertEquals("SA name", message.getEventMetaData().get("Service Agreement Name"));
        assertEquals("SA Description", message.getEventMetaData().get("Service Agreement Description"));
        assertEquals(CreateStatus.ENABLED.toString(), message.getEventMetaData().get("Service Agreement Status"));
        assertEquals("le3", message.getEventMetaData().get("Participant ID"));
        assertEquals("false", message.getEventMetaData().get("Participant is sharing users"));
        assertEquals("true", message.getEventMetaData().get("Participant is sharing accounts"));
        assertEquals("08-08-2019" + " " + "11:11", message.getEventMetaData().get("Start DateTime"));
        assertEquals("11-11-2019", message.getEventMetaData().get("End DateTime"));
    }
}
