package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAssignedCombinationRepository extends
    JpaRepository<UserAssignedFunctionGroupCombination, String>, UserAssignedCombinationRepositoryCustom {

    boolean existsByDataGroupIdsIn(Set<String> dataGroupIds);

    @Query("select distinct combination from UserAssignedFunctionGroupCombination combination "
        + "join fetch combination.userAssignedFunctionGroup uafg "
        + "left join fetch combination.dataGroups dg "
        + "left join fetch combination.selfApprovalPolicies sap "
        + "left join fetch sap.functionGroupItem item "
        + "left join fetch sap.approvalPolicyBounds bounds "
        + "where uafg.functionGroupId = :functionGroupId ")
    List<UserAssignedFunctionGroupCombination> findAllCombinationsByFunctionGroupId(
        @Param("functionGroupId") String functionGroupId
    );
}

