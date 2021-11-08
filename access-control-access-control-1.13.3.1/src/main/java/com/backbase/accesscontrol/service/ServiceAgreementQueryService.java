package com.backbase.accesscontrol.service;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;

public interface ServiceAgreementQueryService {

    ServiceAgreementApprovalDetailsItem getByApprovalId(String approvalId);

}
