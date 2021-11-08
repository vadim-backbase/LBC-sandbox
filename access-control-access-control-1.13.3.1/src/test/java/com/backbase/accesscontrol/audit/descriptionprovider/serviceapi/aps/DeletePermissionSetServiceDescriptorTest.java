package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.aps;

import static com.backbase.accesscontrol.audit.AuditObjectType.PERMISSION_SET;
import static com.backbase.accesscontrol.audit.EventAction.DELETE;
import static com.backbase.audit.client.model.AuditMessage.Status.FAILED;
import static com.backbase.audit.client.model.AuditMessage.Status.INITIATED;
import static com.backbase.audit.client.model.AuditMessage.Status.SUCCESSFUL;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.audit.client.model.AuditMessage;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeletePermissionSetServiceDescriptorTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private DeletePermissionSetServiceDescriptor testy;

    @Test
    public void shouldGetValidEventAction() {

        AuditEventAction auditEventAction = testy.getAuditEventAction();

        assertEquals(DELETE, auditEventAction.getEventAction());
        assertEquals(PERMISSION_SET, auditEventAction.getObjectType());
    }

    @Test
    public void shouldReturnSuccessfulEvent() {
        when(joinPoint.getArgs()).thenReturn(new String[] {"id", "7", "token"});

        List<AuditMessage> messages = testy.getSuccessEventDataList(joinPoint, null);
        assertEquals(singletonList(new AuditMessage()
            .withStatus(SUCCESSFUL)
            .withEventDescription("Delete | Assignable Permission Set | Successful | ID 7")
            .withEventMetaDatum("ID", "7")), messages);
    }

    @Test
    public void shouldReturnInitEvent() {
        when(joinPoint.getArgs()).thenReturn(new String[] {"name", "ASP 1", "token"});

        List<AuditMessage> messages = testy.getInitEventDataList(joinPoint);
        assertEquals(singletonList(new AuditMessage()
            .withStatus(INITIATED)
            .withEventDescription("Delete | Assignable Permission Set | Initiated | name ASP 1")
            .withEventMetaDatum("name", "ASP 1")), messages);
    }

    @Test
    public void shouldReturnFailedEvent() {
        when(joinPoint.getArgs()).thenReturn(new String[] {"name", "ASP 1", "token"});

        List<AuditMessage> messages = testy.getFailedEventDataList(joinPoint);
        assertEquals(singletonList(new AuditMessage()
            .withStatus(FAILED)
            .withEventDescription("Delete | Assignable Permission Set | Failed | name ASP 1")
            .withEventMetaDatum("name", "ASP 1")), messages);
    }
}