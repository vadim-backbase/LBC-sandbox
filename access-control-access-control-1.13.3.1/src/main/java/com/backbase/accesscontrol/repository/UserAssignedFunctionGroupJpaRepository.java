package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAssignedFunctionGroupJpaRepository extends JpaRepository<UserAssignedFunctionGroup, Long>,
    UserAssignedFunctionGroupJpaRepositoryCustom {

    List<UserAssignedFunctionGroup> findAllByFunctionGroupId(String functionGroupId);
   boolean existsByUserContextId(Long userContextId);

    @Query("select distinct uafg from UserAssignedFunctionGroup uafg "
        + "join uafg.userContext uc "
        + "join uafg.functionGroup fg "
        + "left join fetch uafg.userAssignedFunctionGroupCombinations uafgc "
        + "left join fetch uafgc.dataGroupIds dgIds "
        + "left join fetch uafgc.selfApprovalPolicies sap "
        + "left join fetch sap.approvalPolicyBounds apb "
        + "where uc.serviceAgreementId = :serviceAgreementId and uc.userId = :userId and fg.type in (:functionGroupTypes)")
    List<UserAssignedFunctionGroup> findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(
        @Param("userId") String userId,
        @Param("serviceAgreementId") String serviceAgreementId,
        @Param("functionGroupTypes") List<FunctionGroupType> functionGroupTypes
    );
}
