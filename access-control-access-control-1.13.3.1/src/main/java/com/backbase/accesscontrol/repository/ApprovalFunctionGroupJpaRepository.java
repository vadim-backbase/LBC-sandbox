package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalFunctionGroupJpaRepository extends JpaRepository<ApprovalFunctionGroup, Long> {

    boolean existsByNameAndServiceAgreementId(String functionGroupName, String serviceAgreementId);

    boolean existsByServiceAgreementId(String serviceAgreementId);

    Optional<List<ApprovalFunctionGroup>> findByServiceAgreementId(String serviceAgreementId);

}

