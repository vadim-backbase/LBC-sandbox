package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalServiceAgreementJpaRepository extends JpaRepository<ApprovalServiceAgreement, Long> {

    boolean existsByExternalId(String externalId);

    boolean existsByServiceAgreementId(String id);

    Optional<ApprovalServiceAgreement> findByServiceAgreementId(String serviceAgreementId);
}

