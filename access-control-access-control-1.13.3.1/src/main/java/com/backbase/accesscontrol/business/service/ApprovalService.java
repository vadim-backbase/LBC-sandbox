package com.backbase.accesscontrol.business.service;

import static java.util.Collections.singletonList;

import com.backbase.dbs.approval.api.client.v2.ApprovalTypeAssignmentsApi;
import com.backbase.dbs.approval.api.client.v2.ApprovalsApi;
import com.backbase.dbs.approval.api.client.v2.model.GetApprovalTypeResponse;
import com.backbase.dbs.approval.api.client.v2.model.PostApprovalRecordRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalTypeAssignmentDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostBulkApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsResponse;
import com.backbase.dbs.approval.api.client.v2.model.PutApprovalTypeAssignmentRequest;
import com.backbase.dbs.approval.api.client.v2.model.PutUpdateStatusRequest;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ApprovalService {

    private ApprovalsApi approvalsApi;
    private ApprovalTypeAssignmentsApi approvalTypeAssignmentsApi;

    public PresentationGetApprovalDetailResponse getApprovalDetailById(String approvalId, String serviceAgreementId,
        String userId, Boolean enrichUsersWithFullName) {
        return approvalsApi
            .getApprovalById(approvalId, serviceAgreementId, userId, enrichUsersWithFullName);
    }

    public PresentationPostApprovalResponse postApprovals(PresentationPostApprovalRequest approvalRequest) {
        return approvalsApi.postApproval(approvalRequest);
    }

    public void putSetStatusById(String approvalId, PutUpdateStatusRequest putUpdateStatusRequest) {
        approvalsApi.putStatusByApprovalId(approvalId, putUpdateStatusRequest);
    }

    public PresentationPostApprovalResponse postApprovalRecords(String approvalId,
        PostApprovalRecordRequest approvalData) {

        return approvalsApi.postApprovalRecordByApprovalId(approvalId, false, approvalData);
    }

    public PresentationPostFilterApprovalsResponse postFilterApprovals(Integer from, String cursor, Integer size,
        PresentationPostFilterApprovalsRequest presentationPostFilterApprovalsRequest) {
        return approvalsApi.postFilterApprovals(from, cursor, size, presentationPostFilterApprovalsRequest);
    }

    public String getApprovalTypeAssignment(String functionGroupId) {
        GetApprovalTypeResponse assignedApprovalTypeById = approvalTypeAssignmentsApi
            .getAssignedApprovalTypeById(functionGroupId);

        return Objects.requireNonNull(assignedApprovalTypeById).getApprovalType().getId();
    }

    public void postBulkAssignApprovalType(String functionGroupId, String newApprovalType){
        PresentationPostBulkApprovalTypeAssignmentRequest request = new PresentationPostBulkApprovalTypeAssignmentRequest()
            .approvalTypeAssignments(singletonList(
                new PresentationApprovalTypeAssignmentDto()
                .jobProfileId(functionGroupId)
                .approvalTypeId(newApprovalType)));

        approvalTypeAssignmentsApi.postBulk(request);
    }

    public void putApprovalTypeAssignment(String functionGroupId, String newApprovalType) {
        approvalTypeAssignmentsApi.putApprovalTypeAssignmentById(functionGroupId,
            new PutApprovalTypeAssignmentRequest().approvalTypeId(newApprovalType));
    }

    public void deleteApprovalTypeAssignment(String functionGroupId) {
        approvalTypeAssignmentsApi.deleteApprovalTypeAssignmentById(functionGroupId);
    }
}
