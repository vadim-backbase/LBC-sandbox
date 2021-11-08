package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.Participant;
import java.util.Collection;
import java.util.List;

public interface ParticipantJpaRepositoryCustom {

    List<Participant> findByServiceAgreementIdInAndShareUsersIsTrue(List<String> serviceAgreementIds, String graphName);

    List<Participant> findAllParticipantsWithExternalServiceAgreementIdsIn(Collection<String> ids, String graphName);

    List<Participant> findByServiceAgreementId(String serviceAgreementId, String entityGraphName);
}
