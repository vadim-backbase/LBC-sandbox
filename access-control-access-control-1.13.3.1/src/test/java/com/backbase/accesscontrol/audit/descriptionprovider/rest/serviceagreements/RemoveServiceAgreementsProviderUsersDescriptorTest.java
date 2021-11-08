package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreements;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link RemoveServiceAgreementsProviderUsersDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoveServiceAgreementsProviderUsersDescriptorTest {

    @InjectMocks
    private RemoveServiceAgreementsProviderUserDescriptor removeServiceAgreementsProviderUserDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataList() {
        String serviceAgreementId = "SA-01";
        String userId1 = "U-01";
        String userId2 = "U-02";
        UsersForServiceAgreement request =
            new UsersForServiceAgreement()
                .users(asList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(asList(serviceAgreementId, request).toArray());

        List<AuditMessage> successEventDataList = removeServiceAgreementsProviderUserDescriptor.getSuccessEventDataList(
            joinPoint, "");

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Remove User | Service Agreement | Successful | service agreement ID SA-01")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("User ID", userId1);

        AuditMessage event2 = new AuditMessage().withEventDescription(
            "Remove User | Service Agreement | Successful | service agreement ID SA-01")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("User ID", userId2);

        List<AuditMessage> expectedDescription = asList(event1, event2);
        assertEquals(expectedDescription, successEventDataList);
    }

    @Test
    public void getInitEventDataList() {
        String serviceAgreementId = "SA-01";
        String userId1 = "U-01";
        String userId2 = "U-02";
        UsersForServiceAgreement request =
            new UsersForServiceAgreement()
                .users(asList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(asList(serviceAgreementId, request).toArray());

        List<AuditMessage> successEventDataList = removeServiceAgreementsProviderUserDescriptor.getInitEventDataList(
            joinPoint);

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Remove User | Service Agreement | Initiated | service agreement ID SA-01")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("User ID", userId1);

        AuditMessage event2 = new AuditMessage().withEventDescription(
            "Remove User | Service Agreement | Initiated | service agreement ID SA-01")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("User ID", userId2);

        List<AuditMessage> expectedDescription = asList(event1, event2);
        assertEquals(expectedDescription, successEventDataList);
    }

    @Test
    public void getFailedEventDataList() {
        String serviceAgreementId = "SA-01";
        String userId1 = "U-01";
        String userId2 = "U-02";
        UsersForServiceAgreement request =
            new UsersForServiceAgreement()
                .users(asList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(asList(serviceAgreementId, request).toArray());

        List<AuditMessage> successEventDataList = removeServiceAgreementsProviderUserDescriptor.getFailedEventDataList(
            joinPoint);

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Remove User | Service Agreement | Failed | service agreement ID SA-01")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("User ID", userId1);

        AuditMessage event2 = new AuditMessage().withEventDescription(
            "Remove User | Service Agreement | Failed | service agreement ID SA-01")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("User ID", userId2);

        List<AuditMessage> expectedDescription = asList(event1, event2);
        assertEquals(expectedDescription, successEventDataList);
    }
}
