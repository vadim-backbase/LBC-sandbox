package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalServiceAgreementRefJpaRepository extends JpaRepository<ApprovalServiceAgreementRef, Long> {

    Optional<ApprovalServiceAgreementRef> findByApprovalId(String approvalId);

    Optional<ApprovalServiceAgreementRef> findApprovalServiceAgreementRefByServiceAgreementId(
        String serviceAgreementId);

    boolean existsByServiceAgreementId(String serviceAgreementId);

    boolean existsByServiceAgreementExternalId(String externalId);
}
