package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.UserContext;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserContextJpaRepository extends JpaRepository<UserContext, Long>, UserContextJpaRepositoryCustom {

    Optional<UserContext> findByUserIdAndServiceAgreementId(String userId, String serviceAgreementId);

    @Query("select uc.userId from UserAssignedFunctionGroup uafg "
        + " join uafg.userContext uc "
        + " where uc.serviceAgreementId = :serviceAgreementId and uafg.functionGroupId = :functionGroupId ")
    Page<String> findUserIdsByServiceAgreementIdAndFunctionGroupId(
        @Param("serviceAgreementId") String serviceAgreementId,
        @Param("functionGroupId") String functionGroupId,
        Pageable pageable
    );
}
