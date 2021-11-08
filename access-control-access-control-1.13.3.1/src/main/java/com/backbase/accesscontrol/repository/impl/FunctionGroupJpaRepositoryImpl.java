package com.backbase.accesscontrol.repository.impl;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroup_;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreement_;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepositoryCustom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * FunctionGroupJpaRepositoryCustom implementation.
 */
public class FunctionGroupJpaRepositoryImpl implements FunctionGroupJpaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    public Optional<FunctionGroup> readByServiceAgreementExternalIdAndName(String externalServiceAgreementId,
        String name, String entityGraph) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FunctionGroup> cq = criteriaBuilder
            .createQuery(FunctionGroup.class);
        Root<FunctionGroup> functionGroupRoot = cq
            .from(FunctionGroup.class);

        List<Predicate> predicates = new ArrayList<>();
        Join<FunctionGroup, ServiceAgreement> join =
            functionGroupRoot.join(FunctionGroup_.serviceAgreement);
        predicates.add(criteriaBuilder
            .equal(join.get(ServiceAgreement_.externalId), externalServiceAgreementId));
        predicates.add(criteriaBuilder.equal(functionGroupRoot.get(FunctionGroup_.NAME), name));

        cq.select(functionGroupRoot)
            .where(predicates.toArray(new Predicate[]{}));

        TypedQuery<FunctionGroup> query = entityManager.createQuery(cq);
        if (entityGraph != null) {
            EntityGraph<?> graph = entityManager.getEntityGraph(entityGraph);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, graph);
        }

        Optional<FunctionGroup> result;

        try {
            result = Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            result = Optional.empty();
        }

        return result;

    }

    @Override
    public List<FunctionGroup> readByIdIn(Collection<String> idList, String entityGraph) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FunctionGroup> cq = criteriaBuilder.createQuery(FunctionGroup.class);

        Root<FunctionGroup> functionGroupRoot = cq.from(FunctionGroup.class);
        cq.select(functionGroupRoot).where(functionGroupRoot.get(FunctionGroup_.ID).in(idList)).distinct(true);

        TypedQuery<FunctionGroup> query = entityManager.createQuery(cq);
        if (entityGraph != null) {
            EntityGraph<?> graph = entityManager.getEntityGraph(entityGraph);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, graph);
        }
        return query
            .getResultList();
    }
}
