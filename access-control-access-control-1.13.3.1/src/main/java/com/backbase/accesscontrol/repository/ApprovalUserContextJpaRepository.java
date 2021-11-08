package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ApprovalUserContext;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApprovalUserContextJpaRepository extends JpaRepository<ApprovalUserContext, String>,
    ApprovalUserContextJpaRepositoryCustom {

    @Query("select uc from ApprovalUserContext uc "
        + " left join fetch uc.approvalUserContextAssignFunctionGroups afg "
        + " left join fetch afg.approvalSelfApprovalPolicies sap "
        + " left join fetch sap.approvalSelfApprovalPolicyBounds "
        + " where uc.userId = :userId and uc.serviceAgreementId = :serviceAgreementId")
    Optional<ApprovalUserContext> findUserContextByUserIdServiceAgreementIdWithAssignedPermissions(
        @Param("userId") String userId,
        @Param("serviceAgreementId") String serviceAgreementId
    );

    Optional<ApprovalUserContext> findByUserIdAndServiceAgreementId(String userId, String serviceAgreementId);

    Long countByServiceAgreementIdAndUserIdIn(String serviceAgreementId, Set<String> userIds);

    Boolean existsByServiceAgreementIdAndUserIdIn(String serviceAgreementId, Collection<String> userIds);

    Boolean existsByServiceAgreementId(String serviceAgreementId);

    Long countByServiceAgreementIdAndLegalEntityId(String serviceAgreementId, String legalEntityId);

}

