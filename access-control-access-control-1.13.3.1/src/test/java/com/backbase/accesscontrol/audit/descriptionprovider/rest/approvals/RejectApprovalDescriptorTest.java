package com.backbase.accesscontrol.audit.descriptionprovider.rest.approvals;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import java.util.List;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RejectApprovalDescriptorTest {

    private static final String LOGGED_USERNAME = "user1";
    private static final String USER_ID = "U-01";
    private static final String SERVICE_AGREEMENT_ID = "sa-id-01";

    @InjectMocks
    private RejectApprovalDescriptor rejectApprovalDescriptor;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private UserContextUtil userContextUtil;

    @Before
    public void setUp() {
        when(userContextUtil.getServiceAgreementId()).thenReturn(SERVICE_AGREEMENT_ID);
        when(userContextUtil.getOptionalAuthenticatedUserName()).thenReturn(Optional.of(LOGGED_USERNAME));
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(USER_ID, "le"));
    }

    @Test
    public void getInitEventDataListTest() {
        String approvalId = "appId";

        when(joinPoint.getArgs()).thenReturn(singletonList(approvalId).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                "Reject pending operation | Initiated | for user " + USER_ID + " in service agreement "
                    + SERVICE_AGREEMENT_ID)
            .withEventMetaDatum("Approval Request ID", approvalId)
            .withEventMetaDatum("Outcome", "REJECTED");

        List<AuditMessage> actualEventList = rejectApprovalDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getInitEventDataListWhenUserIsAnonymousTest() {
        String approvalId = "appId";

        when(joinPoint.getArgs()).thenReturn(singletonList(approvalId).toArray());
        when(userContextUtil.getOptionalAuthenticatedUserName()).thenReturn(Optional.empty());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Reject pending operation | Initiated | for user anonymous in service agreement "
                + SERVICE_AGREEMENT_ID)
            .withEventMetaDatum("Approval Request ID", approvalId)
            .withEventMetaDatum("Outcome", "REJECTED");

        List<AuditMessage> actualEventList = rejectApprovalDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getSuccessEventDataListTest() {
        String approvalId = "appId";

        PresentationApprovalStatus presentationApprovalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.REJECTED);

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(
                "Reject pending operation | Successful | for user " + USER_ID + " in service agreement "
                    + SERVICE_AGREEMENT_ID)
            .withEventMetaDatum("Approval Request ID", approvalId)
            .withEventMetaDatum("Outcome", "REJECTED");

        when(joinPoint.getArgs()).thenReturn(asList(approvalId, presentationApprovalStatus).toArray());

        List<AuditMessage> actualEventList = rejectApprovalDescriptor
            .getSuccessEventDataList(joinPoint, presentationApprovalStatus);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataListTest() {
        String approvalId = "appId";

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Reject pending operation | Failed | for user " + USER_ID + " in service agreement "
                + SERVICE_AGREEMENT_ID)
            .withEventMetaDatum("Approval Request ID", approvalId)
            .withEventMetaDatum("Outcome", "REJECTED");

        when(joinPoint.getArgs()).thenReturn(singletonList(approvalId).toArray());

        List<AuditMessage> actualEventList = rejectApprovalDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void shouldReturnApprovalIdAsMessageId() {
        String approvalId = "appId";

        when(joinPoint.getArgs()).thenReturn(singletonList(approvalId).toArray());

        List<String> messageIds = rejectApprovalDescriptor.getMessageIds(joinPoint);

        assertEquals(approvalId, messageIds.get(0));
    }
}