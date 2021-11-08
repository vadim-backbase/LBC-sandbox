package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreements;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.google.common.collect.Lists;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link UpdateServiceAgreementsProviderUsersDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementsProviderUsersDescriptorTest {

    @InjectMocks
    private UpdateServiceAgreementsProviderUsersDescriptor updateServiceAgreementsProviderUsersDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataList() {
        String serviceAgreementID = "SA-01";
        String userId1 = "U-01";
        String userId2 = "U-02";
        UsersForServiceAgreement requestBody =
            new UsersForServiceAgreement()
                .users(Lists.newArrayList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(Lists.newArrayList(serviceAgreementID, requestBody).toArray());

        List<AuditMessage> successEventDataList = updateServiceAgreementsProviderUsersDescriptor
            .getSuccessEventDataList(
                joinPoint, "");

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Add User | Service Agreement | Successful | service agreement ID SA-01")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementID)
            .withEventMetaDatum("User ID", userId1);

        AuditMessage event2 = new AuditMessage().withEventDescription(
            "Add User | Service Agreement | Successful | service agreement ID SA-01")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementID)
            .withEventMetaDatum("User ID", userId2);

        List<AuditMessage> expectedDescription = asList(event1, event2);
        assertEquals(expectedDescription, successEventDataList);
    }

    @Test
    public void getInitEventDataList() {
        String serviceAgreementID = "SA-01";
        String userId1 = "U-01";
        String userId2 = "U-02";
        UsersForServiceAgreement requestBody =
            new UsersForServiceAgreement()
                .users(Lists.newArrayList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(Lists.newArrayList(serviceAgreementID, requestBody).toArray());

        List<AuditMessage> successEventDataList = updateServiceAgreementsProviderUsersDescriptor
            .getInitEventDataList(
                joinPoint);

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Add User | Service Agreement | Initiated | service agreement ID SA-01")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementID)
            .withEventMetaDatum("User ID", userId1);

        AuditMessage event2 = new AuditMessage().withEventDescription(
            "Add User | Service Agreement | Initiated | service agreement ID SA-01")
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementID)
            .withEventMetaDatum("User ID", userId2);

        List<AuditMessage> expectedDescription = asList(event1, event2);
        assertEquals(expectedDescription, successEventDataList);
    }

    @Test
    public void getFailedEventDataList() {
        String serviceAgreementID = "SA-01";
        String userId1 = "U-01";
        String userId2 = "U-02";
        UsersForServiceAgreement requestBody =
            new UsersForServiceAgreement()
                .users(Lists.newArrayList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(Lists.newArrayList(serviceAgreementID, requestBody).toArray());

        List<AuditMessage> successEventDataList = updateServiceAgreementsProviderUsersDescriptor
            .getFailedEventDataList(
                joinPoint);

        AuditMessage event1 = new AuditMessage().withEventDescription(
            "Add User | Service Agreement | Failed | service agreement ID SA-01")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementID)
            .withEventMetaDatum("User ID", userId1);

        AuditMessage event2 = new AuditMessage().withEventDescription(
            "Add User | Service Agreement | Failed | service agreement ID SA-01")
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Service Agreement ID", serviceAgreementID)
            .withEventMetaDatum("User ID", userId2);

        List<AuditMessage> expectedDescription = asList(event1, event2);
        assertEquals(expectedDescription, successEventDataList);
    }
}
