package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAgreementJpaRepository extends JpaRepository<ServiceAgreement, String>,
    ServiceAgreementJpaCustomRepository {

    boolean existsById(String id);

    boolean existsByExternalId(String externalId);

    boolean existsByCreatorLegalEntityIdAndIsMasterTrue(String legalEntityId);

    @EntityGraph(value = SERVICE_AGREEMENT_WITH_ADDITIONS, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ServiceAgreement> findByExternalId(String externalId);

    @EntityGraph(value = SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR, type = EntityGraph.EntityGraphType.FETCH)
    List<ServiceAgreement> findServiceAgreementsByName(String serviceAgreementName);

    List<ServiceAgreement> findAllByExternalIdIn(Collection<String> externalIds);

}