package com.backbase.accesscontrol.service;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;

public interface ApprovalService {

    /**
     * Gets persistence approval permissions.
     *
     * @param userId             user id
     * @param serviceAgreementId service agreement id
     * @return {@link PersistenceApprovalPermissions}
     */
    PersistenceApprovalPermissions getPersistenceApprovalPermissions(String userId, String serviceAgreementId);

    /**
     * Approve approval request.
     *
     * @param approvalId - approval id
     */
    void approveApprovalRequest(String approvalId);

    /**
     * Reject approval request.
     *
     * @param approvalId approval id
     */
    void rejectApprovalRequest(String approvalId);

}
