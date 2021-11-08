package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.APS_PERMISSIONS_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_FGS;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR;
import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR_AND_CREATOR_LE_AND_FGS;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_027;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_073;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_100;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_101;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_103;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_104;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_107;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_011;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_012;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_023;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_024;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_025;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_062;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_067;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;

import com.backbase.accesscontrol.business.persistence.transformer.FunctionGroupTransformer;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.mappers.PersistenceFunctionGroupApprovalDetailsItemMapper;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextAssignFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.TimeBoundValidatorService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.PersistencePrivilege;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.BulkFunctionGroupsPostResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class FunctionGroupServiceImpl implements FunctionGroupService {

    private FunctionGroupJpaRepository functionGroupJpaRepository;
    private FunctionGroupTransformer functionGroupTransformer;
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    private ApprovalUserContextAssignFunctionGroupJpaRepository approvalUserContextAssignFunctionGroupJpaRepository;
    private TimeBoundValidatorService timeBoundValidatorService;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private BusinessFunctionCache businessFunctionCache;
    private ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;
    private ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;
    private PersistenceFunctionGroupApprovalDetailsItemMapper mapper;
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    private DateTimeService dateTimeService;
    private ApplicationProperties applicationProperties;
    private UserAssignedCombinationRepository userAssignedCombinationRepository;
    private static final String UNABLE_TO_LOAD_FUNCTION_GROUP = "Unable to load function group {}";

    /**
     * Add Function Group.
     *
     * @param functionGroupBase - post request body for add function group {@link FunctionGroupBase}
     * @return Saved Function Group ID {@link String}
     */
    @Transactional
    @Override
    public String addFunctionGroup(FunctionGroupBase functionGroupBase) {
        log.debug("Creating Function Group: {} ", functionGroupBase);
        AssignablePermissionSet assignablePermissionSet = null;
        checkIfFunctionGroupWithGivenNameAlreadyExists(functionGroupBase.getName(),
            functionGroupBase.getServiceAgreementId());

        List<String> permissions = Optional.ofNullable(functionGroupBase.getPermissions())
            .orElseGet(ArrayList::new)
            .stream().map(Permission::getFunctionId)
            .collect(Collectors.toList());

        checkIfAllSpecifiedFunctionIdsExist(permissions);

        if (functionGroupBase.getType().toString().equals(FunctionGroupType.DEFAULT.toString())) {
            checkIfFunctionGroupWithGivenNameAlreadyPending(functionGroupBase.getServiceAgreementId(),
                functionGroupBase.getName());
        }

        ServiceAgreement serviceAgreement = getServiceAgreement(functionGroupBase.getServiceAgreementId());

        checkIfServiceAgreementIsInPendingState(
            functionGroupBase.getServiceAgreementId());

        if (functionGroupBase.getType().toString().equals(FunctionGroupType.TEMPLATE.toString())) {
            checkIfFunctionGroupTimeSpanIsValid(functionGroupBase.getValidFrom(), functionGroupBase.getValidUntil());
            checkIfJobRoleTemplateCanBeCreatedInServiceAgreement(serviceAgreement);
            assignablePermissionSet = getAssignablePermissionSetByIdOrName(functionGroupBase.getApsId(),
                functionGroupBase.getApsName());

            List<String> afpIds = functionGroupBase.getPermissions().stream()
                .flatMap(permission ->
                    businessFunctionCache.getByFunctionAndPrivilege(permission.getFunctionId(),
                        permission.getAssignedPrivileges().stream()
                            .map(PrivilegeDto::getPrivilege)
                            .collect(Collectors.toList())).stream())
                .collect(Collectors.toList());

            checkIfAllSpecifiedPrivilegesExistInAssociatedAps(afpIds, assignablePermissionSet.getPermissions());
        } else {
            checkIfAllSpecifiedPrivilegesExistInAssociatedAps(functionGroupBase.getPermissions(), serviceAgreement);
            checkIfFunctionGroupTimeSpanIsWithinServiceAgreementTimeSpan(functionGroupBase.getValidFrom(),
                functionGroupBase.getValidUntil(), serviceAgreement);
        }

        FunctionGroup functionGroup = createFunctionGroupForSave(functionGroupBase, serviceAgreement,
            assignablePermissionSet);
        functionGroupJpaRepository.save(functionGroup);

        return functionGroup.getId();
    }

    private AssignablePermissionSet getAssignablePermissionSetByIdOrName(BigDecimal apsId, String apsName) {
        if (nonNull(apsId)) {
            return getAssignablePermissionSetById(apsId.longValue());
        }

        if (nonNull(apsName)) {
            return assignablePermissionSetJpaRepository
                .findByName(apsName, APS_PERMISSIONS_EXTENDED)
                .orElseThrow(() -> {
                    log.warn("Unable to find aps with name {}", apsName);
                    return getBadRequestException(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode());
                });
        }
        log.warn("Either apsId or apsName property should not be null.");
        throw getBadRequestException(ERR_ACC_104.getErrorMessage(), ERR_ACC_104.getErrorCode());
    }

    private AssignablePermissionSet getAssignablePermissionSetById(Long id) {
        return assignablePermissionSetJpaRepository
            .findById(id, APS_PERMISSIONS_EXTENDED)
            .orElseThrow(() -> {
                log.warn("Unable to find aps with id {}", id);
                return getBadRequestException(ERR_ACQ_062.getErrorMessage(), ERR_ACQ_062.getErrorCode());
            });
    }

    private void checkIfJobRoleTemplateCanBeCreatedInServiceAgreement(ServiceAgreement serviceAgreement) {
        if (!serviceAgreement.isMaster()) {
            log.warn("Job role template can be created  only for master service agreement.");
            throw getBadRequestException(ERR_ACC_103.getErrorMessage(), ERR_ACC_103.getErrorCode());
        }

    }

    private void checkIfFunctionGroupWithGivenNameAlreadyPending(String serviceAgreementId, String functionGroupName) {
        if (applicationProperties.getApproval().getValidation().isEnabled()
            && approvalFunctionGroupJpaRepository
            .existsByNameAndServiceAgreementId(functionGroupName, serviceAgreementId)) {
            log.warn("There is pending creation of function group for service agreement "
                    + "with id {} with function group name {}",
                serviceAgreementId, functionGroupName);
            throw getBadRequestException(ERR_ACC_100.getErrorMessage(), ERR_ACC_100.getErrorCode());

        }
    }

    private void checkIfFunctionGroupAlreadyHavePendingRecordForDelete(String functionGroupId) {

        if (applicationProperties.getApproval().getValidation().isEnabled()
            && approvalFunctionGroupRefJpaRepository.existsByFunctionGroupId(functionGroupId)) {
            log.warn(
                "There is pending delete of function group with id  {}", functionGroupId);
            throw getBadRequestException(ERR_ACC_101.getErrorMessage(), ERR_ACC_101.getErrorCode());
        }
    }

    /**
     * Update Function Group.
     *
     * @param functionGroupId   - id of the function group to be updated
     * @param functionGroupBody - put request body for update function group
     */
    @Override
    @Transactional
    public void updateFunctionGroup(String functionGroupId, FunctionGroupBase functionGroupBody) {

        checkIfFunctionGroupAlreadyHavePendingRecordForDelete(functionGroupId);

        FunctionGroup functionGroup = getFunctionGroup(functionGroupId);

        validateServiceAgreementId(functionGroupBody.getServiceAgreementId(), functionGroup.getServiceAgreementId());

        validateNewFunctionGroupName(functionGroup.getName(), functionGroup.getServiceAgreementId(),
            functionGroup.getId(), functionGroupBody.getName());

        List<String> permissions = Optional.ofNullable(functionGroupBody.getPermissions())
            .orElseGet(ArrayList::new)
            .stream().map(Permission::getFunctionId)
            .collect(Collectors.toList());

        checkIfAllSpecifiedFunctionIdsExist(permissions);

        updateFunctionGroupBase(functionGroupBody, functionGroup);
    }

    /**
     * Update Function Group.
     *
     * @param functionGroupId   - id of the function group to be updated
     * @param functionGroupBody - put request body for update function group
     */
    @Override
    @Transactional
    public String updateFunctionGroupWithoutLegalEntity(String functionGroupId, FunctionGroupBase functionGroupBody) {

        FunctionGroup functionGroup = getFunctionGroupByIdAndNotSystemType(functionGroupId);

        checkIfServiceAgreementIsInPendingState(functionGroup.getServiceAgreementId());

        checkIfFunctionGroupAlreadyHavePendingRecordForDelete(functionGroupId);

        validateNewFunctionGroupName(functionGroup.getName(), functionGroup.getServiceAgreementId(),
            functionGroup.getId(), functionGroupBody.getName());

        checkIfAllSpecifiedFunctionIdsExistWithoutLegalEntity(functionGroupBody);

        updateFunctionGroupBase(functionGroupBody, functionGroup);
        return functionGroupId;
    }

    /**
     * Add System Function Group.
     *
     * @return Saved Function Group ID {@link String}
     */
    @Override
    @Transactional
    public String addSystemFunctionGroup(ServiceAgreement serviceAgreement, String functionGroupName,
        List<Permission> permissions) {

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName(functionGroupName);
        functionGroup.setDescription(functionGroupName);
        functionGroup.setServiceAgreement(serviceAgreement);
        functionGroup.setType(FunctionGroupType.SYSTEM);
        functionGroup.setPermissions(createFunctionPrivilegeList(permissions, functionGroup));
        serviceAgreement.getFunctionGroups().add(functionGroupJpaRepository.save(functionGroup));
        return functionGroup.getId();
    }

    /**
     * Checks if the name of the function group is to be changed and then checks if a function group with the new name
     * already exists for another service agreement.
     *
     * @param existingName               - name of function group present in database
     * @param existingServiceAgreementId - service agreement id of function group present in database
     * @param existingId                 - id of function group present in database
     * @param newFunctionGroupName       - new name from update of function group
     */
    private void validateNewFunctionGroupName(String existingName, String existingServiceAgreementId, String existingId,
        String newFunctionGroupName) {
        boolean nameIsChanged = !existingName.equals(newFunctionGroupName);
        if (nameIsChanged) {
            boolean functionGroupsWithSameName = functionGroupJpaRepository
                .existsByNameAndServiceAgreementIdAndIdNot(newFunctionGroupName,
                    existingServiceAgreementId, existingId);
            if (functionGroupsWithSameName) {
                log.warn("Function group with name {} already exists.", newFunctionGroupName);
                throw getBadRequestException(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode());
            }
        }
    }

    /**
     * Validates if the values of service agreement ID from the body match the actual values of the function group.
     *
     * @param requestServiceAgreement - service agreement from the request data
     * @param actualServiceAgreement  - service agreement from database
     * @throws BadRequestException if values do not match.
     */
    private void validateServiceAgreementId(String requestServiceAgreement,
        String actualServiceAgreement) {
        if (isNull(requestServiceAgreement) || !requestServiceAgreement.equals(actualServiceAgreement)) {
            log.warn("Invalid service agreement id {}", requestServiceAgreement);
            throw getBadRequestException(ERR_ACQ_025.getErrorMessage(), ERR_ACQ_025.getErrorCode());
        }
    }

    private FunctionGroup getFunctionGroupByIdWithDefaultType(String functionGroupId) {
        return functionGroupJpaRepository.findByIdAndType(functionGroupId, FunctionGroupType.DEFAULT)
            .orElseThrow(() -> {
                log.warn(UNABLE_TO_LOAD_FUNCTION_GROUP, functionGroupId);
                return getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
            });
    }

    private FunctionGroup getFunctionGroup(String functionGroupId) {
        return functionGroupJpaRepository.findById(functionGroupId)
            .orElseThrow(() -> {
                log.warn(UNABLE_TO_LOAD_FUNCTION_GROUP, functionGroupId);
                return getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
            });
    }


    private FunctionGroup getFunctionGroupByIdAndNotSystemType(String functionGroupId) {
        return functionGroupJpaRepository.findByIdAndTypeNot(functionGroupId, FunctionGroupType.SYSTEM)
            .orElseThrow(() -> {
                log.warn(UNABLE_TO_LOAD_FUNCTION_GROUP, functionGroupId);
                return getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
            });
    }

    /**
     * Delete function group by id.
     *
     * @param id - id of the function group
     */
    @Transactional
    @Override
    public String deleteFunctionGroup(String id) {

        checkIfFunctionGroupAlreadyHavePendingRecordForDelete(id);
        verifyPendingAssignmentForFunctionGroup(id);

        FunctionGroup functionGroup = getFunctionGroupByIdAndNotSystemType(id);
        checkIfServiceAgreementIsInPendingState(functionGroup.getServiceAgreementId());
        verifyUsersAssignedToFunctionGroup(functionGroup);

        functionGroupJpaRepository.deleteById(id);
        return id;
    }

    private void verifyUsersAssignedToFunctionGroup(FunctionGroup functionGroup) {
        List<UserAssignedFunctionGroup> usersAssignedToFunctionGroup = userAssignedFunctionGroupJpaRepository
            .findAllByFunctionGroupId(functionGroup.getId());
        if (!usersAssignedToFunctionGroup.isEmpty()) {
            log.warn("Users assigned to function group {}.", functionGroup.getId());
            throw getBadRequestException(ERR_ACC_027.getErrorMessage(), ERR_ACC_027.getErrorCode());
        }
    }

    /**
     * Find all business functions by service agreement.
     *
     * @param id         service agreement id.
     * @param isExternal boolean
     * @return list of {@link FunctionsGetResponseBody}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FunctionsGetResponseBody> findAllBusinessFunctionsByServiceAgreement(String id, boolean isExternal) {
        ServiceAgreement serviceAgreement;
        if (isExternal) {
            serviceAgreement = serviceAgreementJpaRepository
                .findByExternalId(id, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR)
                .orElseThrow(() -> getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
        } else {
            serviceAgreement = serviceAgreementJpaRepository
                .findById(id, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR)
                .orElseThrow(() -> getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
        }

        Set<ApplicableFunctionPrivilege> applicableFunctionPrivileges = businessFunctionCache
            .getApplicableFunctionPrivileges(serviceAgreement.getPermissionSetsRegular().stream()
                .flatMap(permissionsSet -> permissionsSet.getPermissions().stream()).collect(Collectors.toSet()));

        return transformFunctions(applicableFunctionPrivileges);
    }

    /**
     * Returns bulk function groups by function group ids.
     *
     * @param ids - list of function group ids
     * @return list of {@link BulkFunctionGroupsPostResponseBody}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BulkFunctionGroupsPostResponseBody> getBulkFunctionGroups(Collection<String> ids) {
        validateFunctionGroupIds(ids);
        List<FunctionGroup> functionGroups = functionGroupJpaRepository.findByIdIn(ids);
        validateGetFunctionGroupsByIdsResponse(ids, functionGroups);
        return transformFunctionGroupList(functionGroups);
    }

    /**
     * Get function groups by service agreement id.
     *
     * @param serviceAgreementId service agreement id
     * @return list of {@link FunctionGroupsGetResponseBody}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FunctionGroupsGetResponseBody> getFunctionGroupsByServiceAgreementId(String serviceAgreementId) {
        log.debug("Trying to get Function groups belonging to Service Agreement {}.", serviceAgreementId);

        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR_AND_CREATOR_LE_AND_FGS)
            .orElseThrow(() -> getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));

        ServiceAgreement creatorSa = serviceAgreementJpaRepository
            .findByCreatorLegalEntityIdAndIsMaster(serviceAgreement.getCreatorLegalEntity().getId(), true,
                SERVICE_AGREEMENT_WITH_FGS)
            .orElseThrow(() -> getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));

        Set<Long> permissionSetsRegular = serviceAgreement.getPermissionSetsRegular()
            .stream().map(AssignablePermissionSet::getId).collect(Collectors.toSet());

        Set<FunctionGroup> fgsFromSa = serviceAgreement.getFunctionGroups()
            .stream().filter(
                fg -> fg.getType() == FunctionGroupType.TEMPLATE && permissionSetsRegular
                    .contains(fg.getAssignablePermissionSetId())).collect(Collectors.toSet());

        Set<FunctionGroup> fgsFromCreatorSa = creatorSa.getFunctionGroups()
            .stream().filter(
                fg -> fg.getType() == FunctionGroupType.TEMPLATE && permissionSetsRegular
                    .contains(fg.getAssignablePermissionSetId())).collect(Collectors.toSet());

        List<FunctionGroup> fgs = functionGroupJpaRepository
            .findByCreatorLegalEntityIdAndAps(serviceAgreement.getCreatorLegalEntity().getId(),
                permissionSetsRegular, FunctionGroupType.TEMPLATE);

        List<FunctionGroup> functionGroups = functionGroupJpaRepository
            .findByServiceAgreementId(serviceAgreementId);
        return convertFunctionGroupBody(functionGroups, fgsFromSa, fgs, fgsFromCreatorSa);
    }

    /**
     * Get function group id by name and service agreement id.
     *
     * @param name               function group name
     * @param serviceAgreementId service agreement id
     * @return function group id
     */
    @Override
    @Transactional(readOnly = true)
    public String getFunctionGroupsByNameAndServiceAgreementId(String name, String serviceAgreementId) {
        log.debug("Trying to get Function group with name {}.", name);
        Optional<FunctionGroup> functionGroup = functionGroupJpaRepository
            .findByNameAndServiceAgreementId(name, serviceAgreementId);
        if (functionGroup.isPresent()) {
            return functionGroup.get().getId();
        } else {
            log.warn("Function group with name {} for service agreement with id {} does not exist", name,
                serviceAgreementId);
            throw getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
        }
    }

    /**
     * Creates a record in approval function group temporary table.
     *
     * @param functionGroupApprovalCreate {@link FunctionGroupBase}
     * @return id of saved record from temporary table.
     */
    @Override
    @Transactional
    public String addFunctionGroupApproval(FunctionGroupApprovalBase functionGroupApprovalCreate) {
        log.debug("Creating Function Group With Approval ON: {} ", functionGroupApprovalCreate);

        checkIfFunctionGroupWithGivenNameAlreadyExists(functionGroupApprovalCreate.getName(),
            functionGroupApprovalCreate.getServiceAgreementId());
        checkIfFunctionGroupWithGivenNameAlreadyPending(functionGroupApprovalCreate.getServiceAgreementId(),
            functionGroupApprovalCreate.getName());
        ServiceAgreement serviceAgreement = getServiceAgreement(functionGroupApprovalCreate.getServiceAgreementId());

        List<String> permissions = Optional.ofNullable(functionGroupApprovalCreate.getPermissions())
            .orElseGet(ArrayList::new)
            .stream().map(Permission::getFunctionId)
            .collect(Collectors.toList());

        checkIfAllSpecifiedFunctionIdsExist(permissions);
        checkIfAllSpecifiedPrivilegesExistInAssociatedAps(functionGroupApprovalCreate.getPermissions(),
            serviceAgreement);

        checkIfServiceAgreementIsInPendingState(
            functionGroupApprovalCreate.getServiceAgreementId());

        checkIfFunctionGroupTimeSpanIsWithinServiceAgreementTimeSpan(functionGroupApprovalCreate.getValidFrom(),
            functionGroupApprovalCreate.getValidUntil(), serviceAgreement);

        ApprovalFunctionGroup functionGroup = populateApprovalFunctionGroupDomain(functionGroupApprovalCreate,
            functionGroupApprovalCreate.getApprovalId(), functionGroupApprovalCreate.getApprovalTypeId());

        approvalFunctionGroupJpaRepository.save(functionGroup);
        return functionGroupApprovalCreate.getApprovalId();

    }

    /**
     * Creates a pending record for update of function group.
     *
     * @param requestData {@link FunctionGroupByIdPutRequestBody}
     */
    @Override
    @Transactional
    public void updateFunctionGroupApproval(FunctionGroupByIdPutRequestBody requestData, String functionGroupId,
        String approvalId) {
        checkIfFunctionGroupAlreadyHavePendingRecordForDelete(functionGroupId);
        checkIfFunctionGroupWithGivenNameAlreadyPending(requestData.getServiceAgreementId(), requestData.getName());
        FunctionGroup functionGroup = getFunctionGroup(functionGroupId);
        validateServiceAgreementId(requestData.getServiceAgreementId(), functionGroup.getServiceAgreementId());
        if (functionGroup.getType() == FunctionGroupType.SYSTEM || functionGroup.getType() == FunctionGroupType.TEMPLATE) {
            ApprovalFunctionGroup approvalFunctionGroup = populateApprovalFunctionGroupApprovalTypeOnlyUpdate(
                requestData.getApprovalTypeId(), approvalId, functionGroup);
            checkIfServiceAgreementIsInPendingState(functionGroup.getServiceAgreementId());
            approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);
        } else {
            validateNewFunctionGroupName(functionGroup.getName(), functionGroup.getServiceAgreementId(),
                functionGroup.getId(), requestData.getName());

            List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission> permissions =
                Optional
                    .ofNullable(requestData.getPermissions()).orElseGet(ArrayList::new);

            List<String> businessFunctionIds = permissions.stream().map(
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission::getFunctionId)
                .collect(Collectors.toList());

            checkIfAllSpecifiedFunctionIdsExist(businessFunctionIds);

            ServiceAgreement serviceAgreement = getServiceAgreement(requestData.getServiceAgreementId());

            checkIfServiceAgreementIsInPendingState(functionGroup.getServiceAgreementId());

            List<String> afpIds = permissions.stream()
                .flatMap(
                    permission -> businessFunctionCache
                        .getByFunctionAndPrivilege(permission.getFunctionId(),
                            permission.getAssignedPrivileges().stream().map(Privilege::getPrivilege)
                                .collect(Collectors.toList()))
                        .stream())
                .collect(Collectors.toList());

            Set<String> serviceAgreementPermissions = serviceAgreement.getPermissionSetsRegular().stream()
                .flatMap(ps -> ps.getPermissions().stream()).collect(Collectors.toSet());

            checkIfAllSpecifiedPrivilegesExistInAssociatedAps(afpIds, serviceAgreementPermissions);

            Date startDate = dateTimeService.getStartDateFromDateAndTime(requestData.getValidFromDate(),
                requestData.getValidFromTime());
            Date endDate = dateTimeService.getEndDateFromDateAndTime(requestData.getValidUntilDate(),
                requestData.getValidUntilTime());

            checkIfFunctionGroupTimeSpanIsWithinServiceAgreementTimeSpan(startDate, endDate,
                functionGroup.getServiceAgreement());

            validatePermissions(permissions);

            ApprovalFunctionGroup approvalFunctionGroup = populateApprovalFunctionGroupUpdate(requestData,
                functionGroupId, approvalId, startDate, endDate, new HashSet<>(afpIds));

            approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);
        }
    }

    private void validatePermissions(
        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission> permissions) {
        permissions.stream()
            .flatMap(permission -> {
                List<String> assignedPrivileges = permission.getAssignedPrivileges()
                    .stream()
                    .map(Privilege::getPrivilege)
                    .collect(Collectors.toList());

                return getApplicableFunctionPrivileges(permission.getFunctionId(), assignedPrivileges);
            })
            .collect(Collectors.toSet());
    }

    private ApprovalFunctionGroup populateApprovalFunctionGroupUpdate(FunctionGroupByIdPutRequestBody requestData,
        String functionGroupId, String approvalId, Date startDate, Date endDate, Set<String> afpIds) {
        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setApprovalTypeId(requestData.getApprovalTypeId());
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setFunctionGroupId(functionGroupId);
        approvalFunctionGroup.setServiceAgreementId(requestData.getServiceAgreementId());
        approvalFunctionGroup.setDescription(requestData.getDescription());
        approvalFunctionGroup.setName(requestData.getName());
        approvalFunctionGroup.setPrivileges(afpIds);
        approvalFunctionGroup.setStartDate(startDate);
        approvalFunctionGroup.setEndDate(endDate);
        return approvalFunctionGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PresentationFunctionGroupApprovalDetailsItem getByApprovalId(String approvalId) {
        log.debug("Trying to get approval details for job role with approval id {}", approvalId);
        ApprovalFunctionGroupRef approvalFunctionGroup = getApprovalFunctionGroup(approvalId);
        FunctionGroup functionGroup = null;
        String serviceAgreementName = null;
        if (nonNull(approvalFunctionGroup.getFunctionGroupId())) {
            functionGroup = getFunctionGroupById(approvalFunctionGroup);
            if (nonNull(functionGroup.getServiceAgreement())) {
                serviceAgreementName = functionGroup.getServiceAgreement().getName();
            }
        }

        if (approvalFunctionGroup.getApprovalAction() != ApprovalAction.DELETE) {
            serviceAgreementName =
                getServiceAgreementNameByAction((ApprovalFunctionGroup) approvalFunctionGroup, functionGroup);
        }

        return mapper.getResult(functionGroup, approvalFunctionGroup, serviceAgreementName);
    }

    private ApprovalFunctionGroupRef getApprovalFunctionGroup(String approvalId) {
        return approvalFunctionGroupRefJpaRepository
            .findByApprovalId(approvalId)
            .orElseThrow(() -> {
                log.warn("Approval with id {} does not exist", approvalId);
                return getNotFoundException(ERR_ACQ_067.getErrorMessage(), ERR_ACQ_067.getErrorCode());
            });
    }

    private String getServiceAgreementNameByAction(ApprovalFunctionGroup approvalFunctionGroup,
        FunctionGroup functionGroup) {
        String serviceAgreementName = null;
        if (approvalFunctionGroup.getApprovalAction().equals(ApprovalAction.CREATE)) {
            ServiceAgreement serviceAgreement = serviceAgreementJpaRepository
                .findById(approvalFunctionGroup.getServiceAgreementId(), null)
                .orElseThrow(() -> getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
            serviceAgreementName = serviceAgreement.getName();
        } else if (nonNull(functionGroup)) {
            serviceAgreementName = functionGroup.getServiceAgreement().getName();
        }
        return serviceAgreementName;
    }

    /**
     * Creates a pending record for delete of function group.
     *
     * @param requestData     {@link ApprovalDto}
     * @param functionGroupId String
     */
    @Override
    @Transactional
    public void deleteApprovalFunctionGroup(String functionGroupId, ApprovalDto requestData) {

        FunctionGroup functionGroup = getFunctionGroupByIdWithDefaultType(functionGroupId);
        checkIfServiceAgreementIsInPendingState(functionGroup.getServiceAgreementId());
        verifyUsersAssignedToFunctionGroup(functionGroup);
        verifyPendingAssignmentForFunctionGroup(functionGroupId);
        checkIfFunctionGroupAlreadyHavePendingRecordForDelete(functionGroupId);

        ApprovalFunctionGroupRef approvalFunctionGroupRef = new ApprovalFunctionGroupRef();
        approvalFunctionGroupRef.setFunctionGroupId(functionGroupId);
        approvalFunctionGroupRef.setApprovalId(requestData.getApprovalId());
        approvalFunctionGroupRefJpaRepository.save(approvalFunctionGroupRef);
    }

    private ApprovalFunctionGroup populateApprovalFunctionGroupDomain(
        FunctionGroupApprovalBase functionGroupApprovalCreate, String approvalId, String approvalTypeId) {
        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setApprovalId(approvalId);
        approvalFunctionGroup.setApprovalTypeId(approvalTypeId);
        approvalFunctionGroup.setName(functionGroupApprovalCreate.getName());
        approvalFunctionGroup.setDescription(functionGroupApprovalCreate.getDescription());
        approvalFunctionGroup.setServiceAgreementId(functionGroupApprovalCreate.getServiceAgreementId());

        Set<GroupedFunctionPrivilege> functionPrivilegeList = createFunctionPrivilegeList(
            functionGroupApprovalCreate.getPermissions(),
            new FunctionGroup().withName(functionGroupApprovalCreate.getName()));

        Set<String> privileges = functionPrivilegeList.stream()
            .map(FunctionGroupItem::getApplicableFunctionPrivilegeId
            ).collect(Collectors.toSet());

        approvalFunctionGroup.setPrivileges(privileges);
        approvalFunctionGroup.setStartDate(functionGroupApprovalCreate.getValidFrom());
        approvalFunctionGroup.setEndDate(functionGroupApprovalCreate.getValidUntil());

        return approvalFunctionGroup;
    }


    private List<FunctionGroupsGetResponseBody> convertFunctionGroupBody(Collection<FunctionGroup>... functionGroups) {
        Set<FunctionGroupsGetResponseBody> returnedFgs = new HashSet<>();
        for (Collection<FunctionGroup> fg : functionGroups) {
            returnedFgs.addAll(fg.stream().map(functionGroup -> {
                Optional<ApprovalFunctionGroupRef> approvalFunctionGroup = approvalFunctionGroupRefJpaRepository
                    .findByFunctionGroupId(functionGroup.getId());
                FunctionGroupsGetResponseBody functionGroupsGetResponseBody = functionGroupTransformer
                    .transformFunctionGroup(FunctionGroupsGetResponseBody.class, functionGroup)
                    .withId(functionGroup.getId());

                if (approvalFunctionGroup.isPresent()) {
                    return functionGroupsGetResponseBody
                        .withApprovalId(approvalFunctionGroup.get().getApprovalId());
                }
                return functionGroupsGetResponseBody;
            }).collect(Collectors.toSet()));
        }
        return new ArrayList<>(returnedFgs);
    }

    /**
     * Get Function Group by id.
     *
     * @param id function group id
     * @return {@link FunctionGroupByIdGetResponseBody}
     */
    @Override
    @Transactional(readOnly = true)
    public FunctionGroupByIdGetResponseBody getFunctionGroupById(String id) {
        log.debug("Trying to get Function group by id {}.", id);
        FunctionGroupByIdGetResponseBody functionGroupByIdGetResponseBody = functionGroupJpaRepository
            .findById(id)
            .map(fg -> functionGroupTransformer.transformFunctionGroup(FunctionGroupByIdGetResponseBody.class, fg)
                .withId(fg.getId()))
            .orElseThrow(() -> {
                log.warn("Function group with id {} does not exist", id);
                return getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
            });

        Optional<ApprovalFunctionGroupRef> approvalFunctionGroup = approvalFunctionGroupRefJpaRepository
            .findByFunctionGroupId(functionGroupByIdGetResponseBody.getId());
        approvalFunctionGroup
            .ifPresent(functionGroup -> functionGroupByIdGetResponseBody.setApprovalId(functionGroup.getApprovalId()));

        return functionGroupByIdGetResponseBody;
    }

    private FunctionGroup getFunctionGroupById(ApprovalFunctionGroupRef approvalFunctionGroup) {
        return functionGroupJpaRepository
            .findById(approvalFunctionGroup.getFunctionGroupId())
            .orElseThrow(() -> {
                log.warn("Function group with id {} does not exist", approvalFunctionGroup.getFunctionGroupId());
                return getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
            });
    }

    private Set<GroupedFunctionPrivilege> getNewPrivileges(FunctionGroup functionGroup,
        Set<String> newStateOfAfpIds, Set<String> afpIdsToUpdate) {
        Set<String> newAfpIds = getDifference(newStateOfAfpIds, afpIdsToUpdate);

        Set<ApplicableFunctionPrivilege> newAfps = businessFunctionCache.getApplicableFunctionPrivileges(newAfpIds);
        Set<GroupedFunctionPrivilege> newGroupedFunctionPrivileges = new HashSet<>();

        for (ApplicableFunctionPrivilege newAfp : newAfps) {
            GroupedFunctionPrivilege groupedFunctionPrivilege = getGroupedFunctionPrivilege(newAfp, functionGroup);

            newGroupedFunctionPrivileges.add(groupedFunctionPrivilege);
        }
        return newGroupedFunctionPrivileges;
    }

    private Set<String> getDifference(Set<String> leftSet, Set<String> rightSet) {
        return Sets.difference(leftSet, rightSet);
    }

    private Set<GroupedFunctionPrivilege> getGroupedFunctionForUpdate(FunctionGroup functionGroup,
        Set<String> newStateAfpIds) {
        return functionGroup.getPermissionsStream()
            .filter(groupedFunctionPrivilege -> newStateAfpIds
                .contains(groupedFunctionPrivilege.getApplicableFunctionPrivilegeId()))
            .collect(Collectors.toSet());
    }

    private FunctionGroup createFunctionGroupForSave(FunctionGroupBase functionGroupBase,
        ServiceAgreement serviceAgreement, AssignablePermissionSet assignablePermissionSet) {
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName(functionGroupBase.getName());
        functionGroup.setDescription(functionGroupBase.getDescription());
        functionGroup.setServiceAgreement(serviceAgreement);
        functionGroup.setType(FunctionGroupType.fromString(functionGroupBase.getType().toString()));
        functionGroup.setPermissions(
            createFunctionPrivilegeList(functionGroupBase.getPermissions(), functionGroup));
        functionGroup.setStartDate(functionGroupBase.getValidFrom());
        functionGroup.setEndDate(functionGroupBase.getValidUntil());
        if (nonNull(assignablePermissionSet)) {
            functionGroup.setAssignablePermissionSet(assignablePermissionSet);
        }

        return functionGroup;
    }

    private Set<GroupedFunctionPrivilege> createFunctionPrivilegeList(List<Permission> permissions,
        FunctionGroup functionGroup) {
        return permissions.stream()
            .flatMap(permission -> getGroupedFunctionPrivilege(permission, functionGroup))
            .collect(Collectors.toSet());
    }

    private Stream<GroupedFunctionPrivilege> getGroupedFunctionPrivilege(Permission permission,
        FunctionGroup functionGroup) {
        List<String> assignedPrivileges = permission.getAssignedPrivileges()
            .stream()
            .map(PrivilegeDto::getPrivilege)
            .collect(Collectors.toList());

        return getGroupedFunctionPrivileges(
            getApplicableFunctionPrivileges(permission.getFunctionId(), assignedPrivileges), functionGroup);
    }

    private GroupedFunctionPrivilege getGroupedFunctionPrivilege(
        ApplicableFunctionPrivilege applicableFunctionPrivilege, FunctionGroup functionGroup) {
        GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
        groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(applicableFunctionPrivilege.getId());
        groupedFunctionPrivilege.setFunctionGroup(functionGroup);

        return groupedFunctionPrivilege;
    }

    private Set<ApplicableFunctionPrivilege> getApplicableFunctionPrivileges(List<Permission> requestPermissions) {
        return requestPermissions.stream()
            .flatMap(permission ->
                getApplicableFunctionPrivileges(permission.getFunctionId(),
                    permission.getAssignedPrivileges()
                        .stream()
                        .map(PrivilegeDto::getPrivilege)
                        .collect(Collectors.toList())))
            .collect(Collectors.toSet());
    }

    private Stream<ApplicableFunctionPrivilege> getApplicableFunctionPrivileges(String businessFunctionId,
        List<String> assignedPrivileges) {
        Set<ApplicableFunctionPrivilege> applicableFunctionPrivilegeEmbeddableByFunctionAndPrivileges =
            getApplicableFunctionPrivilegesEmbeddable(businessFunctionId, assignedPrivileges)
                .collect(Collectors.toSet());

        if (applicableFunctionPrivilegeEmbeddableByFunctionAndPrivileges.size() != assignedPrivileges.size()) {
            log.warn("Invalid number of privileges {}, expected {}",
                applicableFunctionPrivilegeEmbeddableByFunctionAndPrivileges.size(), assignedPrivileges.size());
            throw getBadRequestException(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode());
        }
        return applicableFunctionPrivilegeEmbeddableByFunctionAndPrivileges.stream();
    }

    private Stream<GroupedFunctionPrivilege> getGroupedFunctionPrivileges(
        Stream<ApplicableFunctionPrivilege> applicableFunctionPrivilegeEmbeddableByFunctionAndPrivileges,
        FunctionGroup functionGroup) {
        return applicableFunctionPrivilegeEmbeddableByFunctionAndPrivileges.map(
            applicableFunctionPrivilege -> getGroupedFunctionPrivilege(applicableFunctionPrivilege, functionGroup));
    }

    private Stream<ApplicableFunctionPrivilege> getApplicableFunctionPrivilegesEmbeddable(String businessFunctionId,
        List<String> assignedPrivileges) {
        return businessFunctionCache
            .findAllByBusinessFunctionIdAndPrivilegeNameIn(businessFunctionId, assignedPrivileges).stream();
    }

    private void validateFunctionGroupIds(Collection<String> idList) {
        boolean invalidList = idList.stream()
            .anyMatch(String::isEmpty);
        if (invalidList) {
            log.warn("Invalid Function Group ids.");
            throw getBadRequestException(ERR_ACQ_012.getErrorMessage(), ERR_ACQ_012.getErrorCode());
        }
    }

    private void validateGetFunctionGroupsByIdsResponse(Collection<String> idList, List<FunctionGroup> functionGroups) {
        int expectedFunctionGroups = (int) idList.stream()
            .distinct().count();
        if (expectedFunctionGroups != functionGroups.size()) {
            log.warn(ERR_ACQ_003.getErrorMessage());
            throw getBadRequestException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
        }
    }

    private List<FunctionsGetResponseBody> transformFunctions(
        Collection<ApplicableFunctionPrivilege> allApplicableFunctionPrivileges) {

        return allApplicableFunctionPrivileges.stream()
            .collect(groupingBy(ApplicableFunctionPrivilege::getBusinessFunction))
            .entrySet().stream()
            .map(applicableFunctionPrivilege -> {
                BusinessFunction function = applicableFunctionPrivilege.getKey();
                return new FunctionsGetResponseBody()
                    .withName(function.getFunctionName())
                    .withFunctionCode(function.getFunctionCode())
                    .withResource(function.getResourceName())
                    .withResourceCode(function.getResourceCode())
                    .withFunctionId(function.getId())
                    .withPrivileges(getPrivileges(applicableFunctionPrivilege.getValue()));
            })
            .collect(Collectors.toList());
    }

    private List<PersistencePrivilege> getPrivileges(List<ApplicableFunctionPrivilege> value) {
        return value.stream()
            .map(this::getPrivilege)
            .collect(Collectors.toList());
    }

    private PersistencePrivilege getPrivilege(ApplicableFunctionPrivilege afp) {
        return new PersistencePrivilege()
            .withSupportsLimit(afp.isSupportsLimit())
            .withPrivilege(afp.getPrivilege().getName());
    }

    private void checkIfAllSpecifiedFunctionIdsExist(List<String> permissions) {
        List<String> allExistingFunctionsIds = getExistingFunctions();

        if (!allExistingFunctionsIds.containsAll(permissions)) {
            log.warn("Invalid business function(s): {} to update/create function group", permissions);
            throw getBadRequestException(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode());
        }
    }

    private void checkIfAllSpecifiedFunctionIdsExistWithoutLegalEntity(
        FunctionGroupBase functionGroupBase) {
        Map<String, String> allExistingFunctionsIds = getPairExistingFunctionsNameId();

        List<Permission> newPermissions = Optional
            .ofNullable(functionGroupBase.getPermissions())
            .orElse(new ArrayList<>())
            .stream().map(permission
                -> permission.withFunctionId(allExistingFunctionsIds.get(permission.getFunctionId())))
            .collect(Collectors.toList());

        if (newPermissions.stream().anyMatch(el -> el.getFunctionId() == null)) {
            log.warn("Invalid business function(s): {} to update/create function group", newPermissions);
            throw getBadRequestException(ERR_ACQ_024.getErrorMessage(), ERR_ACQ_024.getErrorCode());
        }
        functionGroupBase.setPermissions(newPermissions);
    }

    private List<String> getExistingFunctions() {
        List<ApplicableFunctionPrivilege> functionPrivileges = businessFunctionCache
            .getAllApplicableFunctionPrivileges();
        return functionPrivileges
            .stream()
            .map(applicableFunctionPrivilege -> applicableFunctionPrivilege.getBusinessFunction().getId())
            .collect(Collectors.toList());
    }

    private Map<String, String> getPairExistingFunctionsNameId() {

        return businessFunctionCache.getAllApplicableFunctionPrivileges()
            .stream().map(ApplicableFunctionPrivilege::getBusinessFunction)
            .distinct()
            .collect(Collectors.toMap(BusinessFunction::getFunctionName, BusinessFunction::getId));
    }

    private void checkIfFunctionGroupWithGivenNameAlreadyExists(String name, String serviceAgreementId) {
        if ((functionGroupJpaRepository
            .existsByNameAndServiceAgreementId(name, serviceAgreementId))) {
            log.warn("Function group with name {} exists", name);
            throw getBadRequestException(ERR_ACQ_023.getErrorMessage(), ERR_ACQ_023.getErrorCode());
        }
    }

    private ServiceAgreement getServiceAgreement(String serviceAgreementId) {
        return serviceAgreementJpaRepository
            .findById(serviceAgreementId, SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR)
            .orElseThrow(() -> getBadRequestException(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    private List<BulkFunctionGroupsPostResponseBody> transformFunctionGroupList(List<FunctionGroup> functionGroups) {
        return functionGroups.stream()
            .map(functionGroup -> functionGroupTransformer
                .transformFunctionGroup(BulkFunctionGroupsPostResponseBody.class, functionGroup)
                .withId(functionGroup.getId()))
            .collect(Collectors.toList());
    }

    private void verifyPendingAssignmentForFunctionGroup(String id) {
        if (applicationProperties.getApproval().getValidation().isEnabled()
            && approvalUserContextAssignFunctionGroupJpaRepository.existsByFunctionGroupId(id)) {
            log.warn("There are pending requests for assign permissions including function group {}.",
                id);
            throw getBadRequestException(ERR_ACC_073.getErrorMessage(), ERR_ACC_073.getErrorCode());
        }
    }

    private void checkIfFunctionGroupTimeSpanIsWithinServiceAgreementTimeSpan(Date validFrom, Date getValidUntil,
        ServiceAgreement serviceAgreement) {

        if (!timeBoundValidatorService
            .isPeriodValid(validFrom, getValidUntil, serviceAgreement.getStartDate(), serviceAgreement.getEndDate())) {
            throw getBadRequestException(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode());
        }
    }

    private void checkIfServiceAgreementIsInPendingState(String serviceAgreementId) {

        if (applicationProperties.getApproval().getValidation().isEnabled() &&
            approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)) {
            log.warn(
                "Function group operation is not allowed, there is pending operation on service agreement with id {}",
                serviceAgreementId);
            throw getBadRequestException(ERR_ACC_107.getErrorMessage(), ERR_ACC_107.getErrorCode());
        }
    }

    private void checkIfFunctionGroupTimeSpanIsValid(Date validFrom, Date getValidUntil) {

        if (!timeBoundValidatorService.isPeriodValid(validFrom, getValidUntil)) {
            throw getBadRequestException(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode());
        }
    }

    private void checkIfAllSpecifiedPrivilegesExistInAssociatedAps(
        List<Permission> permissions, ServiceAgreement serviceAgreement) {

        List<String> afpIds = permissions.stream()
            .flatMap(permission ->
                businessFunctionCache.getByFunctionAndPrivilege(permission.getFunctionId(),
                    permission.getAssignedPrivileges().stream()
                        .map(PrivilegeDto::getPrivilege)
                        .collect(Collectors.toList())).stream())
            .collect(Collectors.toList());

        Set<String> serviceAgreementPermissions = serviceAgreement.getPermissionSetsRegular().stream()
            .flatMap(ps -> ps.getPermissions().stream()).collect(Collectors.toSet());

        checkIfAllSpecifiedPrivilegesExistInAssociatedAps(afpIds, serviceAgreementPermissions);
    }

    private void checkIfAllSpecifiedPrivilegesExistInAssociatedAps(
        List<String> afpIds, Set<String> assignablePermissionSetItems) {

        if (!afpIds.stream().allMatch(assignablePermissionSetItems::contains)) {
            log.warn("Function Group can not be created - invalid or not applicable Privilege");
            throw getBadRequestException(ERR_ACQ_011.getErrorMessage(), ERR_ACQ_011.getErrorCode());
        }
    }

    private void updateFunctionGroupBase(
        FunctionGroupBase functionGroupBody, FunctionGroup functionGroup) {

        checkIfServiceAgreementIsInPendingState(functionGroupBody.getServiceAgreementId());

        List<String> afpIds = functionGroupBody.getPermissions().stream()
            .flatMap(permission ->
                businessFunctionCache.getByFunctionAndPrivilege(permission.getFunctionId(),
                    permission.getAssignedPrivileges().stream()
                        .map(PrivilegeDto::getPrivilege)
                        .collect(Collectors.toList())).stream())
            .collect(Collectors.toList());

        if (functionGroup.getType().equals(FunctionGroupType.TEMPLATE)) {
            checkIfFunctionGroupTimeSpanIsValid(functionGroupBody.getValidFrom(), functionGroupBody.getValidUntil());
            AssignablePermissionSet assignablePermissionSet = getAssignablePermissionSetById(
                functionGroup.getAssignablePermissionSetId());

            checkIfAllSpecifiedPrivilegesExistInAssociatedAps(afpIds, assignablePermissionSet.getPermissions());
        } else {
            ServiceAgreement serviceAgreement = getServiceAgreement(functionGroup.getServiceAgreementId());
            checkIfFunctionGroupTimeSpanIsWithinServiceAgreementTimeSpan(functionGroupBody.getValidFrom(),
                functionGroupBody.getValidUntil(),
                serviceAgreement);

            Set<String> serviceAgreementPermissions = serviceAgreement.getPermissionSetsRegular().stream()
                .flatMap(ps -> ps.getPermissions().stream()).collect(Collectors.toSet());

            checkIfAllSpecifiedPrivilegesExistInAssociatedAps(afpIds, serviceAgreementPermissions);
        }

        functionGroup.setName(functionGroupBody.getName());
        functionGroup.setDescription(functionGroupBody.getDescription());
        functionGroup.setStartDate(functionGroupBody.getValidFrom());
        functionGroup.setEndDate(functionGroupBody.getValidUntil());

        Set<GroupedFunctionPrivilege> newPrivileges = calculateNewPrivileges(functionGroupBody, functionGroup);

        Set<String> newAfpIds = newPrivileges.stream()
            .map(FunctionGroupItem::getApplicableFunctionPrivilegeId)
            .collect(Collectors.toSet());

        List<UserAssignedFunctionGroupCombination> existingCombinations = userAssignedCombinationRepository
            .findAllCombinationsByFunctionGroupId(functionGroup.getId());

//      We are copying combinations which should be inserted after FG is updated.
//      This step needed because FG is mapped with permissions as ElementCollection.
//      It means that Hibernate will remove all items and insert them for each update of FG.
//      Since we have constraint on SAP to -> FGI we will remove all SAP on each update of FG and insert applicable SAP after updating FG
        Map<Long, Set<SelfApprovalPolicy>> policiesByCombinationIdToRestore = existingCombinations.stream().collect(
            Collectors.toMap(UserAssignedFunctionGroupCombination::getId,
                item -> copyPoliciesToRestore(item.getSelfApprovalPolicies(), newAfpIds)));

        existingCombinations.forEach(c -> c.getSelfApprovalPolicies().clear());

//      This flush needed because we have to remove all policies before updating functionGroupPermissions,
//      otherwise JdbcSQLIntegrityConstraintViolationException will be thrown
        userAssignedCombinationRepository.flush();

        functionGroup.setPermissions(newPrivileges);

        functionGroupJpaRepository.saveAndFlush(functionGroup);

        restoreSelfApprovalPolicies(existingCombinations, policiesByCombinationIdToRestore);

        removeEmptyCombinations(existingCombinations);
    }

    private Set<GroupedFunctionPrivilege> calculateNewPrivileges(FunctionGroupBase functionGroupBody, FunctionGroup functionGroup) {
        Set<String> newStateAfpIds = getApplicableFunctionPrivileges(functionGroupBody.getPermissions())
            .stream()
            .map(ApplicableFunctionPrivilege::getId)
            .collect(Collectors.toSet());

        Set<GroupedFunctionPrivilege> gfpToUpdate = getGroupedFunctionForUpdate(functionGroup, newStateAfpIds);

        Set<String> afpIdsToUpdate = gfpToUpdate.stream()
            .map(GroupedFunctionPrivilege::getApplicableFunctionPrivilegeId)
            .collect(Collectors.toSet());

        Set<GroupedFunctionPrivilege> newGfps = getNewPrivileges(functionGroup, newStateAfpIds, afpIdsToUpdate);

        return Sets.union(gfpToUpdate, newGfps);
    }

    private void removeEmptyCombinations(List<UserAssignedFunctionGroupCombination> existingCombinations) {
        for (UserAssignedFunctionGroupCombination combination : existingCombinations) {
            if (combination.getDataGroupIds().isEmpty() && combination.getSelfApprovalPolicies().isEmpty()) {
                userAssignedCombinationRepository.delete(combination);
            }
        }
    }

    private void restoreSelfApprovalPolicies(List<UserAssignedFunctionGroupCombination> existingCombinations,
        Map<Long, Set<SelfApprovalPolicy>> policiesByCombinationIdToRestore) {
        for (UserAssignedFunctionGroupCombination combination : existingCombinations) {
            Set<SelfApprovalPolicy> policiesToRestore = policiesByCombinationIdToRestore.get(combination.getId());
            if (CollectionUtils.isNotEmpty(policiesToRestore)) {
                combination.addPolicies(policiesToRestore);
            }
        }
    }

    private Set<SelfApprovalPolicy> copyPoliciesToRestore(Set<SelfApprovalPolicy> existingSelfApprovalPolicies,
        Set<String> newAfpIds) {
        return existingSelfApprovalPolicies.stream()
            .filter(p -> newAfpIds.contains(p.getFunctionGroupItem().getApplicableFunctionPrivilege().getId()))
            .map(SelfApprovalPolicy::new)
            .collect(Collectors.toSet());
    }

	private ApprovalFunctionGroup populateApprovalFunctionGroupApprovalTypeOnlyUpdate(
			String approvalTypeId, String approvalId, FunctionGroup functionGroup) {
		ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
		approvalFunctionGroup.setApprovalTypeId(approvalTypeId);
		approvalFunctionGroup.setApprovalId(approvalId);
		approvalFunctionGroup.setFunctionGroupId(functionGroup.getId());
		approvalFunctionGroup.setServiceAgreementId(functionGroup.getServiceAgreementId());
		approvalFunctionGroup.setDescription(functionGroup.getDescription());
		approvalFunctionGroup.setName(functionGroup.getName());
		approvalFunctionGroup.setPrivileges(functionGroup.getPermissions().stream()
				.map(FunctionGroupItem::getApplicableFunctionPrivilegeId).collect(Collectors.toSet()));
		approvalFunctionGroup.setStartDate(functionGroup.getStartDate());
		approvalFunctionGroup.setEndDate(functionGroup.getEndDate());
		return approvalFunctionGroup;
	}


}
