package com.backbase.accesscontrol.repository.impl;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.DataGroupItem_;
import com.backbase.accesscontrol.domain.DataGroup_;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.domain.FunctionGroupItem_;
import com.backbase.accesscontrol.domain.FunctionGroup_;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.ServiceAgreement_;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination_;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup_;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.UserContext_;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepositoryCustom;
import com.backbase.accesscontrol.service.TimeZoneConverterService;
import com.backbase.accesscontrol.util.DateUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import org.springframework.beans.factory.annotation.Autowired;

public class UserAssignedFunctionGroupJpaRepositoryImpl implements UserAssignedFunctionGroupJpaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TimeZoneConverterService timeZoneConverterService;

    @Override
    @SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck")
    public List<UserAssignedFunctionGroup> findAllByUserIdAndServiceAgreementIdAndFgIdAndFunctionGroupTypeOrderByUserIdAndServiceAgreementId(
        String userId, String serviceAgreementId, String functionGroupId, FunctionGroupType functionGroupType,
        String graphName) {
        return getUserAssignedFunctionGroups(userId, serviceAgreementId, functionGroupId, functionGroupType, graphName,
            true);
    }

    @Override
    @SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck")
    public List<UserAssignedFunctionGroup> findAllByUserIdAndServiceAgreementIdAndFgIdAndFunctionGroupTypeNotOrderByUserIdAndServiceAgreementId(
        String userId, String serviceAgreementId, String functionGroupId, FunctionGroupType functionGroupType,
        String graphName) {
        return getUserAssignedFunctionGroups(userId, serviceAgreementId, functionGroupId, functionGroupType, graphName,
            false);
    }

    @Override
    public Map<String, Set<String>> findByServiceAgreementIdAndAfpIds(String serviceAgreementId,
        Collection<String> afpIds) {
        return findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(serviceAgreementId, null, null, afpIds);
    }

    @Override
    public Map<String, Set<String>> findByServiceAgreementIdAndDataItemIdAndDataGroupTypeAndAfpIds(
        String serviceAgreementId, String dataItemId, String dataItemType, Collection<String> afpIds) {

        if (isEmpty(afpIds)) {
            return Collections.emptyMap();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<UserAssignedFunctionGroup> root = query.from(UserAssignedFunctionGroup.class);

        Join<UserAssignedFunctionGroup, UserContext> userContextJoin = root
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);

        Join<UserAssignedFunctionGroup, FunctionGroup> functionGroupJoin = root
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP, JoinType.INNER);

        SetJoin<FunctionGroup, FunctionGroupItem> functionGroupItemJoin = functionGroupJoin
            .join(FunctionGroup_.permissions, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(userContextJoin.get(UserContext_.serviceAgreementId), serviceAgreementId));
        predicates.add(functionGroupItemJoin.get(FunctionGroupItem_.applicableFunctionPrivilegeId).in(afpIds));

        if (Objects.nonNull(dataItemType) || Objects.nonNull(dataItemId)) {
            SetJoin<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> uacJoin = root
                .join(UserAssignedFunctionGroup_.userAssignedFunctionGroupCombinations, JoinType.INNER);

            Join<UserAssignedFunctionGroupCombination, DataGroup> dataGroupJoin = uacJoin
                .join(UserAssignedFunctionGroupCombination_.DATA_GROUPS, JoinType.INNER);
            SetJoin<DataGroup, DataGroupItem> dataItemJoin = dataGroupJoin
                .join(DataGroup_.dataGroupItems, JoinType.INNER);

            if (Objects.nonNull(dataItemType)) {
                predicates.add(criteriaBuilder.equal(dataGroupJoin.get(DataGroup_.dataItemType), dataItemType));
            }

            if (Objects.nonNull(dataItemId)) {
                predicates.add(criteriaBuilder.equal(dataItemJoin.get(DataGroupItem_.DATA_ITEM_ID), dataItemId));
            }
        }

        Join<UserContext, ServiceAgreement> serviceAgreementJoin = userContextJoin
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

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
                        .equal(serviceAgreementJoin.get(ServiceAgreement_.state), ServiceAgreementState.ENABLED)
                ),
                criteriaBuilder.equal(functionGroupJoin.get(FunctionGroup_.TYPE), FunctionGroupType.SYSTEM)
            ));

        CriteriaQuery<Tuple> select = query.select(
            criteriaBuilder.tuple(
                userContextJoin.get(UserContext_.USER_ID),
                root.get(UserAssignedFunctionGroup_.FUNCTION_GROUP_ID)))
            .where(predicates.toArray(new Predicate[]{}));

        return entityManager.createQuery(select).getResultList().stream()
            .collect(Collectors.groupingBy(tuple -> tuple.get(0, String.class),
                Collectors.mapping(tuple -> tuple.get(1, String.class), Collectors.toSet())));
    }

    @Override
    public List<String> findAfpIdsByUserIdAndServiceAgreementIdAndStateAndAfpIdsIn(
        String userId,
        String serviceAgreementId,
        ServiceAgreementState state,
        Collection<String> appFnPrivilegesIds) {

        if (isEmpty(appFnPrivilegesIds)) {
            return Collections.emptyList();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<String> cq = criteriaBuilder.createQuery(String.class);
        Root<UserAssignedFunctionGroup> root = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = root
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = root
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP);
        Join<FunctionGroup, FunctionGroupItem> joinItem = joinFunctionGroup.join(FunctionGroup_.PERMISSIONS);
        Join<UserContext, ServiceAgreement> joinServiceAgreement = joinUserContext
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(joinUserContext.get(UserContext_.USER_ID), userId));
        predicates
            .add(criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        predicates
            .add(joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID).in(appFnPrivilegesIds));

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
                        .equal(joinServiceAgreement.get(ServiceAgreement_.STATE), state)
                ),
                criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
            ));

        cq.select(joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID));
        CriteriaQuery<String> select = cq
            .where(predicates.toArray(new Predicate[]{}))
            .distinct(true);

        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public List<String> findAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(
        String externalServiceAgreementId, FunctionGroupType functionGroupType) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<String> cq = criteriaBuilder.createQuery(String.class);
        Root<UserAssignedFunctionGroup> root = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = root
            .join(UserAssignedFunctionGroup_.userContext, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = root
            .join(UserAssignedFunctionGroup_.functionGroup, JoinType.INNER);
        Join<UserContext, ServiceAgreement> joinServiceAgreement = joinUserContext
            .join(UserContext_.serviceAgreement, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
            criteriaBuilder.equal(joinServiceAgreement.get(ServiceAgreement_.EXTERNAL_ID), externalServiceAgreementId));
        predicates.add(criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.TYPE), functionGroupType));

        cq.select(joinUserContext.get(UserContext_.USER_ID));
        CriteriaQuery<String> select = cq
            .where(predicates.toArray(new Predicate[]{}))
            .distinct(true);

        return entityManager.createQuery(select).getResultList();
    }

    @Override
    public long countAllByServiceAgreementIdAndUserIdInAndFunctionGroupType(String serviceAgreementId,
        Collection<String> userIds, FunctionGroupType functionGroupType) {

        if (isEmpty(userIds)) {
            return 0;
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
        Root<UserAssignedFunctionGroup> uaFgRoot = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = uaFgRoot
            .join(UserAssignedFunctionGroup_.userContext, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = uaFgRoot
            .join(UserAssignedFunctionGroup_.functionGroup, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
            criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        predicates.add(joinUserContext.get(UserContext_.USER_ID).in(userIds));
        predicates.add(criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.TYPE), functionGroupType));

        cq.select(criteriaBuilder.countDistinct(uaFgRoot))
            .where(predicates.toArray(new Predicate[]{}));

        return entityManager.createQuery(cq).getSingleResult();
    }

    @Override
    public boolean existsByServiceAgreementIdAndUserIdIn(String serviceAgreementId,
        Collection<String> userIds) {
        if (isEmpty(userIds)) {
            return false;
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);
        Root<UserAssignedFunctionGroup> uaFgRoot = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = uaFgRoot
            .join(UserAssignedFunctionGroup_.userContext, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = uaFgRoot
            .join(UserAssignedFunctionGroup_.functionGroup, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
            criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        predicates.add(joinUserContext.get(UserContext_.USER_ID).in(userIds));
        predicates.add(criteriaBuilder.notEqual(joinFunctionGroup.get(FunctionGroup_.TYPE), FunctionGroupType.SYSTEM));

        cq.select(criteriaBuilder.countDistinct(uaFgRoot))
            .where(predicates.toArray(new Predicate[]{}));

        Long result = entityManager.createQuery(cq).getSingleResult();

        return result != 0;
    }

    @Override
    public Optional<UserAssignedFunctionGroup> findByUserIdAndServiceAgreementIdAndFunctionGroupId(String userId,
        String serviceAgreementId, String functionGroupId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<UserAssignedFunctionGroup> cq = criteriaBuilder.createQuery(UserAssignedFunctionGroup.class);
        Root<UserAssignedFunctionGroup> uaFgRoot = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = uaFgRoot
            .join(UserAssignedFunctionGroup_.userContext, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
            criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        predicates.add(criteriaBuilder.equal(joinUserContext.get(UserContext_.USER_ID), userId));
        predicates
            .add(criteriaBuilder.equal(uaFgRoot.get(UserAssignedFunctionGroup_.FUNCTION_GROUP_ID), functionGroupId));

        cq.select(uaFgRoot)
            .where(predicates.toArray(new Predicate[]{}))
            .distinct(true);

        try {
            return Optional.of(entityManager.createQuery(cq).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<UserAssignedFunctionGroup> getUserAssignedFunctionGroups(String userId, String serviceAgreementId,
        String functionGroupId, FunctionGroupType functionGroupType, String graphName, boolean equals) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserAssignedFunctionGroup> cq = criteriaBuilder.createQuery(UserAssignedFunctionGroup.class);
        Root<UserAssignedFunctionGroup> uaFgRoot = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> ucJoin = uaFgRoot
            .join(UserAssignedFunctionGroup_.userContext, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> fgJoin = uaFgRoot
            .join(UserAssignedFunctionGroup_.functionGroup, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        if (userId != null) {
            predicates.add(criteriaBuilder.equal(ucJoin.get(UserContext_.USER_ID), userId));
        }
        if (serviceAgreementId != null) {
            predicates.add(criteriaBuilder.equal(ucJoin.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        }
        if (functionGroupId != null) {
            predicates.add(
                criteriaBuilder.equal(uaFgRoot.get(UserAssignedFunctionGroup_.FUNCTION_GROUP_ID), functionGroupId));
        }
        if (functionGroupType != null) {
            predicates.add(
                getFunctionGroupTypePredicate(functionGroupType, criteriaBuilder, fgJoin.get(FunctionGroup_.TYPE),
                    equals));
        }

        cq.select(uaFgRoot)
            .where(predicates.toArray(new Predicate[]{}))
            .orderBy(
                criteriaBuilder.asc(ucJoin.get(UserContext_.USER_ID)),
                criteriaBuilder.asc(ucJoin.get(UserContext_.SERVICE_AGREEMENT_ID))
            )
            .distinct(true);

        EntityGraph<?> entityGraph = entityManager.getEntityGraph(graphName);

        return entityManager.createQuery(cq)
            .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
            .getResultList();
    }

    private Predicate getFunctionGroupTypePredicate(FunctionGroupType functionGroupType,
        CriteriaBuilder criteriaBuilder, Path<FunctionGroupType> functionGroupTypePath, boolean equals) {

        if (equals) {
            return criteriaBuilder.equal(functionGroupTypePath, functionGroupType);
        }

        return criteriaBuilder.notEqual(functionGroupTypePath, functionGroupType);
    }

}
