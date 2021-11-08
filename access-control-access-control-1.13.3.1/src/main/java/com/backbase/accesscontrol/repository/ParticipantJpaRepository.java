package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_ADMINS;
import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY_AND_SERVICE_AGREEMENT_CREATOR;
import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS;

import com.backbase.accesscontrol.domain.Participant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantJpaRepository extends JpaRepository<Participant, String>, ParticipantJpaRepositoryCustom {

    @EntityGraph(value = PARTICIPANT_WITH_LEGAL_ENTITY_AND_SERVICE_AGREEMENT_CREATOR,
        type = EntityGraph.EntityGraphType.FETCH)
    List<Participant> findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(String saExternalId);

    @EntityGraph(value = PARTICIPANT_WITH_ADMINS, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Participant> findByServiceAgreementExternalIdAndLegalEntityId(String externalServiceAgreementId,
        String legalEntityId);

    @EntityGraph(value = PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS, type = EntityGraph.EntityGraphType.FETCH)
    List<Participant> findDistinctByServiceAgreementIdAndLegalEntityIdInAndShareUsersTrue(String serviceAgreementId,
        Collection<String> userSharingParticipants);

    @EntityGraph(value = PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Participant> findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(
        String serviceAgreementExternalId, String participantId);

    @EntityGraph(value = PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Participant> findByServiceAgreementExternalIdAndLegalEntityExternalId(String externalServiceAgreementId,
        String externalParticipantId);
}
