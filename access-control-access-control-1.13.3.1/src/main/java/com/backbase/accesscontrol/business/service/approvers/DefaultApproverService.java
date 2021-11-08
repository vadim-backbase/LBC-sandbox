package com.backbase.accesscontrol.business.service.approvers;

import com.backbase.accesscontrol.business.persistence.approvals.ApproveApprovalHandler;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DefaultApproverService implements Approver {

    private final ApproveApprovalHandler approveApprovalHandler;

    @Override
    public void manageApprove(String approvalId, String serviceAgreementId, String userId) {
        approveApprovalHandler.handleRequest(new SingleParameterHolder<>(approvalId), null);
    }

    @Override
    public ApproverKey getKey() {
        return null;
    }
}
