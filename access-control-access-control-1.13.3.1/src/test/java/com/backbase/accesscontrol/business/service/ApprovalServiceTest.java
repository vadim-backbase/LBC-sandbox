package com.backbase.accesscontrol.business.service;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.dbs.approval.api.client.v2.ApprovalTypeAssignmentsApi;
import com.backbase.dbs.approval.api.client.v2.ApprovalsApi;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalTypeDto;
import com.backbase.dbs.approval.api.client.v2.model.GetApprovalTypeResponse;
import com.backbase.dbs.approval.api.client.v2.model.PolicyDetailsDto;
import com.backbase.dbs.approval.api.client.v2.model.PostApprovalRecordRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsResponse;
import com.backbase.dbs.approval.api.client.v2.model.PutApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.api.client.v2.model.PutUpdateStatusRequest;
import com.backbase.dbs.approval.api.client.v2.model.RecordStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.google.common.collect.Lists;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApprovalServiceTest {

    @Mock
    private ApprovalsApi approvalsApi;

    @Mock
    private ApprovalTypeAssignmentsApi approvalTypeAssignmentsApi;

    @InjectMocks
    private ApprovalService approvalService;

    @Test
    public void testGetApprovalDetailById() {
        String approvalId = "approvalId";
        String serviceAgreementId = "serviceAgreementId";
        String userId = "userId";
        boolean enrichUsersWithFullName = false;

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .approvalTypes(singletonList(
                    new ApprovalTypeDto().id("approvalId").description("approvalDescription").name("approvalName")
                        .rank(2)))
                .policy(new PolicyDetailsDto().id("policyId").description("policyDescription").name("policyName"))
                .id(approvalId)
                .userId(userId)
                .serviceAgreementId(serviceAgreementId)
                .itemId("itemId")
                .dataAccessItemId("dataAccessItemId")
                .resource("resource")
                .function("function")
                .status(ApprovalStatus.APPROVED)
                .canApprove(true)
                .canReject(true)
            );

        when(approvalsApi
            .getApprovalById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(enrichUsersWithFullName)))
            .thenReturn(presentationGetApprovalDetailResponse);

        PresentationGetApprovalDetailResponse response = approvalService
            .getApprovalDetailById(approvalId, serviceAgreementId, userId, enrichUsersWithFullName);

        assertEquals(presentationGetApprovalDetailResponse, response);
    }

    @Test
    public void testPostApprovals() {
        String itemId = UUID.randomUUID().toString();

        PresentationPostApprovalRequest approvalRequest = new PresentationPostApprovalRequest();
        approvalRequest.setUserId("userId");
        approvalRequest.setServiceAgreementId("serviceAgreementId");
        approvalRequest.setResource("resource");
        approvalRequest.setItemId(itemId);
        approvalRequest.setFunction("function");
        approvalRequest.setAction("action");

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(new ApprovalDto().userId("userId").serviceAgreementId("serviceAgreementId").resource("resource")
                .itemId(itemId).function("function").action("action"));

        when(approvalsApi.postApproval(eq(approvalRequest)))
            .thenReturn(presentationPostApprovalResponse);

        PresentationPostApprovalResponse response = approvalService.postApprovals(approvalRequest);

        assertEquals(presentationPostApprovalResponse, response);
    }

    @Test
    public void testPutSetStatusById() {
        String approvalId = "approvalId";
        PutUpdateStatusRequest putUpdateStatusRequest = new PutUpdateStatusRequest()
            .status(ApprovalStatus.CANCELLED);

        approvalService.putSetStatusById(approvalId, putUpdateStatusRequest);

        verify(approvalsApi).putStatusByApprovalId(eq(approvalId), eq(putUpdateStatusRequest));
    }

    @Test
    public void testPostApprovalRecords() {
        String approvalId = "approvalId";
        String serviceAgreementId = "serviceAgreementId";
        String userId = "userId";

        PostApprovalRecordRequest approvalData = new PostApprovalRecordRequest()
            .serviceAgreementId(serviceAgreementId)
            .status(RecordStatus.REJECTED)
            .userId(userId);

        PresentationPostApprovalResponse presentationPostApprovalResponse = new PresentationPostApprovalResponse()
            .approval(new ApprovalDto().userId(userId).serviceAgreementId(serviceAgreementId).resource("resource")
                .function("function").action("action"));

        when(approvalsApi.postApprovalRecordByApprovalId(approvalId, false, approvalData))
            .thenReturn(presentationPostApprovalResponse);

        PresentationPostApprovalResponse response = approvalService
            .postApprovalRecords(approvalId, approvalData);

        assertEquals(presentationPostApprovalResponse, response);
    }

    @Test
    public void testPostFilterApprovals() {
        String serviceAgreementId = "serviceAgreementId";
        String userId = "userId";

        int from = 0;
        String cursor = "cursor";
        int size = 10;
        PresentationPostFilterApprovalsRequest presentationPostFilterApprovalsRequest = new PresentationPostFilterApprovalsRequest()
            .userId(userId)
            .serviceAgreementId(serviceAgreementId)
            .canReject(true)
            .canApprove(true)
            .functions(Lists.newArrayList(
                PresentationApprovalCategory.ASSIGN_PERMISSIONS.toString(),
                PresentationApprovalCategory.MANAGE_DATA_GROUPS.toString(),
                PresentationApprovalCategory.MANAGE_FUNCTION_GROUPS.toString(),
                PresentationApprovalCategory.MANAGE_LIMITS.toString(),
                PresentationApprovalCategory.MANAGE_SHADOW_LIMITS.toString(),
                PresentationApprovalCategory.MANAGE_SERVICE_AGREEMENTS.toString(),
                PresentationApprovalCategory.UNLOCK_USER.toString()));

        PresentationPostFilterApprovalsResponse presentationPostFilterApprovalsResponse = new PresentationPostFilterApprovalsResponse()
            .approvals(singletonList(
                new ApprovalDto().userId(userId).serviceAgreementId(serviceAgreementId).resource("resource")
                    .function("function").action("action")))
            .cursor(cursor);

        when(approvalsApi
            .postFilterApprovals(eq(from), eq(cursor), eq(size), eq(presentationPostFilterApprovalsRequest)))
            .thenReturn(presentationPostFilterApprovalsResponse);

        PresentationPostFilterApprovalsResponse response = approvalService
            .postFilterApprovals(from, cursor, size, presentationPostFilterApprovalsRequest);

        assertEquals(presentationPostFilterApprovalsResponse, response);
    }

    @Test
    public void testGetApprovalTypeAssignment() {
        String functionGroupId = "functionGroupId";
        String approvalTypeId = "approvalTypeId";

        GetApprovalTypeResponse getApprovalTypeResponse = new GetApprovalTypeResponse()
            .approvalType(new ApprovalTypeDto().id(approvalTypeId));

        when(approvalTypeAssignmentsApi.getAssignedApprovalTypeById(eq(functionGroupId)))
            .thenReturn(getApprovalTypeResponse);

        String response = approvalService.getApprovalTypeAssignment(functionGroupId);

        assertEquals(approvalTypeId, response);
    }

    @Test
    public void testGetApprovalTypeAssignmentThrowsExceptionWhenNullReturned() {
        String functionGroupId = "functionGroupId";

        when(approvalTypeAssignmentsApi.getAssignedApprovalTypeById(eq(functionGroupId)))
            .thenReturn(null);

        assertThrows(NullPointerException.class, () -> approvalService.getApprovalTypeAssignment(functionGroupId));
    }

    @Test
    public void testPostBulkAssignApprovalType() {
        String functionGroupId = "functionGroupId";
        String approvalTypeId = "approvalTypeId";

        approvalService.postBulkAssignApprovalType(functionGroupId, approvalTypeId);

        ArgumentCaptor<PresentationPostBulkApprovalTypeAssignmentRequest> captor = ArgumentCaptor
            .forClass(PresentationPostBulkApprovalTypeAssignmentRequest.class);

        verify(approvalTypeAssignmentsApi).postBulk(captor.capture());
        PresentationPostBulkApprovalTypeAssignmentRequest approvalTypeAssignmentRequest = captor.getValue();
        assertEquals(functionGroupId,
            approvalTypeAssignmentRequest.getApprovalTypeAssignments().get(0).getJobProfileId());
        assertEquals(approvalTypeId,
            approvalTypeAssignmentRequest.getApprovalTypeAssignments().get(0).getApprovalTypeId());
    }

    @Test
    public void testPutApprovalTypeAssignment() {
        String functionGroupId = "functionGroupId";
        String approvalTypeId = "approvalTypeId";

        approvalService.putApprovalTypeAssignment(functionGroupId, approvalTypeId);

        ArgumentCaptor<PutApprovalTypeAssignmentRequest> captor = ArgumentCaptor
            .forClass(PutApprovalTypeAssignmentRequest.class);

        verify(approvalTypeAssignmentsApi).putApprovalTypeAssignmentById(eq(functionGroupId), captor.capture());
        assertEquals(approvalTypeId, captor.getValue().getApprovalTypeId());
    }

    @Test
    public void testDeleteApprovalTypeAssignment() {
        String functionGroupId = "functionGroupId";

        approvalService.deleteApprovalTypeAssignment(functionGroupId);

        verify(approvalTypeAssignmentsApi).deleteApprovalTypeAssignmentById(eq(functionGroupId));
    }
}
