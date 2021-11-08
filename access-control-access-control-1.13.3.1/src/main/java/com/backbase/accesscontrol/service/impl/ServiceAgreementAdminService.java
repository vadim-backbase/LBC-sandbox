package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADMINS_AND_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_047;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_048;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_049;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementAdmin;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementAdminJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ServiceAgreementAdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementAdminService.class);


    private ServiceAgreementSystemFunctionGroupService serviceAgreementSystemFunctionGroupService;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private UserContextService userContextService;
    private UserAccessFunctionGroupService userAccessFunctionGroupService;
    private ParticipantJpaRepository participantJpaRepository;
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    private ServiceAgreementAdminJpaRepository serviceAgreementAdminJpaRepository;

    /**
     * Adds admin in service agreement for given participant.
     *
     * @param serviceAgreementExternalId -service agreement external id
     * @param userInternalId             - user id
     * @param userLegalEntityId          - user's legal entity id
     * @return Service agreement internal id
     */
    @Transactional
    public String addAdminInServiceAgreementBatch(String serviceAgreementExternalId, String userInternalId,
        String userLegalEntityId) {
        ServiceAgreement serviceAgreement = getServiceAgreement(serviceAgreementExternalId);
        Optional<Participant> participantOpt = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityId(serviceAgreementExternalId,
                userLegalEntityId);
        Participant participant = participantOpt
            .orElseThrow(getBadRequestExceptionSupplier(ERR_ACC_047.getErrorCode(), ERR_ACC_047.getErrorMessage()));
        checkIfUserIsAdminOnServiceAgreement(
            (participantItems, userId) -> findParticipantAdmin(participantItems, userInternalId).isPresent(),
            participant, userInternalId, ERR_ACC_048);

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
            .getServiceAgreementFunctionGroups(serviceAgreement);

        participant.addAdmin(userInternalId);
        participantJpaRepository.saveAndFlush(participant);
        assignPermissionsForAdmin(userInternalId, serviceAgreementFunctionGroups);

        return serviceAgreement.getId();
    }

    /**
     * Update service agreement's admins.
     *
     * @param serviceAgreement - Service agreement instance
     * @param participants     - List of service agreement participants
     */
    @Transactional
    public void updateAdmins(ServiceAgreement serviceAgreement,
        Set<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant> participants) {
        Map<String, Set<String>> participantsAdmins = new HashMap<>();
        for (com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant p : participants) {
            participantsAdmins.put(p.getId(), p.getAdmins() != null ? p.getAdmins() : new HashSet<>());
        }
        updateParticipantsAdmins(serviceAgreement, participantsAdmins);
    }

    /**
     * Method that updates the admins on service agreement and assigns/revokes permissions to admins that should be
     * added/removed from service agreement providers and consumers.
     *
     * @param serviceAgreementId   -  ID of the service agreement
     * @param adminsPutRequestBody - request body containing admins to be updated by participants.
     */
    @Transactional
    public void updateAdmins(String serviceAgreementId, AdminsPutRequestBody adminsPutRequestBody) {
        ServiceAgreement serviceAgreement = getById(serviceAgreementId,
            SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADMINS_AND_FUNCTION_GROUPS);

        Map<String, Set<String>> participantsAdmins = adminsPutRequestBody.getParticipants().stream()
            .collect(Collectors
                .toMap(LegalEntityAdmins::getId, participant -> Sets.newHashSet(participant.getAdmins())));
        validateLegalEntitiesAreParticipantsOfTheServiceAgreement(participantsAdmins.keySet(),
            serviceAgreement.getParticipants());

        updateParticipantsAdmins(serviceAgreement, participantsAdmins);
        serviceAgreementJpaRepository.save(serviceAgreement);
    }

    /**
     * Return the list of admins in the service agreement.
     *
     * @param serviceAgreementId - service agreement id
     * @return - list of admins
     */
    @Transactional(readOnly = true)
    public ServiceAgreementAdminsGetResponseBody getServiceAgreementAdmins(String serviceAgreementId) {
        checkIfServiceAgreementExists(serviceAgreementId);
        List<Participant> participants = participantJpaRepository
            .findByServiceAgreementId(serviceAgreementId, GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY);
        Set<String> admins = participants
            .stream()
            .map(Participant::getAdmins)
            .flatMap(e -> e.keySet().stream())
            .collect(Collectors.toSet());
        return new ServiceAgreementAdminsGetResponseBody().withAdmins(admins);
    }

    /**
     * Removes admin from service agreement for given participant.
     *
     * @param serviceAgreementExternalId - Service agreement external id
     * @param userInternalId             - user internal id
     * @param userLegalEntityId          - user's legal entity intenal id
     * @return Service agreement id
     */
    @Transactional
    public String removeAdminFromServiceAgreementBatch(String serviceAgreementExternalId, String userInternalId,
        String userLegalEntityId) {
        ServiceAgreement serviceAgreement = getServiceAgreement(serviceAgreementExternalId);
        Optional<Participant> participants = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityId(serviceAgreementExternalId, userLegalEntityId);
        Participant participant = participants
            .orElseThrow(getBadRequestExceptionSupplier(ERR_ACC_047.getErrorCode(), ERR_ACC_047.getErrorMessage()));

        checkIfUserIsAdminOnServiceAgreement(
            (participantItem, userId) -> !findParticipantAdmin(participantItem, userInternalId).isPresent(),
            participant, userInternalId, ERR_ACC_049);

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
            .getServiceAgreementFunctionGroups(serviceAgreement);

        removeAdminsForParticipant(participant, userInternalId, serviceAgreementFunctionGroups);
        participantJpaRepository.save(participant);
        removeSystemFunctionGroupIfNoAdmins(serviceAgreementFunctionGroups);
        return serviceAgreement.getId();
    }

    /**
     * Adds addmins system permission in the service agreement.
     *
     * @param serviceAgreement - service agreement
     */
    @Transactional
    public void addAdminPermissions(ServiceAgreement serviceAgreement) {
        Map<String, Participant> participants = serviceAgreement.getParticipants();
        if (participants.isEmpty()) {
            return;
        }
        if (participants.values().stream().allMatch(participant -> participant.getAdmins().isEmpty())) {
            return;
        }

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
            .getServiceAgreementFunctionGroups(serviceAgreement);
        for (Entry<String, Participant> pair : participants.entrySet()) {
            Participant participant = pair.getValue();
            for (String userId : participant.getAdmins().keySet()) {
                assignPermissionsForAdmin(userId, serviceAgreementFunctionGroups);
            }
        }
    }

    /**
     * Adds admins system permission participants admin users.
     *
     * @param participants - list of participants
     */
    @Transactional
    public void addParticipantAdminsPermissions(List<Participant> participants) {
        if (CollectionUtils.isEmpty(participants)) {
            return;
        }

        for (Participant participant : participants) {
            ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
                .getServiceAgreementFunctionGroups(participant.getServiceAgreement());
            for (String userId : participant.getAdmins().keySet()) {
                assignPermissionsForAdmin(userId, serviceAgreementFunctionGroups);
            }
        }
    }

    private void validateLegalEntitiesAreParticipantsOfTheServiceAgreement(Set<String> legalEntitiesFromRequest,
        Map<String, Participant> serviceAgreementParticipants) {
        if (!serviceAgreementParticipants.keySet().containsAll(legalEntitiesFromRequest)) {
            LOGGER.warn("Some legal entity id is not participant in the service agreement");
            throw getBadRequestException(ERR_ACC_047.getErrorMessage(), ERR_ACC_047.getErrorCode());
        }
    }

    @Transactional
    public void updateParticipantsAdmins(ServiceAgreement serviceAgreement,
        Map<String, Set<String>> participantAdmins) {
        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = null;

        for (Entry<String, Set<String>> participantEntry : participantAdmins.entrySet()) {
            String participantId = participantEntry.getKey();
            Participant participant = serviceAgreement.getParticipants().get(participantId);

            if (isNull(participant)) {
                continue;
            }

            List<String> adminsToAdd = getAdminsToAdd(participant, participantEntry.getValue());
            List<String> adminsToRemove = getAdminsToRemove(participant, participantEntry.getValue());

            if (!adminsToAdd.isEmpty() || !adminsToRemove.isEmpty()) {
                serviceAgreementFunctionGroups = getSystemFunctionGroup(serviceAgreementFunctionGroups,
                    serviceAgreement);
            }

            for (String userId : adminsToAdd) {
                participant.addAdmin(userId);
                assignPermissionsForAdmin(userId, serviceAgreementFunctionGroups);
            }

            for (String userId : adminsToRemove) {
                removeAdminsForParticipant(participant, userId, serviceAgreementFunctionGroups);
            }
        }

        removeSystemFunctionGroupIfNoAdmins(serviceAgreement, serviceAgreementFunctionGroups);
    }

    private ServiceAgreementFunctionGroups getSystemFunctionGroup(ServiceAgreementFunctionGroups systemFunctionGroups,
        ServiceAgreement serviceAgreement) {
        return Optional.ofNullable(systemFunctionGroups).orElseGet(
            () -> serviceAgreementSystemFunctionGroupService.getServiceAgreementFunctionGroups(serviceAgreement));
    }

    private void removeSystemFunctionGroupIfNoAdmins(ServiceAgreement serviceAgreement,
        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups) {
        if (nonNull(serviceAgreementFunctionGroups) && serviceAgreement.getParticipants().values().stream()
            .allMatch(participant -> participant.getAdmins().size() == 0)) {

            Optional<FunctionGroup> functionGroup = serviceAgreement.getFunctionGroups().stream()
                .filter(fg -> fg.getId().equals(serviceAgreementFunctionGroups.getSystemFunctionGroup()))
                .findFirst();

            if (functionGroup.isPresent()) {
                serviceAgreement.getFunctionGroups().remove(functionGroup.get());
            } else {
                functionGroupJpaRepository.deleteById(serviceAgreementFunctionGroups.getSystemFunctionGroup());
            }
        }
    }

    private void removeSystemFunctionGroupIfNoAdmins(ServiceAgreementFunctionGroups serviceAgreementFunctionGroups) {
        if (!serviceAgreementAdminJpaRepository
            .existsByParticipantServiceAgreement(serviceAgreementFunctionGroups.getServiceAgreement())) {
            functionGroupJpaRepository.deleteById(serviceAgreementFunctionGroups.getSystemFunctionGroup());
        }
    }

    private List<String> getAdminsToAdd(Participant participant, Set<String> admins) {
        List<String> adminsToAdd = Collections.emptyList();

        if (Objects.nonNull(participant)) {
            adminsToAdd = admins.stream().filter(userId -> !participant.getAdmins().containsKey(userId))
                .collect(Collectors.toList());
        }

        return adminsToAdd;
    }

    private void checkIfServiceAgreementExists(String serviceAgreementId) {
        if (!serviceAgreementJpaRepository.existsById(serviceAgreementId)) {
            LOGGER.warn("Service agreement with id {} does not exist", serviceAgreementId);
            throw getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
        }
    }

    private void removeAdminsForParticipant(Participant participant, String userId,
        ServiceAgreementFunctionGroups agreementFunctionGroups) {
        LOGGER.info("Assigning permissions to user: {} in service agreement {}, functionGroup {}", userId,
            agreementFunctionGroups.getServiceAgreement(), agreementFunctionGroups.getSystemFunctionGroup());
        userAccessFunctionGroupService.deleteSystemFunctionGroupFromUserAccess(
            agreementFunctionGroups.getSystemFunctionGroup(),
            userId,
            agreementFunctionGroups.getServiceAgreement());
        participant.getAdmins().remove(userId);
    }

    private void assignPermissionsForAdmin(String userId,
        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups) {

        LOGGER.info("Assigning permissions to user: {} in service agreement {}, functionGroup {}", userId,
            serviceAgreementFunctionGroups.getServiceAgreement(), serviceAgreementFunctionGroups
                .getSystemFunctionGroup());
        addAdminWithPermissions(serviceAgreementFunctionGroups.getServiceAgreement(), userId,
            serviceAgreementFunctionGroups.getSystemFunctionGroup());
    }

    private List<String> getAdminsToRemove(Participant participant, Set<String> admins) {
        if (Objects.isNull(participant)) {
            return Collections.emptyList();
        }

        return participant.getAdmins()
            .keySet()
            .stream()
            .filter(admin -> admins.stream().noneMatch(validAdmins -> validAdmins.equals(admin)))
            .collect(Collectors.toList());
    }

    private ServiceAgreement getById(String id, String entityGraphName) {
        LOGGER.info("Trying to get Service Agreement with id {}", id);
        Optional<ServiceAgreement> serviceAgreementOpt = serviceAgreementJpaRepository
            .findById(id, entityGraphName);

        return serviceAgreementOpt
            .orElseThrow(() -> {
                LOGGER.warn("Service agreement with id {} does not exist", id);
                return getNotFoundException(ERR_ACQ_006.getErrorMessage(),
                    ERR_ACQ_006.getErrorCode());
            });
    }

    private ServiceAgreement getServiceAgreement(String serviceAgreementExternalId) {
        LOGGER.info("Trying to get service agreement by external id");
        return serviceAgreementJpaRepository.findByExternalId(serviceAgreementExternalId)
            .orElseThrow(
                getBadRequestExceptionSupplier(ERR_ACQ_006.getErrorCode(), ERR_ACQ_006.getErrorMessage())
            );
    }


    private Supplier<BadRequestException> getBadRequestExceptionSupplier(String errorCode, String errorMessage) {
        return () ->
            getBadRequestException(errorMessage, errorCode);
    }

    private void addAdminWithPermissions(ServiceAgreement serviceAgreement, String admin,
        String systemFunctionGroupId) {
        UserContext userContext = userContextService.getOrCreateUserContext(admin, serviceAgreement.getId());
        LOGGER.info("User context id is {}", userContext.getId());
        userAccessFunctionGroupService
            .addSystemFunctionGroupToUserAccess(systemFunctionGroupId, serviceAgreement, userContext);
    }

    private void checkIfUserIsAdminOnServiceAgreement(BiPredicate<Participant, String> predicate,
        Participant participant, String userId, CommandErrorCodes errorCode) {
        if (predicate.test(participant, userId)) {
            throw getBadRequestException(errorCode.getErrorMessage(), errorCode.getErrorCode());
        }
    }

    private Optional<ServiceAgreementAdmin> findParticipantAdmin(Participant participant, String userId) {
        return participant.getAdmins().values()
            .stream()
            .filter(admin -> admin.getUserId().equals(userId))
            .findFirst();
    }
}