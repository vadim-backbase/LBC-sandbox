package com.backbase.accesscontrol.repository.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContext_;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.DataGroupItem_;
import com.backbase.accesscontrol.domain.DataGroup_;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.domain.FunctionGroupItem_;
import com.backbase.accesscontrol.domain.FunctionGroup_;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.PermissionSetsInServiceAgreements;
import com.backbase.accesscontrol.domain.PermissionSetsInServiceAgreements_;
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
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaCustomRepository;
import com.backbase.accesscontrol.service.TimeZoneConverterService;
import com.backbase.accesscontrol.util.DateUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.SingularAttribute;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * ServiceAgreementJpaCustomRepository implementation.
 */
public class ServiceAgreementJpaRepositoryImpl implements ServiceAgreementJpaCustomRepository {

    private static final String IS_MASTER_FILED_NAME = "isMaster";
    private static final String CREATOR_LEGAL_ENTITY_FILED_NAME = "creatorLegalEntity";
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TimeZoneConverterService timeZoneConverterService;

    @Override
    public Page<ServiceAgreement> findAllServiceAgreementsByParameters(String name, String creatorId,
        SearchAndPaginationParameters searchAndPaginationParameters, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);
        Root<ServiceAgreement> serviceAgreementRoot = cq.from(ServiceAgreement.class);
        List<Predicate> predicates = new ArrayList<>();
        PageImpl<ServiceAgreement> serviceAgreements;

        if (name != null) {
            predicates.add(criteriaBuilder.equal(serviceAgreementRoot.get("name"), name));
        }
        if (creatorId != null) {
            predicates.add(criteriaBuilder.equal(serviceAgreementRoot.get(IS_MASTER_FILED_NAME), false));
            predicates.add(criteriaBuilder.equal(serviceAgreementRoot
                .get(CREATOR_LEGAL_ENTITY_FILED_NAME).get("id"), creatorId));
        }
        if (searchAndPaginationParameters.getQuery() != null) {
            predicates.add(
                searchByNameOrDescription(searchAndPaginationParameters.getQuery(), criteriaBuilder,
                    serviceAgreementRoot.get("name"), serviceAgreementRoot.get("description")));
        }
        cq.select(serviceAgreementRoot)
            .distinct(true)
            .where(predicates.toArray(new Predicate[]{}))
            .orderBy(criteriaBuilder.asc(serviceAgreementRoot.get("name")));

        TypedQuery<ServiceAgreement> query = entityManager.createQuery(cq);

