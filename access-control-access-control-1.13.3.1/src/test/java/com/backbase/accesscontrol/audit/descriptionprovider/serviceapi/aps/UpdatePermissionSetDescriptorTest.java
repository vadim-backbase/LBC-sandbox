package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.aps;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetPut;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.math.BigDecimal;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePermissionSetDescriptorTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private UpdatePermissionSetDescriptor updatePermissionSetDescriptor;

    @Test
    public void getInitEventDataList() {
        PresentationPermissionSetPut permissionSet = new PresentationPermissionSetPut()
            .externalServiceAgreementId("ex-sa")
            .regularUserAps(new PresentationUserApsIdentifiers()
                .nameIdentifiers(singletonList("aps name")))
            .adminUserAps(new PresentationUserApsIdentifiers()
                .idIdentifiers(singletonList(new BigDecimal(1))));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                "Update | Associated Assignable Permission Sets to a Service Agreement | Initiated | External Service Agreement ID ex-sa")
            .withEventMetaDatum("External Service Agreement ID", "ex-sa")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "aps name")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "1");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData1);
        List<AuditMessage> auditMessages = updatePermissionSetDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getSuccessEventDataList() {
        PresentationPermissionSetPut permissionSet = new PresentationPermissionSetPut()
            .externalServiceAgreementId("ex-sa")
            .regularUserAps(new PresentationUserApsIdentifiers()
                .nameIdentifiers(singletonList("aps name")))
            .adminUserAps(new PresentationUserApsIdentifiers()
                .idIdentifiers(singletonList(new BigDecimal(1))));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                "Update | Associated Assignable Permission Sets to a Service Agreement | Successful | External Service Agreement ID ex-sa")
            .withEventMetaDatum("External Service Agreement ID", "ex-sa")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "aps name")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "1");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData1);
        List<AuditMessage> auditMessages = updatePermissionSetDescriptor
            .getSuccessEventDataList(joinPoint, null);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getFailedEventDataList() {
        PresentationPermissionSetPut permissionSet = new PresentationPermissionSetPut()
            .externalServiceAgreementId("ex-sa")
            .regularUserAps(new PresentationUserApsIdentifiers())
            .adminUserAps(new PresentationUserApsIdentifiers()
                .idIdentifiers(singletonList(new BigDecimal(1))));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(
                "Update | Associated Assignable Permission Sets to a Service Agreement | Failed | External Service Agreement ID ex-sa")
            .withEventMetaDatum("External Service Agreement ID", "ex-sa")
            .withEventMetaDatum("Regular user Assignable Permission Set IDs", "")
            .withEventMetaDatum("Regular user Assignable Permission Set Names", "")
            .withEventMetaDatum("Admin user Assignable Permission Set IDs", "1");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData1);
        List<AuditMessage> auditMessages = updatePermissionSetDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

}