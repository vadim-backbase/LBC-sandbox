package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.SelfApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelfApprovalPolicyJpaRepository extends JpaRepository<SelfApprovalPolicy, String> {

}
