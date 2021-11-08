package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.CreateStatus;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.accesscontrol.service.rest.spec.model.ParticipantIngest;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers;
import com.backbase.accesscontrol.service.rest.spec.model.ServicesAgreementIngest;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.math.BigDecimal;
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
public class CreateServiceAgreementsDescriptorTest {

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @InjectMocks
    private CreateServiceAgreementsDescriptor createServiceAgreementsDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void shouldReturnAuditMessageForEveryParticipant() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le1")
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le2")
            .withEventMetaDatum("Participant is sharing users", "true")
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le3")
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(new IdItem().id("sa1"), HttpStatus.CREATED));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnInitMessageForEveryParticipant() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getInitEventDataList(joinPoint);
        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnFailedMessageForEveryParticipant() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Create | Service Agreement | Failed | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Create | Service Agreement | Failed | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Failed | name SA name")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getFailedEventDataList(joinPoint);
        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnInitMessageForEveryParticipantWhenUserApsIdentifiersAreEmpty() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet)
                .regularUserAps(new PresentationUserApsIdentifiers())
                .adminUserAps(new PresentationUserApsIdentifiers()), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getInitEventDataList(joinPoint);
        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnInitMessageForEveryParticipantWhenUserApsIdentifiersNotEmpty() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet)
                .regularUserAps(new PresentationUserApsIdentifiers()
                    .idIdentifiers(asList(new BigDecimal(1), new BigDecimal(2)))
                    .nameIdentifiers(asList("apsName1", "apsName2")))
                .adminUserAps(new PresentationUserApsIdentifiers()
                .idIdentifiers(asList(new BigDecimal(3), new BigDecimal(4)))
                .nameIdentifiers(asList("apsName3", "apsName4"))), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getInitEventDataList(joinPoint);
        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnInitMessageForEveryParticipantWhenUserApsIdentifiersAreValid() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet)
                .regularUserAps(new PresentationUserApsIdentifiers()
                    .idIdentifiers(asList(new BigDecimal(1), new BigDecimal(2))))
                .adminUserAps(new PresentationUserApsIdentifiers()
                .nameIdentifiers(asList("apsName3", "apsName4"))), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Initiated | name SA name")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getInitEventDataList(joinPoint);
        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnFailedMessageForEveryParticipantWhenUserApsIdentifiersAreValid() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet)
                .regularUserAps(new PresentationUserApsIdentifiers()
                    .nameIdentifiers(asList("apsName1", "apsName2")))
                .adminUserAps(new PresentationUserApsIdentifiers()
                .idIdentifiers(asList(new BigDecimal(3), new BigDecimal(4)))), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Create | Service Agreement | Failed | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Create | Service Agreement | Failed | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Failed | name SA name")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getFailedEventDataList(joinPoint);
        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnAuditMessageForEveryParticipantWhenUserApsIdentifiersAreSentWithIds() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet)
                .regularUserAps(new PresentationUserApsIdentifiers()
                    .idIdentifiers(asList(new BigDecimal(1), new BigDecimal(2))))
                .adminUserAps(new PresentationUserApsIdentifiers()
                .idIdentifiers(asList(new BigDecimal(3), new BigDecimal(4)))), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le1")
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le2")
            .withEventMetaDatum("Participant is sharing users", "true")
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le3")
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "1, 2")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "3, 4");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(new IdItem().id("sa1"), HttpStatus.CREATED));

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void shouldReturnAuditMessageForEveryParticipantWhenUserApsIdentifiersAreSentWithNames() {

        ParticipantIngest participantIngest1 = new ParticipantIngest()
            .externalId("le1")
            .sharingAccounts(true)
            .sharingUsers(false)
            .admins(asList("admin11", "admin12"));
        ParticipantIngest participantIngest2 = new ParticipantIngest()
            .externalId("le2")
            .sharingAccounts(false)
            .sharingUsers(true)
            .admins(singletonList("admin21"))
            .users(singletonList("user21"));
        ParticipantIngest participantIngest3 = new ParticipantIngest()
            .externalId("le3")
            .sharingUsers(false)
            .sharingAccounts(true)
            .admins(emptyList());
        List<ParticipantIngest> participantIngestSet = new ArrayList<>();
        participantIngestSet.add(participantIngest1);
        participantIngestSet.add(participantIngest2);
        participantIngestSet.add(participantIngest3);

        when(joinPoint.getArgs()).thenReturn(new Object[]{
            new ServicesAgreementIngest()
                .status(CreateStatus.ENABLED)
                .name("SA name")
                .description("SA Description")
                .externalId("ExtSA1")
                .isMaster(false)
                .validFromDate("01-08-2019")
                .validFromTime("08:00")
                .validUntilDate("30-08-2019")
                .validUntilTime("10:00")
                .participantsToIngest(participantIngestSet)
                .regularUserAps(new PresentationUserApsIdentifiers()
                    .nameIdentifiers(asList("apsName1", "apsName2")))
                .adminUserAps(new PresentationUserApsIdentifiers()
                .nameIdentifiers(asList("apsName3", "apsName4"))), null, null});

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le1")
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le2")
            .withEventMetaDatum("Participant is sharing users", "true")
            .withEventMetaDatum("Participant is sharing accounts", "false")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        AuditMessage eventData3 = new AuditMessage()
            .withEventDescription("Create | Service Agreement | Successful | name SA name")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("External Service Agreement ID", "ExtSA1")
            .withEventMetaDatum("Service Agreement ID", "sa1")
            .withEventMetaDatum("Master Service Agreement", "false")
            .withEventMetaDatum("Service Agreement Name", "SA name")
            .withEventMetaDatum("Service Agreement Description", "SA Description")
            .withEventMetaDatum("Service Agreement Status", CreateStatus.ENABLED.toString())
            .withEventMetaDatum("External Participant ID", "le3")
            .withEventMetaDatum("Participant is sharing users", "false")
            .withEventMetaDatum("Participant is sharing accounts", "true")
            .withEventMetaDatum("Start DateTime", "01-08-2019 08:00")
            .withEventMetaDatum("End DateTime", "30-08-2019 10:00")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "apsName1, apsName2")
            .withEventMetaDatum("Admin user Assignable Permission Set Names", "apsName3, apsName4");

        List<AuditMessage> expectedDescription = asList(eventData1, eventData2, eventData3);

        List<AuditMessage> initDescription = createServiceAgreementsDescriptor.getSuccessEventDataList(joinPoint,
            new ResponseEntity<>(new IdItem().id("sa1"), HttpStatus.CREATED));

        assertEquals(expectedDescription, initDescription);
    }

}
