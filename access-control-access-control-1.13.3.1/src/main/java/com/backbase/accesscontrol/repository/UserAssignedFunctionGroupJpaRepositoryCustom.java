package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserAssignedFunctionGroupJpaRepositoryCustom {

    @SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck")
    List<UserAssignedFunctionGroup> findAllByUserIdAndServiceAgreementIdAndFgIdAndFunctionGroupTypeOrderByUserIdAndServiceAgreementId(
        String userId, String serviceAgreementId, String functionGroupId, FunctionGroupType functionGroupType,
        String graphName);

    @SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck")
    List<UserAssignedFunctionGroup> findAllByUserIdAndServiceAgreementIdAndFgIdAndFunctionGroupTypeNotOrderByUserIdAndServiceAgreementId(
        String userId, String serviceAgreementId, String functionGroupId, FunctionGroupType functionGroupType,
        String graphName);

    Map<String, Set<String>> findByServiceAgreementIdAndAfpIds(String serviceAgreementId, Collection<String> afpIds);

    Map<String, Set<String>> findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(String serviceAgreementId,
        String dataItemId, String dataItemType, Collection<String> afpIds);

    List<String> findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
        String userId,
        String serviceAgreementId,
        ServiceAgreementState state,
        Collection<String> appFnPrivilegesIds
    );

    /**
     * Return user ids by external service agreement id and function group type.
     *
     * @param externalServiceAgreementId external service agreement id
     * @param functionGroupType          function group type
     * @return list of user ids
     */
    List<String> findAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(String externalServiceAgreementId,
        FunctionGroupType functionGroupType);

    /**
     * Count of user assigned function group by service agreement id, user ids and function group type.
     *
     * @param serviceAgreementId service agreement id
     * @param userIds            collection of user ids
     * @param functionGroupType  function group type
     * @return return number of user assigned function group
     */
    long countAllByServiceAgreementIdAndUserIdInAndFunctionGroupType(String serviceAgreementId,
        Collection<String> userIds, FunctionGroupType functionGroupType);

    /**
     * Checks if there are users with assigned permissions in service agreement.
     *
     * @param serviceAgreementId - service agreement id
     * @param userIds            bulk of user ids
     * @return true/false
     */
    boolean existsByServiceAgreementIdAndUserIdIn(String serviceAgreementId,
        Collection<String> userIds);

    /**
     * Get optional of user assigned function group by user id, service agreement id and function group id.
     *
     * @param userId             user id
     * @param serviceAgreementId service agreement id
     * @param functionGroupId    function group id
     * @return optional of user assigned function group
     */
    Optional<UserAssignedFunctionGroup> findByUserIdAndServiceAgreementIdAndFunctionGroupId(String userId,
        String serviceAgreementId, String functionGroupId);
}
