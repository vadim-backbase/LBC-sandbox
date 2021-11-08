package com.backbase.accesscontrol.repository.impl;

import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.Participant_;
import com.backbase.accesscontrol.domain.ServiceAgreement_;
import com.backbase.accesscontrol.repository.ParticipantJpaRepositoryCustom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

@Repository
public class ParticipantJpaRepositoryImpl implements ParticipantJpaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Participant> findByServiceAgreementIdInAndShareUsersIsTrue(List<String> serviceAgreementIds,
        String graphName) {
        return findAllByServiceAgreementIdInAndShareUsersAndShareAccounts(serviceAgreementIds, true, null, graphName);
    }

    @Override
    public List<Participant> findAllParticipantsWithExternalServiceAgreementIdsIn(
        Collection<String> serviceAgreementIds, String graphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(graphName);

        CriteriaQuery<Participant> cq = criteriaBuilder.createQuery(Participant.class);
        Root<Participant> participantRoot = cq.from(Participant.class);
        cq.select(participantRoot);

        cq.where(participantRoot.get("serviceAgreement").get("externalId").in(serviceAgreementIds))
            .distinct(true);
        return entityManager.createQuery(cq)
            .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
            .getResultList();
    }

    private List<Participant> findAllByServiceAgreementIdInAndShareUsersAndShareAccounts(
        List<String> serviceAgreementIds, Boolean shareUsers, Boolean shareAccounts, String graphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Participant> cq = criteriaBuilder.createQuery(Participant.class);
        Root<Participant> participant = cq.from(Participant.class);
        cq.select(participant);
        List<Predicate> predicates = new ArrayList<>();

        Predicate predicate = participant.get(Participant_.serviceAgreement).get(ServiceAgreement_.id)
            .in(serviceAgreementIds);
        predicates.add(predicate);

        if (shareUsers != null) {
            predicates.add(criteriaBuilder.equal(participant.get(Participant_.shareUsers), shareUsers));
        }

        if (shareAccounts != null) {
            predicates.add(criteriaBuilder.equal(participant.get(Participant_.shareAccounts), shareAccounts));
        }

        cq.where(predicates.toArray(new Predicate[]{})).distinct(true);

        EntityGraph<?> entityGraph = entityManager.getEntityGraph(graphName);

        return entityManager.createQuery(cq)
            .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
            .getResultList();
    }

    @Override
    public List<Participant> findByServiceAgreementId(
        String serviceAgreementId, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);

        CriteriaQuery<Participant> cq = criteriaBuilder.createQuery(Participant.class);
        Root<Participant> participant = cq.from(Participant.class);
        cq.select(participant);
        cq.where(criteriaBuilder.equal(participant.get(Participant_.serviceAgreement)
            .get(ServiceAgreement_.id), serviceAgreementId));
        return entityManager.createQuery(cq)
            .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
            .getResultList();
    }
}
