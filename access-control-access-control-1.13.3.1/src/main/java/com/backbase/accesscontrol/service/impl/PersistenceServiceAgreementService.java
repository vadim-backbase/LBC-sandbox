package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY;
import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS;
import static com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService.ARRANGEMENTS;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_069;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_106;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_108;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_109;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_010;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_038;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_039;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_040;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_042;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_043;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_044;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_045;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_046;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_054;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_056;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_063;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_064;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_065;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_066;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_067;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_068;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_069;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_070;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_053;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_055;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_056;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_057;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_062;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_063;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.IngestParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ParticipantUser;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.accesscontrol.mappers.ApprovalServiceAgreementMapper;
import com.backbase.accesscontrol.mappers.FunctionGroupMapperPersistence;
import com.backbase.accesscontrol.mappers.ServiceAgreementByPermissionSetMapper;
import com.backbase.accesscontrol.repository.ApprovalDataGroupDetailsJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.accesscontrol.service.TimeBoundValidatorService;
import com.backbase.accesscontrol.service.ValidateLegalEntityHierarchyService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.CommonUtils;
import com.backbase.accesscontrol.util.PrivilegesEnum;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementByPermissionSet;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceDataGroupDataItems;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreementDataGroups;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ExistingCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewMasterServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantInfo;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Tuple;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class PersistenceServiceAgreementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceServiceAgreementService.class);
    private static final String SERVICE_AGREEMENT_DOES_NOT_EXIST = "Service agreement with id {} does not exist";
    private static final String SERVICE_AGREEMENT_EXTERNAL_ID_DOES_NOT_EXIST =
        "Requested Service agreement does not exists.";
    private ParticipantJpaRepository participantJpaRepository;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;
    private LegalEntityJpaRepository legalEntityJpaRepository;
    private ServiceAgreementAdminService serviceAgreementAdminService;
    private UserAccessFunctionGroupService userAccessFunctionGroupService;
    private ServiceAgreementSystemFunctionGroupService serviceAgreementSystemFunctionGroupService;
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    private TimeBoundValidatorService timeBoundValidatorService;
    private PermissionSetService permissionSetService;
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    private ServiceAgreementByPermissionSetMapper serviceAgreementByPermissionSetMapper;
    private ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;
    private FunctionGroupMapperPersistence functionGroupMapperPersistence;
    private ValidateLegalEntityHierarchyService validateLegalEntityHierarchyService;
    private BusinessFunctionCache businessFunctionCache;
    private DateTimeService dateTimeService;
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    private ApprovalServiceAgreementRefJpaRepository approvalServiceAgreementRefJpaRepository;
    private ApprovalServiceAgreementMapper approvalServiceAgreementMapper;
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    private IngestParticipantUpdateRemoveDataValidationProcessor ingestParticipantUpdateRemoveDataValidationProcessor;
    private ApplicationProperties applicationProperties;
    private ApprovalDataGroupDetailsJpaRepository approvalDataGroupDetailsJpaRepository;
    private ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    private DataGroupJpaRepository dataGroupJpaRepository;
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    private ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;
    private UserContextJpaRepository userContextJpaRepository;

    /**
     * Gets all Legal Entities from the Participants(Consumers and Providers) for the Service Agreement with
     * serviceAgreementId.
     *
     * @param serviceAgreementId - service agreement ID.
     * @return ServiceAgreementParticipantsGetResponseBody identified by service agreement id.
     */
    @Transactional(readOnly = true)
    public List<Participant> getServiceAgreementParticipants(String serviceAgreementId) {
        LOGGER.info("Trying to get legal entity participants for a Service Agreement with id {}", serviceAgreementId);

        Optional<ServiceAgreement> serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED);

        if (!serviceAgreement.isPresent()) {
            LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreementId);
            throw getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
        }

        return new ArrayList<>(getParticipantsForServiceAgreement(
            participantJpaRepository.findByServiceAgreementId(serviceAgreementId, PARTICIPANT_WITH_LEGAL_ENTITY)
        ));
    }

    /**
     * Creates new Service Agreement and saves it to a relational database.
     *
     * @param serviceAgreementRequestBody - serviceAgreement that should be saved.
     * @param legalEntityId               - creator legal entity id.
     */
    @Transactional
    public ServiceAgreement save(ServiceAgreementPostRequestBody serviceAgreementRequestBody,
        String legalEntityId) {
        LOGGER.info("Trying to save Service Agreement");
        ServiceAgreement serviceAgreement = populateServiceAgreement(serviceAgreementRequestBody, legalEntityId);
        populateAdminsInServiceAgreementsParticipants(serviceAgreementRequestBody.getParticipants(), serviceAgreement);
        populateDefaultPermissionSets(serviceAgreement);

        validateRootServiceAgreementDoesNotHaveStartAndEndDate(serviceAgreement.isMaster(),
            serviceAgreement.getCreatorLegalEntity().getParent(), serviceAgreement.getStartDate(),
            serviceAgreement.getEndDate());
        validateTimePeriodOfServiceAgreement(serviceAgreement.getStartDate(), serviceAgreement.getEndDate(),
            serviceAgreement.getFunctionGroups());

        return save(serviceAgreement);
    }

    @Transactional
    public ServiceAgreement save(ServiceAgreement serviceAgreement) {
        LOGGER.info("Trying to save Service Agreement");
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);
        serviceAgreementAdminService.addAdminPermissions(serviceAgreement);
        return serviceAgreement;
    }
    
    /**
     * Creates new service agreement and saves it to a relational database.
     * 
     * @param legalEntity legal entity participant
     * @param newCsa custom service agreement to save
     * @param creatorLegalEntityId creator legal entity id
     * @return Created custom service agreement
     */
    @Transactional
    public ServiceAgreement save(LegalEntity legalEntity, NewCustomServiceAgreement newCsa, String creatorLegalEntityId) {
        Date startDate = dateTimeService.getStartDateFromDateAndTime(newCsa.getServiceAgreementValidFromDate(),
                        newCsa.getServiceAgreementValidFromTime());
        Date endDate = dateTimeService.getEndDateFromDateAndTime(newCsa.getServiceAgreementValidUntilDate(),
                        newCsa.getServiceAgreementValidUntilTime());
        
        validateTimePeriodOfServiceAgreement(startDate, endDate);

        if (Objects.nonNull(newCsa.getServiceAgreementExternalId()) && serviceAgreementJpaRepository
            .findByExternalId(newCsa.getServiceAgreementExternalId()).isPresent()) {
            LOGGER.warn("Already exist service agreement with the requested external id.");
            throw getBadRequestException(ERR_AG_069.getErrorMessage(), ERR_AG_069.getErrorCode());
        }

        ServiceAgreement serviceAgreement = new ServiceAgreement()
                        .withCreatorLegalEntity(
                                        new LegalEntity().withId(creatorLegalEntityId))
                        .withName(newCsa.getServiceAgreementName())
                        .withDescription(newCsa.getServiceAgreementDescription())
                        .withExternalId(newCsa.getServiceAgreementExternalId())
                        .withStartDate(startDate)
                        .withEndDate(endDate)
                        .withMaster(false)
                        .withState(toServiceAgreementStateEnabledDefault(newCsa.getServiceAgreementState()))
                        .withAdditions(newCsa.getAdditions());
        com.backbase.accesscontrol.domain.Participant participant = new com.backbase.accesscontrol.domain.Participant()
                        .withLegalEntity(legalEntity)
                        .withShareAccounts(newCsa.getParticipantInfo().getShareAccounts())
                        .withShareUsers(newCsa.getParticipantInfo().getShareUsers());
        serviceAgreement.addParticipant(participant);
        
        populateDefaultPermissionSets(serviceAgreement);
        
        return save(serviceAgreement);
    }
    
    /**
     * Creates a new service agreement and saves it to a relational database.
     * 
     * @param legalEntity creator legal entity
     * @param newMsa master service agreement to save
     * @return Created master service agreement
     */
    @Transactional
    public ServiceAgreement save(LegalEntity legalEntity, NewMasterServiceAgreement newMsa) {
        Date startDate = dateTimeService.getStartDateFromDateAndTime(newMsa.getServiceAgreementValidFromDate(),
                        newMsa.getServiceAgreementValidFromTime());
        Date endDate = dateTimeService.getEndDateFromDateAndTime(newMsa.getServiceAgreementValidUntilDate(),
                        newMsa.getServiceAgreementValidUntilTime());
        
        validateTimePeriodOfServiceAgreement(startDate, endDate);
        
        ServiceAgreement serviceAgreement = new ServiceAgreement()
                        .withCreatorLegalEntity(legalEntity)
                        .withName(newMsa.getServiceAgreementName())
                        .withDescription(newMsa.getServiceAgreementDescription())
                        .withExternalId(newMsa.getServiceAgreementExternalId())
                        .withStartDate(startDate)
                        .withEndDate(endDate)
                        .withMaster(true)
                        .withState(toServiceAgreementStateEnabledDefault(newMsa.getServiceAgreementState()))
                        .withAdditions(newMsa.getAdditions());
        com.backbase.accesscontrol.domain.Participant participant = new com.backbase.accesscontrol.domain.Participant()
                        .withLegalEntity(legalEntity)
                        .withShareAccounts(true)
                        .withShareUsers(true);
        serviceAgreement.addParticipant(participant);
        
        populateDefaultPermissionSets(serviceAgreement);
        
        return save(serviceAgreement);
    }
    
    private ServiceAgreementState toServiceAgreementStateEnabledDefault(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status status) {
        if (status == null) {
            return ServiceAgreementState.ENABLED;
        } else {
            return ServiceAgreementState.valueOf(status.toString());
        }
    }

    @Transactional
    public void update(ServiceAgreement serviceAgreement) {
        LOGGER.info("Trying to update Service Agreement");

        ServiceAgreement oldSa = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS)
            .orElseThrow(() -> {
                    LOGGER.error(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreement.getId());
                    return getInternalServerErrorException(ERR_ACQ_006.getErrorMessage());
                }
            );

        Map<String, Set<String>> participantsAdmins = new HashMap<>();
        for (com.backbase.accesscontrol.domain.Participant p : serviceAgreement.getParticipants().values()) {
            participantsAdmins.put(p.getLegalEntity().getId(),
                CollectionUtils.isNotEmpty(p.getAdmins().keySet()) ? p.getAdmins().keySet() : new HashSet<>());
        }
        serviceAgreementAdminService.updateParticipantsAdmins(oldSa, participantsAdmins);
        populateServiceAgreement(serviceAgreement, oldSa);

        serviceAgreementJpaRepository.save(oldSa);
    }

    private void populateServiceAgreement(ServiceAgreement newSa, ServiceAgreement oldSa) {
        oldSa.setName(newSa.getName());
        oldSa.setDescription(newSa.getDescription());
        oldSa.setStartDate(newSa.getStartDate());
        oldSa.setEndDate(newSa.getEndDate());
        oldSa.setExternalId(newSa.getExternalId());
        oldSa.setAdditions(newSa.getAdditions());

        if (!oldSa.getState().equals(newSa.getState())) {
            oldSa.setState(newSa.getState());
            oldSa.setStateChangedAt(new Date());
        }

        List<com.backbase.accesscontrol.domain.Participant> newParticipants = newSa.getParticipants().values()
            .stream()
            .filter(newParticipant -> !oldSa.getParticipants().containsKey(newParticipant.getLegalEntity().getId()))
            .map(p -> {
                com.backbase.accesscontrol.domain.Participant participant = new com.backbase.accesscontrol.domain.Participant();
                participant.setLegalEntity(p.getLegalEntity());
                participant.setShareAccounts(p.isShareAccounts());
                participant.setShareUsers(p.isShareUsers());
                participant.addAdmins(p.getAdmins().keySet());
                return participant;
            })
            .collect(toList());

        List<com.backbase.accesscontrol.domain.Participant> removedParticipants = oldSa.getParticipants().values()
            .stream()
            .filter(participant -> !newSa.getParticipants().containsKey(participant.getLegalEntity().getId()))
            .collect(toList());

        removeAdminPrivilegesForParticipantsThatAreBeingRemoved(removedParticipants);
        oldSa.removeParticipant(removedParticipants);

        updateExistingParticipantRoles(oldSa, newSa);

        oldSa.addParticipant(newParticipants);
        serviceAgreementAdminService.addParticipantAdminsPermissions(newParticipants);
    }

    private void updateExistingParticipantRoles(ServiceAgreement oldSa, ServiceAgreement newSa) {
        oldSa.getParticipants().values().stream()
            .filter(oldParticipant -> newSa.getParticipants().containsKey(oldParticipant.getLegalEntity().getId()))
            .forEach(oldParticipant -> {
                com.backbase.accesscontrol.domain.Participant updatedParticipant = newSa.getParticipants()
                    .get(oldParticipant.getLegalEntity().getId());

                if(oldParticipant.isShareUsers() && !updatedParticipant.isShareUsers()){
                    oldParticipant.getParticipantUsers().clear();
                }

                oldParticipant.setShareAccounts(updatedParticipant.isShareAccounts());
                oldParticipant.setShareUsers(updatedParticipant.isShareUsers());
            });
    }

    private void populateAdminsInServiceAgreementsParticipants(
        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements
            .Participant> participantsRequest, ServiceAgreement serviceAgreement) {

        Map<String, com.backbase.accesscontrol.domain.Participant> saParticipants = serviceAgreement.getParticipants();

        participantsRequest.forEach(participantRequest -> {
            com.backbase.accesscontrol.domain.Participant participant = saParticipants.get(participantRequest.getId());
            Optional.ofNullable(participantRequest.getAdmins()).ifPresent(participant::addAdmins);
        });
    }

    /**
     * Creates new Pending Service Agreement and saves it to a relational database.
     *
     * @param serviceAgreementRequestBody - serviceAgreement that should be saved.
     * @param legalEntityId               - creator legal entity id.
     * @param approvalId                  - approvalId
     * @return approvalId
     */
    public String saveServiceAgreementApproval(ServiceAgreementPostRequestBody serviceAgreementRequestBody,
        String legalEntityId, String approvalId) {

        ApprovalServiceAgreement approvalServiceAgreement = approvalServiceAgreementMapper
            .serviceAgreementPostRequestBodyToApprovalServiceAgreement(serviceAgreementRequestBody, legalEntityId,
                approvalId);
        populateDefaultPermissionSets(approvalServiceAgreement);

        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);

        return approvalId;
    }

    @Transactional(readOnly = true)
    public boolean isServiceAgreementInPendingState(String serviceAgreementId) {
        return approvalServiceAgreementRefJpaRepository.existsByServiceAgreementId(serviceAgreementId);
    }

    @Transactional(readOnly = true)
    public boolean isServiceAgreementInPendingStateByExternalId(String externalId) {
        return approvalServiceAgreementRefJpaRepository.existsByServiceAgreementExternalId(externalId);
    }

    @Transactional(readOnly = true)
    public boolean existsPendingServiceAgreementWithExternalId(String externalId) {
        return approvalServiceAgreementJpaRepository.existsByExternalId(externalId);
    }

    @Transactional(readOnly = true)
    public boolean existsPendingDeleteDataGroupInServiceAgreement(String serviceAgreementId) {
        Set<String> existingDataGroups = dataGroupJpaRepository.findByServiceAgreementId(serviceAgreementId).stream()
            .map(DataGroup::getId)
            .collect(Collectors.toSet());
        return approvalDataGroupJpaRepository
            .existsByDataGroupIdIn(existingDataGroups);
    }

    @Transactional(readOnly = true)
    public boolean existsPendingDeleteJobRoleInServiceAgreement(String serviceAgreementId) {
        Set<String> existingJobRoles = functionGroupJpaRepository.findByServiceAgreementId(serviceAgreementId).stream()
            .map(FunctionGroup::getId)
            .collect(Collectors.toSet());
        return approvalFunctionGroupRefJpaRepository
            .existsByFunctionGroupIdIn(existingJobRoles);
    }

    @Transactional(readOnly = true)
    public boolean existsPendingJobRoleInServiceAgreement(String serviceAgreementId) {
        return approvalFunctionGroupJpaRepository
            .existsByServiceAgreementId(serviceAgreementId);
    }

    @Transactional(readOnly = true)
    public boolean existsPendingDataGroupInServiceAgreement(String serviceAgreementId) {
        return approvalDataGroupDetailsJpaRepository
            .existsByServiceAgreementId(serviceAgreementId);
    }

    @Transactional(readOnly = true)
    public boolean existsPendingPermissionsInServiceAgreement(String serviceAgreementId) {
        return approvalUserContextJpaRepository.existsByServiceAgreementId(serviceAgreementId);
    }

    /**
     * Gets service agreement if it's in pending state.
     *
     * @param serviceAgreementId - external id
     * @return optional of {@link ApprovalServiceAgreementRef}
     */
    @Transactional(readOnly = true)
    public Optional<ApprovalServiceAgreementRef> getServiceAgreementIfPending(String serviceAgreementId) {
        return approvalServiceAgreementRefJpaRepository
            .findApprovalServiceAgreementRefByServiceAgreementId(serviceAgreementId);
    }

    private void populateDefaultPermissionSets(ApprovalServiceAgreement approvalServiceAgreement) {
        approvalServiceAgreement.getPermissionSetsAdmin()
            .add(permissionSetService
                .getAssignablePermissionSetsById(new HashSet<>(), false)
                .stream()
                .map(AssignablePermissionSet::getId)
                .findFirst()
                .orElse(null));

        approvalServiceAgreement.getPermissionSetsRegular()
            .add(permissionSetService
                .getAssignablePermissionSetsById(new HashSet<>(), true)
                .stream()
                .map(AssignablePermissionSet::getId)
                .findFirst()
                .orElse(null));
    }

    /**
     * Update service agreement.
     *
     * @param serviceAgreementId      - service agreement id
     * @param serviceAgreementRequest - service agreement request data
     */
    @Transactional
    public void save(String serviceAgreementId, ServiceAgreementSave serviceAgreementRequest) {
        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)
            .orElseThrow(() -> {
                LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreementId);
                return getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });

        validateUpdateServiceAgreement(serviceAgreement, serviceAgreementRequest);

        setServiceAgreementData(serviceAgreementRequest, serviceAgreement);

        serviceAgreementJpaRepository.save(serviceAgreement);
    }

    /**
     * Create pending update service agreement.
     *
     * @param serviceAgreementRequest - service agreement request data
     * @param serviceAgreementId      - service agreement id
     * @param approvalId              - approval Id
     */
    public void updateServiceAgreementApproval(ServiceAgreementSave serviceAgreementRequest, String serviceAgreementId,
        String approvalId) {
        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)
            .orElseThrow(() -> {
                LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreementId);
                return getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });

        validateUpdateServiceAgreement(serviceAgreement, serviceAgreementRequest);

        checkIfServiceAgreementAlreadyHavePendingRecord(serviceAgreementId);
        validateExternalIdIsUniqueInPending(serviceAgreementRequest.getExternalId());

        ApprovalServiceAgreement approvalServiceAgreement = populateApprovalServiceAgreement(serviceAgreement,
            serviceAgreementRequest, approvalId);

        approvalServiceAgreementJpaRepository.save(approvalServiceAgreement);
    }

    private ApprovalServiceAgreement populateApprovalServiceAgreement(ServiceAgreement serviceAgreement,
        ServiceAgreementSave serviceAgreementRequest, String approvalId) {
        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId(approvalId);
        approvalServiceAgreement.setServiceAgreementId(serviceAgreement.getId());

        approvalServiceAgreement.setName(serviceAgreementRequest.getName());
        approvalServiceAgreement.setDescription(serviceAgreementRequest.getDescription());
        approvalServiceAgreement.setExternalId(serviceAgreementRequest.getExternalId());
        approvalServiceAgreement.setMaster(serviceAgreement.isMaster());
        approvalServiceAgreement.setCreatorLegalEntityId(serviceAgreement.getCreatorLegalEntity().getId());

        Date startDate = dateTimeService.getStartDateFromDateAndTime(serviceAgreementRequest.getValidFromDate(),
            serviceAgreementRequest.getValidFromTime());
        Date endDate = dateTimeService.getEndDateFromDateAndTime(serviceAgreementRequest.getValidUntilDate(),
            serviceAgreementRequest.getValidUntilTime());

        ServiceAgreementState updateState = ServiceAgreementState
            .fromString(serviceAgreementRequest.getStatus().toString());

        approvalServiceAgreement.setState(ServiceAgreementState.fromString(updateState.toString()));

        if ((nonNull(startDate) && !startDate.equals(serviceAgreement.getStartDate()))
            || (nonNull(serviceAgreement.getStartDate()) && !serviceAgreement.getStartDate().equals(startDate))
            || (nonNull(endDate) && !endDate.equals(serviceAgreement.getEndDate()))
            || (nonNull(serviceAgreement.getEndDate()) && !serviceAgreement.getEndDate().equals(endDate))) {

            validateRootServiceAgreementDoesNotHaveStartAndEndDate(serviceAgreement.isMaster(),
                serviceAgreement.getCreatorLegalEntity().getParent(), startDate, endDate);
            validateTimePeriodOfServiceAgreement(startDate, endDate, serviceAgreement.getFunctionGroups());

            approvalServiceAgreement.setStartDate(startDate);
            approvalServiceAgreement.setEndDate(endDate);
        } else {
            approvalServiceAgreement.setStartDate(serviceAgreement.getStartDate());
            approvalServiceAgreement.setEndDate(serviceAgreement.getEndDate());
        }

        approvalServiceAgreement.setParticipants(getParticipantsWithAdmins(serviceAgreementRequest));

        ServiceAgreement saWithPermissionSets = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId(), SERVICE_AGREEMENT_WITH_PERMISSION_SETS)
            .orElseThrow(() -> {
                LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreement.getId());
                return getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });

        approvalServiceAgreement.setPermissionSetsAdmin(
            saWithPermissionSets.getPermissionSetsAdmin().stream().map(AssignablePermissionSet::getId)
                .collect(toSet()));
        approvalServiceAgreement.setPermissionSetsRegular(
            saWithPermissionSets.getPermissionSetsRegular().stream().map(AssignablePermissionSet::getId)
                .collect(toSet()));

        approvalServiceAgreement.setAdditions(serviceAgreementRequest.getAdditions());

        return approvalServiceAgreement;
    }

    private Set<ApprovalServiceAgreementParticipant> getParticipantsWithAdmins(
        ServiceAgreementSave serviceAgreementRequest) {

        return serviceAgreementRequest.getParticipants().stream()
            .map(participant -> {
                ApprovalServiceAgreementParticipant approvalParticipant = new ApprovalServiceAgreementParticipant();
                approvalParticipant.setLegalEntityId(participant.getId());
                approvalParticipant.setShareUsers(participant.getSharingUsers());
                approvalParticipant.setShareAccounts(participant.getSharingAccounts());
                approvalParticipant.setAdmins(participant.getAdmins());
                return approvalParticipant;
            })
            .collect(Collectors.toSet());
    }

    private void checkIfServiceAgreementAlreadyHavePendingRecord(String serviceAgreementId) {
        if (isServiceAgreementInPendingState(serviceAgreementId)) {
            LOGGER.warn("There is pending service agreement with id  {}", serviceAgreementId);
            throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
        }
    }

    private void validateExternalIdIsUniqueInPending(String newExternalId) {
        if (nonNull(newExternalId) && existsPendingServiceAgreementWithExternalId(newExternalId)) {
            LOGGER.warn("Service agreement with external id {} is in pending state.", newExternalId);
            throw getBadRequestException(ERR_AG_106.getErrorMessage(), ERR_AG_106.getErrorCode());
        }
    }

    /**
     * Adds users in a service agreement for certain participant.
     *
     * @param serviceAgreementId - id of the service agreement
     * @param requestBody        - body containing participant id and users to be added
     */
    @Transactional
    public void addUsersInServiceAgreement(String serviceAgreementId, List<UsersDto> requestBody) {
        LOGGER.info("Trying to add users for Service Agreement with id {}.", serviceAgreementId);

        checkServiceAgreement(serviceAgreementId);

        Set<String> userSharingParticipants = requestBody.stream().map(UsersDto::getLegalEntityId)
            .collect(toSet());
        Map<String, com.backbase.accesscontrol.domain.Participant> providers
            = getLegalEntitiesThatShareUsersInServiceAgreement(serviceAgreementId, userSharingParticipants);

        for (UsersDto body : requestBody) {

            Set<ParticipantUser> allParticipantUsers = providers
                .get(body.getLegalEntityId())
                .getParticipantUsers();
            boolean userFoundInProvider = allParticipantUsers.stream().map(ParticipantUser::getUserId)
                .anyMatch(userId -> body.getUsers().contains(userId));
            if (userFoundInProvider) {
                LOGGER.warn("User already exists in service agreement");
                throw getBadRequestException(ERR_ACC_039.getErrorMessage(), ERR_ACC_039.getErrorCode());

            }
            com.backbase.accesscontrol.domain.Participant providerToBeUpdated = providers
                .get(body.getLegalEntityId());
            Optional.ofNullable(providerToBeUpdated)
                .ifPresent(participant -> participant
                    .addParticipantUsers(new ArrayList<>(body.getUsers())));
        }

        participantJpaRepository.saveAll(providers.values());
    }

    /**
     * Retrieves service agreement by id and transforms it to {@link  ServiceAgreementItem}.
     *
     * @param id - id of the service agreement
     * @return {@link ServiceAgreementItem}
     */
    @Transactional(readOnly = true)
    public ServiceAgreementItem getServiceAgreementResponseBodyById(String id) {
        ServiceAgreement serviceAgreement =
            serviceAgreementJpaRepository.findById(id, SERVICE_AGREEMENT_WITH_ADDITIONS)
                .orElseThrow(() -> {
                    LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, id);
                    return getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
                });
        ServiceAgreementItem responseBody = serviceAgreementTransformerPersistence
            .transformServiceAgreement(ServiceAgreementItem.class, serviceAgreement);

        responseBody.setCreatorLegalEntity(serviceAgreement.getCreatorLegalEntity().getId());
        responseBody.setStatus(Status.fromValue(serviceAgreement.getState().toString()));
        responseBody.setAdditions(serviceAgreement.getAdditions());
        responseBody.setValidFrom(serviceAgreement.getStartDate());
        responseBody.setValidUntil(serviceAgreement.getEndDate());
        return responseBody;
    }

    /**
     * Returns the Service Agreement by ID.
     *
     * @param id          Service Agreement ID.
     * @param entityGraph - entity graph
     * @return Service Agreement identified by ID.
     * @throws NotFoundException if a Service Agreement with given ID is not found.
     */
    @Transactional(readOnly = true)
    public ServiceAgreement getById(String id, String entityGraph) {
        LOGGER.info("Trying to get Service Agreement with id {}", id);
        Optional<ServiceAgreement> serviceAgreementOpt = serviceAgreementJpaRepository
            .findById(id, entityGraph);

        return serviceAgreementOpt.orElseThrow(() -> {
            LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, id);

            return getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
        });
    }

    /**
     * Returns the Service Agreement by external ID.
     *
     * @param externalId Service Agreement external ID.
     * @return {@link ServiceAgreementItem} Service Agreement identified by external ID.
     * @throws NotFoundException if a Service Agreement with given external ID is not found.
     */
    @Transactional(readOnly = true)
    public Optional<ServiceAgreement> getServiceAgreementResponseBodyByExternalId(String externalId) {
        return serviceAgreementJpaRepository.findByExternalId(externalId);
    }


    /**
     * Returns a Service Agreement by external ID.
     *
     * @param externalServiceAgreementId - external ID of the Service Agreements
     * @return {@link ServiceAgreement}
     */
    public ServiceAgreement getServiceAgreementByExternalId(String externalServiceAgreementId) {
        LOGGER.info("Trying to get service agreement by external id.");
        return serviceAgreementJpaRepository.findByExternalId(externalServiceAgreementId)
            .orElseThrow(() -> {
                LOGGER.warn(SERVICE_AGREEMENT_EXTERNAL_ID_DOES_NOT_EXIST);
                return getBadRequestException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });
    }

    /**
     * Returns a paginated result of Service Agreements by name and creator ID and paginated params.
     *
     * @param name                          service agreement name.
     * @param creatorId                     legal entity ID.
     * @param searchAndPaginationParameters parameters for searching and pagination
     * @return List of service agreements based on parameters that we are sending
     */
    @Transactional(readOnly = true)
    public Page<ServiceAgreement> getServiceAgreements(String name, String creatorId,
        SearchAndPaginationParameters searchAndPaginationParameters) {
        LOGGER.info("Trying to get Service Agreements.");
        return serviceAgreementJpaRepository
            .findAllServiceAgreementsByParameters(name, creatorId, searchAndPaginationParameters,
                SERVICE_AGREEMENT_WITH_ADDITIONS);
    }

    /**
     * Update Service Agreement to a relational database.
     *
     * @param serviceAgreementId             - internal service agreement id
     * @param serviceAgreementPutRequestBody - Service Agreement request body with the ServiceAgreement that should be
     *                                       updated.
     */
    @Transactional
    public void updateServiceAgreement(String serviceAgreementId,
        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody) {
        LOGGER.info("Trying to update Service Agreement {}", serviceAgreementPutRequestBody.getName());
        ServiceAgreement serviceAgreement = getById(serviceAgreementId,
            SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS);

        UpdateKind.NAME.updateServiceAgreementFields(serviceAgreement, serviceAgreementPutRequestBody.getName());
        UpdateKind.DESCRIPTION
            .updateServiceAgreementFields(serviceAgreement, serviceAgreementPutRequestBody.getDescription());
        UpdateKind.EXTERNAL_ID
            .updateServiceAgreementFields(serviceAgreement, serviceAgreementPutRequestBody.getExternalId());

        Date fromDate = dateTimeService.getStartDateFromDateAndTime(serviceAgreementPutRequestBody.getValidFromDate(),
            serviceAgreementPutRequestBody.getValidFromTime());
        Date untilDate = dateTimeService.getEndDateFromDateAndTime(serviceAgreementPutRequestBody.getValidUntilDate(),
            serviceAgreementPutRequestBody.getValidUntilTime());

        validateTimeBoundServiceAgreementInPending(serviceAgreementId, fromDate, untilDate);
        serviceAgreement.setAdditions(serviceAgreementPutRequestBody.getAdditions());

        ServiceAgreementState state = Optional.ofNullable(serviceAgreementPutRequestBody.getStatus())
            .map(status -> ServiceAgreementState.fromString(status.toString()))
            .orElse(serviceAgreement.getState());

        serviceAgreementUpdateWithValidations(serviceAgreement,
            state,
            fromDate, untilDate);

        serviceAgreementJpaRepository.save(serviceAgreement);
    }

    /**
     * Update Service Agreement state to a relational database.
     *
     * @param serviceAgreementId - internal service agreement id
     * @param state              - Service Agreement State that should be updated.
     */
    @Transactional
    public void updateServiceAgreementState(String serviceAgreementId, ServiceAgreementState state) {

        ServiceAgreement serviceAgreement = getById(serviceAgreementId, null);
        LOGGER
            .info("Trying to update Service Agreement {}, with state {}", serviceAgreement.getName(), state);
        serviceAgreement.setState(state);
        serviceAgreement.setStateChangedAt(new Date());
        serviceAgreementJpaRepository.save(serviceAgreement);
    }

    private void serviceAgreementUpdateWithValidations(ServiceAgreement serviceAgreement, ServiceAgreementState state,
        Date validFrom, Date validUntil) {
        if (shouldUpdatePrivileges(serviceAgreement, state, validFrom, validUntil)) {

            updateServiceAgreementStatus(serviceAgreement, state);
            serviceAgreement.setStartDate(validFrom);
            serviceAgreement.setEndDate(validUntil);

            validateRootServiceAgreementDoesNotHaveStartAndEndDate(serviceAgreement.isMaster(),
                serviceAgreement.getCreatorLegalEntity().getParent(), serviceAgreement.getStartDate(),
                serviceAgreement.getEndDate());
            validateTimePeriodOfServiceAgreement(serviceAgreement.getStartDate(), serviceAgreement.getEndDate(),
                serviceAgreement.getFunctionGroups());
        }
    }

    private void validateTimeBoundServiceAgreementInPending(String serviceAgreementId, Date startDate,
        Date endDate) {
        Optional<List<ApprovalFunctionGroup>> byServiceAgreementId = approvalFunctionGroupJpaRepository
            .findByServiceAgreementId(serviceAgreementId);
        if (byServiceAgreementId.isPresent()) {
            Set<FunctionGroup> functionGroupsList = byServiceAgreementId.get().stream()
                .map(e -> functionGroupMapperPersistence
                    .approvalFunctionGroupToFunctionGroup(e, FunctionGroupType.DEFAULT))
                .collect(toSet());
            if (!timeBoundValidatorService.isPeriodValid(startDate, endDate, functionGroupsList)) {
                LOGGER.warn("Invalid time period");
                throw getBadRequestException(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode());
            }
        }
    }

    /**
     * Adds user in service agreement for given provider.
     *
     * @param serviceAgreementExternalId -service agreement external id
     * @param userInternalId             - user id
     * @param userLegalEntityId          - user's legal entity id
     * @return Service agreement id
     */
    @Transactional
    public String addUserInServiceAgreementBatch(String serviceAgreementExternalId, String userInternalId,
        String userLegalEntityId) {
        LOGGER.info("Adding users from a batch request in a service agreement");

        com.backbase.accesscontrol.domain.Participant provider = findProviderInServiceAgreement(
            serviceAgreementExternalId, userLegalEntityId);
        if (provider.getServiceAgreement().isMaster()) {
            LOGGER.warn("Cannot add users in MSA");
            throw getBadRequestException(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode());
        }
        checkIfUserIsAddedOnServiceAgreement(
            (providerWithUsers, userId) -> findProviderUser(provider, userInternalId).isPresent(),
            provider, userInternalId, ERR_ACC_039);

        provider.addParticipantUser(userInternalId);
        participantJpaRepository.save(provider);
        return provider.getServiceAgreement().getId();
    }

    /**
     * Removes user from service agreement for given provider.
     *
     * @param serviceAgreementExternalId -service agreement external id
     * @param userInternalId             - user id
     * @param userLegalEntityId          - user's legal entity id
     * @return Service agreement id
     */
    @Transactional
    public String removeUserFromServiceAgreementBatch(String serviceAgreementExternalId, String userInternalId,
        String userLegalEntityId) {
        LOGGER.info("Removing users from a batch request in a service agreement");
        com.backbase.accesscontrol.domain.Participant provider = findProviderInServiceAgreement(
            serviceAgreementExternalId, userLegalEntityId);

        if (provider.getServiceAgreement().isMaster()) {
            LOGGER.warn("Cannot remove users in MSA");
            throw getBadRequestException(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode());
        }
        final Optional<ParticipantUser> participantUser = findProviderUser(provider, userInternalId);
        checkIfUserIsAddedOnServiceAgreement((providerWithUsers, userId) -> !participantUser.isPresent(), provider,
            userInternalId, ERR_ACC_040);

        userAccessFunctionGroupService
            .checkIfUsersHaveAssignedPrivilegesForServiceAgreement(provider.getServiceAgreement().getId(),
                newHashSet(userInternalId));
        validatePendingApprovalsForUser(provider.getServiceAgreement().getId(), Sets.newHashSet(userInternalId));
        participantUser.ifPresent(participantUserToRemove ->
            provider.getParticipantUsers().remove(participantUserToRemove));
        participantJpaRepository.save(provider);
        return provider.getServiceAgreement().getId();
    }

    /**
     * Removes users in a service agreement for certain participant.
     *
     * @param serviceAgreementId - id of the service agreement
     * @param requestBody        - body containing participant id and users to be added
     */
    @Transactional
    public void removeUsersFromServiceAgreement(String serviceAgreementId,
        List<UsersDto> requestBody) {

        LOGGER.info("Trying to remove users for Service Agreement with id {} for participant", serviceAgreementId);

        checkServiceAgreement(serviceAgreementId);

        Set<String> allUserIdsToBeRemoved = requestBody.stream()
            .flatMap(body -> body.getUsers().stream())
            .collect(toSet());
        userAccessFunctionGroupService
            .checkIfUsersHaveAssignedPrivilegesForServiceAgreement(serviceAgreementId, allUserIdsToBeRemoved);
        validatePendingApprovalsForUser(serviceAgreementId, allUserIdsToBeRemoved);

        Set<String> userSharingParticipants = requestBody.stream()
            .map(UsersDto::getLegalEntityId)
            .collect(toSet());
        Map<String, com.backbase.accesscontrol.domain.Participant> providers
            = getLegalEntitiesThatShareUsersInServiceAgreement(serviceAgreementId, userSharingParticipants);

        removeProviderUsers(requestBody, providers);
        participantJpaRepository.saveAll(providers.values());
    }

    /**
     * Retrieve service agreement users.
     *
     * @param serviceAgreementId internal service agreements id
     * @return {@link ServiceAgreementUsersGetResponseBody}
     */
    @Transactional
    public ServiceAgreementUsersGetResponseBody getServiceAgreementUsers(String serviceAgreementId) {
        LOGGER.info("Trying to get service agreement users for service agreement with id {}", serviceAgreementId);
        boolean serviceAgreementExists = serviceAgreementJpaRepository.existsById(serviceAgreementId);

        if (!serviceAgreementExists) {
            LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreementId);
            throw getBadRequestException(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode());
        }

        List<com.backbase.accesscontrol.domain.Participant> providersInServiceAgreement = participantJpaRepository
            .findByServiceAgreementIdInAndShareUsersIsTrue(
                singletonList(serviceAgreementId),
                PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS
            );

        Set<String> userIds = providersInServiceAgreement.stream()
            .map(com.backbase.accesscontrol.domain.Participant::getParticipantUsers)
            .flatMap(Set::stream)
            .map(ParticipantUser::getUserId)
            .collect(toSet());

        return new ServiceAgreementUsersGetResponseBody()
            .withUserIds(userIds);
    }

    /**
     * Adds participants in the Service Agreement.
     *
     * @param item the payload with participants
     * @return Service agreement id
     */
    @Transactional
    public String addParticipant(PresentationParticipantPutBody item) {
        if (isSharingNothing(item.getSharingAccounts(), item.getSharingUsers())) {
            LOGGER.warn("LE Participant must share Users and/or Accounts");
            throw getBadRequestException(ERR_ACC_043.getErrorMessage(), ERR_ACC_043.getErrorCode());
        }
        List<com.backbase.accesscontrol.domain.Participant> participantList = participantJpaRepository
            .findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(item.getExternalServiceAgreementId());

        if (participantList.stream().anyMatch(isAlreadyExposedParticipant(item))) {
            LOGGER.warn("Invalid participant");
            throw getBadRequestException(ERR_ACC_045.getErrorMessage(), ERR_ACC_045.getErrorCode());
        }
        LegalEntity legalEntity = legalEntityJpaRepository
            .findByExternalId(item.getExternalParticipantId())
            .orElseThrow(() -> {
                LOGGER.warn("Legal entity with external id doesn't exist ");
                return getNotFoundException(ERR_ACC_010.getErrorMessage(), ERR_ACC_010.getErrorCode());
            });

        Optional<ServiceAgreement> serviceAgreementOptional = serviceAgreementJpaRepository
            .findByExternalId(item.getExternalServiceAgreementId());
        LegalEntity creatorLegalEntity = serviceAgreementOptional
            .map(ServiceAgreement::getCreatorLegalEntity)
            .orElse(null);

        if (isInvalidLegalEntity(legalEntity, creatorLegalEntity)) {
            LOGGER.warn("Legal entity with external id not in hierarchy.");
            throw getBadRequestException(ERR_ACC_046.getErrorMessage(), ERR_ACC_046.getErrorCode());
        }

        ServiceAgreement serviceAgreement = serviceAgreementOptional
            .orElseThrow(() ->
                getInternalServerErrorException(ERR_ACC_044.getErrorMessage()));

        if (serviceAgreement.isMaster()) {
            LOGGER.warn("Cannot add participant in MSA.");
            throw getBadRequestException(ERR_ACC_044.getErrorMessage(), ERR_ACC_044.getErrorCode());
        }

        com.backbase.accesscontrol.domain.Participant participant
            = new com.backbase.accesscontrol.domain.Participant();
        participant.setLegalEntity(legalEntity);
        participant.setServiceAgreement(serviceAgreement);
        participant.setShareAccounts(item.getSharingAccounts());
        participant.setShareUsers(item.getSharingUsers());

        participantJpaRepository.save(participant);

        return serviceAgreement.getId();
    }
    
    /**
     * Adds participant into the service agreement.
     * @param existingServiceAgreement service agreement details
     * @param legalEntityExternalId identifier for the participant
     * @return Service agreement id
     */
    @Transactional
    public String addParticipant(ExistingCustomServiceAgreement existingServiceAgreement, String legalEntityExternalId) {
        ParticipantInfo participantInfo = existingServiceAgreement.getParticipantInfo();
        if (isSharingNothing(participantInfo.getShareAccounts(), participantInfo.getShareUsers())) {
            LOGGER.warn("LE Participant must share Users and/or Accounts");
            throw getBadRequestException(ERR_ACC_043.getErrorMessage(), ERR_ACC_043.getErrorCode());
        }
        
        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
                        .findById(existingServiceAgreement.getServiceAgreementId())
                        .orElseThrow(() -> getBadRequestException(ERR_ACC_044.getErrorMessage(), ERR_ACC_044.getErrorCode()));
        
        if (serviceAgreement.isMaster()) {
            LOGGER.warn("Cannot add participant in MSA.");
            throw getBadRequestException(ERR_ACC_044.getErrorMessage(), ERR_ACC_044.getErrorCode());
        }
        
        List<com.backbase.accesscontrol.domain.Participant> participantList = participantJpaRepository
            .findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(serviceAgreement.getExternalId());

        if (participantList.stream().anyMatch(participant -> StringUtils.equals(legalEntityExternalId,
                        participant.getLegalEntity().getExternalId()))) {
            LOGGER.warn("Invalid participant");
            throw getBadRequestException(ERR_ACC_045.getErrorMessage(), ERR_ACC_045.getErrorCode());
        }
        LegalEntity legalEntity = legalEntityJpaRepository.findByExternalId(legalEntityExternalId).orElseThrow(() -> {
            LOGGER.warn("Legal entity with external id doesn't exist ");
            return getNotFoundException(ERR_ACC_010.getErrorMessage(), ERR_ACC_010.getErrorCode());
        });

        LegalEntity creatorLegalEntity = serviceAgreement.getCreatorLegalEntity();
        if (isInvalidLegalEntity(legalEntity, creatorLegalEntity)) {
            LOGGER.warn("Legal entity with external id not in hierarchy.");
            throw getBadRequestException(ERR_ACC_046.getErrorMessage(), ERR_ACC_046.getErrorCode());
        }

        com.backbase.accesscontrol.domain.Participant participant
            = new com.backbase.accesscontrol.domain.Participant();
        participant.setLegalEntity(legalEntity);
        participant.setServiceAgreement(serviceAgreement);
        participant.setShareAccounts(participantInfo.getShareAccounts());
        participant.setShareUsers(participantInfo.getShareUsers());

        participantJpaRepository.save(participant);

        return serviceAgreement.getId();
    }

    /**
     * Removes a Participant from the given Service Agreement.
     *
     * @param item - participant to be removed
     * @return Service agreement id
     */
    @Transactional
    public String removeParticipant(PresentationParticipantPutBody item) {
        com.backbase.accesscontrol.domain.Participant participant = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId(item.getExternalServiceAgreementId(),
                item.getExternalParticipantId())
            .orElseThrow(() -> {
                LOGGER
                    .warn("Invalid service agreement external id or participant external id.");
                return getNotFoundException(ERR_ACC_044.getErrorMessage(), ERR_ACC_044.getErrorCode());
            });

        if (participant.getServiceAgreement().isMaster()) {
            LOGGER.warn("Cannot remove participants in MSA.");
            throw getBadRequestException(ERR_ACC_056.getErrorMessage(), ERR_ACC_056.getErrorCode());
        }

        Set<String> adminIds = participant.getAdmins().keySet();

        checkIfSomeUserFromLegalEntityHasPermissions(item.getExternalServiceAgreementId(), participant);
        checkIfSomeUserFromLegalEntityHasPendingPermissions(participant);

        if (isNotEmpty(adminIds)) {
            String systemFunctionGroupForParticipant = getSystemFunctionGroupIdsForParticipant(participant);
            for (String adminId : adminIds) {
                userAccessFunctionGroupService
                    .deleteSystemFunctionGroupFromUserAccess(systemFunctionGroupForParticipant, adminId,
                        participant.getServiceAgreement());
            }
        }
        String serviceAgreementId = participant.getServiceAgreement().getId();
        participantJpaRepository.delete(participant);
        return serviceAgreementId;
    }

    /**
     * Populate default permission sets into service agreement.
     *
     * @param serviceAgreement service agreement
     */
    public void populateDefaultPermissionSets(ServiceAgreement serviceAgreement) {

        serviceAgreement.getPermissionSetsAdmin()
            .add(permissionSetService
                .getAssignablePermissionSetsById(new HashSet<>(), false)
                .stream()
                .findFirst()
                .orElse(null));
        serviceAgreement.getPermissionSetsRegular()
            .add(permissionSetService
                .getAssignablePermissionSetsById(new HashSet<>(), true)
                .stream()
                .findFirst()
                .orElse(null));
    }


    /**
     * Lists all service agreements assigned to particular permission set defined by id.
     *
     * @param id                            of permission set
     * @param searchAndPaginationParameters search and pagination parameters.
     */
    public Page<ServiceAgreementByPermissionSet> getByPermissionSetById(
        String id,
        SearchAndPaginationParameters searchAndPaginationParameters) {

        Long longId;

        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            LOGGER.warn("APS id {} should be number.", id);
            throw getBadRequestException(ERR_ACQ_063.getErrorMessage(), ERR_ACQ_063.getErrorCode());
        }

        AssignablePermissionSet assignablePermissionSet = assignablePermissionSetJpaRepository.findById(longId)
            .orElseThrow(
                () -> {
                    LOGGER.warn("APS with id {} does not exist.", longId);
                    return getNotFoundException(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode());
                }
            );

        return getServiceAgreementByPermissionSet(searchAndPaginationParameters, assignablePermissionSet);

    }

    /**
     * Lists all service agreements assigned to particular permission set defined by name.
     *
     * @param name                          of permission set
     * @param searchAndPaginationParameters search and pagination parameters.
     */
    public Page<ServiceAgreementByPermissionSet> getByPermissionSetByName(
        String name,
        SearchAndPaginationParameters searchAndPaginationParameters) {

        AssignablePermissionSet assignablePermissionSet = assignablePermissionSetJpaRepository.findByName(name)
            .orElseThrow(
                () -> {
                    LOGGER.warn("APS with name {} does not exist", name);
                    return getNotFoundException(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode());
                }
            );

        return getServiceAgreementByPermissionSet(searchAndPaginationParameters, assignablePermissionSet);
    }

    private Page<ServiceAgreementByPermissionSet> getServiceAgreementByPermissionSet(
        SearchAndPaginationParameters searchAndPaginationParameters, AssignablePermissionSet assignablePermissionSet) {
        Page<ServiceAgreement> list = serviceAgreementJpaRepository.getServiceAgreementByPermissionSetId(
            assignablePermissionSet,
            searchAndPaginationParameters
        );

        List<ServiceAgreementByPermissionSet> resultList = serviceAgreementByPermissionSetMapper
            .sourceToDestination(list.getContent());

        return new PageImpl<>(resultList, Pageable.unpaged(), list.getTotalElements());
    }


    private void checkServiceAgreement(String serviceAgreementId) {
        Optional<ServiceAgreement> serviceAgreement = serviceAgreementJpaRepository.findById(serviceAgreementId, null);
        if (!serviceAgreement.isPresent()) {
            LOGGER.warn("Service agreement with id {} does not exist in repo", serviceAgreementId);
            throw getNotFoundException(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode());
        }
        if (serviceAgreement.get().isMaster()) {
            LOGGER.warn("Cannot add/remove users from master service agreement");
            throw getBadRequestException(ERR_ACC_067.getErrorMessage(), ERR_ACC_067.getErrorCode());
        }
    }

    private String getSystemFunctionGroupIdsForParticipant(
        com.backbase.accesscontrol.domain.Participant participant) {
        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
            .getServiceAgreementFunctionGroups(participant.getServiceAgreement());
        return serviceAgreementFunctionGroups.getSystemFunctionGroup();
    }

    private void checkIfSomeUserFromLegalEntityHasPermissions(String externalServiceAgreementId,
        com.backbase.accesscontrol.domain.Participant participant) {
        if (participant.isShareUsers()) {
            Set<String> userIdsBySaExternalIdAndFgType = new HashSet<>(userAccessFunctionGroupService
                .getAllUserIdsByServiceAgreementExternalIdAndFunctionGroupType(externalServiceAgreementId,
                    FunctionGroupType.DEFAULT));

            Set<String> userIds = participant.getParticipantUsers()
                .stream()
                .map(ParticipantUser::getUserId)
                .collect(Collectors.toSet());

            Sets.SetView<String> intersection = Sets.intersection(userIdsBySaExternalIdAndFgType, userIds);
            if (isNotEmpty(intersection)) {
                LOGGER.warn("Users with ids {} have permission and cannot be removed", intersection);
                throw getBadRequestException(ERR_ACC_054.getErrorMessage(), ERR_ACC_054.getErrorCode());
            }
        }
    }

    private void checkIfSomeUserFromLegalEntityHasPendingPermissions(
        com.backbase.accesscontrol.domain.Participant participant) {

        String serviceAgreementId = participant.getServiceAgreement().getId();
        String legalEntityId = participant.getLegalEntity().getId();

        long count = approvalUserContextJpaRepository.countByServiceAgreementIdAndLegalEntityId(
            serviceAgreementId, legalEntityId);
        if (count > 0) {
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_078.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_078.getErrorCode());
        }
    }

    /**
     * Returns a list of participants that have one of the external service agreement ids from provided list.
     *
     * @param externalSaIds - external service agreement ids to iterate
     * @return list of {@link PersistenceExtendedParticipant}
     */
    public List<PersistenceExtendedParticipant> listParticipantsByExternalServiceAgreementIds(
        Collection<String> externalSaIds) {

        return transformToPersistenceParticipantExtendedItems(participantJpaRepository
            .findAllParticipantsWithExternalServiceAgreementIdsIn(externalSaIds,
                "graph.Participant.withLegalEntityAndServiceAgreementCreator"));
    }


    /**
     * Method that creates new service agreements.
     *
     * @param serviceAgreement - service agreement that needs to be created
     * @return the id of the created service agreement
     */
    @Transactional
    public ServiceAgreement create(ServiceAgreement serviceAgreement) {
        validateLegalEntityHierarchyService.validateLegalEntityHierarchy(serviceAgreement.getCreatorLegalEntity(),
            serviceAgreement.getParticipants().keySet());
        validParticipants(serviceAgreement.getParticipants());
        validateMasterServiceAgreementAndUpdateCreator(serviceAgreement);
        validateExternalIDisUnique(serviceAgreement.getExternalId(), null);
        validateTimePeriodOfServiceAgreement(serviceAgreement.getStartDate(), serviceAgreement.getEndDate(),
            serviceAgreement.getFunctionGroups());
        ServiceAgreement saved = serviceAgreementJpaRepository.save(serviceAgreement);
        serviceAgreementAdminService.addAdminPermissions(saved);
        return saved;
    }

    private void validateMasterServiceAgreementAndUpdateCreator(ServiceAgreement serviceAgreement) {
        if (serviceAgreement.isMaster()) {
            com.backbase.accesscontrol.domain.Participant participant
                = getMasterServiceAgreementParticipant(serviceAgreement);
            if (validateSharingUsersAndAccounts(participant)) {
                throw getBadRequestException(ERR_ACC_065.getErrorMessage(), ERR_ACC_065.getErrorCode());
            }
            if (isNotEmpty(participant.getParticipantUsers())) {
                throw getBadRequestException(ERR_ACC_063.getErrorMessage(), ERR_ACC_063.getErrorCode());
            }
            LegalEntity legalEntity = participant.getLegalEntity();
            if (serviceAgreementJpaRepository.existsByCreatorLegalEntityIdAndIsMasterTrue(legalEntity.getId())) {
                throw getBadRequestException(ERR_ACC_066.getErrorMessage(), ERR_ACC_066.getErrorCode());
            }
            serviceAgreement.setCreatorLegalEntity(legalEntity);
        }
    }

    private boolean validateSharingUsersAndAccounts(com.backbase.accesscontrol.domain.Participant participant) {
        return doesNotShareAccountOrUsers(participant.isShareUsers(), participant.isShareAccounts());
    }

    private com.backbase.accesscontrol.domain.Participant getMasterServiceAgreementParticipant(
        ServiceAgreement serviceAgreement) {
        Map<String, com.backbase.accesscontrol.domain.Participant> participants = serviceAgreement
            .getParticipants();
        if (participants.size() != 1) {
            throw getBadRequestException(ERR_ACC_064.getErrorMessage(), ERR_ACC_064.getErrorCode());
        }
        return participants.values().stream()
            .findAny()
            .orElseThrow(() -> getBadRequestException(ERR_ACC_064.getErrorMessage(), ERR_ACC_064.getErrorCode()));
    }

    private void validParticipants(Map<String, com.backbase.accesscontrol.domain.Participant> participants) {
        if (hasParticipantsThatShareNothing(participants)) {
            throw getBadRequestException(ERR_ACC_043.getErrorMessage(), ERR_ACC_043.getErrorCode());
        }
        if (hasParticipantsThatDoNotShareUsersButExpose(participants)) {
            throw getBadRequestException(ERR_ACC_063.getErrorMessage(), ERR_ACC_063.getErrorCode());
        }
    }

    private boolean hasParticipantsThatDoNotShareUsersButExpose(
        Map<String, com.backbase.accesscontrol.domain.Participant> participants) {
        return participants
            .values()
            .stream()
            .filter(participant -> !participant.isShareUsers())
            .anyMatch(participant -> isNotEmpty(participant.getParticipantUsers()));
    }

    private boolean hasParticipantsThatShareNothing(
        Map<String, com.backbase.accesscontrol.domain.Participant> participants) {
        return participants
            .values()
            .stream()
            .anyMatch(participant -> isSharingNothing(participant.isShareAccounts(), participant.isShareUsers()));
    }

    private boolean isSharingNothing(boolean shareAccounts, boolean shareUsers) {
        return !shareAccounts && !shareUsers;
    }

    private List<PersistenceExtendedParticipant> transformToPersistenceParticipantExtendedItems(
        List<com.backbase.accesscontrol.domain.Participant> participants) {
        return participants.stream()
            .map(this::transformPersistenceParticipantExtendedItem)
            .collect(toList());
    }

    private PersistenceExtendedParticipant transformPersistenceParticipantExtendedItem(
        com.backbase.accesscontrol.domain.Participant participant) {
        PersistenceExtendedParticipant item = new PersistenceExtendedParticipant();
        item.setId(participant.getLegalEntity().getId());
        item.setExternalId(participant.getLegalEntity().getExternalId());
        item.setExternalServiceAgreementId(participant.getServiceAgreement().getExternalId());
        item.setName(participant.getLegalEntity().getName());
        item.setSharingAccounts(participant.isShareAccounts());
        item.setSharingUsers(participant.isShareUsers());
        return item;
    }

    /**
     * Retrieves service agreements by creatorId.
     *
     * @param creatorId                     creator of the service agreements
     * @param userParameters                user parameters
     * @param searchAndPaginationParameters - parameters for searching and pagination
     * @return list of service agreements that satisfy the conditions
     */
    @Transactional(readOnly = true)
    public Page<ServiceAgreement> listServiceAgreements(String creatorId, UserParameters userParameters,
        SearchAndPaginationParameters searchAndPaginationParameters) {
        LOGGER.info("Trying to list service agreements in hierarchy");
        return listServiceAgreementsByCreatorIdIn(creatorId, userParameters,
            searchAndPaginationParameters);
    }

    private void setServiceAgreementData(ServiceAgreementSave serviceAgreementRequest,
        ServiceAgreement serviceAgreement) {
        UpdateKind.NAME.updateServiceAgreementFields(serviceAgreement, serviceAgreementRequest.getName());
        UpdateKind.DESCRIPTION.updateServiceAgreementFields(serviceAgreement, serviceAgreementRequest.getDescription());
        UpdateKind.EXTERNAL_ID.updateServiceAgreementFields(serviceAgreement, serviceAgreementRequest.getExternalId());

        Date fromDate = dateTimeService.getStartDateFromDateAndTime(serviceAgreementRequest.getValidFromDate(),
            serviceAgreementRequest.getValidFromTime());
        Date untilDate = dateTimeService.getEndDateFromDateAndTime(serviceAgreementRequest.getValidUntilDate(),
            serviceAgreementRequest.getValidUntilTime());

        serviceAgreementUpdateWithValidations(serviceAgreement,
            ServiceAgreementState.fromString(serviceAgreementRequest.getStatus().toString()),
            fromDate, untilDate);

        Set<String> newParticipants = serviceAgreementRequest.getParticipants().stream().map(
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant::getId)
            .collect(toSet());

        List<com.backbase.accesscontrol.domain.Participant> removedParticipants = serviceAgreement.getParticipants()
            .entrySet()
            .stream().filter(p -> !newParticipants.contains(p.getKey())).map(Entry::getValue)
            .collect(toList());

        List<com.backbase.accesscontrol.domain.Participant> addedOrUpdatedParticipants = serviceAgreementRequest
            .getParticipants().stream()
            .map(transformParticipantDtoToPersistence())
            .collect(toList());

        serviceAgreementRequest.getParticipants().stream()
            .filter(newParticipant -> serviceAgreement.getParticipants().containsKey(newParticipant.getId())
                && serviceAgreement.getParticipants().get(newParticipant.getId()).isShareUsers()
                && !newParticipant.getSharingUsers())
            .forEach(participant -> serviceAgreement.getParticipants()
                .get(participant.getId()).getParticipantUsers().clear());

        serviceAgreement.removeParticipant(removedParticipants);
        removeAdminPrivilegesForParticipantsThatAreBeingRemoved(removedParticipants);
        serviceAgreement.addParticipant(addedOrUpdatedParticipants);
        serviceAgreement.setAdditions(serviceAgreementRequest.getAdditions());

        serviceAgreementAdminService.updateAdmins(serviceAgreement, serviceAgreementRequest.getParticipants());
    }

    private void removeAdminPrivilegesForParticipantsThatAreBeingRemoved(
        List<com.backbase.accesscontrol.domain.Participant> removedParticipants) {
        removedParticipants.forEach(participant -> {
            if (isNotEmpty(participant.getAdmins().keySet())) {
                String systemFunctionGroupForParticipant = getSystemFunctionGroupIdsForParticipant(participant);
                for (String adminId : participant.getAdmins().keySet()) {
                    userAccessFunctionGroupService
                        .deleteSystemFunctionGroupFromUserAccess(systemFunctionGroupForParticipant, adminId,
                            participant.getServiceAgreement());
                }
            }
        });
    }

    private void validateUpdateServiceAgreement(ServiceAgreement serviceAgreementPrev,
        ServiceAgreementSave serviceAgreementNew) {

        validateIsSameType(serviceAgreementPrev, serviceAgreementNew);
        validateExternalIDisUnique(serviceAgreementNew.getExternalId(), serviceAgreementPrev.getExternalId());
        validateParticipantsNotRemoved(serviceAgreementPrev, serviceAgreementNew);
        validateParticipantKeepShareAccountsOrUsers(serviceAgreementPrev, serviceAgreementNew);
        validateMasterServiceAgreementKeepParticipantAndCreator(serviceAgreementPrev, serviceAgreementNew);
        validateLegalEntityHierarchyService.validateLegalEntityHierarchy(serviceAgreementPrev.getCreatorLegalEntity(),
            serviceAgreementNew.getParticipants()
                .stream()
                .map(
                    com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant::getId)
                .collect(toSet()));

        Date fromDate = dateTimeService.getStartDateFromDateAndTime(serviceAgreementNew.getValidFromDate(),
            serviceAgreementNew.getValidFromTime());
        Date untilDate = dateTimeService.getEndDateFromDateAndTime(serviceAgreementNew.getValidUntilDate(),
            serviceAgreementNew.getValidUntilTime());

        validateTimeBoundServiceAgreementInPending(serviceAgreementPrev.getId(), fromDate,
            untilDate);
    }

    private void validateMasterServiceAgreementKeepParticipantAndCreator(ServiceAgreement serviceAgreementPrev,
        ServiceAgreementSave serviceAgreementNew) {
        if (serviceAgreementPrev.isMaster()) {
            if (isNotEmpty(
                difference(getServiceAgreementSaveParticipants(serviceAgreementNew),
                    serviceAgreementPrev.getParticipants().keySet()))) {
                throw getBadRequestException(ERR_ACC_069.getErrorMessage(), ERR_ACC_069.getErrorCode());
            }
            if (serviceAgreementNew.getParticipants().stream()
                .anyMatch(this::haveValidParticipantOptionsForMasterServiceAgreement)) {
                throw getBadRequestException(ERR_ACC_065.getErrorMessage(), ERR_ACC_065.getErrorCode());
            }
            if (com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.DISABLED
                .toString()
                .equals(serviceAgreementNew.getStatus().toString())
                && isNull(serviceAgreementPrev.getCreatorLegalEntity().getParent())) {
                throw getBadRequestException(ERR_ACC_070.getErrorMessage(), ERR_ACC_070.getErrorCode());
            }
        }
    }

    private boolean haveValidParticipantOptionsForMasterServiceAgreement(
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant participant) {
        return doesNotShareAccountOrUsers(participant.getSharingAccounts(), participant.getSharingUsers());
    }

    private boolean doesNotShareAccountOrUsers(Boolean sharingAccounts, Boolean sharingUsers) {
        return !sharingAccounts || !sharingUsers;
    }

    private Set<String> getServiceAgreementSaveParticipants(
        ServiceAgreementSave serviceAgreementNew) {
        return serviceAgreementNew
            .getParticipants()
            .stream()
            .map(com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant::getId)
            .collect(Collectors.toSet());
    }

    private void validateIsSameType(ServiceAgreement serviceAgreementPrev,
        ServiceAgreementSave serviceAgreementNew) {
        if (serviceAgreementPrev.isMaster() != serviceAgreementNew.getIsMaster()) {
            throw getBadRequestException(ERR_ACC_068.getErrorMessage(), ERR_ACC_068.getErrorCode());
        }
    }

    private void validateParticipantKeepShareAccountsOrUsers(ServiceAgreement serviceAgreementPrev,
        ServiceAgreementSave serviceAgreementNew) {

        Set<String> oldSharingAccounts = serviceAgreementPrev.getParticipants().values().stream()
            .filter(com.backbase.accesscontrol.domain.Participant::isShareAccounts)
            .map(participant -> participant.getLegalEntity().getId())
            .collect(Collectors.toSet());
        Set<String> newSharingAccounts = serviceAgreementNew.getParticipants()
            .stream()
            .filter(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant
                    ::getSharingAccounts)
            .map(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant::getId)
            .collect(Collectors.toSet());

        Set<String> oldSharingUsers = serviceAgreementPrev.getParticipants().values().stream()
            .filter(com.backbase.accesscontrol.domain.Participant::isShareUsers)
            .map(participant -> participant.getLegalEntity().getId())
            .collect(Collectors.toSet());
        Set<String> newSharingUsers = serviceAgreementNew.getParticipants().stream()
            .filter(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant
                    ::getSharingUsers)
            .map(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant::getId)
            .collect(Collectors.toSet());
        if (serviceAgreementPrev.isMaster()
            && isInvalidParticipantKeepingShareAccountsOrUsers(oldSharingAccounts, newSharingAccounts, oldSharingUsers,
            newSharingUsers)) {
            throw getBadRequestException(QueryErrorCodes.ERR_ACQ_038.getErrorMessage(),
                QueryErrorCodes.ERR_ACQ_038.getErrorCode());
        }
    }

    private boolean isInvalidParticipantKeepingShareAccountsOrUsers(Set<String> oldSharingAccounts,
        Set<String> newSharingAccounts,
        Set<String> oldSharingUsers,
        Set<String> newSharingUsers) {
        return !newSharingAccounts.containsAll(oldSharingAccounts)
            || !newSharingUsers.containsAll(oldSharingUsers)
            || !Sets.intersection(difference(newSharingAccounts, oldSharingAccounts), oldSharingUsers).isEmpty()
            || !Sets.intersection(difference(newSharingUsers, oldSharingUsers), oldSharingAccounts).isEmpty();
    }

    private void validateParticipantsNotRemoved(ServiceAgreement serviceAgreementPrev,
        ServiceAgreementSave serviceAgreementNew) {
        if (serviceAgreementPrev.isMaster()) {
            Set<String> oldParticipants = Sets.newHashSet(serviceAgreementPrev.getParticipants().keySet());
            Set<String> newParticipants = Sets.newHashSet(getServiceAgreementSaveParticipants(serviceAgreementNew));
            if (!newParticipants.containsAll(oldParticipants)) {
                LOGGER.info("Participant cannot be removed");
                throw getBadRequestException(QueryErrorCodes.ERR_ACQ_037.getErrorMessage(),
                    QueryErrorCodes.ERR_ACQ_037.getErrorCode());
            }
        } else {
            validateIfParticipantsCanBeRemovedOrRoleCanBeChanged(serviceAgreementNew, serviceAgreementPrev);
        }
    }

    private void validateIfParticipantsCanBeRemovedOrRoleCanBeChanged(ServiceAgreementSave serviceAgreementNew,
        ServiceAgreement serviceAgreementPrev) {

        List<String> arrangementItems = serviceAgreementPrev.getDataGroups()
            .stream()
            .filter(dataGroup -> dataGroup.getDataItemType().equals(ARRANGEMENTS))
            .flatMap(dataGroup -> dataGroup.getDataGroupItems().stream())
            .map(DataGroupItem::getDataItemId)
            .collect(Collectors.toList());

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            arrangementItems.addAll(approvalDataGroupDetailsJpaRepository
                .findAllByServiceAgreementIdAndType(serviceAgreementPrev.getId(), "ARRANGEMENTS").stream()
                .flatMap(dg -> dg.getItems().stream())
                .collect(toSet()));
        }

        Map<String, com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups
            .serviceagreements.Participant> newParticipantsMap = serviceAgreementNew.getParticipants()
            .stream()
            .collect(Collectors.toMap(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant::getId,
                participant -> participant));

        Set<String> removedSharingUsers = serviceAgreementPrev.getParticipants().entrySet().stream()
            .filter(entry -> entry.getValue().isShareUsers())
            .filter(entry -> !newParticipantsMap.containsKey(entry.getKey())
                || (newParticipantsMap.containsKey(entry.getKey()) && !newParticipantsMap.get(entry.getKey())
                .getSharingUsers()))
            .flatMap(entry -> entry.getValue().getParticipantUsers().stream())
            .map(ParticipantUser::getUserId)
            .collect(toSet());

        Set<String> sharingAccountsToStay = serviceAgreementPrev.getParticipants().entrySet().stream()
            .filter(entry -> entry.getValue().isShareAccounts())
            .map(Entry::getKey)
            .filter(id -> newParticipantsMap.containsKey(id) && newParticipantsMap.get(id).getSharingAccounts())
            .collect(toSet());

        if (!removedSharingUsers.isEmpty()) {

            Stream<List<String>> batchRequestOnChunks = CommonUtils
                .getBatchRequestOnChunks(new ArrayList<>(removedSharingUsers), 1000);

            batchRequestOnChunks.forEach(chunk -> {
                boolean hasAssignedUserPending = false;
                if (applicationProperties.getApproval().getValidation().isEnabled()) {
                    hasAssignedUserPending = approvalUserContextJpaRepository
                        .existsByServiceAgreementIdAndUserIdIn(serviceAgreementPrev.getId(), chunk);
                }
                boolean hasAssignedUser = userAssignedFunctionGroupJpaRepository
                    .existsByServiceAgreementIdAndUserIdIn(serviceAgreementPrev.getId(), chunk);
                if (hasAssignedUser || hasAssignedUserPending) {
                    LOGGER.warn(
                        "Participant can not be removed from the service agreement,there are users with permissions"
                            + " in the service agreement");
                    throw getBadRequestException(ERR_ACC_054.getErrorMessage(), ERR_ACC_054.getErrorCode());
                }
            });
        }
        ingestParticipantUpdateRemoveDataValidationProcessor
            .canRemoveParticipantSharingAccounts(arrangementItems, new ArrayList<>(sharingAccountsToStay),
                serviceAgreementPrev.getId());
    }

    private void validateExternalIDisUnique(String newExternalId, String previousExternalId) {
        if (newExternalId != null
            && !newExternalId.equals(previousExternalId)
            && serviceAgreementJpaRepository.existsByExternalId(newExternalId)) {
            throw getBadRequestException(QueryErrorCodes.ERR_ACQ_036.getErrorMessage(),
                QueryErrorCodes.ERR_ACQ_036.getErrorCode());
        }
    }

    private void validateRootServiceAgreementDoesNotHaveStartAndEndDate(boolean isMaster, LegalEntity parentLegalEntity,
        Date startDate, Date endDate) {
        if (!timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLegalEntity, startDate, endDate)) {
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_076.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_076.getErrorCode());
        }
    }

    private void validateTimePeriodOfServiceAgreement(Date startDate, Date endDate, Set<FunctionGroup> functionGroups) {
        if (!timeBoundValidatorService.isPeriodValid(startDate, endDate, functionGroups)) {
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_077.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_077.getErrorCode());
        }
    }
    
    private void validateTimePeriodOfServiceAgreement(Date startDate, Date endDate) {
        if (!timeBoundValidatorService.isPeriodValid(startDate, endDate)) {
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_077.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_077.getErrorCode());
        }
    }

    private Map<String, com.backbase.accesscontrol.domain.Participant> getLegalEntitiesThatShareUsersInServiceAgreement(
        String serviceAgreementId,
        Set<String> userSharingParticipants) {
        List<com.backbase.accesscontrol.domain.Participant> providers = participantJpaRepository
            .findDistinctByServiceAgreementIdAndLegalEntityIdInAndShareUsersTrue(serviceAgreementId,
                userSharingParticipants);
        if (providers.size() != userSharingParticipants.size()) {
            throw getBadRequestException(ERR_ACC_042.getErrorMessage(), ERR_ACC_042.getErrorCode());
        }
        return providers.stream()
            .collect(toMap(participant -> participant.getLegalEntity().getId(), identity()));
    }

    private void updateServiceAgreementStatus(ServiceAgreement serviceAgreement, ServiceAgreementState updateState) {
        LOGGER.info("Trying to change status from {} to {}", serviceAgreement.getState(), updateState);
        if (updateState != null && !serviceAgreement.getState().toString().equals(updateState.toString())) {

            serviceAgreement.setState(ServiceAgreementState.fromString(updateState.toString()));
            serviceAgreement.setStateChangedAt(new Date());
        }
    }

    private boolean shouldUpdatePrivileges(ServiceAgreement serviceAgreement,
        ServiceAgreementState updateState,
        Date from, Date until) {

        return (updateState != null && !serviceAgreement.getState().toString().equals(updateState.toString()))
            || (nonNull(from) && !from.equals(serviceAgreement.getStartDate()))
            || (nonNull(serviceAgreement.getStartDate()) && !serviceAgreement.getStartDate().equals(from))
            || (nonNull(until) && !until.equals(serviceAgreement.getEndDate()))
            || (nonNull(serviceAgreement.getEndDate()) && !serviceAgreement.getEndDate().equals(until));
    }

    private ServiceAgreement populateServiceAgreement(ServiceAgreementPostRequestBody agreementPostRequestBody,
        String legalEntityId) {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName(agreementPostRequestBody.getName());
        serviceAgreement.setDescription(agreementPostRequestBody.getDescription());
        serviceAgreement.setState(ServiceAgreementState.fromString(agreementPostRequestBody.getStatus().toString()));
        serviceAgreement.setStateChangedAt(new Date());
        LegalEntity creatorLegalEntity = new LegalEntity();
        creatorLegalEntity.setId(legalEntityId);
        serviceAgreement.setCreatorLegalEntity(creatorLegalEntity);
        serviceAgreement.setMaster(false);
        serviceAgreement.addParticipant(agreementPostRequestBody.getParticipants()
            .stream()
            .map(transformParticipantDtoToPersistence())
            .collect(toList())
        );

        Date startDate = dateTimeService
            .getStartDateFromDateAndTime(agreementPostRequestBody.getValidFromDate(),
                agreementPostRequestBody.getValidFromTime());
        Date endDate = dateTimeService
            .getEndDateFromDateAndTime(agreementPostRequestBody.getValidUntilDate(),
                agreementPostRequestBody.getValidUntilTime());

        if (nonNull(startDate) && nonNull(endDate)) {
            serviceAgreement.setStartDate(startDate);
            serviceAgreement.setEndDate(endDate);
        } else if (isNull(startDate) && nonNull(endDate)) {
            serviceAgreement.setEndDate(endDate);
        } else if (nonNull(startDate)) {
            serviceAgreement.setStartDate(startDate);
        }
        serviceAgreement.setAdditions(agreementPostRequestBody.getAdditions());
        return serviceAgreement;
    }

    private Function<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant,
        com.backbase.accesscontrol.domain.Participant> transformParticipantDtoToPersistence() {
        return participantDTO -> {
            com.backbase.accesscontrol.domain.Participant participant
                = new com.backbase.accesscontrol.domain.Participant();
            LegalEntity legalEntity = new LegalEntity();
            legalEntity.setId(participantDTO.getId());
            participant.setLegalEntity(legalEntity);
            participant.setShareAccounts(participantDTO.getSharingAccounts());
            participant.setShareUsers(participantDTO.getSharingUsers());
            return participant;
        };
    }

    private List<Participant> getParticipantsForServiceAgreement(
        List<com.backbase.accesscontrol.domain.Participant> participantList) {
        return participantList.stream()
            .map(participantsByLegalEntity ->
                createParticipant(participantsByLegalEntity.getLegalEntity(),
                    participantsByLegalEntity.isShareAccounts(), participantsByLegalEntity.isShareUsers())
            )
            .collect(toList());
    }

    private Participant createParticipant(LegalEntity legalEntity, Boolean isShareAccounts, Boolean isShareUsers) {
        return new Participant()
            .withName(legalEntity.getName())
            .withId(legalEntity.getId())
            .withExternalId(legalEntity.getExternalId())
            .withSharingAccounts(isShareAccounts)
            .withSharingUsers(isShareUsers);
    }

    private Optional<ParticipantUser> findProviderUser(com.backbase.accesscontrol.domain.Participant provider,
        String userId) {
        return provider.getParticipantUsers()
            .stream()
            .filter(providerUser -> providerUser.getUserId().equals(userId))
            .findFirst();
    }

    private void removeProviderUsers(List<UsersDto> requestBody,
        Map<String, com.backbase.accesscontrol.domain.Participant> providers) {
        for (UsersDto body : requestBody) {
            com.backbase.accesscontrol.domain.Participant providerToBeUpdated
                = providers.get(body.getLegalEntityId());
            boolean containsAllUsersToBeRemoved = providerToBeUpdated.getParticipantUsers()
                .stream()
                .map(ParticipantUser::getUserId)
                .collect(toSet())
                .containsAll(body.getUsers());
            if (containsAllUsersToBeRemoved) {
                Set<ParticipantUser> users = providerToBeUpdated.getParticipantUsers().stream()
                    .filter(user -> body.getUsers().contains(user.getUserId()))
                    .collect(toSet());
                providerToBeUpdated.getParticipantUsers().removeAll(users);
            } else {
                throw getBadRequestException(CommandErrorCodes.ERR_ACC_040.getErrorMessage(),
                    CommandErrorCodes.ERR_ACC_040.getErrorCode());

            }
        }
    }

    private com.backbase.accesscontrol.domain.Participant findProviderInServiceAgreement(
        String serviceAgreementExternalId, String legalEntityId) {
        return participantJpaRepository.findDistinctByServiceAgreementExternalIdAndLegalEntityIdAndShareUsersTrue(
            serviceAgreementExternalId, legalEntityId)
            .orElseThrow(() -> getBadRequestException(ERR_ACC_038.getErrorMessage(), ERR_ACC_038.getErrorCode()));
    }

    private void checkIfUserIsAddedOnServiceAgreement(
        BiPredicate<com.backbase.accesscontrol.domain.Participant, String> predicate,
        com.backbase.accesscontrol.domain.Participant provider, String userId, CommandErrorCodes errorCode) {
        if (predicate.test(provider, userId)) {
            throw getBadRequestException(errorCode.getErrorMessage(), errorCode.getErrorCode());
        }
    }

    private boolean isInvalidLegalEntity(LegalEntity legalEntity, LegalEntity creatorLegalEntity) {
        return Objects.isNull(creatorLegalEntity) || (!legalEntity.equals(creatorLegalEntity)
            && legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(
            creatorLegalEntity.getId(),
            singletonList(legalEntity.getId())).isEmpty());
    }

    private Predicate<com.backbase.accesscontrol.domain.Participant> isAlreadyExposedParticipant(
        PresentationParticipantPutBody item) {
        return participant -> item.getExternalParticipantId().equals(participant.getLegalEntity().getExternalId());
    }

    private Page<ServiceAgreement> listServiceAgreementsByCreatorIdIn(String creatorId, UserParameters userParameters,
        SearchAndPaginationParameters searchAndPaginationParameters) {
        LOGGER.info("Trying to get Service Agreements by creator Ids");
        return serviceAgreementJpaRepository
            .findByCreatorIdInHierarchyAndParameters(creatorId, userParameters, searchAndPaginationParameters,
                SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS);
    }

    private void validatePendingApprovalsForUser(String serviceAgreementId, Set<String> allUserIdsToBeRemoved) {
        long count = approvalUserContextJpaRepository.countByServiceAgreementIdAndUserIdIn(
            serviceAgreementId, allUserIdsToBeRemoved);
        if (count > 0) {
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_075.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_075.getErrorCode());
        }
    }

    /**
     * Deletes service agreement by identifier.
     *
     * @param identifier - service agreement identifier
     */
    @Transactional
    public String deleteServiceAgreementByIdentifier(
        PresentationServiceAgreementIdentifier identifier) {

        ServiceAgreement serviceAgreement = getServiceAgreementByIdentifier(identifier);

        validateIfServiceAgreementCanBeDeleted(serviceAgreement);

        serviceAgreementJpaRepository.delete(serviceAgreement);

        return serviceAgreement.getId();
    }

    /**
     * Get service agreement ids with data group ids and data item ids.
     *
     * @param userId        user id
     * @param dataGroupType data group type
     * @param functionName  functionName name
     * @param resourceName  resourceName name
     * @param privileges    privileges
     * @return list of {@link PersistenceServiceAgreementDataGroups}
     */
    public List<PersistenceServiceAgreementDataGroups> getServiceAgreementsDataGroups(String userId,
        String dataGroupType, String functionName, String resourceName, String privileges) {

        LOGGER.info("Trying to get service agreement ids with data group ids and data item ids");

        List<String> privilegesList = Optional
            .ofNullable(privileges)
            .filter(StringUtils::isNotEmpty)
            .map(p -> asList(p.split(",")))
            .orElseGet(Collections::emptyList);

        List<String> validPrivilegeNames = Arrays.stream(PrivilegesEnum.values())
            .map(PrivilegesEnum::getPrivilegeName)
            .collect(toList());

        if (!validPrivilegeNames.containsAll(privilegesList)) {
            LOGGER.warn("Some of the privilege names provided are not valid.");
            return emptyList();
        }

        Set<String> afpIds = emptySet();

        if (isNotEmpty(functionName) || isNotEmpty(resourceName)) {
            afpIds = businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privilegesList);

            if (CollectionUtils.isEmpty(afpIds)) {
                LOGGER.warn("There are no applicable function privileges for function, resource name or privileges");
                return emptyList();
            }
        }

        List<Tuple> saDgItemsWithAfpId = serviceAgreementJpaRepository
            .findByUserIdAndDataGroupTypeAndAfpIdsIn(userId, dataGroupType,
                privilegesList.isEmpty() ? afpIds : Collections.emptyList());

        if (CollectionUtils.isNotEmpty(privilegesList)) {
            Map<String, Map<String, Set<String>>> saDgAfpIds = saDgItemsWithAfpId.stream()
                .collect(
                    Collectors.groupingBy(tuple -> tuple.get(0, String.class),
                        Collectors.groupingBy(tuple -> tuple.get(1, String.class),
                            Collectors.mapping(tuple -> tuple.get(3, String.class), toSet()))));

            Map<String, Set<String>> saDgMap = getServiceAgreementDataGroupMapWhichHavePrivileges(functionName,
                resourceName, privilegesList, saDgAfpIds);

            Map<String, Map<String, Set<String>>> saIdDgIdsItemIds = saDgItemsWithAfpId.stream()
                .filter(tuple -> existsSaDgWhichHasAllPrivilegesProvided(tuple.get(0, String.class),
                    tuple.get(1, String.class), saDgMap))
                .collect(
                    Collectors.groupingBy(tuple -> tuple.get(0, String.class),
                        Collectors.groupingBy(tuple -> tuple.get(1, String.class),
                            Collectors.mapping(tuple -> tuple.get(2, String.class), toSet()))));

            return transform(saIdDgIdsItemIds);
        }

        Map<String, Map<String, Set<String>>> saIdDgIdsDgItemIds = saDgItemsWithAfpId.stream()
            .collect(
                Collectors.groupingBy(tuple -> tuple.get(0, String.class),
                    Collectors.groupingBy(tuple -> tuple.get(1, String.class),
                        Collectors.mapping(tuple -> tuple.get(2, String.class), toSet()))));

        return transform(saIdDgIdsDgItemIds);
    }

    private Map<String, Set<String>> getServiceAgreementDataGroupMapWhichHavePrivileges(String functionName,
        String resourceName, List<String> privilegesList, Map<String, Map<String, Set<String>>> saDgAfpIds) {

        Map<String, Set<String>> saDgWhichHavePrivileges = new HashMap<>();

        for (Entry<String, Map<String, Set<String>>> saDgAfpIdEntry : saDgAfpIds.entrySet()) {
            Map<String, Set<String>> dgAfpIdMap = saDgAfpIdEntry.getValue();
            for (Entry<String, Set<String>> dgAfpIdEntry : dgAfpIdMap.entrySet()) {

                Stream<ApplicableFunctionPrivilege> afpStream = businessFunctionCache
                    .getApplicableFunctionPrivileges(dgAfpIdEntry.getValue())
                    .stream();

                if (StringUtils.isNotEmpty(functionName)) {
                    afpStream = afpStream.filter(afp -> afp.getBusinessFunctionName().equals(functionName));
                }

                if (StringUtils.isNotEmpty(resourceName)) {
                    afpStream = afpStream.filter(afp -> afp.getBusinessFunctionResourceName().equals(resourceName));
                }

                Map<String, Set<String>> bfPrivileges = afpStream.collect(
                    Collectors.groupingBy(ApplicableFunctionPrivilege::getBusinessFunctionName,
                        mapping(ApplicableFunctionPrivilege::getPrivilegeName, toSet())));

                boolean matchedAllPrivileges = !bfPrivileges.isEmpty() && bfPrivileges.values().stream()
                    .allMatch(privileges -> privileges.containsAll(privilegesList));

                if (matchedAllPrivileges) {
                    addSaDgToMap(saDgWhichHavePrivileges, saDgAfpIdEntry.getKey(), dgAfpIdEntry.getKey());
                }

            }
        }
        return saDgWhichHavePrivileges;
    }

    private void addSaDgToMap(Map<String, Set<String>> saDgWhichHavePrivileges, String saId, String dgId) {
        if (isNull(saDgWhichHavePrivileges.get(saId))) {
            saDgWhichHavePrivileges.put(saId, newHashSet(dgId));
        } else {
            Set<String> dgIds = saDgWhichHavePrivileges.get(saId);
            dgIds.add(dgId);
            saDgWhichHavePrivileges.put(saId, dgIds);
        }
    }

    private boolean existsSaDgWhichHasAllPrivilegesProvided(String saId, String dgId,
        Map<String, Set<String>> validSaDg) {

        if (!validSaDg.containsKey(saId)) {
            return false;
        }

        Set<String> dgIds = validSaDg.get(saId);
        return nonNull(dgIds) && dgIds.contains(dgId);
    }

    private List<PersistenceServiceAgreementDataGroups> transform(Map<String, Map<String, Set<String>>> saDgItems) {

        return saDgItems.entrySet().stream()
            .map(entry -> transform(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private PersistenceServiceAgreementDataGroups transform(String saId, Map<String, Set<String>> dgItems) {

        List<PersistenceDataGroupDataItems> dataGroupDataItemsList = dgItems.entrySet().stream()
            .map(entry -> {
                PersistenceDataGroupDataItems dataGroupDataItems = new PersistenceDataGroupDataItems();
                dataGroupDataItems.setId(entry.getKey());
                dataGroupDataItems.setItems(new ArrayList<>(entry.getValue()));
                return dataGroupDataItems;
            })
            .collect(toList());

        PersistenceServiceAgreementDataGroups serviceAgreementDataGroups = new PersistenceServiceAgreementDataGroups();
        serviceAgreementDataGroups.setServiceAgreementId(saId);
        serviceAgreementDataGroups.setDataGroups(dataGroupDataItemsList);
        return serviceAgreementDataGroups;
    }

    private ServiceAgreement getServiceAgreementByIdentifier(PresentationServiceAgreementIdentifier identifier) {

        if (isNotEmpty(identifier.getIdIdentifier())) {
            return serviceAgreementJpaRepository
                .findById(identifier.getIdIdentifier(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR)
                .orElseThrow(() -> {
                    LOGGER.warn(SERVICE_AGREEMENT_EXTERNAL_ID_DOES_NOT_EXIST);
                    return getNotFoundException(ERR_ACQ_006.getErrorMessage(),
                        ERR_ACQ_006.getErrorCode());
                });
        }
        if (isNotEmpty(identifier.getExternalIdIdentifier())) {
            return serviceAgreementJpaRepository
                .findByExternalId(identifier.getExternalIdIdentifier(), SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR)
                .orElseThrow(() -> {
                    LOGGER.warn(SERVICE_AGREEMENT_EXTERNAL_ID_DOES_NOT_EXIST);
                    return getNotFoundException(ERR_ACQ_006.getErrorMessage(),
                        ERR_ACQ_006.getErrorCode());
                });
        }
        return getServiceAgreementsByNameIdentifier(identifier.getNameIdentifier());
    }

    public ServiceAgreement getServiceAgreementsByNameIdentifier(String nameIdentifier) {
        List<ServiceAgreement> serviceAgreements = serviceAgreementJpaRepository
            .findServiceAgreementsByName(nameIdentifier);
        if (serviceAgreements.isEmpty()) {
            LOGGER.warn(SERVICE_AGREEMENT_EXTERNAL_ID_DOES_NOT_EXIST);
            throw getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
        }
        Set<String> uniqueServiceAgreementIds = serviceAgreements.stream()
            .map(ServiceAgreement::getId)
            .collect(toSet());

        if (uniqueServiceAgreementIds.size() != 1) {
            LOGGER.warn("There are more than one service agreements with the same name.");
            throw getBadRequestException(ERR_ACQ_057.getErrorMessage(), ERR_ACQ_057.getErrorCode());
        }
        return serviceAgreements.get(0);
    }

    private void validateIfServiceAgreementCanBeDeleted(ServiceAgreement serviceAgreement) {
        if (isNull(serviceAgreement.getCreatorLegalEntity().getParent()) && serviceAgreement.isMaster()) {
            LOGGER.warn("Service Agreement {} cannot be deleted because it is the master of root legal entity {}.",
                serviceAgreement.getId(), serviceAgreement.getCreatorLegalEntity().getId());
            throw getBadRequestException(ERR_ACQ_056.getErrorMessage(), ERR_ACQ_056.getErrorCode());
        }
        if (serviceAgreementJpaRepository
            .checkIfExistsUsersWithAssignedPermissionsInServiceAgreement(serviceAgreement)) {
            LOGGER.warn("There are users with assigned permissions in service agreement with id {} ",
                serviceAgreement.getId());
            throw getBadRequestException(ERR_ACQ_053.getErrorMessage(), ERR_ACQ_053.getErrorCode());
        }
        if (serviceAgreementJpaRepository
            .checkIsExistsUsersWithPendingPermissionsInServiceAgreement(serviceAgreement.getId())) {
            LOGGER.warn("There are users with assigned pending permissions in service agreement with id {} ",
                serviceAgreement.getId());
            throw getBadRequestException(ERR_ACQ_055.getErrorMessage(), ERR_ACQ_055.getErrorCode());
        }
        if (existsPendingDataGroupInServiceAgreement(serviceAgreement.getId())
            || existsPendingDeleteDataGroupInServiceAgreement(serviceAgreement.getId())) {
            LOGGER.warn("You cannot manage this service agreement, while there is a pending data group");
            throw getBadRequestException(ERR_AG_109.getErrorMessage(), ERR_AG_109.getErrorCode());
        }
        if (existsPendingJobRoleInServiceAgreement(serviceAgreement.getId()) ||
            existsPendingDeleteJobRoleInServiceAgreement(serviceAgreement.getId())) {
            LOGGER.warn("You cannot manage this service agreement, while there is a pending function group");
            throw getBadRequestException(ERR_AG_108.getErrorMessage(), ERR_AG_108.getErrorCode());
        }

    }

    private enum UpdateKind {
        NAME {
            @Override
            public void updateServiceAgreementFields(ServiceAgreement serviceAgreement, Object fieldValue) {
                update(fieldValue, (Object field) -> serviceAgreement.setName((String) field));
            }
        },
        DESCRIPTION {
            @Override
            public void updateServiceAgreementFields(ServiceAgreement serviceAgreement, Object fieldValue) {
                update(fieldValue, (Object field) -> serviceAgreement.setDescription((String) field));
            }
        },
        EXTERNAL_ID {
            @Override
            public void updateServiceAgreementFields(ServiceAgreement serviceAgreement, Object fieldValue) {
                update(fieldValue, (Object field) -> serviceAgreement.setExternalId((String) field));
            }
        };

        private static void update(Object field, java.util.function.Consumer<Object> update) {
            if (field != null) {
                update.accept(field);
            }
        }

        abstract void updateServiceAgreementFields(ServiceAgreement serviceAgreement, Object fieldValue);

    }
}
