package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.repository.IdProjection;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ValidateLegalEntityHierarchyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateLegalEntityHierarchyService.class);

    private LegalEntityJpaRepository legalEntityJpaRepository;

    /**
     * Validate legal entity hierarchy.
     *
     * @param creatorLegalEntity - creator legal entity.
     * @param newLegalEntities   list of legal entities to be validated whether they belong under the creator
     */
    public void validateLegalEntityHierarchy(LegalEntity creatorLegalEntity, Set<String> newLegalEntities) {

        Set<String> allValidLegalEntitiesIds = new HashSet<>(
            getLegalEntityHierarchy(creatorLegalEntity.getId(), new ArrayList<>(newLegalEntities)));
        if (!allValidLegalEntitiesIds.containsAll(newLegalEntities)) {
            throw getBadRequestException(QueryErrorCodes.ERR_ACQ_039.getErrorMessage(),
                QueryErrorCodes.ERR_ACQ_039.getErrorCode());
        }
    }

    /**
     * Validate legal entity hierarchy.
     *
     * @param descendantLegalEntityId - descendant legal entity.
     * @param ancestorLegalEntities   list of legal entities to be validated whether they belong under the creator
     */
    public void validateLegalEntityAncestorHierarchy(String descendantLegalEntityId,
        Set<String> ancestorLegalEntities) {
        if (!ancestorLegalEntities.isEmpty()) {
            int response = getLegalEntityAncestorHierarchy(descendantLegalEntityId,
                new ArrayList<>(ancestorLegalEntities));
            if (response != ancestorLegalEntities.size()) {
                LOGGER.warn("Validation failed, legalEntity is not in hierarchy");
                throw new ValidationException("Validation failed, legalEntity is not in hierarchy");
            }
        }
    }

    /**
     * Retrieve all legalEntities that exist in the list of participants.
     *
     * @param creatorLegalEntityId creator legal entity id
     * @param participants         list of participants
     * @return list of all legal entities that exist in the list of participants
     */
    public List<String> getLegalEntityHierarchy(String creatorLegalEntityId, List<String> participants) {
        int batchSize = 1000;
        List<String> response = new ArrayList<>();
        response.add(creatorLegalEntityId);
        response.addAll(
            IntStream.range(0, (participants.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> participants
                    .subList(i * batchSize, Math.min(participants.size(), (i + 1) * batchSize))
                )
                .map(batch -> getSubEntities(creatorLegalEntityId, batch))
                .flatMap(List::stream)
                .collect(toList()));
        return response;
    }

    private List<String> getSubEntities(String legalEntityId, List<String> participants) {
        return legalEntityJpaRepository
            .findByLegalEntityAncestorsIdAndIdIn(legalEntityId, participants)
            .stream()
            .map(IdProjection::getId)
            .collect(toList());
    }

    /**
     * Retrieve all legalEntities that exist in the list of participants.
     *
     * @param legalEntityId descendant legal entity id
     * @param ancestors     list of ancestors legal entity ids
     * @return list of all legal entities that exist in the list of ancestors
     */
    public int getLegalEntityAncestorHierarchy(String legalEntityId, List<String> ancestors) {
        int batchSize = 1000;
        return IntStream.range(0, (ancestors.size() + batchSize - 1) / batchSize)
            .mapToObj(i -> ancestors
                .subList(i * batchSize, Math.min(ancestors.size(), (i + 1) * batchSize))
            )
            .mapToInt(batch -> getAncestorSubEntities(legalEntityId, batch))
            .sum();
    }

    private int getAncestorSubEntities(String legalEntityId, List<String> ancestors) {
        return legalEntityJpaRepository
            .countByLegalEntityAncestorsIdInAndId(ancestors, legalEntityId);
    }


}
