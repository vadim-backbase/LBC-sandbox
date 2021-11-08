package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.accesscontrol.domain.dto.CheckDataItemsPermissions;
import com.backbase.accesscontrol.domain.dto.UserAssignedFunctionGroupDataGroupPermissions;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserContextJpaRepositoryCustom {

    Optional<UserContext> findByUserIdAndServiceAgreementIdWithFunctionAndDataGroups(String userId,
        String serviceAgreementId);

    Optional<UserContext> findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(String userId,
        String serviceAgreementId);

    List<String> findAllByUserIdAndServiceAgreementIdAndAfpIds(String userId, String saId,
        ServiceAgreementState state, Collection<String> appFnPrivilegesIds);

    List<String> findAfpIdsByUserIdAndServiceAgreementId(
        String userId,
        String serviceAgreementId
    );

    List<UserContextProjection> findAllUserContextsByAssignDataGroupId(String dataGroupId);

    List<UserContextProjection> findAllUserContextsByAssignFunctionGroupId(String functionGroupId);

    Set<UserAssignedFunctionGroupDataGroupPermissions> findByUserIdAndServiceAgreementIdAndAfpIdInAndDataGroupTypeIn(
        String userId, String serviceAgreementId, Collection<String> afpIds, Collection<String> dataGroupTypes);

    /**
     * jira story for more info: https://backbase.atlassian.net/browse/ENTI-9516
     * The query checks how many distinct types are in the combination and the sum of types and dataitems
     * for requested dataItems.
     * Example return:
     * 15 4 3
     * explanation: combination with id 15 has 4 types in combination, the sum from types and dataitems
     * (from dataItemsPermissions) shows that only 3 are part of the combination.
     *
     * returns combination id, distinct types per combination, sum of dataitems and types per combination (check sum)
     *
     */
    List<CheckDataItemsPermissions> findDataItemsPermissions(
        DataItemsPermissions dataItemsPermissions, Set<String> appFnPrivilegeIds,
        String internalUserId, String serviceAgreementId);

    boolean checkIfPredefinedTypesAreInCombination(Long combinationId, Set<String> arrangements);

    boolean checkIfPermissionIsAssignedWithoutDataGroups(Set<String> appFnPrivilegeIds, String internalUserId,
        String serviceAgreementId);
}
