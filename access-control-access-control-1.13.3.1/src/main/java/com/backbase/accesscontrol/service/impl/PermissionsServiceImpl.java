package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.FUNCTION_GROUP_WITH_SA;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_111;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_001;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_002;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_071;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_072;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_084;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_107;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_109;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_049;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_061;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_066;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_068;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_069;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_076;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.dto.AssignUserPermissionsData;
import com.backbase.accesscontrol.dto.FunctionGroupDataGroups;
import com.backbase.accesscontrol.mappers.ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper;
import com.backbase.accesscontrol.mappers.SelfApprovalPolicyMapper;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantUserJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.accesscontrol.service.ValidateLegalEntityHierarchyService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.ApprovalSelfApprovalPolicyFactory;
import com.backbase.accesscontrol.util.SelfApprovalPolicyFactory;
import com.backbase.accesscontrol.util.UserContextPermissionsFactory;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import com.backbase.accesscontrol.util.validation.UserContextPermissionsSelfApprovalPolicyValidator;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationFunctionGroupDataGroup;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionsServiceImpl implements PermissionService {

    private static final String SERVICE_AGREEMENT_DOES_NOT_EXIST = "Service agreement with id {} does not exist";
    private static final String SERVICE_AGREEMENT_DOES_NOT_EXIST_BY_EXTERNAL_ID =
        "Service agreement with external id {} does not exist";

    @PersistenceContext
    private final EntityManager entityManager;
    private final FunctionGroupJpaRepository functionGroupJpaRepository;
    private final UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    private final UserContextJpaRepository userContextJpaRepository;
    private final ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private final ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    private final ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    private final ParticipantUserJpaRepository participantUserJpaRepository;
    private final ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;
    private final ValidateLegalEntityHierarchyService validateLegalEntityHierarchyService;
    private final ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    private final ApplicationProperties applicationProperties;
    private final UserContextPermissionsSelfApprovalPolicyValidator selfApprovalPolicyValidator;
    private final SelfApprovalPolicyFactory selfApprovalPolicyFactory;
    private final ApprovalSelfApprovalPolicyFactory approvalSelfApprovalPolicyFactory;
    private final UserContextPermissionsFactory userContextPermissionsFactory;
    private final SelfApprovalPolicyMapper selfApprovalPolicyMapper;
    private final ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper userContextPermissionsMapper;
    private final BusinessFunctionCache businessFunctionCache;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String updateUserPermission(AssignUserPermissionsData assignUserPermissionsData) {
        log.info("Updating permission with put body {}", assignUserPermissionsData);

        ServiceAgreement serviceAgreement = getServiceAgreementByExternalId(
            assignUserPermissionsData.getAssignUserPermissions().getExternalServiceAgreementId());

        checkIfServiceAgreementsIsInPendingState(serviceAgreement.getId());

        String externalUserId = assignUserPermissionsData.getAssignUserPermissions().getExternalUserId();
        String internalUserId = assignUserPermissionsData.getUsersByExternalId()
            .get(externalUserId).getId();

        checkIfUserBelongInServiceAgreement(internalUserId, assignUserPermissionsData.getUsersByExternalId()
            .get(externalUserId).getLegalEntityId(), serviceAgreement);

        PermissionIndexes permissionIndexes = new PermissionIndexes(serviceAgreement);

        Set<FunctionGroup> functionGroups = getAllFunctionGroups(
            assignUserPermissionsData.getAssignUserPermissions().getFunctionGroupDataGroups(),
            serviceAgreement, permissionIndexes);

        List<FunctionGroup> functionGroupTemplates = functionGroups.stream()
            .filter(fg -> fg.getType().equals(FunctionGroupType.TEMPLATE))
            .collect(Collectors.toList());

        updatePermissionIndexesWithTemplates(permissionIndexes, functionGroupTemplates);

        validationsJobRoleTemplate(functionGroupTemplates, serviceAgreement);

        List<UserAssignedFunctionGroup> currentUserAssignedFunctionGroup = userAssignedFunctionGroupJpaRepository
            .findDistinctByUserIdAndServiceAgreementIdAndFunctionGroupTypeIn(
                internalUserId,
                serviceAgreement.getId(),
                Arrays.asList(FunctionGroupType.DEFAULT, FunctionGroupType.TEMPLATE));

        checkIfExistsDataGroupInPendingDeleteState(assignUserPermissionsData, serviceAgreement.getDataGroups());

        Set<String> functionGroupIds = functionGroups.stream().map(FunctionGroup::getId)
            .collect(Collectors.toSet());

        validateFunctionGroupsIsInPendingDelete(functionGroupIds);

        Set<FunctionGroupDataGroups> newFunctionGroupDataGroupsState = assignUserPermissionsData
            .getAssignUserPermissions()
            .getFunctionGroupDataGroups().stream()
            .map(fgDg -> convertToFunctionGroupDataGroups(fgDg, permissionIndexes))
            .collect(Collectors.toSet());

        UserContext userContext = createUserContextIfMissing(internalUserId, serviceAgreement);

        removeAssignedPermissions(currentUserAssignedFunctionGroup, newFunctionGroupDataGroupsState);

        assignPermissionsToUser(currentUserAssignedFunctionGroup, newFunctionGroupDataGroupsState, userContext);

        userAssignedFunctionGroupJpaRepository.saveAll(currentUserAssignedFunctionGroup);
        return serviceAgreement.getId();
    }

    private void updatePermissionIndexesWithTemplates(PermissionIndexes permissionIndexes,
        List<FunctionGroup> functionGroupTemplates) {
        functionGroupTemplates.forEach(fg -> {
            permissionIndexes.getMapByFunctionGroupId().put(fg.getId(), fg);
            permissionIndexes.getMapByFunctionGroupName().put(fg.getName(), fg);
        });
    }

    private FunctionGroupDataGroups convertToFunctionGroupDataGroups(PresentationFunctionGroupDataGroup fgDg,
        PermissionIndexes permissionIndexes) {

        FunctionGroup fg = getFunctionGroupByIdentifier(fgDg.getFunctionGroupIdentifier(), permissionIndexes);

        List<DataGroup> dgs = fgDg.getDataGroupIdentifiers().stream()
            .map(dg -> getDataGroupByIdentifier(dg, permissionIndexes))
            .collect(Collectors.toList());
        return new FunctionGroupDataGroups(fg, dgs);
    }

    private DataGroup getDataGroupByIdentifier(PresentationIdentifier identifier, PermissionIndexes permissionIndexes) {

        DataGroup dataGroup;
        if (nonNull(identifier.getIdIdentifier())) {
            dataGroup = permissionIndexes.getMapByDataGroupId().get(identifier.getIdIdentifier());
        } else {
            dataGroup = permissionIndexes.getMapByDataGroupName()
                .get(identifier.getNameIdentifier().getName());
        }
        if (isNull(dataGroup)) {
            log.warn("Data group not found");
            throw getNotFoundException(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode());
        }
        return dataGroup;
    }

    private FunctionGroup getFunctionGroupByIdentifier(PresentationIdentifier identifier, PermissionIndexes indexes) {
        if (nonNull(identifier.getIdIdentifier())) {
            return indexes.getMapByFunctionGroupId().get(identifier.getIdIdentifier());
        } else {
            return indexes.getMapByFunctionGroupName()
                .get(identifier.getNameIdentifier().getName());
        }
    }

    private Set<FunctionGroup> getAllFunctionGroups(List<PresentationFunctionGroupDataGroup> functionGroupDataGroups,
        ServiceAgreement serviceAgreement,
        PermissionIndexes permissionIndexes) {

        Map<String, FunctionGroup> templatesMappedById = new HashMap<>();
        Map<NameIdentifier, FunctionGroup> templatesMappedByNamedId = new HashMap<>();
        Map<FunctionGroup, Boolean> result = new HashMap<>();
        for (PresentationFunctionGroupDataGroup fgDg : functionGroupDataGroups) {
            Boolean functionGroupHasNoCombination = Optional.ofNullable(fgDg.getDataGroupIdentifiers())
                .orElseGet(ArrayList::new).isEmpty();
            Optional<FunctionGroup> functionGroup = getDefaultFunctionGroup(serviceAgreement, permissionIndexes, fgDg);
            if (functionGroup.isEmpty()) {
                FunctionGroup fgTemplate = getTemplateFunctionGroup(templatesMappedById, templatesMappedByNamedId, fgDg);

                if (!fgTemplate.getType().equals(FunctionGroupType.TEMPLATE)) {
                    log.warn("Function Group should be template function group or it's not found");
                    throw getBadRequestException(ERR_ACQ_069.getErrorMessage(), ERR_ACQ_069.getErrorCode());
                }
                if (result.containsKey(fgTemplate)
                    && !result.get(fgTemplate).equals(functionGroupHasNoCombination)) {
                    throw getBadRequestException(ERR_AG_111.getErrorMessage(), ERR_AG_111.getErrorCode());
                }
                result.put(fgTemplate, functionGroupHasNoCombination);
            } else {
                if (result.containsKey(functionGroup.get())
                    && !result.get(functionGroup.get()).equals(functionGroupHasNoCombination)) {
                    throw getBadRequestException(ERR_AG_111.getErrorMessage(), ERR_AG_111.getErrorCode());
                }
                result.put(functionGroup.get(), functionGroupHasNoCombination);
            }
        }
        return result.keySet();
    }

    private Optional<FunctionGroup> getDefaultFunctionGroup(ServiceAgreement serviceAgreement,
        PermissionIndexes permissionIndexes, PresentationFunctionGroupDataGroup fgDg) {
        Optional<FunctionGroup> functionGroup = Optional.empty();
        if (nonNull(fgDg.getFunctionGroupIdentifier().getNameIdentifier())) {
            if (serviceAgreement.getExternalId()
                .equals(fgDg.getFunctionGroupIdentifier().getNameIdentifier().getExternalServiceAgreementId())) {
                functionGroup = Optional.ofNullable(permissionIndexes
                    .mapByFunctionGroupName.get(fgDg.getFunctionGroupIdentifier().getNameIdentifier().getName()));
            }
        } else {
            functionGroup = Optional.ofNullable(
                permissionIndexes.mapByFunctionGroupId.get(fgDg.getFunctionGroupIdentifier().getIdIdentifier()));
        }
        return functionGroup;
    }

    private FunctionGroup getTemplateFunctionGroup(Map<String, FunctionGroup> templatesMappedById,
        Map<NameIdentifier, FunctionGroup> templatesMappedByNamedId, PresentationFunctionGroupDataGroup fgDg) {
        FunctionGroup fgTemplate;
        if (isNull(fgDg.getFunctionGroupIdentifier().getIdIdentifier())) {

            if (templatesMappedByNamedId.containsKey(fgDg.getFunctionGroupIdentifier().getNameIdentifier())) {
                fgTemplate = templatesMappedByNamedId.get(fgDg.getFunctionGroupIdentifier().getNameIdentifier());
            } else {
                fgTemplate = functionGroupJpaRepository
                    .findByServiceAgreementExternalIdAndName(
                        fgDg.getFunctionGroupIdentifier().getNameIdentifier().getExternalServiceAgreementId(),
                        fgDg.getFunctionGroupIdentifier().getNameIdentifier().getName())
                    .orElseThrow(
                        () -> {
                            log.warn("Function group does not exist");
                            return getNotFoundException(ERR_ACQ_003.getErrorMessage(),
                                ERR_ACQ_003.getErrorCode());
                        });
                templatesMappedById.put(fgTemplate.getId(), fgTemplate);
                templatesMappedByNamedId.put(fgDg.getFunctionGroupIdentifier().getNameIdentifier(), fgTemplate);
            }
        } else {
            if (templatesMappedById.containsKey(fgDg.getFunctionGroupIdentifier().getIdIdentifier())) {
                fgTemplate = templatesMappedById.get(fgDg.getFunctionGroupIdentifier().getIdIdentifier());
            } else {
                fgTemplate = functionGroupJpaRepository
                    .findById(fgDg.getFunctionGroupIdentifier().getIdIdentifier())
                    .orElseThrow(
                        () -> {
                            log.warn("Function group does not exist");
                            return getNotFoundException(ERR_ACQ_003.getErrorMessage(),
                                ERR_ACQ_003.getErrorCode());
                        });
                templatesMappedById.put(fgTemplate.getId(), fgTemplate);
            }
        }
        return fgTemplate;
    }

    private void checkIfExistsDataGroupInPendingDeleteState(AssignUserPermissionsData assignUserPermissionsData,
        Set<DataGroup> dataGroups) {

        Map<String, String> externalIdToDataGroupMap = dataGroups
            .stream()
            .collect(Collectors.toMap(DataGroup::getName, DataGroup::getId));

        if (applicationProperties.getApproval().getValidation().isEnabled() &&
            approvalDataGroupJpaRepository.findByDataGroupIdIn(
                assignUserPermissionsData.getAssignUserPermissions().getFunctionGroupDataGroups()
                    .stream()
                    .flatMap(fgdg -> fgdg.getDataGroupIdentifiers().stream())
                    .map(dg -> nonNull(dg.getIdIdentifier())
                        ? dg.getIdIdentifier() : externalIdToDataGroupMap.get(dg.getNameIdentifier().getName()))
                    .collect(Collectors.toSet()))
                .stream()
                .anyMatch(dg -> dg.getApprovalAction().equals(ApprovalAction.DELETE))) {

            log.warn("Trying to assign pending delete data group.");
            throw getBadRequestException(ERR_ACQ_061.getErrorMessage(), ERR_ACQ_061.getErrorCode());
        }
    }

    private ServiceAgreement getServiceAgreementByExternalId(String externalServiceAgreementId) {
        return serviceAgreementJpaRepository.findByExternalId(
            externalServiceAgreementId, SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS)
            .orElseThrow(() -> {
                log.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST_BY_EXTERNAL_ID,
                    externalServiceAgreementId);
                return getBadRequestException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });
    }

    private UserContext createUserContextIfMissing(String userId, ServiceAgreement serviceAgreement) {
        return userContextJpaRepository
            .findByUserIdAndServiceAgreementId(userId, serviceAgreement.getId())
            .orElseGet(
                () -> userContextJpaRepository.save(new UserContext(userId, serviceAgreement.getId()))
            );
    }

    private void checkIfUserBelongInServiceAgreement(String userId, String userLegalEntityId,
        ServiceAgreement serviceAgreement) {
        if (serviceAgreement.isMaster()) {
            log.info("check if user {} belong in same legal entity {} as MSA {}", userId, userLegalEntityId,
                serviceAgreement.getId());
            if (!serviceAgreement.getCreatorLegalEntity().getId().equals(userLegalEntityId)) {
                throwUserNotBelongInServiceAgreement(userId);
            }
        } else {
            log.info("check if user {} is exposed in SA {}", userId, serviceAgreement.getId());
            boolean userIsExposed = serviceAgreement
                .getParticipants()
                .entrySet()
                .stream()
                .filter(pair -> pair.getValue().isShareUsers())
                .flatMap(provider -> provider.getValue().getParticipantUsers().stream())
                .anyMatch(providerUser -> userId.equals(providerUser.getUserId()));
            if (!userIsExposed) {
                throwUserNotBelongInServiceAgreement(userId);
            }
        }
    }

    private void throwUserNotBelongInServiceAgreement(String userId) {
        log.warn("User {} not belong in Service Agreement", userId);
        throw getBadRequestException("User with id " + userId + " does not belong in service agreement",
            CommandErrorCodes.ERR_ACC_033.getErrorCode());
    }

    private void assignPermissionsToUser(Collection<UserAssignedFunctionGroup> currentAssignedFunctionGroups,
        Set<FunctionGroupDataGroups> newFunctionGroupDataGroups, UserContext userContext) {

        for (FunctionGroupDataGroups fgDg : newFunctionGroupDataGroups) {
            Optional<UserAssignedFunctionGroup> userAssignedFunctionGroup = currentAssignedFunctionGroups.stream()
                .filter(uaFg -> uaFg.getFunctionGroupId().equals(fgDg.getFunctionGroup().getId()))
                .findFirst();
            Set<String> dataGroupIds = fgDg.getDataGroups().stream().map(DataGroup::getId)
                .collect(Collectors.toSet());

            if (userAssignedFunctionGroup.isPresent()) {

                if (!combinationExists(dataGroupIds,
                    userAssignedFunctionGroup.get().getUserAssignedFunctionGroupCombinations())) {

                    UserAssignedFunctionGroup assignedFunctionGroup = userAssignedFunctionGroup.get();

                    addDataGroupsCombinationInUserAssignedFunctionGroup(dataGroupIds, assignedFunctionGroup);
                }
            } else {
                UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup(
                    fgDg.getFunctionGroup(), userContext);

                addDataGroupsCombinationInUserAssignedFunctionGroup(dataGroupIds, assignedFunctionGroup);

                currentAssignedFunctionGroups.add(assignedFunctionGroup);
            }
        }
    }

    private void addDataGroupsCombinationInUserAssignedFunctionGroup(Set<String> dataGroupIds,
        UserAssignedFunctionGroup assignedFunctionGroup) {
        if (CollectionUtils.isNotEmpty(dataGroupIds)) {
            UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination(
                dataGroupIds, assignedFunctionGroup);
            assignedFunctionGroup.getUserAssignedFunctionGroupCombinations().add(combination);
        }
    }

    private boolean combinationExists(Set<String> dgIds,
        Collection<UserAssignedFunctionGroupCombination> combinations) {

        return combinations.stream()
            .anyMatch(combination -> combination.getDataGroupIds().size() == dgIds.size()
                && combination.getDataGroupIds().containsAll(dgIds));
    }

    private void removeAssignedPermissions(List<UserAssignedFunctionGroup> currentUaFg,
        Set<FunctionGroupDataGroups> newState) {

        Map<String, Set<Set<String>>> combinations = newState.stream()
            .collect(Collectors.toMap(fg -> fg.getFunctionGroup().getId(),
                e -> {
                    Set<Set<String>> dgIds = new HashSet<>();
                    dgIds.add(e.getDataGroups().stream().map(DataGroup::getId).collect(Collectors.toSet()));
                    return dgIds;
                },
                (existing, replacement) -> {
                    existing.addAll(replacement);
                    return existing;
                }
            ));

        Set<UserAssignedFunctionGroup> toBeRemoved = currentUaFg.stream()
            .filter(uafg -> !combinations.containsKey(uafg.getFunctionGroupId()))
            .collect(Collectors.toSet());

        currentUaFg.removeAll(toBeRemoved);
        userAssignedFunctionGroupJpaRepository.deleteAll(toBeRemoved);

        for (UserAssignedFunctionGroup assignedFunctionGroup : currentUaFg) {
            if (combinations.containsKey(assignedFunctionGroup.getFunctionGroupId())) {
                Set<Set<String>> dgIdCombinations = combinations.get(assignedFunctionGroup.getFunctionGroupId());

                removeCombinations(assignedFunctionGroup, dgIdCombinations);

                removeSelfApprovalPolicies(assignedFunctionGroup, dgIdCombinations);
            }
        }
    }

    /**
     * @param existingAssignedFunctionGroup FunctionGroup assigned to user
     * @param newDgIdCombinations New combination of DataGroups
     * This method is used to remove selfApprovalPolicies from userAssignedFunctionGroupCombination
     * only when updating permissions for list of users via integration endpoint.
     * It will always remove selfApprovalPolicies if they exist in functionGroupCombination because current specification
     * for integration(and related service) endpoint does not provide possibility to update permissions and assign selfApprovalPolicies.
     */
    private void removeSelfApprovalPolicies(UserAssignedFunctionGroup existingAssignedFunctionGroup,
        Set<Set<String>> newDgIdCombinations) {
        Set<UserAssignedFunctionGroupCombination> existingCombinations = existingAssignedFunctionGroup
            .getUserAssignedFunctionGroupCombinations();
        for (UserAssignedFunctionGroupCombination existingCombination : existingCombinations) {

            Set<com.backbase.accesscontrol.domain.SelfApprovalPolicy> existingPolicies = existingCombination
                .getSelfApprovalPolicies();

            if (newDgIdCombinations.contains(existingCombination.getDataGroupIds()) && !existingPolicies.isEmpty()) {
                existingCombination.getSelfApprovalPolicies().clear();
            }
        }
    }

    /**
     * @param existingAssignedFunctionGroup FunctionGroup assigned to user
     * @param newDgIdCombinations New combination of DataGroups
     * This method is used to remove combinations for UserAssignedFunctionGroup only when updating permissions for list of users
     * via integration endpoint.
     * It will remove combinations in 2 cases:
     *      a) when combination does not contain any dataGroups (means only selfApprovalPolicies assigned to currentFGCombination)
     *      b) when dataGroupIds for currentFGCombination differs from newDGrIdCombinations
     */
    private void removeCombinations(UserAssignedFunctionGroup existingAssignedFunctionGroup,
        Set<Set<String>> newDgIdCombinations) {
        Predicate<UserAssignedFunctionGroupCombination> existingDataGroupsEmptyOrDiffersFromNew =
            uafgc -> uafgc.getDataGroupIds().isEmpty() || !newDgIdCombinations.contains(uafgc.getDataGroupIds());

        existingAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations()
            .removeIf(existingDataGroupsEmptyOrDiffersFromNew);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void assignUserContextPermissions(String serviceAgreementId, String userId,
        String userLegalEntityId, Set<UserContextPermissions> newPermissionsState) {
        log.info("Trying to assign permissions for user {} under legal entity {} and service agreement {}",
            userId, userLegalEntityId, serviceAgreementId);

        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId,
                SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS)
            .orElseThrow(() -> {
                log.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreementId);
                return getBadRequestException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });

        PermissionIndexes permissionIndexes = new PermissionIndexes(serviceAgreement);
        checkIfServiceAgreementsIsInPendingState(serviceAgreementId);

        validateIsUserInMasterServiceAgreement(userLegalEntityId, serviceAgreement);

        List<FunctionGroup> functionGroupTemplates = getFunctionGroupTemplate(newPermissionsState, permissionIndexes);
        validationsJobRoleTemplate(functionGroupTemplates, serviceAgreement);

        if (newPermissionsState.isEmpty()) {
            userContextJpaRepository
                .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(userId, serviceAgreementId)
                .ifPresent(userContext -> {
                    if (userContext.getUserAssignedFunctionGroups()
                        .stream()
                        .noneMatch(userAssignedFunctionGroup -> userAssignedFunctionGroup
                            .getFunctionGroup().getType() == FunctionGroupType.SYSTEM)) {
                        entityManager.flush();

                        userAssignedFunctionGroupJpaRepository
                            .deleteAll(userContext.getUserAssignedFunctionGroups());
                        userContextJpaRepository.delete(userContext);
                    } else {
                        saveUserContextPermissions(userContext, serviceAgreement, newPermissionsState,
                            permissionIndexes, functionGroupTemplates);
                    }
                });
        } else {
            Set<String> functionGroupDataGroupsIds = newPermissionsState.stream()
                .map(UserContextPermissions::getFunctionGroupId).collect(
                    Collectors.toSet());
            validateFunctionGroupsIsInPendingDelete(functionGroupDataGroupsIds);

            selfApprovalPolicyValidator.validateSelfApprovalPolicies(newPermissionsState);

            UserContext userContext = getOrCreateUserAccess(serviceAgreementId, userId);
            saveUserContextPermissions(userContext, serviceAgreement, newPermissionsState, permissionIndexes,
                functionGroupTemplates);
        }
    }

    private void checkIfServiceAgreementsIsInPendingState(String serviceAgreementId) {
        if (applicationProperties.getApproval().getValidation().isEnabled() &&
            approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)) {

            log.warn(
                "Assign permission operation is not allowed, there is pending operation on service agreement with id {}",
                serviceAgreementId);
            throw getBadRequestException(ERR_ACC_107.getErrorMessage(), ERR_ACC_107.getErrorCode());
        }
    }

    private void validateFunctionGroupsIsInPendingDelete(Set<String> functionGroupDataGroupsIds) {

        if (applicationProperties.getApproval().getValidation().isEnabled() && !functionGroupDataGroupsIds.isEmpty()) {
            Optional<List<ApprovalFunctionGroupRef>> approvalFunctionGroupList = approvalFunctionGroupRefJpaRepository
                .findByFunctionGroupIdIn(functionGroupDataGroupsIds);

            if (approvalFunctionGroupList.isPresent() &&
                approvalFunctionGroupList.get()
                    .stream()
                    .anyMatch(fg -> fg.getApprovalAction().equals(ApprovalAction.DELETE))) {
                log.warn("There is a pending delete function group.");
                throw getBadRequestException(ERR_ACQ_066.getErrorMessage(), ERR_ACQ_066.getErrorCode());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void assignUserContextPermissionsApproval(String serviceAgreementId, String userId, String userLegalEntityId,
        String approvalId, Set<UserContextPermissions> requestData) {
        log.info("Trying to assign permissions for user {} under legal entity {} and service agreement {} "
                + "for approval with {}",
            userId, userLegalEntityId, serviceAgreementId, approvalId);

        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS)
            .orElseThrow(() -> {
                log.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST, serviceAgreementId);
                return getBadRequestException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });

        checkIfServiceAgreementsIsInPendingState(serviceAgreementId);

        validateIsUserInMasterServiceAgreement(userLegalEntityId, serviceAgreement);

        validateUserIsInExposedUsers(userId, serviceAgreement);

        PermissionIndexes permissionIndexes = new PermissionIndexes(serviceAgreement);

        List<FunctionGroup> functionGroupTemplates = getFunctionGroupTemplate(requestData, permissionIndexes);

        validationsJobRoleTemplate(functionGroupTemplates, serviceAgreement);

        validateFunctionGroupsBelongToServiceAgreement(requestData,
            permissionIndexes.getMapByFunctionGroupId().keySet(), functionGroupTemplates);

        validateDataGroupsBelongToServiceAgreement(getDataGroupsFromRequestData(requestData),
            permissionIndexes.getMapByDataGroupId().keySet());

        checkIfAssignmentForUserAndServiceAgreementAlreadyPending(serviceAgreementId, userId);

        checkPendingRequestsForDataGroups(requestData);
        Set<String> functionGroupDataGroupsIds = requestData.stream()
            .map(UserContextPermissions::getFunctionGroupId).collect(
                Collectors.toSet());
        validateFunctionGroupsIsInPendingDelete(functionGroupDataGroupsIds);

        selfApprovalPolicyValidator.validateSelfApprovalPolicies(requestData);

        ApprovalUserContext approvalUserContext = createApprovalUserContextFromRequestData(
            serviceAgreementId, userId,
            userLegalEntityId, approvalId, requestData);
        approvalUserContextJpaRepository.save(approvalUserContext);
    }

    private List<FunctionGroup> getFunctionGroupTemplate(Set<UserContextPermissions> requestData,
        PermissionIndexes permissionIndexes) {
        Set<String> templateFunctionGroupIds = requestData.stream()
            .map(UserContextPermissions::getFunctionGroupId)
            .filter(id -> !permissionIndexes.getMapByFunctionGroupId().containsKey(id))
            .collect(Collectors.toSet());

        List<FunctionGroup> templateFunctionGroups = functionGroupJpaRepository
            .readByIdIn(templateFunctionGroupIds, FUNCTION_GROUP_WITH_SA);

        if (templateFunctionGroupIds.size() != templateFunctionGroups.size()) {
            log.info("Invalid function group id in the function group list");
            throw getBadRequestException(ERR_ACQ_076.getErrorMessage(), ERR_ACQ_076.getErrorCode());
        }

        Set<FunctionGroup> functionGroupTemplatesIndexed = requestData.stream()
            .map(UserContextPermissions::getFunctionGroupId)
            .filter(id -> permissionIndexes.getMapByFunctionGroupId().containsKey(id)
                && permissionIndexes.getMapByFunctionGroupId().get(id).getType().equals(FunctionGroupType.TEMPLATE))
            .map(id -> permissionIndexes.getMapByFunctionGroupId().get(id))
            .collect(Collectors.toSet());

        templateFunctionGroups.addAll(functionGroupTemplatesIndexed);

        templateFunctionGroups.forEach(item ->
        {
            if (!item.getType().equals(FunctionGroupType.TEMPLATE)) {
                log.info("Function Group must be of type Template");
                throw getBadRequestException(ERR_ACC_001.getErrorMessage(), ERR_ACC_001.getErrorCode());
            } else {
                permissionIndexes.getMapByFunctionGroupId().put(item.getId(), item);
            }
        });

        return templateFunctionGroups;
    }

    private void checkPendingRequestsForDataGroups(Set<UserContextPermissions> requestData) {
        Set<String> dataGroupIds = requestData.stream()
            .flatMap(e -> e.getDataGroupIds().stream())
            .collect(Collectors.toSet());

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            List<ApprovalDataGroup> approvalsForDataGroups = approvalDataGroupJpaRepository
                .findByDataGroupIdIn(dataGroupIds);
            boolean hasPendingDeleteForDataGroup = approvalsForDataGroups.stream()
                .anyMatch(e -> e.getApprovalAction() == ApprovalAction.DELETE);

            if (hasPendingDeleteForDataGroup) {
                throw getBadRequestException(ERR_ACC_084.getErrorMessage(), ERR_ACC_084.getErrorCode());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PresentationPermissionsApprovalDetailsItem getUserPermissionApprovalDetails(String approvalId) {

        ApprovalUserContext approvalUserContext = approvalUserContextJpaRepository
            .findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(approvalId)
            .orElseThrow(() -> {
                log.error("Approval user context for approval with id {} does not exist", approvalId);
                return getNotFoundException(ERR_ACQ_049.getErrorMessage(), ERR_ACQ_049.getErrorCode());
            });

        String serviceAgreementId = approvalUserContext.getServiceAgreementId();
        String userId = approvalUserContext.getUserId();

        UserContext userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(userId,
                serviceAgreementId)
            .orElseGet(() -> new UserContext()
                .withServiceAgreementId(serviceAgreementId)
                .withUserId(userId));

        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS)
            .orElseThrow(() -> getBadRequestException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));

        Set<UserAssignedFunctionGroup> oldState = userContext.getUserAssignedFunctionGroups().stream()
            .filter(uaFg -> uaFg.getFunctionGroup().getType().equals(FunctionGroupType.TEMPLATE))
            .collect(Collectors.toSet());

        Set<UserContextPermissions> newState = approvalUserContext
                .getApprovalUserContextAssignFunctionGroups()
                .stream()
                .map(item -> new UserContextPermissions(item.getFunctionGroupId(),
                    item.getDataGroups(), userContextPermissionsMapper.map(item.getApprovalSelfApprovalPolicies())))
                .collect(Collectors.toSet());

        PermissionIndexes permissionIndexes = new PermissionIndexes(serviceAgreement);
        getFunctionGroupTemplate(newState, permissionIndexes);
        getFunctionGroupTemplateOldState(oldState, permissionIndexes);
        PermissionDifferences differences = new PermissionDifferences(userContext, serviceAgreement, newState);

        return
            new PresentationPermissionsApprovalDetailsItem()
                .withAction(PresentationApprovalAction.EDIT)
                .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
                .withServiceAgreementId(serviceAgreementId)
                .withUserId(userContext.getUserId())
                .withServiceAgreementName(serviceAgreement.getName())
                .withServiceAgreementDescription(serviceAgreement.getDescription())
                .withNewFunctionGroups(
                    getFunctionGroupInfo(differences.getAddNewFunctionGroups(),
                        permissionIndexes))
                .withRemovedFunctionGroups(
                    getFunctionGroupInfo(differences.getRemoveIds(),
                        permissionIndexes))
                .withUnmodifiedFunctionGroups(
                    getFunctionGroupInfo(differences.getUnmodifiedIds(),
                        permissionIndexes));
    }

    private void getFunctionGroupTemplateOldState(Set<UserAssignedFunctionGroup> oldState,
        PermissionIndexes permissionIndexes) {
        Set<UserContextPermissions> oldStateConverted = oldState.stream()
            .map(userContextPermissionsFactory::createUserContextPermissions)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
        this.getFunctionGroupTemplate(oldStateConverted, permissionIndexes);
    }

    private String toType(FunctionGroupType type) {
        if (type.equals(FunctionGroupType.DEFAULT)) {
            return Type.REGULAR.toString();
        }
        return type.toString();
    }

    private List<PresentationDataGroupApprovalItem> getDataGroups(UserContextPermissions userContextPermissions,
        PermissionIndexes permissionIndexes) {

        return userContextPermissions.getDataGroupIds().stream()
            .map(dg ->
                new PresentationDataGroupApprovalItem().withId(dg)
                    .withDescription(permissionIndexes.getMapByDataGroupId().get(dg).getDescription())
                    .withName(permissionIndexes.getMapByDataGroupId().get(dg).getName())
                    .withType(permissionIndexes.getMapByDataGroupId().get(dg).getDataItemType())
            ).collect(Collectors.toList());
    }

    private List<PresentationFunctionGroupsDataGroupsPair> getFunctionGroupInfo(
        Set<UserContextPermissions> userContextPermissions,
        PermissionIndexes permissionIndexes) {

        return userContextPermissions.stream()
            .map(item -> new PresentationFunctionGroupsDataGroupsPair()
                .withId(item.getFunctionGroupId())
                .withName(permissionIndexes.getMapByFunctionGroupId()
                    .get(item.getFunctionGroupId()).getName())
                .withDescription(permissionIndexes.getMapByFunctionGroupId()
                    .get(item.getFunctionGroupId()).getDescription())
                .withType(PresentationFunctionGroupsDataGroupsPair.Type
                    .valueOf(
                        toType(permissionIndexes.getMapByFunctionGroupId().get(item.getFunctionGroupId()).getType())))
                .withDataGroups(getDataGroups(item, permissionIndexes))
                .withSelfApprovalPolicies(mapPoliciesToPresentation(item.getSelfApprovalPolicies()))
            )
            .collect(Collectors.toList());
    }

    private List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationSelfApprovalPolicy> mapPoliciesToPresentation(Set<SelfApprovalPolicy> selfApprovalPolicies) {
        return selfApprovalPolicies.stream()
                .map(policy -> selfApprovalPolicyMapper.map(policy, getBusinessFunctionCode(policy)))
                .collect(Collectors.toList());
    }

    private String getBusinessFunctionCode(SelfApprovalPolicy selfApprovalPolicy) {
        String businessFunctionName = selfApprovalPolicy.getBusinessFunctionName();

        return businessFunctionCache.getBusinessFunctionByFunctionName(businessFunctionName)
                .map(BusinessFunction::getFunctionCode)
                .orElseThrow(() -> {
                    log.warn("Business Function " + businessFunctionName + " is not supported");
                    String errorMessage = String.format(ERR_ACC_109.getErrorMessage(), businessFunctionName);
                    return getBadRequestException(errorMessage, ERR_ACC_109.getErrorCode());
                });
    }

    private ApprovalUserContext createApprovalUserContextFromRequestData(String serviceAgreementId, String userId,
        String userLegalEntityId, String approvalId, Set<UserContextPermissions> requestData) {
        Set<ApprovalUserContextAssignFunctionGroup> approvalUserContextAssignFunctionGroups = requestData.stream()
            .map(this::createApprovalUserContextAssignFunctionGroup)
            .collect(Collectors.toSet());

        ApprovalUserContext approvalUserContext = new ApprovalUserContext()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withLegalEntityId(userLegalEntityId)
            .withApprovalId(approvalId);
        approvalUserContext.addApprovalUserContextAssignFunctionGroups(approvalUserContextAssignFunctionGroups);
        return approvalUserContext;
    }

    private ApprovalUserContextAssignFunctionGroup createApprovalUserContextAssignFunctionGroup(
        UserContextPermissions permission) {
        ApprovalUserContextAssignFunctionGroup assignFunctionGroup = new ApprovalUserContextAssignFunctionGroup();
        assignFunctionGroup.setFunctionGroupId(permission.getFunctionGroupId());
        assignFunctionGroup.setDataGroups(Sets.newHashSet(permission.getDataGroupIds()));
        Set<ApprovalSelfApprovalPolicy> policies = permission.getSelfApprovalPolicies().stream()
            .map(policy -> approvalSelfApprovalPolicyFactory.createPolicy(permission.getFunctionGroupId(), policy))
            .collect(Collectors.toSet());
        assignFunctionGroup.addPolicies(policies);
        return assignFunctionGroup;
    }

    private void validationsJobRoleTemplate(List<FunctionGroup> functionGroups, ServiceAgreement sa) {

        LegalEntity creatorLegalEntity = sa.getCreatorLegalEntity();
        validateAllApsAreSetAndRegularUserDefault(functionGroups, sa);

        Set<String> legalEntityIds = functionGroups.stream()
            .map(e -> e.getServiceAgreement().getCreatorLegalEntity().getId())
            .collect(Collectors.toSet());
        if (!isNull(creatorLegalEntity)) {
            legalEntityIds.remove(creatorLegalEntity.getId());
            try {
                validateLegalEntityHierarchyService
                    .validateLegalEntityAncestorHierarchy(creatorLegalEntity.getId(), legalEntityIds);
            } catch (ValidationException e) {
                throw getBadRequestException(QueryErrorCodes.ERR_ACQ_070.getErrorMessage(),
                    QueryErrorCodes.ERR_ACQ_070.getErrorCode());
            }
        }

    }

    private void validateAllApsAreSetAndRegularUserDefault(List<FunctionGroup> functionGroups, ServiceAgreement sa) {

        functionGroups.forEach(functionGroup -> {
            if (!sa.getPermissionSetsRegular().contains(functionGroup.getAssignablePermissionSet())) {
                log.warn("Assigning aps has failed due to function group {}", functionGroup.getName());
                throw getBadRequestException(ERR_ACQ_068.getErrorMessage(), ERR_ACQ_068.getErrorCode());
            }
        });
    }

    private Set<String> getDataGroupsFromRequestData(Set<UserContextPermissions> requestData) {
        return requestData.stream()
            .flatMap(functionGroupWithDataGroups -> functionGroupWithDataGroups.getDataGroupIds().stream())
            .collect(Collectors.toSet());
    }

    private void validateIsUserInMasterServiceAgreement(String userLegalEntityId, ServiceAgreement serviceAgreement) {
        if (serviceAgreement.isMaster()
            && !serviceAgreement.getCreatorLegalEntity().getId().equals(userLegalEntityId)) {

            log.warn("Legal entity with id {} of the user do not belong to the service agreement",
                userLegalEntityId);
            throw getBadRequestException(ERR_ACC_071.getErrorMessage(), ERR_ACC_071.getErrorCode());
        }
    }

    private void saveUserContextPermissions(UserContext userContext, ServiceAgreement serviceAgreement,
        Set<UserContextPermissions> newPermissionsState, PermissionIndexes permissionIndexes,
        List<FunctionGroup> functionGroupTemplates) {

        validateUserIsInExposedUsers(userContext.getUserId(), serviceAgreement);

        PermissionDifferences permissionDifferences = new PermissionDifferences(userContext, serviceAgreement,
            newPermissionsState);

        removePermissions(userContext, permissionDifferences);

        createPermissions(userContext, permissionDifferences, permissionIndexes, functionGroupTemplates);

        userContextJpaRepository.save(userContext);
    }

    private void validateUserIsInExposedUsers(String userId, ServiceAgreement serviceAgreement) {
        if (!serviceAgreement.isMaster() && !participantUserJpaRepository
            .existsByUserIdAndParticipantServiceAgreement(userId, serviceAgreement)) {

            log.warn("User with id {} does not belong to service agreement with id {}", userId,
                serviceAgreement.getId());
            throw getBadRequestException(ERR_ACC_071.getErrorMessage(), ERR_ACC_071.getErrorCode());
        }
    }

    private void removePermissions(UserContext userContext, PermissionDifferences permissionDifferences) {
        Set<String> uaFgToAdd = permissionDifferences.getAddNewFunctionGroups().stream()
            .map(UserContextPermissions::getFunctionGroupId)
            .collect(Collectors.toSet());

        Set<String> uaFgUnmodified = permissionDifferences.getUnmodifiedIds().stream()
            .map(UserContextPermissions::getFunctionGroupId)
            .collect(Collectors.toSet());

        Set<String> uaFgToRemove = permissionDifferences.getRemoveIds().stream()
            .map(UserContextPermissions::getFunctionGroupId)
            .collect(Collectors.toSet());

        removeUserAssignedFunctionGroups(userContext,
            Sets.difference(Sets.difference(uaFgToRemove, uaFgToAdd), uaFgUnmodified));

        removeUserAssignedFunctionGroupCombinations(userContext, permissionDifferences.getRemoveIds());
    }

    private void createPermissions(UserContext userContext,
        PermissionDifferences permissionDifferences, PermissionIndexes permissionIndexes,
        List<FunctionGroup> functionGroupTemplates) {

        if (!permissionDifferences.getAddNewFunctionGroups().isEmpty()) {
            validateFunctionGroupsBelongToServiceAgreement(permissionDifferences.getAddNewFunctionGroups(),
                permissionIndexes.getMapByFunctionGroupId().keySet(), functionGroupTemplates);

            addNewUserAssignedFunctionGroups(userContext,
                permissionDifferences.getAddNewFunctionGroups(), permissionIndexes.getMapByFunctionGroupId(),
                permissionIndexes.getMapByDataGroupId(), functionGroupTemplates);
        }
    }

    private void removeUserAssignedFunctionGroupCombinations(UserContext userContext,
        Set<UserContextPermissions> removeCombinations) {
        Map<String, Set<Set<String>>> combinationsToRemove = removeCombinations.stream()
            .collect(Collectors.toMap(
                UserContextPermissions::getFunctionGroupId,
                e -> {
                    Set<Set<String>> dgIds = new HashSet<>();
                    dgIds.add(new HashSet<>(e.getDataGroupIds()));
                    return dgIds;
                },
                (existing, replacement) -> {
                    existing.addAll(replacement);
                    return existing;
                }
            ));

        for (UserAssignedFunctionGroup uaFg : userContext.getUserAssignedFunctionGroups()) {
            if (combinationsToRemove.containsKey(uaFg.getFunctionGroupId())) {
                Set<Set<String>> dgIdCombinations = combinationsToRemove.get(uaFg.getFunctionGroupId());

                uaFg.getUserAssignedFunctionGroupCombinations()
                    .removeIf(uaFgC -> dgIdCombinations.contains(uaFgC.getDataGroupIds()));
            }
        }
    }

    private void removeUserAssignedFunctionGroups(UserContext userContext, Set<String> removeIds) {
        Set<UserAssignedFunctionGroup> uafgToRemove = userContext.getUserAssignedFunctionGroups()
            .stream()
            .filter(
                userAssignedFunctionGroup -> removeIds
                    .contains(userAssignedFunctionGroup.getFunctionGroupId()))
            .collect(Collectors.toSet());

        userContext.getUserAssignedFunctionGroups().removeAll(uafgToRemove);
    }

    private UserContext getOrCreateUserAccess(String serviceAgreementId, String userId) {
        return userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(userId, serviceAgreementId)
            .orElseGet(() -> new UserContext(userId, serviceAgreementId));
    }

    private void addNewUserAssignedFunctionGroups(UserContext userContext,
        Set<UserContextPermissions> addNewFunctionGroups,
        Map<String, FunctionGroup> mapByFunctionGroupId, Map<String, DataGroup> mapByDataGroupId,
        List<FunctionGroup> functionGroupTemplates) {
        Map<String, FunctionGroup> functionGroupTemplatesMap = functionGroupTemplates.stream()
            .collect(Collectors.toMap(FunctionGroup::getId, Function.identity()));

        addNewFunctionGroups
            .forEach(functionGroupData -> createNewAssignedFunctionGroup(functionGroupData, userContext,
                mapByFunctionGroupId, mapByDataGroupId, functionGroupTemplatesMap));
    }

    private void validateFunctionGroupsBelongToServiceAgreement(
        Set<UserContextPermissions> addNewFunctionGroups,
        Set<String> serviceAgreementFunctionGroupsIds, List<FunctionGroup> functionGroupTemplates) {
        Set<String> newFunctionGroups = addNewFunctionGroups.stream()
            .map(UserContextPermissions::getFunctionGroupId)
            .collect(Collectors.toSet());
        newFunctionGroups.removeAll(functionGroupTemplates.stream().map(FunctionGroup::getId)
            .collect(Collectors.toSet()));
        if (!newFunctionGroups.isEmpty()
            && !serviceAgreementFunctionGroupsIds.containsAll(newFunctionGroups)) {
            log.warn("Some of the function groups does not belong to the service agreement.");
            throw getBadRequestException(ERR_ACC_001.getErrorMessage(), ERR_ACC_001.getErrorCode());
        }
    }

    private void validateDataGroupsBelongToServiceAgreement(Set<String> addNewDataGroups,
        Set<String> serviceAgreementDataGroupsIds) {
        if (!serviceAgreementDataGroupsIds.containsAll(addNewDataGroups)) {

            log.warn("Some of the data groups does not belong to the service agreement.");
            throw getBadRequestException(ERR_ACC_002.getErrorMessage(), ERR_ACC_002.getErrorCode());
        }
    }

    private void createNewAssignedFunctionGroup(
        UserContextPermissions functionGroupData, UserContext userContext,
        Map<String, FunctionGroup> mapByFunctionGroupId, Map<String, DataGroup> mapByDataGroupId,
        Map<String, FunctionGroup> mapByFunctionGroupTemplateId) {

        FunctionGroup functionGroup = mapByFunctionGroupId.getOrDefault(functionGroupData.getFunctionGroupId(),
            mapByFunctionGroupTemplateId.get(functionGroupData.getFunctionGroupId()));

        UserAssignedFunctionGroup userAssignedFunctionGroup = userContext.getUserAssignedFunctionGroups().stream()
            .filter(item -> item.getFunctionGroupId().equals(functionGroup.getId()))
            .findFirst()
            .orElseGet(() -> new UserAssignedFunctionGroup(functionGroup, userContext));

        Set<String> dataGroupIds = new HashSet<>(functionGroupData.getDataGroupIds());

        validateDataGroupsBelongToServiceAgreement(dataGroupIds, mapByDataGroupId.keySet());

        addDataGroupsAndSelfApprovalPoliciesInCombination(dataGroupIds, functionGroupData.getSelfApprovalPolicies(),
            userAssignedFunctionGroup);

        userContext.getUserAssignedFunctionGroups().add(userAssignedFunctionGroup);
    }

    private void addDataGroupsAndSelfApprovalPoliciesInCombination(Set<String> dataGroupIds,
        Set<SelfApprovalPolicy> selfApprovalPolicies, UserAssignedFunctionGroup assignedFunctionGroup) {
        if (!dataGroupIds.isEmpty() || !selfApprovalPolicies.isEmpty()) {
            UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();

            if (!dataGroupIds.isEmpty()) {
                combination.getDataGroupIds().addAll(dataGroupIds);
            }

            if (!selfApprovalPolicies.isEmpty()) {
                String functionGroupId = assignedFunctionGroup.getFunctionGroupId();
                Set<com.backbase.accesscontrol.domain.SelfApprovalPolicy> policies = selfApprovalPolicies.stream()
                    .map(policy -> selfApprovalPolicyFactory.createPolicy(functionGroupId, policy))
                    .collect(Collectors.toSet());

                combination.addPolicies(policies);
            }

            assignedFunctionGroup.addCombination(combination);
        }
    }

    private void checkIfAssignmentForUserAndServiceAgreementAlreadyPending(String serviceAgreementId, String userId) {
        if (applicationProperties.getApproval().getValidation().isEnabled() &&
            approvalUserContextJpaRepository.findByUserIdAndServiceAgreementId(userId, serviceAgreementId)
                .isPresent()) {
            log.warn("There is pending permission for service agreement with id {} for user with id {}",
                serviceAgreementId, userId);
            throw getBadRequestException(ERR_ACC_072.getErrorMessage(), ERR_ACC_072.getErrorCode());
        }
    }

    private class PermissionDifferences {

        private UserContext userContext;

        private Set<UserContextPermissions> newPermissionsState;
        private Set<UserContextPermissions> removeIds;
        private Set<UserContextPermissions> unmodifiedIds;
        private Set<UserContextPermissions> addNewFunctionGroups;
        private ServiceAgreement serviceAgreement;

        public PermissionDifferences(UserContext userContext,
            ServiceAgreement serviceAgreement,
            Set<UserContextPermissions> newPermissionsState) {
            this.userContext = userContext;
            this.newPermissionsState = newPermissionsState;
            this.serviceAgreement = serviceAgreement;
            this.invoke();
        }

        public Set<UserContextPermissions> getRemoveIds() {
            return removeIds;
        }

        public Set<UserContextPermissions> getUnmodifiedIds() {
            return unmodifiedIds;
        }

        public Set<UserContextPermissions> getAddNewFunctionGroups() {
            return addNewFunctionGroups;
        }

        private PermissionDifferences invoke() {
            Set<UserContextPermissions> oldPermissionsState = getPreviousUserContextState(
                userContext);

            Set<UserContextPermissions> updateOrRemove =
                Sets.difference(oldPermissionsState, newPermissionsState);

            Set<UserContextPermissions> addOrUpdate =
                Sets.difference(newPermissionsState, oldPermissionsState);

            Set<String> systemFunctionGroupsIds = serviceAgreement.getFunctionGroups()
                .stream()
                .filter(functionGroup -> functionGroup.getType() == FunctionGroupType.SYSTEM)
                .map(FunctionGroup::getId)
                .collect(Collectors.toSet());

            unmodifiedIds = new HashSet<>(oldPermissionsState);

            Set<UserContextPermissions> intersectionChangeIds =
                Sets.intersection(addOrUpdate, updateOrRemove);
            
            Set<UserContextPermissions> changeIds = intersectionChangeIds.stream()
                .filter(item -> !systemFunctionGroupsIds.contains(item.getFunctionGroupId()))
                .collect(Collectors.toSet());

            Set<UserContextPermissions> differenceChangeIds =
                Sets.difference(updateOrRemove, changeIds);

            removeIds = differenceChangeIds.stream()
                .filter(item -> !systemFunctionGroupsIds.contains(item.getFunctionGroupId()))
                .collect(Collectors.toSet());

            log.info("Changed FG ids: {}, removed; {}, unmodified: {}", changeIds, removeIds, unmodifiedIds);
            unmodifiedIds.removeAll(removeIds);
            unmodifiedIds.removeAll(changeIds);
            addNewFunctionGroups = Sets.difference(newPermissionsState, oldPermissionsState)
                .stream()
                .filter(assignedFunctionData -> !changeIds.stream().map(UserContextPermissions::getFunctionGroupId)
                    .collect(Collectors.toSet())
                    .contains(assignedFunctionData.getFunctionGroupId())).collect(Collectors.toSet());
            return this;
        }

        private Set<UserContextPermissions> getPreviousUserContextState(UserContext userContext) {
            return userContext.getUserAssignedFunctionGroups()
                .stream()
                .map(userContextPermissionsFactory::createUserContextPermissions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        }
    }

    private class PermissionIndexes {

        private Map<String, FunctionGroup> mapByFunctionGroupId;
        private Map<String, DataGroup> mapByDataGroupId;
        private Map<String, FunctionGroup> mapByFunctionGroupName;
        private Map<String, DataGroup> mapByDataGroupName;

        public PermissionIndexes(ServiceAgreement serviceAgreement) {
            this.mapByFunctionGroupId = serviceAgreement.getFunctionGroups()
                .stream()
                .collect(Collectors.toMap(FunctionGroup::getId, functionGroup -> functionGroup));

            this.mapByDataGroupId = serviceAgreement.getDataGroups()
                .stream()
                .collect(Collectors.toMap(DataGroup::getId, dataGroup -> dataGroup));

            this.mapByFunctionGroupName = serviceAgreement.getFunctionGroups()
                .stream()
                .collect(Collectors.toMap(FunctionGroup::getName, functionGroup -> functionGroup));

            this.mapByDataGroupName = serviceAgreement.getDataGroups()
                .stream()
                .collect(Collectors.toMap(DataGroup::getName, dataGroup -> dataGroup));
        }

        public Map<String, FunctionGroup> getMapByFunctionGroupId() {
            return mapByFunctionGroupId;
        }

        public Map<String, DataGroup> getMapByDataGroupId() {
            return mapByDataGroupId;
        }

        public Map<String, FunctionGroup> getMapByFunctionGroupName() {
            return mapByFunctionGroupName;
        }

        public Map<String, DataGroup> getMapByDataGroupName() {
            return mapByDataGroupName;
        }
    }
}