        Integer from = searchAndPaginationParameters.getFrom();
        Integer size = searchAndPaginationParameters.getSize();
        if (from != null && size != null) {
            query.setFirstResult(from * size);
            query.setMaxResults(size);
            if (entityGraphName != null) {
                serviceAgreements = new PageImpl<>(getServiceAgreementsByIds(query.getResultList()
                        .stream().map(ServiceAgreement::getId)
                        .collect(Collectors.toSet()),
                    ServiceAgreement_.name, entityGraphName), Pageable.unpaged(),
                    getTotalNumberOfElements(criteriaBuilder, predicates));
            } else {
                serviceAgreements = new PageImpl<>(query.getResultList(), Pageable.unpaged(),
                    getTotalNumberOfElements(criteriaBuilder, predicates));
            }
        } else {
            if (entityGraphName != null) {
                EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
                query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
            }
            List<ServiceAgreement> resultList = query.getResultList();
            serviceAgreements = new PageImpl<>(resultList, Pageable.unpaged(), resultList.size());
        }
        return serviceAgreements;
    }

    @Override
    public Optional<ServiceAgreement> findById(String id, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);
        Root<ServiceAgreement> serviceAgreementRoot = cq.from(ServiceAgreement.class);

        cq.select(serviceAgreementRoot)
            .where(criteriaBuilder.equal(serviceAgreementRoot.get("id"), id));

        TypedQuery<ServiceAgreement> query = entityManager.createQuery(cq);
        if (entityGraphName != null) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }
        return getServiceAgreement(query);
    }

    @Override
    public Page<ServiceAgreement> findByCreatorIdInHierarchyAndParameters(String creatorId,
        UserParameters userParameters, SearchAndPaginationParameters searchAndPaginationParameters,
        String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        TypedQuery<ServiceAgreement> query = getServiceAgreementTypedQuery(criteriaBuilder,
            searchAndPaginationParameters, creatorId, userParameters);

        Integer from = searchAndPaginationParameters.getFrom();
        Integer size = searchAndPaginationParameters.getSize();

        PageImpl<ServiceAgreement> serviceAgreements;
        if (from != null && size != null) {
            Long totalSubServiceAgreementRecords = getTotalSubServiceAgreementRecords(criteriaBuilder,
                searchAndPaginationParameters, creatorId, userParameters);
            query.setFirstResult(from * size);
            query.setMaxResults(size);

            serviceAgreements = new PageImpl<>(
                getServiceAgreementsByIds(query.getResultList().stream()
                    .map(ServiceAgreement::getId).collect(Collectors.toSet()), ServiceAgreement_.name, entityGraphName),
                Pageable.unpaged(),
                totalSubServiceAgreementRecords);
        } else {
            List<ServiceAgreement> resultList = query.getResultList();
            serviceAgreements = new PageImpl<>(resultList, Pageable.unpaged(), resultList.size());
        }
        return serviceAgreements;
    }

    @Override
    public Optional<ServiceAgreement> findByCreatorLegalEntityIdAndIsMaster(String legalEntityId, boolean isMaster,
        String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);
        Root<ServiceAgreement> serviceAgreementRoot = cq.from(ServiceAgreement.class);

        cq.select(serviceAgreementRoot)
            .where(criteriaBuilder.equal(serviceAgreementRoot
                    .get(CREATOR_LEGAL_ENTITY_FILED_NAME).get("id"), legalEntityId),
                criteriaBuilder.equal(serviceAgreementRoot.get(IS_MASTER_FILED_NAME), isMaster)
            ).distinct(true);

        TypedQuery<ServiceAgreement> query = entityManager
            .createQuery(cq);
        if (entityGraphName != null) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }

        return getServiceAgreement(query);
    }

    @Override
    public Page<ServiceAgreement> findServiceAgreementsWhereUserHasPermissions(String userId, String query,
        Pageable pageable) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);
        CriteriaQuery<Long> cqCount = criteriaBuilder.createQuery(Long.class);

        Root<UserAssignedFunctionGroup> countRoot = cqCount
            .from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> ucJoinCount = countRoot
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserContext, ServiceAgreement> saJoinCount = ucJoinCount
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> fgJoinCount = countRoot
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP, JoinType.INNER);

        cqCount
            .select(criteriaBuilder.countDistinct(ucJoinCount.get(UserContext_.serviceAgreement)))
            .where(criteriaBuilder.and(getPredicatesForServiceAgreementsForUserContext(userId, query, criteriaBuilder,
                ucJoinCount, saJoinCount, fgJoinCount).toArray(new Predicate[]{})));

        long count = entityManager.createQuery(cqCount).getSingleResult();

        Root<UserAssignedFunctionGroup> root = cq
            .from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> ucJoin = root
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserContext, ServiceAgreement> saJoin = ucJoin
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> fgJoin = root
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP, JoinType.INNER);

        cq.select(ucJoin.get(UserContext_.serviceAgreement))
            .distinct(true)
            .where(criteriaBuilder.and(getPredicatesForServiceAgreementsForUserContext(userId, query, criteriaBuilder,
                ucJoin, saJoin, fgJoin).toArray(new Predicate[]{})));

        List<ServiceAgreement> result = entityManager.createQuery(cq)
            .setFirstResult(Math.toIntExact(pageable.getOffset()))
            .setMaxResults(pageable.getPageSize()).getResultList();

        return new PageImpl<>(result, pageable, count);
    }

    @Override
    public boolean existContextForUserIdAndServiceAgreementId(String userId, String serviceAgreementId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);

        Root<UserAssignedFunctionGroup> from = query
            .from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> ucJoin = from
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserContext, ServiceAgreement> saJoin = ucJoin
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

        List<Predicate> conditions = new ArrayList<>();

        conditions.add(criteriaBuilder.equal(ucJoin.get(UserContext_.userId), userId));
        conditions.add(criteriaBuilder.equal(ucJoin.get(UserContext_.serviceAgreementId), serviceAgreementId));

        Join<UserAssignedFunctionGroup, FunctionGroup> fgJoin = from
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP, JoinType.INNER);
        Join<FunctionGroup, FunctionGroupItem> fgiJoin = fgJoin.join(FunctionGroup_.PERMISSIONS, JoinType.INNER);

        Predicate fgTimePredicate = criteriaBuilder.between(
            criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
            criteriaBuilder.coalesce(fgJoin.get(FunctionGroup_.startDate), DateUtil.MIN_DATE),
            criteriaBuilder.coalesce(fgJoin.get(FunctionGroup_.endDate), DateUtil.MAX_DATE)
        );

        Predicate saTimePredicate = criteriaBuilder.between(
            criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
            criteriaBuilder.coalesce(saJoin.get(ServiceAgreement_.startDate), DateUtil.MIN_DATE),
            criteriaBuilder.coalesce(saJoin.get(ServiceAgreement_.endDate), DateUtil.MAX_DATE)
        );

        conditions.add(
            criteriaBuilder.and(
                criteriaBuilder.and(saTimePredicate, fgTimePredicate),
                criteriaBuilder.or(
                    criteriaBuilder.equal(saJoin.get(ServiceAgreement_.state), ServiceAgreementState.ENABLED),
                    criteriaBuilder.equal(fgJoin.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
                )
            )
        );

        query
            .select(fgiJoin.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID))
            .where(criteriaBuilder.and(conditions.toArray(new Predicate[]{})));

        Optional<String> res = Optional.empty();

        try {
            res = Optional.of(entityManager.createQuery(query).setMaxResults(1).getSingleResult());
        } catch (NoResultException e) {
            //res is pre-initialized
        }

        return res.isPresent();
    }

    @Override
    public Optional<ServiceAgreement> findByExternalId(String externalId, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);

        Root<ServiceAgreement> msa = cq.from(ServiceAgreement.class);
        cq.select(msa);
        Predicate predicate = criteriaBuilder.equal(msa.get(ServiceAgreement_.externalId), externalId);
        cq.where(predicate).distinct(true);

        TypedQuery<ServiceAgreement> query = entityManager.createQuery(cq);
        if (entityGraphName != null) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }
        Optional<ServiceAgreement> result;
        try {
            result = Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            result = Optional.empty();
        }
        return result;
    }

    @Override
    public Boolean checkIfExistsUsersWithAssignedPermissionsInServiceAgreement(
        ServiceAgreement serviceAgreement) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);

        Root<UserAssignedFunctionGroup> from = cq
            .from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserContext> ucJoin = from
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserContext, ServiceAgreement> saJoin = ucJoin
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder
            .equal(saJoin.get(ServiceAgreement_.id),
                serviceAgreement.getId()));
        cq.select(criteriaBuilder.count(from))
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        return entityManager.createQuery(cq).getSingleResult() != 0;
    }

    @Override
    public Boolean checkIsExistsUsersWithPendingPermissionsInServiceAgreement(
        String serviceAgreementId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = criteriaBuilder.createQuery(Long.class);

        Root<ServiceAgreement> root = cq.from(ServiceAgreement.class);
        Root<ApprovalUserContext> rootApprovalUserContext = cq.from(ApprovalUserContext.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root
            .get(ServiceAgreement_.id), serviceAgreementId));

        predicates.add(criteriaBuilder.equal(
            root.get(ServiceAgreement_.id),
            rootApprovalUserContext.get(ApprovalUserContext_.serviceAgreementId)
        ));

        cq
            .select(criteriaBuilder.countDistinct(root))
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        return entityManager.createQuery(cq).getSingleResult() != 0;
    }

    @Override
    public Page<ServiceAgreement> getServiceAgreementByPermissionSetId(
        AssignablePermissionSet assignablePermissionSet,
        SearchAndPaginationParameters searchAndPaginationParameters) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);

        Root<ServiceAgreement> serviceAgreement = cq.from(ServiceAgreement.class);
        Root<PermissionSetsInServiceAgreements> permissionSet = cq.from(PermissionSetsInServiceAgreements.class);

        createGetByPermissionSetPredicates(assignablePermissionSet, criteriaBuilder, cq, serviceAgreement,
            permissionSet);
        cq.orderBy(criteriaBuilder.asc(serviceAgreement.get(ServiceAgreement_.name)));

        Integer from = searchAndPaginationParameters.getFrom();
        Integer size = searchAndPaginationParameters.getSize();

        cq.select(serviceAgreement).distinct(true);
        List<ServiceAgreement> serviceAgreementList = entityManager
            .createQuery(cq)
            .setFirstResult(from * size)
            .setMaxResults(size)
            .getResultList();

        Long totalElements = getServiceAgreementByPermissionSetCount(assignablePermissionSet, criteriaBuilder);

        return new PageImpl<>(getServiceAgreementsByIds(serviceAgreementList.stream()
                .map(ServiceAgreement::getId)
                .collect(Collectors.toSet()),
            ServiceAgreement_.name, SERVICE_AGREEMENT_WITH_PERMISSION_SETS), Pageable.unpaged(), totalElements);

    }

    @Override
    public List<ServiceAgreement> getServiceAgreementsByIds(Collection<String> ids,
        SingularAttribute<ServiceAgreement, String> orderedBy,
        String entityGraphName) {

        if (ids.isEmpty()) {
            return emptyList();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);
        Root<ServiceAgreement> serviceAgreement = cq.from(ServiceAgreement.class);

        if (nonNull(orderedBy)) {
            cq.orderBy(criteriaBuilder.asc(serviceAgreement.get(orderedBy)));
        }
        cq.where(serviceAgreement.get(ServiceAgreement_.ID).in(ids));
        TypedQuery<ServiceAgreement> query = entityManager.createQuery(cq);
        if (nonNull(entityGraphName)) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }

        return query.getResultList();
    }

    @Override
    public List<Tuple> findByUserIdAndDataGroupTypeAndAfpIdsIn(String userId, String dataGroupType,
        Collection<String> afpIds) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<UserAssignedFunctionGroup> root = cq.from(UserAssignedFunctionGroup.class);

        SetJoin<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> joinUac = root
            .join(UserAssignedFunctionGroup_.userAssignedFunctionGroupCombinations, JoinType.INNER);
        Join<UserAssignedFunctionGroupCombination, DataGroup> joinDataGroup = joinUac
            .join(UserAssignedFunctionGroupCombination_.DATA_GROUPS, JoinType.INNER);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = root
            .join(UserAssignedFunctionGroup_.userContext, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = root
            .join(UserAssignedFunctionGroup_.functionGroup, JoinType.INNER);
        SetJoin<FunctionGroup, FunctionGroupItem> joinItem = joinFunctionGroup
            .join(FunctionGroup_.permissions, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(joinUserContext.get(UserContext_.USER_ID), userId));

        predicates.add(criteriaBuilder.equal(joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE), dataGroupType));

        if (CollectionUtils.isNotEmpty(afpIds)) {
            predicates.add(joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID).in(afpIds));
        }

        Join<UserContext, ServiceAgreement> joinServiceAgreement = joinUserContext
            .join(UserContext_.serviceAgreement, JoinType.INNER);

        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.and(criteriaBuilder.between(
                        criteriaBuilder.currentTimestamp(),
                        criteriaBuilder
                            .coalesce(joinServiceAgreement.get(ServiceAgreement_.START_DATE), DateUtil.MIN_DATE),
                        criteriaBuilder
                            .coalesce(joinServiceAgreement.get(ServiceAgreement_.END_DATE), DateUtil.MAX_DATE)
                    ), criteriaBuilder.between(
                        criteriaBuilder.currentTimestamp(),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.START_DATE), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.END_DATE), DateUtil.MAX_DATE)
                    )),
                    criteriaBuilder
                        .equal(joinServiceAgreement.get(ServiceAgreement_.STATE), ServiceAgreementState.ENABLED)
                ),
                criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
            ));

        SetJoin<DataGroup, DataGroupItem> joinDataItems = joinDataGroup.join(DataGroup_.dataGroupItems, JoinType.INNER);

        cq.select(
            criteriaBuilder.tuple(
                joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID),
                joinDataGroup.get(DataGroup_.ID),
                joinDataItems.get(DataGroupItem_.DATA_ITEM_ID),
                joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID)))
            .where(predicates.toArray(new Predicate[]{}))
            .distinct(true);

        return entityManager.createQuery(cq).getResultList();
    }

    private Long getServiceAgreementByPermissionSetCount(AssignablePermissionSet assignablePermissionSet,
        CriteriaBuilder criteriaBuilder) {
        Long totalElements;
        CriteriaQuery<Long> cqCount = criteriaBuilder.createQuery(Long.class);
        Root<ServiceAgreement> serviceAgreementCount = cqCount.from(ServiceAgreement.class);
        Root<PermissionSetsInServiceAgreements> permissionSetCount = cqCount.from(
            PermissionSetsInServiceAgreements.class);

        createGetByPermissionSetPredicates(assignablePermissionSet, criteriaBuilder, cqCount, serviceAgreementCount,
            permissionSetCount);
        cqCount.select(criteriaBuilder.countDistinct(serviceAgreementCount));
        totalElements = entityManager.createQuery(cqCount).getSingleResult();
        return totalElements;
    }

    private void createGetByPermissionSetPredicates(AssignablePermissionSet assignablePermissionSet,
        CriteriaBuilder criteriaBuilder, CriteriaQuery<?> cq, Root<ServiceAgreement> serviceAgreementRoot,
        Root<PermissionSetsInServiceAgreements> permissionSetRoot) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(
            serviceAgreementRoot.get(ServiceAgreement_.id),
            permissionSetRoot.get(PermissionSetsInServiceAgreements_.serviceAgreementId)
        ));
        predicates.add(criteriaBuilder.equal(
            permissionSetRoot.get(PermissionSetsInServiceAgreements_.assignablePermissionSetId),
            assignablePermissionSet.getId())
        );

        cq.where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));
    }

    private List<Predicate> getPredicatesForServiceAgreementsForUserContext(String userId, String query,
        CriteriaBuilder criteriaBuilder, Join<UserAssignedFunctionGroup, UserContext> userContextJoin,
        Join<UserContext, ServiceAgreement> serviceAgreementJoin,
        Join<UserAssignedFunctionGroup, FunctionGroup> functionGroupJoin) {
        List<Predicate> conditions = new ArrayList<>();
        if (Objects.nonNull(query) && query.trim().length() > 0) {
            conditions.add(criteriaBuilder.like(serviceAgreementJoin.get(ServiceAgreement_.name),
                "%".concat(query).concat("%")));
        }
        conditions.add(criteriaBuilder.equal(userContextJoin
            .get(UserContext_.userId), userId));

        Predicate saTimePredicate = criteriaBuilder.between(
            criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
            criteriaBuilder.coalesce(serviceAgreementJoin.get(ServiceAgreement_.startDate), DateUtil.MIN_DATE),
            criteriaBuilder.coalesce(serviceAgreementJoin.get(ServiceAgreement_.endDate), DateUtil.MAX_DATE)
        );

        Predicate fgTimePredicate = criteriaBuilder.between(
            criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
            criteriaBuilder.coalesce(functionGroupJoin.get(FunctionGroup_.startDate), DateUtil.MIN_DATE),
            criteriaBuilder.coalesce(functionGroupJoin.get(FunctionGroup_.endDate), DateUtil.MAX_DATE)
        );

        conditions.add(criteriaBuilder.or(criteriaBuilder.and(criteriaBuilder.and(saTimePredicate, fgTimePredicate),
            criteriaBuilder.equal(serviceAgreementJoin.get(ServiceAgreement_.state), ServiceAgreementState.ENABLED)),
            criteriaBuilder.equal(functionGroupJoin.get(FunctionGroup_.TYPE), FunctionGroupType.SYSTEM)));

        return conditions;
    }

    private Optional<ServiceAgreement> getServiceAgreement(TypedQuery<ServiceAgreement> query) {
        Optional<ServiceAgreement> result;
        try {
            result = Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            result = Optional.empty();
        }
        return result;
    }

    private TypedQuery<ServiceAgreement> getServiceAgreementTypedQuery(CriteriaBuilder criteriaBuilder,
        SearchAndPaginationParameters searchAndPaginationParameters, String creatorId, UserParameters userParameters) {
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);
        Root<ServiceAgreement> serviceAgreementRoot = cq.from(ServiceAgreement.class);
        List<Predicate> predicates = getSubServiceAgreementPredicates(creatorId, searchAndPaginationParameters,
            criteriaBuilder, serviceAgreementRoot, userParameters);

        cq.select(serviceAgreementRoot)
            .distinct(true)
            .where(predicates.toArray(new Predicate[0]))
            .orderBy(criteriaBuilder.asc(serviceAgreementRoot.get("name")));

        return entityManager.createQuery(cq);
    }

    private List<Predicate> getSubServiceAgreementPredicates(String creatorId,
        SearchAndPaginationParameters searchAndPaginationParameters, CriteriaBuilder criteriaBuilder,
        Root<ServiceAgreement> serviceAgreementRoot, UserParameters userParameters) {
        List<Predicate> predicates = new ArrayList<>();

        Join<ServiceAgreement, LegalEntity> creatorJoin = serviceAgreementRoot
            .join(CREATOR_LEGAL_ENTITY_FILED_NAME, JoinType.INNER);
        Join<LegalEntity, LegalEntity> ancesorJoin = creatorJoin
            .join("legalEntityAncestors", JoinType.LEFT);
        predicates.add(criteriaBuilder.or(criteriaBuilder
            .equal(ancesorJoin.get("id"), creatorId), criteriaBuilder.equal(creatorJoin.get("id"), creatorId)));

        if (hasUserFilter(userParameters)) {
            Join<ServiceAgreement, Participant> participantsJoin = serviceAgreementRoot
                .join("participants", JoinType.LEFT);
            predicates.add(
                criteriaBuilder.or(
                    criteriaBuilder.and(
                        criteriaBuilder.equal(serviceAgreementRoot.get(IS_MASTER_FILED_NAME), true),
                        criteriaBuilder.equal(serviceAgreementRoot.get(CREATOR_LEGAL_ENTITY_FILED_NAME).get("id"),
                            userParameters.getUserLegalEntityId())
                    ),
                    criteriaBuilder.and(
                        criteriaBuilder.equal(serviceAgreementRoot.get(IS_MASTER_FILED_NAME), false),
                        criteriaBuilder.or(
                            criteriaBuilder
                                .equal(participantsJoin.join("participantUsers", JoinType.LEFT).get("userId"),
                                    userParameters.getUserId()),
                            criteriaBuilder.equal(participantsJoin.join("admins", JoinType.LEFT).get("userId"),
                                userParameters.getUserId())
                        )
                    )
                )
            );
        }

        if (searchAndPaginationParameters.getQuery() != null) {
            predicates.add(
                searchByNameOrDescription(searchAndPaginationParameters.getQuery(), criteriaBuilder,
                    serviceAgreementRoot.get("name"), serviceAgreementRoot.get("description")));
        }
        return predicates;
    }

    private boolean hasUserFilter(UserParameters userParameters) {
        return StringUtils.isNotEmpty(userParameters.getUserId())
            && StringUtils.isNotEmpty(userParameters.getUserLegalEntityId());
    }

    private Long getTotalSubServiceAgreementRecords(CriteriaBuilder criteriaBuilder,
        SearchAndPaginationParameters searchAndPaginationParameters, String creatorId, UserParameters userParameters) {
        CriteriaQuery<Long> countQueryBuilder = criteriaBuilder.createQuery(Long.class);
        Root<ServiceAgreement> serviceAgreementRoot = countQueryBuilder.from(ServiceAgreement.class);
        List<Predicate> predicates = getSubServiceAgreementPredicates(creatorId, searchAndPaginationParameters,
            criteriaBuilder, serviceAgreementRoot, userParameters);
        countQueryBuilder.select(criteriaBuilder.countDistinct(serviceAgreementRoot))
            .where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQueryBuilder).getSingleResult();
    }

    private Long getTotalNumberOfElements(CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        CriteriaQuery<Long> cqCount = criteriaBuilder.createQuery(Long.class);
        Root<ServiceAgreement> serviceAgreementCount = cqCount.from(ServiceAgreement.class);
        cqCount.select(criteriaBuilder.count(serviceAgreementCount))
            .distinct(true)
            .where(predicates.toArray(new Predicate[0]));
        TypedQuery<Long> queryForCount = entityManager.createQuery(cqCount);
        return queryForCount.getSingleResult();
    }

    private Predicate searchByNameOrDescription(String parameterQuery, CriteriaBuilder criteriaBuilder,
        Path<String> name, Path<String> description) {
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(name), "%" + escapeChars(parameterQuery) + "%", '!'),
            criteriaBuilder.like(criteriaBuilder.upper(description), "%" + escapeChars(parameterQuery) + "%", '!'));
    }

    private String escapeChars(Object value) {
        return value
            .toString()
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_");
    }
}
