package com.backbase.accesscontrol.repository.impl;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroup_;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.domain.FunctionGroupItem_;
import com.backbase.accesscontrol.domain.FunctionGroup_;
import com.backbase.accesscontrol.domain.SelfApprovalPolicy_;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.ServiceAgreement_;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination_;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup_;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.UserContext_;
import com.backbase.accesscontrol.domain.dto.CheckDataItemsPermissions;
import com.backbase.accesscontrol.domain.dto.FunctionGroupDataGroupCombinations;
import com.backbase.accesscontrol.domain.dto.UserAssignedFunctionGroupDataGroupPermissions;
import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.repository.UserContextJpaRepositoryCustom;
import com.backbase.accesscontrol.service.TimeZoneConverterService;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import com.backbase.accesscontrol.util.DateUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class UserContextJpaRepositoryImpl implements UserContextJpaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TimeZoneConverterService timeZoneConverterService;

    @Override
    public Optional<UserContext> findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(String userId,
        String serviceAgreementId) {

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(serviceAgreementId)) {
            return Optional.empty();
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserContext> cq = criteriaBuilder.createQuery(UserContext.class);
        Root<UserContext> userContextRoot = cq.from(UserContext.class);

        Fetch<UserContext, UserAssignedFunctionGroup> userAssignedFunctionGroupFetch = userContextRoot
            .fetch(UserContext_.userAssignedFunctionGroups, JoinType.LEFT);

        userAssignedFunctionGroupFetch.fetch(UserAssignedFunctionGroup_.functionGroup, JoinType.LEFT);

        Fetch<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> assignedFunctionGroupCombinationFetch = userAssignedFunctionGroupFetch
            .fetch(UserAssignedFunctionGroup_.userAssignedFunctionGroupCombinations, JoinType.LEFT);

        assignedFunctionGroupCombinationFetch
            .fetch(UserAssignedFunctionGroupCombination_.dataGroupIds, JoinType.LEFT);

        assignedFunctionGroupCombinationFetch
            .fetch(UserAssignedFunctionGroupCombination_.selfApprovalPolicies, JoinType.LEFT)
            .fetch(SelfApprovalPolicy_.approvalPolicyBounds, JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(userContextRoot.get(UserContext_.userId), userId));
        predicates.add(criteriaBuilder.equal(userContextRoot.get(UserContext_.serviceAgreementId), serviceAgreementId));

        cq.select(userContextRoot).where(predicates.toArray(new Predicate[]{}));

        try {
            return Optional.of(entityManager.createQuery(cq)
                .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserContext> findByUserIdAndServiceAgreementIdWithFunctionAndDataGroups(String userId,
        String serviceAgreementId) {

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(serviceAgreementId)) {
            return Optional.empty();
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserContext> cq = criteriaBuilder.createQuery(UserContext.class);
        Root<UserContext> userContextRoot = cq.from(UserContext.class);

        Fetch<UserContext, UserAssignedFunctionGroup> userAssignedFunctionGroupFetch = userContextRoot
            .fetch(UserContext_.userAssignedFunctionGroups, JoinType.LEFT);

        userAssignedFunctionGroupFetch
            .fetch(UserAssignedFunctionGroup_.userAssignedFunctionGroupCombinations, JoinType.LEFT)
            .fetch(UserAssignedFunctionGroupCombination_.DATA_GROUPS, JoinType.LEFT);

        userAssignedFunctionGroupFetch.fetch(UserAssignedFunctionGroup_.functionGroup, JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(userContextRoot.get(UserContext_.userId), userId));
        predicates.add(criteriaBuilder.equal(userContextRoot.get(UserContext_.serviceAgreementId), serviceAgreementId));

        cq.select(userContextRoot).where(predicates.toArray(new Predicate[]{}));

        try {
            return Optional.of(entityManager.createQuery(cq)
                .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> findAllByUserIdAndServiceAgreementIdAndAfpIds(String userId, String saId,
        ServiceAgreementState state, Collection<String> afpIds) {

        if (CollectionUtils.isEmpty(afpIds)) {
            return Collections.emptyList();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cq = criteriaBuilder.createQuery(String.class);

        Root<UserContext> root = cq.from(UserContext.class);

        Join<UserContext, ServiceAgreement> serviceAgreementJoin = root
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);
        Join<UserContext, UserAssignedFunctionGroup> userAssignedFunctionGroupJoin = root
            .join(UserContext_.USER_ASSIGNED_FUNCTION_GROUPS, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> functionGroupJoin = userAssignedFunctionGroupJoin
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP, JoinType.INNER);
        SetJoin<FunctionGroup, FunctionGroupItem> functionGroupItemJoin = functionGroupJoin
            .join(FunctionGroup_.permissions, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get(UserContext_.userId), userId));
        predicates.add(criteriaBuilder.equal(root.get(UserContext_.serviceAgreementId), saId));
        predicates
            .add(functionGroupItemJoin.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID).in(afpIds));

        Predicate fgTimePredicate = criteriaBuilder.between(
            criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
            criteriaBuilder.coalesce(functionGroupJoin.get(FunctionGroup_.startDate), DateUtil.MIN_DATE),
            criteriaBuilder.coalesce(functionGroupJoin.get(FunctionGroup_.endDate), DateUtil.MAX_DATE)
        );
        Predicate saTimePredicate = criteriaBuilder.between(
            criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
            criteriaBuilder.coalesce(serviceAgreementJoin.get(ServiceAgreement_.startDate), DateUtil.MIN_DATE),
            criteriaBuilder.coalesce(serviceAgreementJoin.get(ServiceAgreement_.endDate), DateUtil.MAX_DATE)
        );

        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.and(saTimePredicate, fgTimePredicate),
                    criteriaBuilder
                        .equal(serviceAgreementJoin.get(ServiceAgreement_.state), state)
                ),
                criteriaBuilder.equal(functionGroupJoin.get(FunctionGroup_.TYPE), FunctionGroupType.SYSTEM)
            ));

        CriteriaQuery<String> select = cq
            .select(functionGroupItemJoin.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID))
            .where(predicates.toArray(new Predicate[]{}));

        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public List<String> findAfpIdsByUserIdAndServiceAgreementId(
        String userId, String serviceAgreementId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<String> cq = criteriaBuilder.createQuery(String.class);
        Root<UserAssignedFunctionGroup> root = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = root
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = root
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP);
        Join<UserContext, ServiceAgreement> joinServiceAgreement = joinUserContext
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(joinUserContext.get(UserContext_.USER_ID), userId));
        predicates
            .add(criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));

        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.and(criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinServiceAgreement.get(ServiceAgreement_.startDate),
                            DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinServiceAgreement.get(ServiceAgreement_.endDate), DateUtil.MAX_DATE)
                    ), criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.startDate), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.endDate), DateUtil.MAX_DATE)
                    )),
                    criteriaBuilder
                        .equal(joinServiceAgreement.get(ServiceAgreement_.STATE),
                            ServiceAgreementState.ENABLED)
                ),
                criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
            ));

        Join<FunctionGroup, FunctionGroupItem> joinItem = joinFunctionGroup.join(
            FunctionGroup_.PERMISSIONS);

        cq.select(joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID));
        CriteriaQuery<String> select = cq
            .where(predicates.toArray(new Predicate[]{}))
            .distinct(true);

        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public Set<UserAssignedFunctionGroupDataGroupPermissions> findByUserIdAndServiceAgreementIdAndAfpIdInAndDataGroupTypeIn(
        String userId, String serviceAgreementId, Collection<String> afpIds, Collection<String> dataGroupTypes) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<UserAssignedFunctionGroup> root = cq.from(UserAssignedFunctionGroup.class);

        SetJoin<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> joinUac = root
            .join(UserAssignedFunctionGroup_.userAssignedFunctionGroupCombinations, JoinType.INNER);
        SetJoin<UserAssignedFunctionGroupCombination, DataGroup> joinDataGroup = joinUac
            .join(UserAssignedFunctionGroupCombination_.dataGroups, JoinType.INNER);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = root
            .join(UserAssignedFunctionGroup_.userContext, JoinType.INNER);
        Join<UserContext, ServiceAgreement> joinServiceAgreement = joinUserContext
            .join(UserContext_.serviceAgreement, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = root
            .join(UserAssignedFunctionGroup_.functionGroup, JoinType.INNER);
        SetJoin<FunctionGroup, FunctionGroupItem> joinItem = joinFunctionGroup
            .join(FunctionGroup_.permissions, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(joinUserContext.get(UserContext_.USER_ID), userId));
        predicates
            .add(criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));

        if (CollectionUtils.isNotEmpty(afpIds)) {
            predicates.add(joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID).in(afpIds));
        }

        predicates.add(joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE).in(dataGroupTypes));

        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.and(criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinServiceAgreement.get(ServiceAgreement_.startDate),
                            DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinServiceAgreement.get(ServiceAgreement_.endDate), DateUtil.MAX_DATE)
                    ), criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.startDate), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.endDate), DateUtil.MAX_DATE)
                    )),
                    criteriaBuilder
                        .equal(joinServiceAgreement.get(ServiceAgreement_.STATE), ServiceAgreementState.ENABLED)
                ),
                criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
            ));

        cq.select(
            criteriaBuilder.tuple(
                root.get(UserAssignedFunctionGroup_.ID),
                joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID),
                joinUac.get(UserAssignedFunctionGroupCombination_.ID),
                joinDataGroup.get(DataGroup_.ID),
                joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE)))
            .where(predicates.toArray(new Predicate[]{}))
            .distinct(true);

        return getUserAssignedFunctionGroupDataGroupPermissions(entityManager.createQuery(cq).getResultList());
    }

    private Set<UserAssignedFunctionGroupDataGroupPermissions> getUserAssignedFunctionGroupDataGroupPermissions(
        List<Tuple> result) {

        Map<Long, Set<String>> uaFgPermissionsMap = result.stream()
            .collect(groupingBy(t -> t.get(0, Long.class),
                mapping(t -> t.get(1, String.class), Collectors.toSet())));

        Map<Long, Set<Long>> uaFgCombinationsMap = result.stream()
            .collect(groupingBy(t -> t.get(0, Long.class),
                mapping(t -> t.get(2, Long.class), Collectors.toSet())));

        Map<Long, Map<String, Set<String>>> combinationDgTypeMap = result.stream()
            .collect(groupingBy(t -> t.get(2, Long.class),
                groupingBy(t -> t.get(4, String.class), mapping(t -> t.get(3, String.class), toSet()))));

        return uaFgPermissionsMap.keySet().stream()
            .map(uaFgId -> new UserAssignedFunctionGroupDataGroupPermissions(uaFgId,
                uaFgPermissionsMap.get(uaFgId),
                uaFgCombinationsMap.get(uaFgId).stream()
                    .map(fgDgCombinationId -> new FunctionGroupDataGroupCombinations(fgDgCombinationId,
                        combinationDgTypeMap.get(fgDgCombinationId)))
                    .collect(Collectors.toSet())))
            .collect(Collectors.toSet());
    }

    @Override
    public List<CheckDataItemsPermissions> findDataItemsPermissions(DataItemsPermissions dataItemsPermissions,
        Set<String> appFnPrivilegeIds, String userId,
        String serviceAgreementId) {

        if (isEmpty(appFnPrivilegeIds)) {
            return new ArrayList<>();
        }
        StringBuilder selectString = new StringBuilder();
        StringBuilder havingString = new StringBuilder();
        for (int i = 0; i < dataItemsPermissions.getDataItems().size(); i++) {
            selectString.append(" dg.dataItemType = '").append(dataItemsPermissions.getDataItems().get(i).getItemType())
                .append("' AND dgi.dataItemId = '").append(dataItemsPermissions.getDataItems().get(i).getItemId())
                .append("' ");
            havingString.append(" SUM(CASE WHEN dg.dataItemType = '")
                .append(dataItemsPermissions.getDataItems().get(i).getItemType()).append("' and dgi.dataItemId = '")
                .append(dataItemsPermissions.getDataItems().get(i).getItemId()).append("' then 1 else 0 end )>0 ");
            if (i != dataItemsPermissions.getDataItems().size() - 1) {
                selectString.append("OR ");
                havingString.append("OR ");
            }
        }

        StringBuilder stringBuilder = new StringBuilder(
            "SELECT new com.backbase.accesscontrol.domain.dto.CheckDataItemsPermissions(uafgc.id, \n")
            .append("COUNT(DISTINCT dg.dataItemType), \n"
                + "SUM(CASE WHEN ")
            .append(selectString).append(
                " THEN 1 ELSE 0 END))\n "
                    + "FROM UserContext AS uc \n"
                    + "INNER JOIN uc.userAssignedFunctionGroups AS uafg \n"
                    + "INNER JOIN uafg.functionGroup AS fg \n"
                    + "INNER JOIN fg.permissions AS fgi \n"
                    + "INNER JOIN uafg.userAssignedFunctionGroupCombinations AS uafgc \n"
                    + "INNER JOIN uafgc.dataGroups AS dg \n"
                    + "INNER JOIN dg.dataGroupItems AS dgi \n"
                    + "INNER JOIN uc.serviceAgreement AS sa \n"
                    + "WHERE uc.userId = '")
            .append(userId)
            .append("' AND uc.serviceAgreementId='")
            .append(serviceAgreementId).append("' AND fgi.applicableFunctionPrivilegeId = '")
            .append(appFnPrivilegeIds.iterator().next())
            .append("' AND  (\n" + " (:currentTime)  ")
            .append(" BETWEEN COALESCE(sa.startDate, (:minDate)) AND COALESCE(sa.endDate, (:maxDate)) ")
            .append("AND (\n (:currentTime) ")
            .append(" BETWEEN COALESCE(fg.startDate, (:minDate)) AND COALESCE(fg.endDate, (:maxDate)) ")
            .append(" \n           ) \n").append("            AND sa.state= (:state)")
            .append(" \n").append("            OR fg.type=(:functionType)").append("\n")
            .append("        )")
            .append(" GROUP BY uafgc.id \n"
                + "HAVING")
            .append(havingString);

        return entityManager.createQuery(stringBuilder.toString())
            .setParameter("currentTime", timeZoneConverterService.getCurrentTime())
            .setParameter("minDate", DateUtil.MIN_DATE)
            .setParameter("maxDate", DateUtil.MAX_DATE)
            .setParameter("state", ServiceAgreementState.ENABLED)
            .setParameter("functionType", FunctionGroupType.SYSTEM).getResultList();
    }

    @Override
    public boolean checkIfPredefinedTypesAreInCombination(Long combinationId, Set<String> arrangements) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
        Root<UserAssignedFunctionGroupCombination> root = cq.from(UserAssignedFunctionGroupCombination.class);
        Join<UserAssignedFunctionGroupCombination, DataGroup> joinDataGroup = root
            .join(
                UserAssignedFunctionGroupCombination_.DATA_GROUPS, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get(UserAssignedFunctionGroupCombination_.ID), combinationId));
        predicates.add(joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE).in(arrangements));

        cq.select(criteriaBuilder.count(joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE)))
            .distinct(true)
            .where(predicates.toArray(new Predicate[0]));
        try {
            return entityManager.createQuery(cq).getSingleResult() > 0;
        } catch (NoResultException e) {
            //res is pre-initialized
            return false;
        }
    }

    @Override
    public boolean checkIfPermissionIsAssignedWithoutDataGroups(Set<String> appFnPrivilegeIds,
        String internalUserId, String serviceAgreementId) {

        if (isEmpty(appFnPrivilegeIds)) {
            return false;
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> cq = criteriaBuilder.createQuery(Tuple.class);
        Root<UserContext> root = cq.from(UserContext.class);
        Join<UserContext, UserAssignedFunctionGroup> joinUserAFP = root
            .join(UserContext_.USER_ASSIGNED_FUNCTION_GROUPS, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = joinUserAFP
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP, JoinType.INNER);
        Join<FunctionGroup, FunctionGroupItem> joinFunctionGroupItem = joinFunctionGroup
            .join(FunctionGroup_.PERMISSIONS, JoinType.INNER);
        Join<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> combinationJoin = joinUserAFP
            .join(UserAssignedFunctionGroup_.USER_ASSIGNED_FUNCTION_GROUP_COMBINATIONS, JoinType.LEFT);
        Join<UserContext, ServiceAgreement> joinServiceAgreement = root
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get(UserContext_.USER_ID), internalUserId));
        predicates.add(criteriaBuilder.equal(root.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        predicates.add(criteriaBuilder
            .equal(joinFunctionGroupItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID),
                appFnPrivilegeIds.iterator().next()));
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.and(criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinServiceAgreement.get("startDate"), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinServiceAgreement.get("endDate"), DateUtil.MAX_DATE)
                    ), criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinFunctionGroup.get("startDate"), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinFunctionGroup.get("endDate"), DateUtil.MAX_DATE)
                    )),
                    criteriaBuilder
                        .equal(joinServiceAgreement.get(ServiceAgreement_.STATE), ServiceAgreementState.ENABLED)
                ),
                criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
            ));

        cq.multiselect(criteriaBuilder.count(joinUserAFP.get(UserAssignedFunctionGroup_.ID)),
            criteriaBuilder.sum(criteriaBuilder.coalesce(
                combinationJoin.get(UserAssignedFunctionGroupCombination_.ID), 0)))
            .where(predicates.toArray(new Predicate[0]));
        try {
            Tuple res = entityManager.createQuery(cq).getSingleResult();
            return res.get(0, Long.class) > 0 && res.get(1, Long.class) == 0;
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public List<UserContextProjection> findAllUserContextsByAssignDataGroupId(String dataGroupId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserContextProjection>  cq = criteriaBuilder.createQuery(UserContextProjection.class);
        Root<UserContext> root = cq.from(UserContext.class);
        Join<UserContext, UserAssignedFunctionGroup> joinUserAFP = root
                .join(UserContext_.USER_ASSIGNED_FUNCTION_GROUPS, JoinType.INNER);
        Join<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> combinationJoin = joinUserAFP
                .join(UserAssignedFunctionGroup_.USER_ASSIGNED_FUNCTION_GROUP_COMBINATIONS, JoinType.INNER);
        Join<UserAssignedFunctionGroupCombination, DataGroup> dataGroupJoin =
                combinationJoin.join(UserAssignedFunctionGroupCombination_.dataGroups, JoinType.INNER);
        cq.multiselect(root.get("userId"), root.get("serviceAgreementId")).where(criteriaBuilder.equal(dataGroupJoin.get(DataGroup_.id), dataGroupId));
        return entityManager.createQuery(cq).getResultList().stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<UserContextProjection> findAllUserContextsByAssignFunctionGroupId(String functionGroupId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserContextProjection>  cq = criteriaBuilder.createQuery(UserContextProjection.class);
        Root<UserContext> root = cq.from(UserContext.class);
        Join<UserContext, UserAssignedFunctionGroup> joinUserAFP = root
                .join(UserContext_.USER_ASSIGNED_FUNCTION_GROUPS, JoinType.INNER);
        cq.multiselect(root.get("userId"), root.get("serviceAgreementId"))
                .where(criteriaBuilder.equal(joinUserAFP.get(UserAssignedFunctionGroup_.FUNCTION_GROUP_ID),
                        functionGroupId));
        return entityManager.createQuery(cq).getResultList().stream().distinct().collect(Collectors.toList());
    }
}
