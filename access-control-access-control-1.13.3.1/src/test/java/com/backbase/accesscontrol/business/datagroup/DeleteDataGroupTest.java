package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
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
public class DeleteDataGroupTest {

    @Mock
    private DataGroupPAndPService dataGroupPAndPService;
    @Mock
    private ApprovalsService approvalsService;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;

    private DeleteDataGroup deleteDataGroup;

    @Before
    public void setUp() {
        deleteDataGroup = new DeleteDataGroup(dataGroupPAndPService, approvalsService,
            userContextUtil, applicationProperties);
    }

    @Test
    public void shouldDeleteDataGroupWhenApprovalOff() {
        mockApprovalValidation(applicationProperties, false);

        String daId = "123";
        InternalRequest request = new InternalRequest<>();
        mockDeleteDataGroupById(daId);
        deleteDataGroup.deleteDataGroup(request, daId);
        verify(dataGroupPAndPService, times(1)).deleteDataGroup(daId);
    }

    @Test
    public void shouldCallDataGroupPandPServiceWhenApprovalOn() {
        mockApprovalValidation(applicationProperties, true);

        String approvalId = "approvalId";

        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("DELETE");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq("user"), eq("saId"), eq("Entitlements"), eq("Manage Data Groups"), eq("DELETE")))
            .thenReturn(approvalResponse);
        doNothing().when(dataGroupPAndPService).deleteDataGroupWithApproval(eq("dg_id"), eq(approvalId));
        InternalRequest<Void> request = getInternalRequest(null);
        deleteDataGroup.deleteDataGroup(request, "dg_id").getData();

        verify(dataGroupPAndPService, times(1)).deleteDataGroupWithApproval(eq("dg_id"), eq(approvalId));
    }

    @Test
    public void shouldCallDataGroupPandPServiceWhenApprovalOnAndGetBadRequestToCancelApproval() {
        mockApprovalValidation(applicationProperties, true);

        String dataGroupId = "dg-id";

        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("DELETE");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        doThrow(getBadRequestException("error", "error"))
            .when(dataGroupPAndPService)
            .deleteDataGroupWithApproval(eq(dataGroupId), eq("approvalId"));
        doNothing().when(approvalsService).cancelApprovalRequest(eq("approvalId"));

        InternalRequest<Void> request = getInternalRequest(null);

        assertThrows(BadRequestException.class,
            () -> deleteDataGroup.deleteDataGroup(request, dataGroupId).getData());

        verify(approvalsService, times(1)).cancelApprovalRequest(eq("approvalId"));
    }

    @Test
    public void shouldCallDataGroupPAndPServiceWithZeroApproval() {
        mockApprovalValidation(applicationProperties, true);

        String approvalId = "approvalId";
        String dataGroupId = "dg-id";

        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("DELETE");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        InternalRequest<Void> request = getInternalRequest(null);

        deleteDataGroup.deleteDataGroup(request, dataGroupId).getData();
        verify(dataGroupPAndPService, times(1)).deleteDataGroup(eq(dataGroupId));

        verifyNoMoreInteractions(dataGroupPAndPService);
    }

    private void mockDeleteDataGroupById(String dgId) {
        doNothing().when(dataGroupPAndPService).deleteDataGroup(eq(dgId));
    }
}