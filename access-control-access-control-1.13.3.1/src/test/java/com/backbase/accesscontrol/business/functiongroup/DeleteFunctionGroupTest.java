package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteFunctionGroupTest {

    @Mock
    private FunctionGroupPAndPService functionGroupPAndPService;

    private DeleteFunctionGroup deleteFunctionGroup;
    @Mock
    private ApprovalsService approvalsService;
    @Mock
    private ApprovalOnRequestScope approvalOnRequestScope;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Mock
    private AccessControlApprovalService accessControlApprovalService;

    @Before
    public void setUp() throws Exception {
        deleteFunctionGroup = new DeleteFunctionGroup(
            functionGroupPAndPService,
            approvalsService, accessControlApprovalService, approvalOnRequestScope,
            userContextUtil, applicationProperties);
    }

    @Test
    public void shouldDeleteFunctionGroup() {
        String fgId = "123";
        InternalRequest request = new InternalRequest<>();
        mockDeleteFunctionGroupById(fgId);

        mockApprovalValidation(applicationProperties, false);
        deleteFunctionGroup.deleteFunctionGroup(request, fgId);
        verify(functionGroupPAndPService, times(1)).deleteFunctionGroup(fgId);
    }

    @Test
    public void shouldCreatePendingDeleteFunctionGroup() {
        String serviceAgreementId = "ServiceAgreement";
        String userId = "userId";
        String approvalId = "approvalId";

        mockApprovalValidation(applicationProperties, true);

        String fgId = "123";
        InternalRequest request = new InternalRequest<>();

        mockApprovalService(ApprovalStatus.PENDING, approvalId);
        mockGetInternalUserId(userId);
        mockUserContext(serviceAgreementId);

        deleteFunctionGroup.deleteFunctionGroup(request, fgId);
        verify(functionGroupPAndPService, times(0)).deleteFunctionGroup(fgId);
        verify(functionGroupPAndPService, times(1)).deleteFunctionGroup(fgId, approvalId);
    }

    @Test
    public void shouldDeleteFunctionGroupZeroPolicy() {
        String serviceAgreementId = "ServiceAgreement";
        String userId = "userId";
        String approvalId = "approvalId";

        mockApprovalValidation(applicationProperties, true);

        String fgId = "123";
        InternalRequest request = new InternalRequest<>();

        mockApprovalService(ApprovalStatus.APPROVED, approvalId);
        mockGetInternalUserId(userId);
        mockUserContext(serviceAgreementId);

        deleteFunctionGroup.deleteFunctionGroup(request, fgId);
        verify(functionGroupPAndPService, times(1)).deleteFunctionGroup(fgId);
        verify(functionGroupPAndPService, times(0)).deleteFunctionGroup(any(String.class), any(String.class));
    }

    @Test
    public void shouldDeleteFunctionGroupZeroPolicyWithDeleteApprovalTypeId() {
        String serviceAgreementId = "ServiceAgreement";
        String userId = "userId";
        String approvalId = "approvalId";
        String oldApprovalTypeId = "type id";

        mockApprovalValidation(applicationProperties, true);

        String fgId = "123";
        InternalRequest request = new InternalRequest<>();

        mockApprovalService(ApprovalStatus.APPROVED, approvalId);
        mockGetInternalUserId(userId);
        mockUserContext(serviceAgreementId);
        when(accessControlApprovalService.getApprovalTypeIdFromApprovals(fgId))
            .thenReturn(oldApprovalTypeId);
        deleteFunctionGroup.deleteFunctionGroup(request, fgId);
        verify(functionGroupPAndPService, times(1)).deleteFunctionGroup(fgId);
        verify(functionGroupPAndPService, times(0)).deleteFunctionGroup(any(String.class), any(String.class));
        verify(accessControlApprovalService, times(1))
            .deleteApprovalType(eq(fgId));
    }

    @Test
    public void shouldThrowBadRequestExceptionErrorFromPandp() {
        String serviceAgreementId = "ServiceAgreement";
        String userId = "userId";
        String approvalId = "approvalId";

        mockApprovalValidation(applicationProperties, true);

        String fgId = "123";
        InternalRequest request = new InternalRequest<>();

        mockApprovalService(ApprovalStatus.PENDING, approvalId);
        mockGetInternalUserId(userId);
        mockUserContext(serviceAgreementId);

        doThrow(getBadRequestException(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode()))
            .when(functionGroupPAndPService)
            .deleteFunctionGroup(any(), any());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> deleteFunctionGroup.deleteFunctionGroup(request, fgId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())));
    }


    private void mockDeleteFunctionGroupById(String fgId) {
        doNothing().when(functionGroupPAndPService).deleteFunctionGroup(eq(fgId));
    }

    private void mockUserContext(String serviceAgreementId) {
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);
    }

    private void mockGetInternalUserId(String userId) {
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "le"));
    }

    private void mockApprovalService(ApprovalStatus status, String approvalId) {
        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId("ServiceAgreement")
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("DELETE")
            .status(status);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
        when(approvalsService.getApprovalResponse(any(), any(), any(), any(), any()))
            .thenReturn(approvalResponse);
    }

}
