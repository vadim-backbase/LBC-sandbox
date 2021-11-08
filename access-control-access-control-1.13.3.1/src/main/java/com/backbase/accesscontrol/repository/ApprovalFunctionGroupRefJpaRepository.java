package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalFunctionGroupRefJpaRepository extends JpaRepository<ApprovalFunctionGroupRef, Long> {

    Optional<List<ApprovalFunctionGroupRef>> findByFunctionGroupIdIn(Collection<String> id);

    Optional<ApprovalFunctionGroupRef> findByFunctionGroupId(String id);

    boolean existsByFunctionGroupId(String id);

    boolean existsByFunctionGroupIdIn(Collection<String> ids);

    Optional<ApprovalFunctionGroupRef> findByApprovalId(String approvalId);

}
