package com.backbase.accesscontrol.business.service.approvers;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ACTION_DELETE;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.business.persistence.approvals.ApproveApprovalHandler;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ManageFunctionGroupDeleteApprover implements Approver {

    private final AccessControlApprovalService accessControlApprovalService;
    private final ApproveApprovalHandler approveApprovalHandler;

    @Override
    public void manageApprove(String approvalId, String serviceAgreementId, String userId) {

        PresentationFunctionGroupApprovalDetailsItem functionGroupDetails = accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(approvalId, serviceAgreementId, userId);

        approveApprovalHandler.handleRequest(new SingleParameterHolder<>(approvalId), null);

        String functionGroupId = functionGroupDetails.getFunctionGroupId();
        if (nonNull(accessControlApprovalService.getApprovalTypeIdFromApprovals(functionGroupId))) {

            accessControlApprovalService.deleteApprovalType(functionGroupId);
        }
    }

    @Override
    public ApproverKey getKey() {
        return new ApproverKey(ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, ACTION_DELETE);
    }
}
