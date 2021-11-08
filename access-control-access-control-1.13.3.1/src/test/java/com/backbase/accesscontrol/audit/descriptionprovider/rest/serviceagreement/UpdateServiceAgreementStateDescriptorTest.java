package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementStatePut;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.Collections;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link UpdateServiceAgreementStateDescriptor}
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementStateDescriptorTest {

    @InjectMocks
    private UpdateServiceAgreementStateDescriptor updateServiceAgreementStateDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataList() {
        String serviceAgreementId = "SA-01";
        com.backbase.accesscontrol.client.rest.spec.model.Status state =
            com.backbase.accesscontrol.client.rest.spec.model.Status.ENABLED;
        ServiceAgreementStatePut request = new ServiceAgreementStatePut()
            .state(state);

        when(joinPoint.getArgs()).thenReturn(new Object[]{serviceAgreementId, request });

        List<AuditMessage> successEventDataList = updateServiceAgreementStateDescriptor.getSuccessEventDataList(
            joinPoint, "");

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Update | Service Agreement State | Successful | State " + state.toString())
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Status", state.toString());

        List<AuditMessage> expectedDescription = Collections.singletonList(event1);
        assertEquals(expectedDescription, successEventDataList);
    }

    @Test
    public void getInitEventDataList() {
        String serviceAgreementId = "SA-01";
        com.backbase.accesscontrol.client.rest.spec.model.Status state =
            com.backbase.accesscontrol.client.rest.spec.model.Status.ENABLED;
        ServiceAgreementStatePut request = new ServiceAgreementStatePut()
            .state(state);

        when(joinPoint.getArgs()).thenReturn(new Object[]{serviceAgreementId, request});

        List<AuditMessage> successEventDataList = updateServiceAgreementStateDescriptor.getInitEventDataList(joinPoint);

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Update | Service Agreement State | Initiated | State " + state)
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Status", state.toString());

        List<AuditMessage> expectedDescription = Collections.singletonList(event1);
        assertEquals(expectedDescription, successEventDataList);
    }

    @Test
    public void getFailedEventDataList() {

        String serviceAgreementId = "SA-01";
        com.backbase.accesscontrol.client.rest.spec.model.Status state =
            com.backbase.accesscontrol.client.rest.spec.model.Status.ENABLED;
        ServiceAgreementStatePut request = new ServiceAgreementStatePut()
            .state(state);

        when(joinPoint.getArgs()).thenReturn(new Object[]{serviceAgreementId, request});

        List<AuditMessage> successEventDataList = updateServiceAgreementStateDescriptor.getFailedEventDataList(
            joinPoint);

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Update | Service Agreement State | Failed | State " + state.toString())
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Status", state.toString());

        List<AuditMessage> expectedDescription = Collections.singletonList(event1);
        assertEquals(expectedDescription, successEventDataList);
    }
}
