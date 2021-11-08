package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.google.common.collect.Sets;
import java.util.ArrayList;
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
public class EditServiceAgreementDescriptorTest {

    private static final String VALID_FROM_DATE = "08-08-2019";
    private static final String VALID_UNTIL_DATE = "11-11-2019";
    private static final String TIME = "11:11";

    @InjectMocks
    private EditServiceAgreementDescriptor editServiceAgreementDescriptor;

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataList() {
        String serviceAgreementId = "SA-01";
        String participantId1 = "LE-01";
        String serviceAgreementName = "SA name";
        String description = "description";
        String externalId = "SA-EX-01";
        HashSet<String> admins1 = Sets.newHashSet("U-01", "U-02");
        String participantId2 = "LE-02";
        HashSet<String> admins2 = Sets.newHashSet("U-03", "U-04");

        ServiceAgreementSave request = new ServiceAgreementSave()
            .withName(serviceAgreementName)
            .withDescription(description)
            .withExternalId(externalId)
            .withValidFromDate(VALID_FROM_DATE)
            .withValidFromTime(TIME)
            .withValidUntilDate(VALID_UNTIL_DATE)
            .withValidUntilTime(TIME)
            .withStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.ENABLED)
            .withParticipants(Sets.newHashSet(
                new Participant()
                    .withId(participantId1)
                    .withSharingUsers(true)
                    .withSharingAccounts(false)
                    .withAdmins(admins1),
                new Participant()
                    .withId(participantId2)
                    .withSharingUsers(false)
                    .withSharingAccounts(true)
                    .withAdmins(admins2)
            ));

        when(joinPoint.getArgs()).thenReturn(new Object[]{request, serviceAgreementId});

        List<AuditMessage> returnedAuditMessages = editServiceAgreementDescriptor
            .getInitEventDataList(joinPoint);
        AuditMessage auditMessage1 = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Initiated | name SA name")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status", request.getStatus().toString())
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Participant ID", participantId2)
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "08-08-2019" + " " + "11:11")
            .withEventMetaDatum("End DateTime", "11-11-2019" + " " + "11:11");

        AuditMessage auditMessage2 = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Initiated | name SA name")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status", request.getStatus().toString())
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Participant ID", participantId1)
            .withEventMetaDatum("Participant is sharing users", "true")
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Start DateTime", "08-08-2019" + " " + "11:11")
            .withEventMetaDatum("End DateTime", "11-11-2019" + " " + "11:11");
        List<AuditMessage> expectedAuditMessages = new ArrayList<>(asList(auditMessage1, auditMessage2));
        assertEquals(expectedAuditMessages.get(0), returnedAuditMessages.get(0));
        assertEquals(expectedAuditMessages.get(1), returnedAuditMessages.get(1));
    }


    @Test
    public void getSuccessEventDataList() {
        String serviceAgreementId = "SA-01";
        String participantId1 = "LE-01";
        String serviceAgreementName = "SA name";
        String description = "description";
        String externalId = "SA-EX-01";
        HashSet<String> admins1 = Sets.newHashSet("U-01", "U-02");
        String participantId2 = "LE-02";
        HashSet<String> admins2 = Sets.newHashSet("U-03", "U-04");

        ServiceAgreementSave request = new ServiceAgreementSave()
            .withName(serviceAgreementName)
            .withDescription(description)
            .withExternalId(externalId)
            .withValidFromDate(VALID_FROM_DATE)
            .withValidUntilDate(VALID_UNTIL_DATE)
            .withStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.ENABLED)
            .withIsMaster(false)
            .withParticipants(Sets.newHashSet(
                new Participant()
                    .withId(participantId1)
                    .withSharingUsers(true)
                    .withSharingAccounts(false)
                    .withAdmins(admins1),
                new Participant()
                    .withId(participantId2)
                    .withSharingUsers(false)
                    .withSharingAccounts(true)
                    .withAdmins(admins2)
            ));

        when(joinPoint.getArgs()).thenReturn(new Object[]{request, serviceAgreementId});

        List<AuditMessage> returnedAuditMessages = editServiceAgreementDescriptor
            .getSuccessEventDataList(joinPoint, null);
        AuditMessage auditMessage1 = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Successful | name SA name")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status", request.getStatus().toString())
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Participant ID", participantId2)
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "08-08-2019")
            .withEventMetaDatum("End DateTime", "11-11-2019");

        AuditMessage auditMessage2 = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Successful | name SA name")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status", request.getStatus().toString())
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Participant ID", participantId1)
            .withEventMetaDatum("Participant is sharing users", "true")
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Start DateTime", "08-08-2019")
            .withEventMetaDatum("End DateTime", "11-11-2019");
        List<AuditMessage> expectedAuditMessages = new ArrayList<>(asList(auditMessage1, auditMessage2));
        assertEquals(expectedAuditMessages.get(0), returnedAuditMessages.get(0));
        assertEquals(expectedAuditMessages.get(1), returnedAuditMessages.get(1));
    }

    @Test
    public void getFailedEventDataList() {
        String serviceAgreementId = "SA-01";
        String participantId1 = "LE-01";
        String serviceAgreementName = "SA name";
        String description = "description";
        String externalId = "SA-EX-01";
        HashSet<String> admins1 = Sets.newHashSet("U-01", "U-02");
        String participantId2 = "LE-02";
        HashSet<String> admins2 = Sets.newHashSet("U-03", "U-04");

        ServiceAgreementSave request = new ServiceAgreementSave()
            .withName(serviceAgreementName)
            .withDescription(description)
            .withExternalId(externalId)
            .withValidFromDate(VALID_FROM_DATE)
            .withValidFromTime(TIME)
            .withValidUntilDate(VALID_UNTIL_DATE)
            .withStatus(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.ENABLED)
            .withIsMaster(false)
            .withParticipants(Sets.newHashSet(
                new Participant()
                    .withId(participantId1)
                    .withSharingUsers(true)
                    .withSharingAccounts(false)
                    .withAdmins(admins1),
                new Participant()
                    .withId(participantId2)
                    .withSharingUsers(false)
                    .withSharingAccounts(true)
                    .withAdmins(admins2)
            ));

        when(joinPoint.getArgs()).thenReturn(new Object[]{request, serviceAgreementId});

        List<AuditMessage> returnedAuditMessages = editServiceAgreementDescriptor
            .getFailedEventDataList(joinPoint);
        AuditMessage auditMessage1 = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Failed | name SA name")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status", request.getStatus().toString())
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Participant ID", participantId2)
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "08-08-2019" + " " + "11:11")
            .withEventMetaDatum("End DateTime", "11-11-2019");

        AuditMessage auditMessage2 = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Failed | name SA name")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status", request.getStatus().toString())
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Participant ID", participantId1)
            .withEventMetaDatum("Participant is sharing users", "true")
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Start DateTime", "08-08-2019" + " " + "11:11")
            .withEventMetaDatum("End DateTime", "11-11-2019");
        List<AuditMessage> expectedAuditMessages = new ArrayList<>(asList(auditMessage1, auditMessage2));
        assertEquals(expectedAuditMessages.get(0), returnedAuditMessages.get(0));
        assertEquals(expectedAuditMessages.get(1), returnedAuditMessages.get(1));
    }
}
