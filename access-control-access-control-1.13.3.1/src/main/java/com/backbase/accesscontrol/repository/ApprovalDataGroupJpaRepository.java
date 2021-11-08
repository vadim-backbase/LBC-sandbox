package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalDataGroupJpaRepository extends JpaRepository<ApprovalDataGroup, Long> {

    Boolean existsByDataGroupId(String dataGroupId);

    List<ApprovalDataGroup> findByDataGroupIdIn(Set<String> id);

    boolean existsByDataGroupIdIn(Set<String> id);

    Optional<ApprovalDataGroup> findByApprovalId(String approvalId);
}
