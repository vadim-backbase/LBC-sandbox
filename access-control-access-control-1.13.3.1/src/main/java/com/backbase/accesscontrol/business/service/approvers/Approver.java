package com.backbase.accesscontrol.business.service.approvers;

public interface Approver {

    void manageApprove(String approvalId, String serviceAgreementId, String userId);

    ApproverKey getKey();

}
