package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.ServiceAgreementApprovalService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddServiceAgreementTest {

    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private ServiceAgreementApprovalService serviceAgreementApprovalService;
    @Mock
    private ApprovalsService approvalsService;
    @Spy
    private ApprovalOnRequestScope approvalOnRequestScope;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @InjectMocks
    private AddServiceAgreement addServiceAgreement;

    @Test
    public void shouldSuccessfullyAddServiceAgreement() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        String userId = "userId";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto(userId, legalEntityId));

        mockApprovalValidation(applicationProperties, false);

        when(serviceAgreementApprovalService.createServiceAgreement(eq(postRequest), eq(legalEntityId)))
            .thenReturn(new ServiceAgreementPostResponseBody().withId(serviceAgreementId));

        ServiceAgreementPostResponseBody response = addServiceAgreement
            .addServiceAgreement(getInternalRequest(postRequest)).getData();

        assertEquals(serviceAgreementId, response.getId());
        assertFalse(approvalOnRequestScope.isApproval());
    }

    @Test
    public void shouldSuccessfullyAddServiceAgreementWithApprovalOnAndZeroPolicy() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        String userId = "userId";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, legalEntityId));
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        mockApprovalValidation(applicationProperties, true);
        mockApprovalService(userId, serviceAgreementId, ApprovalStatus.APPROVED);

        when(serviceAgreementApprovalService.createServiceAgreement(eq(postRequest), eq(legalEntityId)))
            .thenReturn(new ServiceAgreementPostResponseBody().withId(serviceAgreementId));

        ServiceAgreementPostResponseBody response = addServiceAgreement
            .addServiceAgreement(getInternalRequest(postRequest)).getData();

        assertEquals(serviceAgreementId, response.getId());
        assertFalse(approvalOnRequestScope.isApproval());
    }

    @Test
    public void shouldSuccessfullyAddServiceAgreementWithApprovalOn() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        String userId = "userId";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, legalEntityId));
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        mockApprovalValidation(applicationProperties, true);
        ApprovalDto approvalDto = mockApprovalService(userId, serviceAgreementId, ApprovalStatus.PENDING);

        when(serviceAgreementApprovalService
            .createServiceAgreementWithApproval(eq(postRequest), eq(legalEntityId), eq(approvalDto.getId())))
            .thenReturn(new ServiceAgreementPostResponseBody().withId(serviceAgreementId));

        ServiceAgreementPostResponseBody response = addServiceAgreement
            .addServiceAgreement(getInternalRequest(postRequest)).getData();

        assertEquals(serviceAgreementId, response.getId());
        assertTrue(approvalOnRequestScope.isApproval());
    }

    @Test
    public void shouldCancelApprovalRequestWhenExceptionThrownOnAddServiceAgreementWithApprovalOn() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        String userId = "userId";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, legalEntityId));
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        mockApprovalValidation(applicationProperties, true);
        ApprovalDto approvalDto = mockApprovalService(userId, serviceAgreementId, ApprovalStatus.PENDING);

        when(serviceAgreementApprovalService
            .createServiceAgreementWithApproval(eq(postRequest), eq(legalEntityId), eq(approvalDto.getId())))
            .thenThrow(new BadRequestException("Exception thrown"));

        assertThrows(BadRequestException.class,
            () -> addServiceAgreement.addServiceAgreement(getInternalRequest(postRequest)).getData());

        verify(approvalsService).cancelApprovalRequest(eq(approvalDto.getId()));
        assertFalse(approvalOnRequestScope.isApproval());
    }

    private ApprovalDto mockApprovalService(String userId, String saId, ApprovalStatus status) {
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId(userId)
            .serviceAgreementId(saId)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Service Agreements")
            .action("CREATE")
            .status(status);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
        when(approvalsService
            .getApprovalResponse(eq(userId), eq(saId), eq("Entitlements"), eq("Manage Service Agreements"),
                eq("CREATE")))
            .thenReturn(approvalResponse);
        return approval;
    }
}
