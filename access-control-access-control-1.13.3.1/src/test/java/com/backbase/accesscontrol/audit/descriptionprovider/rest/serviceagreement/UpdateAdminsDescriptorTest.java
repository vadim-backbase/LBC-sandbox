package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAdmins;
import com.backbase.accesscontrol.client.rest.spec.model.UpdateAdmins;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.Collections;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UpdateAdminsDescriptorTest {

    @InjectMocks
    private UpdateAdminsDescriptor updateAdminsDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private static final String SAID = "SA-01";
    private static final String PARTICIPANT_ID_01 = "participantId-01";
    private static final String PARTICIPANT_ID_02 = "participantId-02";
    private static final String ADMIN_11 = "admin-11";
    private static final String ADMIN_12 = "admin-12";
    private static final String ADMIN_21 = "admin-21";
    private static final String ADMIN_22 = "admin-22";
    private static final String ADMIN_23 = "admin-23";

    @Before
    public void setup() {
        LegalEntityAdmins legalEntityAdmins1 = new LegalEntityAdmins()
            .id(PARTICIPANT_ID_01)
            .admins(asList(ADMIN_11, ADMIN_12));
        LegalEntityAdmins legalEntityAdmins2 = new LegalEntityAdmins()
            .id(PARTICIPANT_ID_02)
            .admins(asList(ADMIN_21, ADMIN_22, ADMIN_23));
        UpdateAdmins adminsPutRequestBody = new UpdateAdmins()
            .participants(asList(legalEntityAdmins1, legalEntityAdmins2));

        when(joinPoint.getArgs())
            .thenReturn(newArrayList(SAID, adminsPutRequestBody).toArray());
    }

    @Test
    public void shouldReturnInitEventMetaData() {
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Update Admins | Service Agreement | Initiated | service agreement ID " + SAID)
            .withEventMetaDatum("Service Agreement ID", SAID);

        List<AuditMessage> successAuditMessages = updateAdminsDescriptor.getInitEventDataList(joinPoint);

        assertEquals(successAuditMessages, Collections.singletonList(auditMessage));
    }

    @Test
    public void shouldReturnSuccessfulEventMetaData() {
        AuditMessage auditMessage11 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Update Admins | Service Agreement | Successful | service agreement ID " + SAID)
            .withEventMetaDatum("Service Agreement ID", SAID)
            .withEventMetaDatum("Participant ID", PARTICIPANT_ID_01)
            .withEventMetaDatum("Admin ID", ADMIN_11);
        AuditMessage auditMessage12 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Update Admins | Service Agreement | Successful | service agreement ID " + SAID)
            .withEventMetaDatum("Service Agreement ID", SAID)
            .withEventMetaDatum("Participant ID", PARTICIPANT_ID_01)
            .withEventMetaDatum("Admin ID", ADMIN_12);
        AuditMessage auditMessage21 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Update Admins | Service Agreement | Successful | service agreement ID " + SAID)
            .withEventMetaDatum("Service Agreement ID", SAID)
            .withEventMetaDatum("Participant ID", PARTICIPANT_ID_02)
            .withEventMetaDatum("Admin ID", ADMIN_21);
        AuditMessage auditMessage22 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Update Admins | Service Agreement | Successful | service agreement ID " + SAID)
            .withEventMetaDatum("Service Agreement ID", SAID)
            .withEventMetaDatum("Participant ID", PARTICIPANT_ID_02)
            .withEventMetaDatum("Admin ID", ADMIN_22);
        AuditMessage auditMessage23 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Update Admins | Service Agreement | Successful | service agreement ID " + SAID)
            .withEventMetaDatum("Service Agreement ID", SAID)
            .withEventMetaDatum("Participant ID", PARTICIPANT_ID_02)
            .withEventMetaDatum("Admin ID", ADMIN_23);

        List<AuditMessage> expectedAuditMessages = asList(auditMessage11, auditMessage12, auditMessage21,
            auditMessage22, auditMessage23);

        List<AuditMessage> successAuditMessages = updateAdminsDescriptor
            .getSuccessEventDataList(joinPoint, null);

        assertEquals(successAuditMessages, expectedAuditMessages);
    }

    @Test
    public void shouldReturnFailedEventMetaData() {
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Update Admins | Service Agreement | Failed | service agreement ID " + SAID)
            .withEventMetaDatum("Service Agreement ID", SAID);

        List<AuditMessage> successAuditMessages = updateAdminsDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(successAuditMessages, Collections.singletonList(auditMessage));
    }
}
