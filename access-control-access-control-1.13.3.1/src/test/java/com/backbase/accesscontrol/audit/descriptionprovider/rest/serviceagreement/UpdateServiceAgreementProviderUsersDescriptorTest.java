package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement;
import com.backbase.accesscontrol.util.UserContextUtil;
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
 * Tests for {@link UpdateServiceAgreementProviderUsersDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceAgreementProviderUsersDescriptorTest {

    @InjectMocks
    private UpdateServiceAgreementProviderUsersDescriptor updateServiceAgreementProviderUsersDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private UserContextUtil userContextUtil;

    @Test
    public void getSuccessEventDataList() {
        String serviceAgreementID = "SA-01";
        String userId1 = "U-01";
        String userId2 = "U-02";
        UsersForServiceAgreement requestBody = new UsersForServiceAgreement()
                .users(asList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(singletonList(requestBody).toArray());
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementID);

        List<AuditMessage> successEventDataList = updateServiceAgreementProviderUsersDescriptor.getSuccessEventDataList(
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
        UsersForServiceAgreement requestBody = new UsersForServiceAgreement()
                .users(asList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(singletonList(requestBody).toArray());
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementID);

        List<AuditMessage> successEventDataList = updateServiceAgreementProviderUsersDescriptor.getInitEventDataList(
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
        UsersForServiceAgreement requestBody = new UsersForServiceAgreement()
                .users(asList(userId1, userId2));

        when(joinPoint.getArgs())
            .thenReturn(singletonList(requestBody).toArray());

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementID);

        List<AuditMessage> successEventDataList = updateServiceAgreementProviderUsersDescriptor.getFailedEventDataList(
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
