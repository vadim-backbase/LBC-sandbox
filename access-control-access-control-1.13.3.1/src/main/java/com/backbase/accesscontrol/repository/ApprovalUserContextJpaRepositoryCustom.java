package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalUserContext;
import java.util.Optional;

public interface ApprovalUserContextJpaRepositoryCustom {

    Optional<ApprovalUserContext> findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(String approvalId);
}
