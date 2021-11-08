package com.backbase.accesscontrol.business.serviceagreement.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_108;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_109;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_110;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.ServiceAgreementParticipantDto;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServiceAgreementBusinessRulesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementBusinessRulesService.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private PersistenceLegalEntityService persistenceLegalEntityService;
    private UserManagementService userManagementService;
    private ApplicationProperties applicationProperties;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    /**
     * Method that checks if participants share at least users or accounts.
     *
     * @param serviceAgreementParticipants participants in service agreement
     * @return true if not all participants are sharing users or accounts.
     */
    public boolean isInvalidParticipant(List<ServiceAgreementParticipantDto> serviceAgreementParticipants) {
        return !serviceAgreementParticipants.stream()
            .allMatch(participant -> participant.getSharingAccounts() || participant.getSharingUsers());
    }

    /**
     * Checks if service agreement is in pending state.
     *
     * @param serviceAgreementId service agreement id
     * @return true if it is in pending state adn aproval is on.
     */
    public boolean isServiceAgreementInPendingState(String serviceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled()
            && persistenceServiceAgreementService.isServiceAgreementInPendingState(serviceAgreementId);
    }

    public boolean isServiceAgreementInPendingStateByExternalId(String externalServiceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled()
            && persistenceServiceAgreementService
            .isServiceAgreementInPendingStateByExternalId(externalServiceAgreementId);
    }

    /**
     * Method that checks if at least one participants share accounts.
     *
     * @param participants participants in service agreement
     * @return true if there is not participant who is sharing accounts.
     */
    public boolean participantSharingUsersNotExists(List<ServiceAgreementParticipantDto> participants) {
        return participants.stream()
            .noneMatch(ServiceAgreementParticipantDto::getSharingUsers);
    }

    /**
     * Method that checks if at least one participants share users.
     *
     * @param participants list of participants in service agreement
     * @return true if there is not participant who is sharing users.
     */
    public boolean participantSharingAccountsNotExists(List<ServiceAgreementParticipantDto> participants) {
        return participants.stream()
            .noneMatch(ServiceAgreementParticipantDto::getSharingAccounts);
    }

    /**
     * Return true if one of the admins not belongs to the provider/consumer.
     *
     * @param usersByLegalEntity list of existing users by Legal Entity.
     * @param adminsToAssign     list of admins to check for assignment.
     * @return true if one of the admins not belongs to the provider/consumer.
     */
    public boolean adminUsersNotBelongToTheGivenLegalEntities(Map<String, Set<String>> usersByLegalEntity,
        Map<String, Set<String>> adminsToAssign) {
        return !adminsToAssign.entrySet().stream().allMatch(adminsOfParticipant -> {
            Set<String> userIds = usersByLegalEntity.get(adminsOfParticipant.getKey());
            return adminsOfParticipant.getValue().isEmpty() || (userIds != null && userIds
                .containsAll(adminsOfParticipant.getValue()));
        });
    }

    /**
     * Checks if service agreement with given external id already exists.
     *
     * @param putData          body containing name, description and external id
     * @param serviceAgreement service agreement
     * @return true if service agreement with provided external id exist, otherwise false
     */
    public boolean serviceAgreementWithGivenExternalIdAlreadyExistsAndNotNull(ServiceAgreementPutRequestBody putData,
        ServiceAgreementItem serviceAgreement) {

        if (Optional.ofNullable(putData.getExternalId()).isPresent()) {
            return checkIfServiceAgreementWithExternalIdExists(putData, serviceAgreement);
        }
        return false;
    }

    /**
     * Checks if service agreement is root master service agreement.
     *
     * @param serviceAgreement service agreement to be checked
     * @return true if service agreement is root master service agreement, otherwise false
     */
    public boolean isServiceAgreementRootMasterServiceAgreement(ServiceAgreementItem serviceAgreement) {
        boolean isRootMasterServiceAgreement = false;
        if (Boolean.TRUE.equals(serviceAgreement.getIsMaster())) {
            LegalEntity legalEntityById = persistenceLegalEntityService
                .getLegalEntityById(serviceAgreement.getCreatorLegalEntity());
            isRootMasterServiceAgreement = legalEntityById.getParent() == null;
        }
        return isRootMasterServiceAgreement;
    }

    /**
     * Checks if there is duplicate participant id in the post body.
     *
     * @param serviceAgreementParticipants - service agreement participants
     * @return true if there is a duplicate participant, false otherwise
     */
    public boolean isDuplicateParticipant(List<String> serviceAgreementParticipants) {
        return serviceAgreementParticipants.stream().distinct().count() != serviceAgreementParticipants.size();
    }

    private boolean checkIfServiceAgreementWithExternalIdExists(ServiceAgreementPutRequestBody putData,
        ServiceAgreementItem serviceAgreement) {
        LOGGER.info("Checking if service agreement with external id: {} exists", putData.getExternalId());
        Optional<ServiceAgreement> serviceAgreementByExternalId = persistenceServiceAgreementService
            .getServiceAgreementResponseBodyByExternalId(putData.getExternalId());

        return (serviceAgreementByExternalId.isPresent()
            && !serviceAgreementByExternalId.get().getId().equals(serviceAgreement.getId()));
    }

    /**
     * Checks if exists service agreement with given external id in pending state.
     *
     * @param externalServiceAgreementId service agreement external id
     */
    public boolean existsPendingServiceAgreementWithExternalId(String externalServiceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled() && persistenceServiceAgreementService
            .existsPendingServiceAgreementWithExternalId(externalServiceAgreementId);
    }

    /**
     * Return true if one of the admins not belongs to the legal entity.
     *
     * @param adminsToBeUpdated list of existing users
     * @param participants      list of participants with admins to be updated
     * @return true if one of the admins not belongs to the corresponding legal entity.
     */
    public boolean adminsDoNotBelongToParticipantsLegalEntities(
        List<GetUser> adminsToBeUpdated,
        List<Participant> participants) {
        Map<String, Set<String>> usersByLegalEntityId = userManagementService
            .getUsersByLegalEntityId(adminsToBeUpdated);
        return !participants.stream().allMatch(participant -> {
            Set<String> userIds = usersByLegalEntityId.get(participant.getId());
            return participant.getAdmins() == null
                || (participant.getAdmins().isEmpty() && userIds == null)
                || (userIds != null && userIds.containsAll(participant.getAdmins()));
        });
    }

    /**
     * Checks if the validity period of the service agreement is in right date range.
     *
     * @param from  beginning of the validity period
     * @param until end of the validity period
     * @return true / false
     */
    public boolean isPeriodValid(Date from, Date until) {

        return Objects.isNull(from) || Objects.isNull(until) || until.after(from);
    }

    private boolean isThereJobRolePendingInTheServiceAgreement(String serviceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled() && persistenceServiceAgreementService
            .existsPendingJobRoleInServiceAgreement(serviceAgreementId);
    }

    private boolean isThereDeleteJobRolePendingInTheServiceAgreement(String serviceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled() && persistenceServiceAgreementService
            .existsPendingDeleteJobRoleInServiceAgreement(serviceAgreementId);
    }

    private boolean isThereDataGroupPendingInTheServiceAgreement(String serviceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled() && persistenceServiceAgreementService
            .existsPendingDataGroupInServiceAgreement(serviceAgreementId);
    }

    private boolean isThereDeleteDataGroupPendingInTheServiceAgreement(String serviceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled() && persistenceServiceAgreementService
            .existsPendingDeleteDataGroupInServiceAgreement(serviceAgreementId);
    }

    private boolean isTherePermissionsPendingInTheServiceAgreement(String serviceAgreementId) {
        return applicationProperties.getApproval().getValidation().isEnabled() && persistenceServiceAgreementService
            .existsPendingPermissionsInServiceAgreement(serviceAgreementId);
    }

    /**
     * Check for pending validation  for external service agreement id.
     *
     * @param serviceAgreementExternalId external service agreement id
     */
    public void checkPendingValidationsInServiceAgreementExternalServiceAgreementId(String serviceAgreementExternalId) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            Optional<ServiceAgreement> serviceAgreementOptional = serviceAgreementJpaRepository
                .findByExternalId(serviceAgreementExternalId);
            if (serviceAgreementOptional.isPresent()) {
                String serviceAgreementId = serviceAgreementOptional.get().getId();
                checkPendingValidationsInServiceAgreement(serviceAgreementId);
            }
        }
    }

    /**
     * Check for pending validation  for internal service agreement id.
     *
     * @param serviceAgreementId service agreement id
     */
    public void checkPendingValidationsInServiceAgreement(String serviceAgreementId) {
        if (isThereJobRolePendingInTheServiceAgreement(serviceAgreementId)) {
            LOGGER.warn("You cannot manage this service agreement with id {} , while there is a pending function group",
                serviceAgreementId);
            throw getBadRequestException(ERR_AG_108.getErrorMessage(), ERR_AG_108.getErrorCode());
        }
        if (isThereDataGroupPendingInTheServiceAgreement(serviceAgreementId)) {
            LOGGER.warn("You cannot manage this service agreement with id {} , while there is a pending data group"
                , serviceAgreementId);
            throw getBadRequestException(ERR_AG_109.getErrorMessage(), ERR_AG_109.getErrorCode());
        }
        if (isTherePermissionsPendingInTheServiceAgreement(serviceAgreementId)) {
            LOGGER.warn(
                "You cannot manage this service agreement with id {}, while there is a pending permission assignment",
                serviceAgreementId);
            throw getBadRequestException(ERR_AG_110.getErrorMessage(), ERR_AG_110.getErrorCode());
        }
    }

    public void checkPendingDeleteOfFunctionOrDataGroupInServiceAgreementExternalId(String serviceAgreementExternalId) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            Optional<ServiceAgreement> serviceAgreementOptional = serviceAgreementJpaRepository
                .findByExternalId(serviceAgreementExternalId);
            if (serviceAgreementOptional.isPresent()) {
                String serviceAgreementId = serviceAgreementOptional.get().getId();
                checkPendingDeleteOfFunctionOrDataGroupInServiceAgreement(serviceAgreementId);
            }
        }
    }

    /**
     * Checks if there is pending delete for data group or function group in a given service agreement.
     *
     * @param serviceAgreementId - service agreement id.
     */
    public void checkPendingDeleteOfFunctionOrDataGroupInServiceAgreement(String serviceAgreementId) {
        if (isThereDeleteDataGroupPendingInTheServiceAgreement(serviceAgreementId)) {
            LOGGER.warn("You cannot manage this service agreement with id {} , while there is a pending data group"
                , serviceAgreementId);
            throw getBadRequestException(ERR_AG_109.getErrorMessage(), ERR_AG_109.getErrorCode());
        }
        if (isThereDeleteJobRolePendingInTheServiceAgreement(serviceAgreementId)) {
            LOGGER.warn("You cannot manage this service agreement with id {} , while there is a pending function group",
                serviceAgreementId);
            throw getBadRequestException(ERR_AG_108.getErrorMessage(), ERR_AG_108.getErrorCode());
        }
    }
}
