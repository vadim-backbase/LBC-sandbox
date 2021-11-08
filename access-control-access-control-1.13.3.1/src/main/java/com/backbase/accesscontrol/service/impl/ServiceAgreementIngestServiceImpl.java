package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_057;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_058;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_059;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_060;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_061;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_062;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_095;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_005;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ParticipantUser;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.dto.ServiceAgreementData;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.accesscontrol.service.ServiceAgreementIngestService;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ServiceAgreementIngestServiceImpl implements ServiceAgreementIngestService {

    private LegalEntityJpaRepository legalEntityJpaRepository;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private PermissionSetService permissionSetService;
    private DateTimeService dateTimeService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String ingestServiceAgreement(ServiceAgreementData<ServiceAgreementIngestPostRequestBody> request) {
        log.debug("Ingesting service agreement");
        ServiceAgreementIngestPostRequestBody requestData = request.getRequest();
        Map<String, GetUser> usersByExternalId = request
            .getUsersByExternalId();
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName(requestData.getName());
        serviceAgreement.setExternalId(requestData.getExternalId());
        serviceAgreement.setState(ServiceAgreementState.valueOf(requestData.getStatus().name()));
        serviceAgreement.setMaster(requestData.getIsMaster());
        serviceAgreement.setDescription(requestData.getDescription());
        serviceAgreement.addParticipant(
            createParticipants(
                requestData.getParticipantsToIngest(),
                getLegalEntities(requestData.getParticipantsToIngest()),
                usersByExternalId)
        );
        serviceAgreement.setStartDate(
            dateTimeService
                .getStartDateFromDateAndTime(requestData.getValidFromDate(), requestData.getValidFromTime()));
        serviceAgreement.setEndDate(
            dateTimeService
                .getEndDateFromDateAndTime(requestData.getValidUntilDate(), requestData.getValidUntilTime()));
        serviceAgreement.setStateChangedAt(new Date());
        serviceAgreement.setAdditions(requestData.getAdditions());

        LegalEntity legalEntity = extractLegalEntity(requestData.getCreatorLegalEntity());
        serviceAgreement.setCreatorLegalEntity(legalEntity);

        serviceAgreement.setPermissionSetsRegular(getAssignablePermissionSets(requestData.getRegularUserAps(), true));
        serviceAgreement.setPermissionSetsAdmin(getAssignablePermissionSets(requestData.getAdminUserAps(), false));

        log.debug("Invoke creating service agreement ");
        ServiceAgreement createdServiceAgreement = persistenceServiceAgreementService.create(serviceAgreement);
        log.debug("Created service agreement with id {}", createdServiceAgreement.getId());
        return createdServiceAgreement.getId();
    }

    private LegalEntity extractLegalEntity(String creatorLegalEntity) {
        if (creatorLegalEntity != null) {
            return legalEntityJpaRepository.findById(creatorLegalEntity)
                .orElseThrow(() -> {
                    log.warn("Legal entity with id {} does not exist", creatorLegalEntity);
                    return getNotFoundException(ERR_ACQ_005.getErrorMessage(), ERR_ACQ_005.getErrorCode());
                });
        } else {
            List<LegalEntity> rootLegalEntities = legalEntityJpaRepository.findDistinctByParentIsNull(null);
            if (rootLegalEntities.size() != 1) {
                throw getInternalServerErrorException(ERR_ACC_062.getErrorMessage());
            }
            return rootLegalEntities.get(0);
        }
    }

    private Set<AssignablePermissionSet> getAssignablePermissionSets(PresentationUserApsIdentifiers requestData,
        boolean isRegularUser) {
        Optional<PresentationUserApsIdentifiers> permissionSetsRequestData = Optional.ofNullable(requestData);

        return permissionSetsRequestData.map(identifier -> getPermissionSets(identifier, isRegularUser))
            .orElseGet(() -> permissionSetService.getAssignablePermissionSetsById(new HashSet<>(), isRegularUser));
    }

    private Set<AssignablePermissionSet> getPermissionSets(PresentationUserApsIdentifiers identifier,
        boolean isRegularUser) {
        if ((identifier.getIdIdentifiers().isEmpty() && identifier.getNameIdentifiers().isEmpty())
            || (!identifier.getIdIdentifiers().isEmpty() && !identifier.getNameIdentifiers().isEmpty())) {
            throw getBadRequestException(ERR_ACC_095.getErrorMessage(), ERR_ACC_095.getErrorCode());
        }

        if (identifier.getIdIdentifiers().isEmpty()) {
            return permissionSetService.getAssignablePermissionSetsByName(identifier.getNameIdentifiers(), true);
        } else {
            return permissionSetService.getAssignablePermissionSetsById(
                identifier.getIdIdentifiers().stream().map(BigDecimal::longValue).collect(toSet()), isRegularUser);
        }
    }

    private List<LegalEntity> getLegalEntities(Set<ParticipantIngest> participantsToIngest) {
        log.debug("Getting legal entities");
        List<String> legalEntityExternalIds = getLegalEntityExternalIds(participantsToIngest);
        if (legalEntityExternalIds.isEmpty()) {
            log.debug("Returning empty list, no legal entities are present");
            return Collections.emptyList();
        }
        List<LegalEntity> distinctByExternalIdIn = legalEntityJpaRepository
            .findDistinctByExternalIdIn(legalEntityExternalIds, null);
        log.debug("Retrieved distinct legal entities ");
        if (distinctByExternalIdIn.size() != legalEntityExternalIds.size()) {
            log.warn("Requested and returned legal entities mismatch requested {}, returned {}",
                legalEntityExternalIds.size(),
                distinctByExternalIdIn.size());
            throw getBadRequestException(ERR_ACC_060.getErrorMessage(), ERR_ACC_060.getErrorCode());
        }
        return distinctByExternalIdIn;
    }

    private List<Participant> createParticipants(Set<ParticipantIngest> participantsToIngest,
        List<LegalEntity> distinctByExternalIdIn,
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalId) {
        log.debug("Transforming participants");
        if (participantsToIngest != null && participantsToIngest.size() != distinctByExternalIdIn.size()) {
            log.warn("Duplicated participants in service agreement");
            throw getBadRequestException(ERR_ACC_061.getErrorMessage(), ERR_ACC_061.getErrorCode());
        }
        Map<String, LegalEntity> legalEntityMap = getLegalEntitiesExternalIdMap(distinctByExternalIdIn);
        List<Participant> participantList = getSetItems(participantsToIngest)
            .stream()
            .map(pa -> createParticipant(legalEntityMap, pa, usersByExternalId))
            .collect(Collectors.toList());
        log.debug("Participants list created");
        return participantList;
    }

    private Map<String, LegalEntity> getLegalEntitiesExternalIdMap(List<LegalEntity> distinctByExternalIdIn) {
        return distinctByExternalIdIn
            .stream()
            .collect(toMap(LegalEntity::getExternalId, identity()));
    }

    private Participant createParticipant(Map<String, LegalEntity> collect, ParticipantIngest pa,
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalId) {
        LegalEntity legalEntity = collect.get(pa.getExternalId());
        log.debug("Transforming participant");
        if (legalEntity == null) {
            log.warn("Missing legal entity for participant");
            throw getBadRequestException(ERR_ACC_059.getErrorMessage(), ERR_ACC_059.getErrorCode());
        }
        Participant participant = new Participant();
        participant.setLegalEntity(legalEntity);
        participant.setParticipantUsers(getParticipantUsers(pa, participant, usersByExternalId));
        participant.setShareAccounts(pa.getSharingAccounts());
        participant.setShareUsers(pa.getSharingUsers());
        addParticipantAdmins(participant, pa.getAdmins(), usersByExternalId);
        return participant;
    }

    private void addParticipantAdmins(Participant participant, Set<String> admins,
        Map<String, GetUser> usersByExternalId) {

        for (String u : admins) {
            String userId = getUserId(participant, usersByExternalId.get(u.toLowerCase()), ERR_ACC_058);
            participant.addAdmin(userId);
        }
    }

    private <E> Set<E> getSetItems(Set<E> nullableSet) {
        return Optional.ofNullable(nullableSet).orElseGet(HashSet::new);
    }

    private Set<ParticipantUser> getParticipantUsers(ParticipantIngest pa, Participant participant,
        Map<String, GetUser> usersByExternalId) {

        return pa.getUsers()
            .stream()
            .map(user -> getUserId(participant, usersByExternalId.get(user.toLowerCase()), ERR_ACC_057))
            .map(userId -> new ParticipantUser(participant, userId))
            .collect(toSet());
    }

    private String getUserId(Participant participant, GetUser u,
        CommandErrorCodes errorCode) {

        if (!participant.getLegalEntity().getId().equals(u.getLegalEntityId())) {
            throw getBadRequestException(errorCode.getErrorMessage(), errorCode.getErrorCode());
        }
        return u.getId();
    }

    private List<String> getLegalEntityExternalIds(Set<ParticipantIngest> participantsToIngest) {
        return getSetItems(participantsToIngest).stream()
            .map(ParticipantIngest::getExternalId)
            .distinct()
            .collect(Collectors.toList());
    }
}
