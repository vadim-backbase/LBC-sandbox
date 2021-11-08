package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessControlApprovalJpaRepository extends JpaRepository<AccessControlApproval, String> {

    Optional<AccessControlApproval> findByApprovalId(String approvalId);

}
