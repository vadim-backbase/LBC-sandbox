package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalUserContextAssignFunctionGroupJpaRepository extends
    JpaRepository<ApprovalUserContextAssignFunctionGroup, String> {

    boolean existsByFunctionGroupId(String functionGroupId);

    boolean existsByDataGroups(String dataGroupId);
}
