package com.backbase.accesscontrol.repository.impl;

import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContext_;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.DataGroupItem_;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.LegalEntityAncestor;
import com.backbase.accesscontrol.domain.LegalEntityAncestor_;
import com.backbase.accesscontrol.domain.LegalEntity_;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.Participant_;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreement_;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup_;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.UserContext_;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepositoryCustom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * LegalEntityJpaRepositoryCustom custom repository implementation.
 */
@Repository
public class LegalEntityJpaRepositoryImpl implements LegalEntityJpaRepositoryCustom {


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<LegalEntity> findById(String id, String entityGraphName) {
        return findBySinglePropertyEquals("id", id, entityGraphName);
    }

    @Override
    public List<LegalEntity> findDistinctByParentIsNull(String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LegalEntity> cq = criteriaBuilder.createQuery(LegalEntity.class);

        Root<LegalEntity> legalEntityRoot = cq.from(LegalEntity.class);
        cq.select(legalEntityRoot);
        Predicate predicate = criteriaBuilder.isNull(legalEntityRoot.get("parent"));
        cq.where(predicate).distinct(true);
        TypedQuery<LegalEntity> query = entityManager.createQuery(cq);
        if (entityGraphName != null) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query
                .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }
        return query
            .getResultList();
    }

    @Override
    public Optional<LegalEntity> findByExternalId(String externalId, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LegalEntity> cq = criteriaBuilder.createQuery(LegalEntity.class);

        Root<LegalEntity> legalEntityRoot = cq.from(LegalEntity.class);
        cq.select(legalEntityRoot);
        Predicate predicate = criteriaBuilder.equal(legalEntityRoot.get(LegalEntity_.externalId), externalId);
        cq.where(predicate).distinct(true);

        TypedQuery<LegalEntity> query = entityManager.createQuery(cq);
        if (entityGraphName != null) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }
        Optional<LegalEntity> result;
        try {
            result = Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            result = Optional.empty();
        }
        return result;
    }

    @Override
    public List<LegalEntity> findDistinctByExternalIdIn(List<String> externalIds, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LegalEntity> cq = criteriaBuilder.createQuery(LegalEntity.class);

        Root<LegalEntity> legalEntityRoot = cq.from(LegalEntity.class);
        cq.select(legalEntityRoot);
        Predicate predicate = legalEntityRoot.get(LegalEntity_.externalId).in(externalIds);
        cq.where(predicate).distinct(true);

        TypedQuery<LegalEntity> query = entityManager.createQuery(cq);
        if (entityGraphName != null) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }
        return query.getResultList();
    }

    @Override
    public Page<LegalEntity> findAllSubEntities(String ancestorId,
        SearchAndPaginationParameters searchAndPaginationParameters, Collection<String> excludeIds,
        String entityGraphName) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LegalEntity> cq = criteriaBuilder.createQuery(LegalEntity.class);
        Root<LegalEntity> legalEntityRoot = cq.from(LegalEntity.class);
        List<Predicate> predicates = new ArrayList<>();

        Join<LegalEntity, LegalEntityAncestor> joinAncestor =
            legalEntityRoot.join(LegalEntity_.legalEntityDescendents, JoinType.LEFT);

        predicates.add(getPredicateConditionWithExcludeIds(ancestorId, excludeIds, criteriaBuilder,
            legalEntityRoot, joinAncestor));

        if (searchAndPaginationParameters.getQuery() != null) {
            predicates.add(
                searchLike(searchAndPaginationParameters.getQuery(), criteriaBuilder,
                    legalEntityRoot.get("name")));
        }
        cq.select(legalEntityRoot)
            .distinct(true)
            .where(predicates.toArray(new Predicate[]{}))
            .orderBy(criteriaBuilder.asc(legalEntityRoot.get("name")));

        TypedQuery<LegalEntity> query = entityManager.createQuery(cq);

        EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
        List<LegalEntity> legalEntities =
            getLegalEntities(searchAndPaginationParameters, entityGraph, criteriaBuilder, query);

        Long count = getTotalNumberOfElements(criteriaBuilder, predicates);

        return new PageImpl<>(legalEntities, Pageable.unpaged(), count);

    }

    private Predicate getPredicateConditionWithExcludeIds(String ancestorId, Collection<String> excludeIds,
        CriteriaBuilder criteriaBuilder, Root<LegalEntity> legalEntityRoot,
        Join<LegalEntity, LegalEntityAncestor> joinAncestor) {

        Predicate ancestor = criteriaBuilder.equal(joinAncestor.get(LegalEntityAncestor_.ancestorId), ancestorId);

        if (CollectionUtils.isNotEmpty(excludeIds)) {
            ancestor = criteriaBuilder.and(ancestor,
                criteriaBuilder.not(joinAncestor.get(LegalEntityAncestor_.descendentId).in(excludeIds))
            );
        }

        if (CollectionUtils.isEmpty(excludeIds) || !excludeIds.contains(ancestorId)) {
            ancestor = criteriaBuilder.or(
                criteriaBuilder.equal(legalEntityRoot.get(LegalEntity_.id), ancestorId),
                ancestor
            );
        }

        return ancestor;
    }

    private List<LegalEntity> getLegalEntities(SearchAndPaginationParameters searchAndPaginationParameters,
        EntityGraph<?> entityGraph, CriteriaBuilder criteriaBuilder, TypedQuery<LegalEntity> query) {
        List<LegalEntity> legalEntities;
        Integer from = searchAndPaginationParameters.getFrom();
        Integer size = searchAndPaginationParameters.getSize();

        if (from != null && size != null) {
            query.setFirstResult(from * size);
            query.setMaxResults(size);
            legalEntities = readLegalEntitiesPage(criteriaBuilder, query.getResultList(), entityGraph);
        } else {
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
            legalEntities = query.getResultList();
        }
        return legalEntities;
    }

    @Override
    public Boolean checkIfNotParticipantInCustomServiceAgreement(String externalId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);

        Root<ServiceAgreement> root = cq.from(ServiceAgreement.class);

        Join<Participant, LegalEntity> leJoin = root
            .join(ServiceAgreement_.participants, JoinType.INNER)
            .join(Participant_.legalEntity, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(leJoin.get(LegalEntity_.externalId), externalId));
        predicates.add(criteriaBuilder.isFalse(root.get(ServiceAgreement_.isMaster)));

        cq.select(root)
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        Optional<ServiceAgreement> res = Optional.empty();

        try {
            res = Optional.of(entityManager.createQuery(cq).setMaxResults(1).getSingleResult());
        } catch (NoResultException e) {
            //res is pre-initialized
        }
        return !res.isPresent();
    }

    @Override
    public Boolean checkIfExistsUsersFromLeWithAssignedPermissionsInMsa(String externalId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserAssignedFunctionGroup> cq = criteriaBuilder.createQuery(UserAssignedFunctionGroup.class);

        Root<UserAssignedFunctionGroup> root = cq.from(UserAssignedFunctionGroup.class);

        Join<UserContext, ServiceAgreement> saJoin = root.join(UserAssignedFunctionGroup_.userContext, JoinType.INNER)
            .join(UserContext_.serviceAgreement, JoinType.INNER);
        Join<ServiceAgreement, LegalEntity> leJoin = saJoin.join(ServiceAgreement_.creatorLegalEntity, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(leJoin.get(LegalEntity_.externalId), externalId));

        predicates.add(criteriaBuilder.isTrue(saJoin.get(ServiceAgreement_.isMaster)));

        cq.select(root)
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        Optional<UserAssignedFunctionGroup> res = Optional.empty();

        try {
            res = Optional.of(entityManager.createQuery(cq).setMaxResults(1).getSingleResult());
        } catch (NoResultException e) {
            //res is pre-initialized
        }
        return res.isPresent();
    }

    @Override
    public Boolean checkIsExistsUsersFromLeWithPendingPermissionsInMsa(String externalId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);

        Root<ServiceAgreement> root = cq.from(ServiceAgreement.class);
        root.join(ServiceAgreement_.creatorLegalEntity, JoinType.INNER);

        Root<ApprovalUserContext> rootApprovalUserContext = cq.from(ApprovalUserContext.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root
            .get(ServiceAgreement_.creatorLegalEntity)
            .get(LegalEntity_.externalId), externalId));

        predicates.add(criteriaBuilder.equal(
            root.get(ServiceAgreement_.id),
            rootApprovalUserContext.get(ApprovalUserContext_.serviceAgreementId)
        ));

        predicates.add(criteriaBuilder.isTrue(root
            .get(ServiceAgreement_.isMaster)));

        cq.select(root)
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        Optional<ServiceAgreement> res = Optional.empty();

        try {
            res = Optional.of(entityManager.createQuery(cq).setMaxResults(1).getSingleResult());
        } catch (NoResultException e) {
            //res is pre-initialized
        }
        return res.isPresent();
    }

    @Override
    public Boolean checkIfNotCreatorOfAnyCsa(String externalId) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServiceAgreement> cq = criteriaBuilder.createQuery(ServiceAgreement.class);

        Root<ServiceAgreement> root = cq.from(ServiceAgreement.class);
        root.join(ServiceAgreement_.creatorLegalEntity, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(
            root.get(ServiceAgreement_.creatorLegalEntity)
                .get(LegalEntity_.externalId), externalId)
        );
        predicates.add(criteriaBuilder.isFalse(root.get(ServiceAgreement_.isMaster)));

        cq.select(root)
            .distinct(true)
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        Optional<ServiceAgreement> res = Optional.empty();

        try {
            res = Optional.of(entityManager.createQuery(cq).setMaxResults(1).getSingleResult());
        } catch (NoResultException e) {
            //res is pre-initialized
        }
        return !res.isPresent();
    }

    @Override
    public Page<LegalEntity> findAllLegalEntitiesSegmentation(
        SearchAndPaginationParameters searchParameters,
        Set<String> dataGroupIds, String graphLegalEntityWithAdditions) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LegalEntity> cq = criteriaBuilder.createQuery(LegalEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        Root<LegalEntity> legalEntityRoot = cq.from(LegalEntity.class);
        Root<DataGroupItem> dataGroupItemRoot = cq.from(DataGroupItem.class);

        if (searchParameters.getQuery() != null) {
            predicates.add(
                searchExactOrSearchLike(searchParameters.getQuery(), criteriaBuilder,
                    legalEntityRoot.get(LegalEntity_.EXTERNAL_ID), legalEntityRoot.get(LegalEntity_.NAME)));
        }

        predicates.add(criteriaBuilder
            .equal(legalEntityRoot.get(LegalEntity_.ID), dataGroupItemRoot.get(DataGroupItem_.dataItemId)));
        predicates.add(dataGroupItemRoot.get(DataGroupItem_.dataGroupId).in(dataGroupIds));

        cq.select(legalEntityRoot)
            .distinct(true)
            .where(predicates.toArray(new Predicate[]{}))
            .orderBy(criteriaBuilder.asc(legalEntityRoot.get(LegalEntity_.NAME)));

        TypedQuery<LegalEntity> query = entityManager.createQuery(cq);

        EntityGraph<?> entityGraph = entityManager.getEntityGraph(graphLegalEntityWithAdditions);
        List<LegalEntity> legalEntities = getLegalEntities(searchParameters, entityGraph,
            criteriaBuilder, query);

        Long count = getTotalNumberOfElementsSegmentation(criteriaBuilder, predicates);

        return new PageImpl<>(legalEntities, Pageable.unpaged(), count);
    }

    private Predicate searchExactOrSearchLike(String parameterQuery, CriteriaBuilder criteriaBuilder,
        Path<String> exactTerm,
        Path<String> likeTerm) {
        return criteriaBuilder
            .or(criteriaBuilder.equal(criteriaBuilder.upper(exactTerm), parameterQuery),
                criteriaBuilder.like(criteriaBuilder.upper(likeTerm), "%" + escapeChars(parameterQuery) + "%", '!'));
    }

    private List<LegalEntity> readLegalEntitiesPage(CriteriaBuilder criteriaBuilder,
        List<LegalEntity> resultLegalEntities, EntityGraph<?> entityGraph) {
        Set<String> legalEntityIds = resultLegalEntities.stream()
            .map(LegalEntity::getId)
            .collect(Collectors.toSet());

        List<LegalEntity> legalEntities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(legalEntityIds)) {
            CriteriaQuery<LegalEntity> legalEntityQuery = criteriaBuilder.createQuery(LegalEntity.class);
            Root<LegalEntity> legalEntityRoot = legalEntityQuery.from(LegalEntity.class);
            legalEntityQuery.select(legalEntityRoot);
            Path<UUID> id = legalEntityRoot.get("id");
            Predicate predicate = id.in(legalEntityIds);
            legalEntityQuery.where(predicate)
                .distinct(true);

            legalEntities = entityManager.createQuery(legalEntityQuery)
                .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
                .getResultList();
        }

        return legalEntities;
    }

    private Optional<LegalEntity> findBySinglePropertyEquals(String propertyName, String propertyValue,
        String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
        CriteriaQuery<LegalEntity> cq = criteriaBuilder.createQuery(LegalEntity.class);

        Root<LegalEntity> legalEntityRoot = cq.from(LegalEntity.class);
        cq.select(legalEntityRoot);
        Predicate predicate = criteriaBuilder.equal(legalEntityRoot.get(propertyName), propertyValue);
        cq.where(predicate).distinct(true);

        return Optional.ofNullable(DataAccessUtils.singleResult(entityManager.createQuery(cq)
            .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph).getResultList()));
    }

    private Predicate searchLike(String parameterQuery, CriteriaBuilder criteriaBuilder,
        Path<String> query) {
        return
            criteriaBuilder.like(
                criteriaBuilder.upper(query), "%" + escapeChars(parameterQuery) + "%", '!');
    }

    private String escapeChars(Object value) {
        return value
            .toString()
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_");
    }

    private Long getTotalNumberOfElements(CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        CriteriaQuery<Long> cqCount = criteriaBuilder.createQuery(Long.class);
        Root<LegalEntity> legalEntityCount = cqCount.from(LegalEntity.class);
        legalEntityCount
            .join(LegalEntity_.legalEntityDescendents, JoinType.LEFT);
        cqCount.select(criteriaBuilder.count(legalEntityCount))
            .distinct(true)
            .where(predicates.toArray(new Predicate[0]));
        TypedQuery<Long> queryForCount = entityManager.createQuery(cqCount);
        return queryForCount.getSingleResult();
    }

    private Long getTotalNumberOfElementsSegmentation(CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        CriteriaQuery<Long> cqCount = criteriaBuilder.createQuery(Long.class);

        Root<LegalEntity> legalEntityRoot = cqCount.from(LegalEntity.class);
        cqCount.from(DataGroupItem.class);

        cqCount.select(criteriaBuilder.count(legalEntityRoot))
            .distinct(true)
            .where(predicates.toArray(new Predicate[]{}));
        TypedQuery<Long> queryForCount = entityManager.createQuery(cqCount);
        return queryForCount.getSingleResult();
    }

}
