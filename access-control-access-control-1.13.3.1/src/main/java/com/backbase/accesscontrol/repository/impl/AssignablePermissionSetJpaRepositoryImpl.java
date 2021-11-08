package com.backbase.accesscontrol.repository.impl;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.AssignablePermissionSet_;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaCustomRepository;
import java.util.Optional;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class AssignablePermissionSetJpaRepositoryImpl implements AssignablePermissionSetJpaCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<AssignablePermissionSet> findById(Long id, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignablePermissionSet> cq = criteriaBuilder.createQuery(AssignablePermissionSet.class);
        Root<AssignablePermissionSet> assignablePermissionSetRoot = cq.from(AssignablePermissionSet.class);

        cq.select(assignablePermissionSetRoot)
            .where(criteriaBuilder.equal(assignablePermissionSetRoot.get(AssignablePermissionSet_.ID), id));

        TypedQuery<AssignablePermissionSet> query = entityManager.createQuery(cq);
        if (isNotEmpty(entityGraphName)) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }
        return getAssignablePermissionSet(query);
    }

    @Override
    public Optional<AssignablePermissionSet> findByName(String name, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AssignablePermissionSet> cq = criteriaBuilder.createQuery(AssignablePermissionSet.class);
        Root<AssignablePermissionSet> assignablePermissionSetRoot = cq.from(AssignablePermissionSet.class);

        cq.select(assignablePermissionSetRoot)
            .where(criteriaBuilder.equal(assignablePermissionSetRoot.get(AssignablePermissionSet_.NAME), name));

        TypedQuery<AssignablePermissionSet> query = entityManager.createQuery(cq);
        if (isNotEmpty(entityGraphName)) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            query.setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph);
        }
        return getAssignablePermissionSet(query);
    }

    private Optional<AssignablePermissionSet> getAssignablePermissionSet(TypedQuery<AssignablePermissionSet> query) {
        Optional<AssignablePermissionSet> result;
        try {
            result = Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            result = Optional.empty();
        }
        return result;
    }

}
