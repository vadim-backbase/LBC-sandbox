package com.backbase.accesscontrol.business.flows.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.mappers.SubEntitiesPersistenceToRecordsDtoMapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.ExceptionUtil;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetSubLegalEntitlesFlow extends
    AbstractFlow<GetLegalEntitiesRequestDto, RecordsDto<SubEntitiesPostResponseBody>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetSubLegalEntitlesFlow.class);

    private UserContextUtil userContextUtil;
    private UserManagementService userManagementService;
    private AccessControlValidator accessControlValidator;
    private SubEntitiesPersistenceToRecordsDtoMapper subEntitiesPersistenceToRecordsDtoMapper;
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Override
    protected RecordsDto<SubEntitiesPostResponseBody> execute(GetLegalEntitiesRequestDto legalEntitiesRequestDto) {
        LOGGER.info(
            "Trying to fetch a list of all Legal Entity's children of the provided legal entity id or logged User");
        String internalLegalEntityId = legalEntitiesRequestDto.getParentEntityId();
        String getAuthenticatedUserName = userContextUtil.getAuthenticatedUserName();

        if (isNull(internalLegalEntityId)) {
            internalLegalEntityId = getInternalLegalEntityIdByUser(getAuthenticatedUserName);
        }

        validateHierarchy(internalLegalEntityId);

        Page<LegalEntity> legalEntities = persistenceLegalEntityService
            .getSubEntities(internalLegalEntityId,
                new SearchAndPaginationParameters(
                    legalEntitiesRequestDto.getFrom(),
                    legalEntitiesRequestDto.getSize(),
                    legalEntitiesRequestDto.getQuery(),
                    legalEntitiesRequestDto.getCursor()),
                legalEntitiesRequestDto.getExcludeIds());

        return subEntitiesPersistenceToRecordsDtoMapper
            .toPresentation(legalEntities);
    }

    private void validateHierarchy(String internalLegalEntityId) {
        if (accessControlValidator
            .userHasNoAccessToEntitlementResource(internalLegalEntityId, AccessResourceType.NONE)) {

            throw ExceptionUtil.getForbiddenException(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode());
        }
    }

    private String getInternalLegalEntityIdByUser(String externalUserId) {
        LOGGER.info("Trying to fetch legal entity by external user id {}", externalUserId);
        GetUser user = userManagementService
            .getUserByExternalId(externalUserId);
        return Objects.requireNonNull(user).getLegalEntityId();
    }
}
