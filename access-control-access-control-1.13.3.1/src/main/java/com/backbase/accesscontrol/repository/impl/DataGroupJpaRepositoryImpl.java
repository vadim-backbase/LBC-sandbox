package com.backbase.accesscontrol.repository.impl;

import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.domain.*;
import com.backbase.accesscontrol.domain.DataGroup_;
import com.backbase.accesscontrol.domain.LegalEntity_;
import com.backbase.accesscontrol.domain.Participant_;
import com.backbase.accesscontrol.domain.ServiceAgreement_;
import com.backbase.accesscontrol.repository.DataGroupJpaCustomRepository;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.SharesEnum;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

/**
 * DataGroupJpaCustomRepository implementation.
 */
public class DataGroupJpaRepositoryImpl implements DataGroupJpaCustomRepository {

    public static final String SERVICE_AGREEMENT_ATTRIBUTE_NAME = "serviceAgreement";
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DataGroup> findById(String id, String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);
        Root<DataGroup> dataGroupRoot = cq.from(DataGroup.class);
        cq.select(dataGroupRoot).where(criteriaBuilder.equal(dataGroupRoot.get(DataGroup_.id), id));
        if (nonNull(entityGraphName)) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            return entityManager.createQuery(cq)
                .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
                .getResultList().stream().findFirst();
        }
        return entityManager.createQuery(cq)
            .getResultList().stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataGroup> findAllDataGroupsWithIdsIn(Collection<String> ids, String graphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(graphName);

        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);
        Root<DataGroup> dataGroupRoot = cq.from(DataGroup.class);
        cq.select(dataGroupRoot);
        Path<UUID> id = dataGroupRoot.get("id");
        Predicate predicate = id.in(ids);
        cq.where(predicate).distinct(true);

        return entityManager.createQuery(cq)
            .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
            .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataGroup> findAllDataGroupsWithExternalServiceAgreementIdsIn(Collection<String> ids,
        String queryGraph) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(queryGraph);

        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);
        Root<DataGroup> dataGroupRoot = cq.from(DataGroup.class);
        cq.select(dataGroupRoot);

        cq.where(dataGroupRoot.get(SERVICE_AGREEMENT_ATTRIBUTE_NAME).get("externalId").in(ids))
            .distinct(true);
        return entityManager.createQuery(cq)
            .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
            .getResultList();
    }

    /**
     * Find service agreement id and name in or by service agreement id and id in.
     *
     * @param serviceAgreementId service agreement id
     * @param dataGroupNames     data group names
     * @param ids                list of ids
     * @return list of {@link DataGroup}
     */
    public List<DataGroup> findByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn(String serviceAgreementId,
        List<String> dataGroupNames, List<String> ids) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);
        Root<DataGroup> dataGroupRoot = cq.from(DataGroup.class);
        Predicate namesCriteriaBuilder = null;
        Predicate idsCriteriaBuilder = null;
        List<Predicate> predicates = new ArrayList<>();

        namesCriteriaBuilder = populateNamesCriteriaBuilder(serviceAgreementId, dataGroupNames, criteriaBuilder,
            dataGroupRoot, namesCriteriaBuilder);
        idsCriteriaBuilder = populateIdsCriteriaBuilder(serviceAgreementId, ids, criteriaBuilder, dataGroupRoot,
            idsCriteriaBuilder);

        if (idsCriteriaBuilder != null && namesCriteriaBuilder != null) {
            predicates.add(
                criteriaBuilder.or(
                    namesCriteriaBuilder,
                    idsCriteriaBuilder
                )
            );
        } else if (idsCriteriaBuilder != null) {
            predicates.add(idsCriteriaBuilder);
        } else if (namesCriteriaBuilder != null) {
            predicates.add(namesCriteriaBuilder);
        }

        cq.select(dataGroupRoot).where(predicates.toArray(new Predicate[]{}));

        return entityManager.createQuery(cq)
            .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataGroup> findAllDataGroupsByServiceAgreementAndDataItem(String type,
        String serviceAgreementId, String serviceAgreementName, String serviceAgreementExternalId, String dataItemId,
        String leExternalId, SharesEnum shares) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);

        Root<DataGroup> dataGroupRoot = cq.from(DataGroup.class);
        dataGroupRoot.fetch(DataGroup_.serviceAgreement, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(
            dataGroupRoot.get(DataGroup_.dataItemType),
            type
        ));

        createDataGroupItemJoin(dataItemId, criteriaBuilder, dataGroupRoot, predicates);
        createLegalEntityJoin(leExternalId, criteriaBuilder, dataGroupRoot, predicates, shares);
        createSearchCriteriaPredicates(serviceAgreementId, serviceAgreementExternalId, serviceAgreementName,
            criteriaBuilder, dataGroupRoot, predicates);

        cq
            .select(dataGroupRoot)
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        return entityManager.createQuery(cq)
            .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DataGroup> findByServiceAgreementExternalIdAndName(String externalServiceAgreementId, String name,
        String entityGraphName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);
        Root<DataGroup> dataGroupRoot = cq.from(DataGroup.class);
        List<Predicate> predicates = new ArrayList<>();

        dataGroupRoot.join(DataGroup_.serviceAgreement, JoinType.LEFT);

        predicates.add(criteriaBuilder
            .equal(dataGroupRoot.get(SERVICE_AGREEMENT_ATTRIBUTE_NAME).get(ServiceAgreement_.EXTERNAL_ID),
                externalServiceAgreementId));
        predicates.add(criteriaBuilder.equal(dataGroupRoot.get(DataGroup_.NAME), name));

        cq.select(dataGroupRoot)
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        if (nonNull(entityGraphName)) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            return entityManager.createQuery(cq)
                .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
                .getResultList().stream().findFirst();
        }
        return entityManager.createQuery(cq).getResultList().stream().findFirst();
    }


    @Override
    public List<DataGroup> findByServiceAgreementId(String serviceAgreementId, String entityGraphName) {

        return findByServiceAgreementIdAndDataItemType(serviceAgreementId, null, entityGraphName);
    }

    @Override
    public List<DataGroup> findByServiceAgreementIdAndDataItemType(String serviceAgreementId,
        String dataItemType, String entityGraphName) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);
        Root<DataGroup> dataGroupRoot = cq.from(DataGroup.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder
            .equal(dataGroupRoot.get(DataGroup_.serviceAgreement).get(ServiceAgreement_.id),
                serviceAgreementId));
        if (Objects.nonNull(dataItemType)) {
            predicates.add(criteriaBuilder
                .equal(dataGroupRoot.get(DataGroup_.dataItemType), dataItemType));
        }

        cq.select(dataGroupRoot)
            .where(criteriaBuilder.and(predicates.toArray(new Predicate[]{})));

        if (nonNull(entityGraphName)) {
            EntityGraph<?> entityGraph = entityManager.getEntityGraph(entityGraphName);
            return entityManager.createQuery(cq)
                .setHint(GraphConstants.JAVAX_PERSISTENCE_FETCHGRAPH, entityGraph)
                .getResultList();
        }
        return entityManager.createQuery(cq).getResultList();
    }

    private void createDataGroupItemJoin(String dataItemId, CriteriaBuilder criteriaBuilder,
        Root<DataGroup> dataGroupRoot,
        List<Predicate> predicates) {

        if (!Strings.isNullOrEmpty(dataItemId)) {
            predicates.add(
                criteriaBuilder.isMember(dataItemId, dataGroupRoot.get(DataGroup_.dataItemIds)));
        }
    }

    private void createLegalEntityJoin(String leExternalId, CriteriaBuilder criteriaBuilder,
                                       Root<DataGroup> dataGroupRoot,
                                       List<Predicate> predicates, SharesEnum shares) {
        if(!Strings.isNullOrEmpty(leExternalId)) {
            Join<DataGroup, ServiceAgreement> joinSA = dataGroupRoot.join(DataGroup_.SERVICE_AGREEMENT, JoinType.INNER);
            Join<ServiceAgreement, Participant> joinParticipants = joinSA
                .join(ServiceAgreement_.participants, JoinType.INNER);
            Join<Participant, LegalEntity> joinLE = joinParticipants.join(Participant_.legalEntity, JoinType.INNER);
            if (shares == SharesEnum.ACCOUNTS) {
                predicates.add(
                    criteriaBuilder.and(criteriaBuilder.equal(joinParticipants.get(Participant_.SHARE_ACCOUNTS), true),
                        criteriaBuilder.equal(joinLE.get(LegalEntity_.EXTERNAL_ID), leExternalId)));
            } else if (shares == SharesEnum.USERS) {
                predicates.add(
                    criteriaBuilder.and(criteriaBuilder.equal(joinParticipants.get(Participant_.SHARE_USERS), true),
                        criteriaBuilder.equal(joinLE.get(LegalEntity_.EXTERNAL_ID), leExternalId)));
            } else if (shares == SharesEnum.USERSANDACCOUNTS) {
                predicates.add(
                    criteriaBuilder.and(criteriaBuilder.and(
                        criteriaBuilder.equal(joinParticipants.get(Participant_.SHARE_ACCOUNTS), true),
                        criteriaBuilder.equal(joinParticipants.get(Participant_.SHARE_USERS), true)),
                        criteriaBuilder.equal(joinLE.get(LegalEntity_.EXTERNAL_ID), leExternalId)));
            } else {
                throw new IllegalArgumentException("Unexpected value for shares parameter: '" + shares + "'");
            }
        }

    }

    private void createSearchCriteriaPredicates(String serviceAgreementId, String serviceAgreementExternalId,
        String serviceAgreementName, CriteriaBuilder criteriaBuilder,
        Root<DataGroup> dataGroupRoot,
        List<Predicate> predicates) {
        if (!Strings.isNullOrEmpty(serviceAgreementId)) {
            predicates.add(criteriaBuilder.equal(
                dataGroupRoot.get(DataGroup_.serviceAgreement).get(ServiceAgreement_.id),
                serviceAgreementId
            ));
        }
        if (!Strings.isNullOrEmpty(serviceAgreementName)) {
            predicates.add(criteriaBuilder.equal(
                dataGroupRoot.get(DataGroup_.serviceAgreement).get(ServiceAgreement_.name),
                serviceAgreementName
            ));
        }

        if (!Strings.isNullOrEmpty(serviceAgreementExternalId)) {
            predicates.add(criteriaBuilder.equal(
                dataGroupRoot.get(DataGroup_.serviceAgreement).get(ServiceAgreement_.externalId),
                serviceAgreementExternalId
            ));
        }
    }

    private Predicate populateIdsCriteriaBuilder(String serviceAgreementId, List<String> ids,
        CriteriaBuilder criteriaBuilder, Root<DataGroup> dataGroupRoot, Predicate idsCriteriaBuilder) {
        if (ids != null && !ids.isEmpty()) {
            idsCriteriaBuilder = criteriaBuilder.and(
                criteriaBuilder.equal(dataGroupRoot.get(SERVICE_AGREEMENT_ATTRIBUTE_NAME)
                    .get("id"), serviceAgreementId),
                dataGroupRoot.get("id").in(ids));
        }
        return idsCriteriaBuilder;
    }

    private Predicate populateNamesCriteriaBuilder(String serviceAgreementId, List<String> dataGroupNames,
        CriteriaBuilder criteriaBuilder, Root<DataGroup> dataGroupRoot, Predicate namesCriteriaBuilder) {
        if (dataGroupNames != null && !dataGroupNames.isEmpty()) {
            namesCriteriaBuilder = criteriaBuilder.and(
                criteriaBuilder.equal(dataGroupRoot.get(SERVICE_AGREEMENT_ATTRIBUTE_NAME)
                    .get("id"), serviceAgreementId),
                dataGroupRoot.get("name").in(dataGroupNames));
        }
        return namesCriteriaBuilder;
    }
}
