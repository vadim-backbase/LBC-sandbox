package com.backbase.accesscontrol.business.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AgreementsPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsPersistenceService.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    /**
     * Retrieves Participants.
     *
     * @param externalServiceAgreementIds - list of service agreement external ids
     * @return a map of {@link PersistenceExtendedParticipant} grouped by external id
     */
    public Map<String, Set<PersistenceExtendedParticipant>> getParticipantsPerExternalId(
        Set<String> externalServiceAgreementIds) {
        LOGGER.info("Retrieving participants for external service agreement ids {}", externalServiceAgreementIds);
        if (CollectionUtils.isEmpty(externalServiceAgreementIds)) {
            LOGGER.info("No service agreement ids are present: {}", externalServiceAgreementIds);
            return new HashMap<>();
        }

        List<PersistenceExtendedParticipant> persistenceExtendedParticipants =
            persistenceServiceAgreementService.
                listParticipantsByExternalServiceAgreementIds(externalServiceAgreementIds);

        LOGGER.info("Response bodies of the participants {}", persistenceExtendedParticipants);
        return persistenceExtendedParticipants
            .stream()
            .collect(
                groupingBy(PersistenceExtendedParticipant::getExternalId,
                    mapping(item -> item, toSet())
                )
            );
    }

    /**
     * Retrieves Participants.
     *
     * @param externalServiceAgreementId - list of service agreement external ids
     * @return set of participant ids which share accounts
     */
    public Set<String> getSharingAccountsParticipantIdsForServiceAgreement(String externalServiceAgreementId) {
        LOGGER.info("Retrieving participants ids for external service agreement ids {}", externalServiceAgreementId);
        if (Strings.isNullOrEmpty(externalServiceAgreementId)) {
            LOGGER.info("No service agreement id is present: {}", externalServiceAgreementId);
            return Collections.emptySet();
        }
        return persistenceServiceAgreementService.
            listParticipantsByExternalServiceAgreementIds(Sets.newHashSet(externalServiceAgreementId))
            .stream()
            .filter(PersistenceExtendedParticipant::isSharingAccounts)
            .map(PersistenceExtendedParticipant::getId)
            .collect(Collectors.toSet());
    }

}
