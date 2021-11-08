package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;

public interface LegalEntityJpaRepositoryCustom {

    Optional<LegalEntity> findById(String id, String entityGraphName);

    List<LegalEntity> findDistinctByParentIsNull(String entityGraphName);

    Optional<LegalEntity> findByExternalId(String externalId, String entityGraphName);

    List<LegalEntity> findDistinctByExternalIdIn(List<String> ids, String graphName);

    Page<LegalEntity> findAllSubEntities(String ancestorId,
        SearchAndPaginationParameters searchAndPaginationParameters, Collection<String> excludeIds,
        String entityGraphName);

    Boolean checkIfNotParticipantInCustomServiceAgreement(String externalId);

    Boolean checkIfExistsUsersFromLeWithAssignedPermissionsInMsa(String externalId);

    Boolean checkIsExistsUsersFromLeWithPendingPermissionsInMsa(String externalId);

    Boolean checkIfNotCreatorOfAnyCsa(String externalId);

    Page<LegalEntity> findAllLegalEntitiesSegmentation(
        SearchAndPaginationParameters searchAndPaginationParameters,
        Set<String> dataGroupIds, String graphLegalEntityWithAdditions);
}
