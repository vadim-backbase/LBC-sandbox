package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.domain.GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.domain.enums.AssignablePermissionType.ADMIN_USER_DEFAULT;
import static com.backbase.accesscontrol.domain.enums.AssignablePermissionType.REGULAR_USER_DEFAULT;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_087;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_088;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_089;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_094;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_096;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_102;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.mappers.PermissionSetMapper;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementAssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.accesscontrol.util.validation.ValidatePermissionSetIdentifiers;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PermissionSetServiceImpl implements PermissionSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionSetServiceImpl.class);
    private static final String APS_MIGRATION_OR_CONFIGURATION_ERROR = "Configuration error for assignable "
        + "permission sets or incorrect migration";
    private static final String SERVICE_AGREEMENT_DOES_NOT_EXIST_BY_EXTERNAL_ID =
        "Service agreement with external id {} does not exist";
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    private ServiceAgreementAssignablePermissionSetJpaRepository serviceAgreementAssignablePermissionSetJpaRepository;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    private PermissionSetMapper permissionSetMapper;
    private BusinessFunctionCache businessFunctionCache;
    private ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AssignablePermissionSet> getPermissionSetFilteredByName(String name) {
        LOGGER.info("Getting assignable permission sets filtered by name  {}", name);

        if (isNull(name)) {
            return assignablePermissionSetJpaRepository
                .findAll();
        } else {
            return assignablePermissionSetJpaRepository
                .findByNameContainingIgnoreCase(name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BigDecimal save(PresentationPermissionSet persistencePermissionSet) {
        LOGGER.info("Saving permission set {} ...", persistencePermissionSet);
        checkIfPermissionSetNameAlreadyExists(persistencePermissionSet);
        checkIfBusinessFunctionPrivilegesPairAreValid(persistencePermissionSet.getPermissions());
        AssignablePermissionSet assignablePermissionSet = permissionSetMapper.toDbModel(persistencePermissionSet);
        AssignablePermissionSet savedPermissionSet = assignablePermissionSetJpaRepository.save(assignablePermissionSet);
        return new BigDecimal(savedPermissionSet.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long delete(String identifierType, String identifier) {

        LOGGER.info("Delete permission set defined with type: {} identifier {}", identifierType, identifier);
        AssignablePermissionSet permissionSet = getAssignablePermissionSet(identifierType, identifier);

        validateIsCustom(permissionSet);
        validateIsAssigned(permissionSet);
        assignablePermissionSetJpaRepository.delete(permissionSet);

        return permissionSet.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AssignablePermissionSet> getAssignablePermissionSetsByName(Set<String> apsNames, boolean isRegularUser) {
        if (apsNames.isEmpty()) {
            return getAssignablePermissionSetsById(new HashSet<>(), isRegularUser);
        }
        Set<AssignablePermissionSet> res = assignablePermissionSetJpaRepository.findAllByNameIn(apsNames);
        if (res.size() != apsNames.size()) {
            LOGGER.warn("Invalid name identifier of assignable permission set.");
            throw getBadRequestException(ERR_ACC_094.getErrorMessage(), ERR_ACC_094.getErrorCode());
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AssignablePermissionSet> getAssignablePermissionSetsById(Set<Long> apsIds, boolean isRegularUser) {
        Set<Long> ids = new HashSet<>(apsIds);
        if (ids.isEmpty()) {
            return Sets.newHashSet(assignablePermissionSetJpaRepository
                .findFirstByType(isRegularUser ? REGULAR_USER_DEFAULT.getValue() : ADMIN_USER_DEFAULT.getValue())
                .orElseThrow(this::getErrorIncorrectConfigurationOrMigration));
        }
        Set<AssignablePermissionSet> res = assignablePermissionSetJpaRepository.findAllByIdIn(ids);
        if (res.size() != apsIds.size()) {
            LOGGER.warn("Invalid id identifier of assignable permission set.");
            throw getBadRequestException(ERR_ACC_094.getErrorMessage(), ERR_ACC_094.getErrorCode());
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public String update(PresentationPermissionSetItemPut requestData) {

        ServiceAgreement serviceAgreement = getServiceAgreement(requestData);

        Set<AssignablePermissionSet> allAps = getAllAssignablePermissionSets();

        List<FunctionGroup> defaultFg = getFunctionGroupsForServiceAgreement(serviceAgreement);

        Optional<FunctionGroup> optionalSystemFg = getSystemFunctionGroup(serviceAgreement);

        Set<AssignablePermissionSet> newApsStateAdminUser = getNewStateAps(allAps, requestData.getAdminUserAps());
        if (isNotEmpty(newApsStateAdminUser) && optionalSystemFg.isPresent()) {
            FunctionGroup systemFg = optionalSystemFg.get();
            Set<String> systemAfp = getFunctionGroupAfps(Lists.newArrayList(systemFg));
            Set<String> afpStateAdminUser = convertNewStateToAfp(newApsStateAdminUser);

            Set<String> adminAfpsToBeRemoved = Sets.difference(systemAfp, afpStateAdminUser);
            Set<String> adminAfpsToBeAdded = Sets.difference(afpStateAdminUser, systemAfp);
            validateIfFgPrivilegesFromApsMatchFgPrivilegesInPendingIfExists(
                afpStateAdminUser, serviceAgreement.getId());
            removeSystemPermissions(systemFg, adminAfpsToBeRemoved);

            addNewSystemPermissions(systemFg, adminAfpsToBeAdded);
            functionGroupJpaRepository.saveAndFlush(systemFg);
        }

        Set<String> defaultAfp = getFunctionGroupAfps(defaultFg);
        Set<AssignablePermissionSet> newApsStateRegularUser = getNewStateAps(allAps, requestData.getRegularUserAps());

        if (isNotEmpty(newApsStateRegularUser)) {
            Set<String> afpStateRegularUser = convertNewStateToAfp(newApsStateRegularUser);
            validateIfFgPrivilegesFromApsMatchFgPrivilegesInPendingIfExists(
                afpStateRegularUser, serviceAgreement.getId());

            LOGGER.info(
                "Checking if there is a set up error APS state of regular user should not contain all default fg APSs");

            if (!afpStateRegularUser.containsAll(defaultAfp)) {
                LOGGER.warn(ERR_ACC_096.getErrorMessage());
                throw getBadRequestException(ERR_ACC_096.getErrorMessage(), ERR_ACC_096.getErrorCode());
            }

            serviceAgreement.getPermissionSetsRegular().clear();
            serviceAgreement.getPermissionSetsRegular().addAll(newApsStateRegularUser);
        }

        if (isNotEmpty(newApsStateAdminUser)) {
            serviceAgreement.getPermissionSetsAdmin().clear();
            serviceAgreement.getPermissionSetsAdmin().addAll(newApsStateAdminUser);
        }

        LOGGER.info("Saving service agreement with new aps state {}", serviceAgreement);
        return  serviceAgreementJpaRepository.save(serviceAgreement).getId();
    }

    private void validateIfFgPrivilegesFromApsMatchFgPrivilegesInPendingIfExists(
        Set<String> afpStateRegularUser, String serviceAgreementId) {
        Optional<List<ApprovalFunctionGroup>> approvalFunctionGroups = approvalFunctionGroupJpaRepository
            .findByServiceAgreementId(serviceAgreementId);

        approvalFunctionGroups.ifPresent(functionGroups -> functionGroups.forEach(functionGroup -> {
                Set<String> afpsInFgPending = functionGroup.getPrivileges()
                    .stream().map(privilege -> businessFunctionCache
                        .getApplicableFunctionPrivilegeById(privilege)
                        .getId())
                    .collect(Collectors.toSet());
                if (!afpStateRegularUser.containsAll(afpsInFgPending)) {
                    LOGGER.warn(ERR_ACC_102.getErrorMessage());
                    throw getBadRequestException(ERR_ACC_102.getErrorMessage(), ERR_ACC_102.getErrorCode());

                }
            }
        ));
    }

    private void addNewSystemPermissions(FunctionGroup systemFg, Set<String> adminAfpsToBeAdded) {
        Set<GroupedFunctionPrivilege> gfpToBeAdded = getAllGroupFunctionPrivilegeForApfs(
            adminAfpsToBeAdded, systemFg);

        systemFg.getPermissions().addAll(gfpToBeAdded);
    }

    private void removeSystemPermissions(FunctionGroup systemFG, Set<String> adminAfpsToBeRemoved) {
        if (!adminAfpsToBeRemoved.isEmpty()) {
            Set<GroupedFunctionPrivilege> gfpsToBeRemoved = systemFG.getPermissionsStream()
                .filter(gfp -> adminAfpsToBeRemoved.contains(gfp.getApplicableFunctionPrivilegeId()))
                .collect(Collectors.toSet());

            systemFG.getPermissions().removeAll(gfpsToBeRemoved);
        }
    }

    private Optional<FunctionGroup> getSystemFunctionGroup(ServiceAgreement serviceAgreement) {
        return functionGroupJpaRepository
            .findByServiceAgreementAndType(serviceAgreement, FunctionGroupType.SYSTEM).stream().findFirst();
    }

    private Set<GroupedFunctionPrivilege> getAllGroupFunctionPrivilegeForApfs(Set<String> adminAfpsToBeAdded,
        FunctionGroup systemFG) {
        Collection<ApplicableFunctionPrivilege> all = businessFunctionCache.getAllApplicableFunctionPrivileges();
        return all.stream().filter(item -> adminAfpsToBeAdded.contains(item.getId()))
            .map(afp -> new GroupedFunctionPrivilege(systemFG, afp.getId()))
            .collect(Collectors.toSet());
    }

    private Set<String> convertNewStateToAfp(Set<AssignablePermissionSet> newApsStateRegularUser) {
        return newApsStateRegularUser
            .stream()
            .flatMap(item -> item.getPermissions().stream())
            .collect(Collectors.toSet());
    }

    private Set<AssignablePermissionSet> getNewStateAps(Set<AssignablePermissionSet> allAps,
        PresentationUserApsIdentifiers userAps) {
        Set<AssignablePermissionSet> newApsStateByUser = new HashSet<>();
        if (nonNull(userAps) && nonNull(userAps.getIdIdentifiers()) && !userAps.getIdIdentifiers().isEmpty()) {
            Set<BigDecimal> apsIdIdentifiers = new HashSet<>(userAps.getIdIdentifiers());
            checkIfAllIdIdentifiersAreValid(apsIdIdentifiers, allAps);
            allAps.forEach(item -> apsIdIdentifiers.forEach(id -> {
                if (id.longValue() == (item.getId())) {
                    newApsStateByUser.add(item);
                }
            }));
        }
        if (nonNull(userAps) && nonNull(userAps.getNameIdentifiers()) && !userAps.getNameIdentifiers().isEmpty()) {
            Set<String> apsNameIdentifiers = new HashSet<>(userAps.getNameIdentifiers());
            checkIfAllNameIdentifiersAreValid(apsNameIdentifiers, allAps);
            allAps.forEach(item -> apsNameIdentifiers.forEach(name -> {
                if (name.equals(item.getName())) {
                    newApsStateByUser.add(item);
                }
            }));
        }
        return newApsStateByUser;
    }

    private void checkIfAllNameIdentifiersAreValid(Set<String> regularUserNameIdentifiers,
        Set<AssignablePermissionSet> allAps) {
        if (regularUserNameIdentifiers.contains(null)) {
            throw getBadRequestException(ERR_ACC_089.getErrorMessage(), ERR_ACC_089.getErrorCode());
        }
        Set<String> allApsNames = allAps.stream().map(AssignablePermissionSet::getName).collect(Collectors.toSet());
        if (!allApsNames.containsAll(regularUserNameIdentifiers)) {
            throw getBadRequestException(ERR_ACC_089.getErrorMessage(), ERR_ACC_089.getErrorCode());
        }
    }

    private void checkIfAllIdIdentifiersAreValid(Set<BigDecimal> userIds, Set<AssignablePermissionSet> allAps) {
        if (userIds.contains(null)) {
            throw getBadRequestException(ERR_ACC_089.getErrorMessage(), ERR_ACC_089.getErrorCode());
        }
        Set<Long> longUserIds = userIds.stream().map(BigDecimal::longValue).collect(Collectors.toSet());
        Set<Long> allApsIds = allAps.stream().map(AssignablePermissionSet::getId).collect(Collectors.toSet());
        if (!allApsIds.containsAll(longUserIds)) {
            throw getBadRequestException(ERR_ACC_089.getErrorMessage(), ERR_ACC_089.getErrorCode());
        }
    }

    private Set<String> getFunctionGroupAfps(List<FunctionGroup> defaultFG) {
        return defaultFG.stream().flatMap(FunctionGroup::getPermissionsStream)
            .map(GroupedFunctionPrivilege::getApplicableFunctionPrivilegeId).collect(Collectors.toSet());
    }

    private List<FunctionGroup> getFunctionGroupsForServiceAgreement(ServiceAgreement serviceAgreement) {
        return functionGroupJpaRepository
            .findByServiceAgreementAndType(serviceAgreement, FunctionGroupType.DEFAULT);
    }

    private Set<AssignablePermissionSet> getAllAssignablePermissionSets() {
        return new HashSet<>(assignablePermissionSetJpaRepository.findAll());
    }

    private ServiceAgreement getServiceAgreement(PresentationPermissionSetItemPut requestData) {
        return serviceAgreementJpaRepository.findByExternalId(
            requestData.getExternalServiceAgreementId(), SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS)
            .orElseThrow(() -> {
                LOGGER.warn(SERVICE_AGREEMENT_DOES_NOT_EXIST_BY_EXTERNAL_ID,
                    requestData.getExternalServiceAgreementId());
                return getBadRequestException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode());
            });
    }

    private InternalServerErrorException getErrorIncorrectConfigurationOrMigration() {
        LOGGER.error(APS_MIGRATION_OR_CONFIGURATION_ERROR);
        return getInternalServerErrorException(APS_MIGRATION_OR_CONFIGURATION_ERROR);
    }

    private void validateIsAssigned(AssignablePermissionSet permissionSet) {
        if (serviceAgreementAssignablePermissionSetJpaRepository
            .existsByAssignablePermissionSetId(permissionSet.getId())) {
            LOGGER.warn("Permission set already assigned in SA {}", permissionSet);
            throw getBadRequestException(
                CommandErrorCodes.ERR_ACC_092.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_092.getErrorCode()
            );
        }
    }

    private void validateIsCustom(AssignablePermissionSet permissionSet) {
        if (!permissionSet.getType().equals(AssignablePermissionType.CUSTOM)) {
            LOGGER.warn("Permission set must type CUSTOM {}", permissionSet);
            throw getBadRequestException(
                CommandErrorCodes.ERR_ACC_091.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_091.getErrorCode()
            );
        }
    }

    private AssignablePermissionSet getAssignablePermissionSet(String identifierType, String identifier) {
        Optional<AssignablePermissionSet> optionalPermissionSet;
        if (ValidatePermissionSetIdentifiers.isIdIdentifier(identifierType)) {
            Long id = Long.parseLong(identifier);
            optionalPermissionSet = assignablePermissionSetJpaRepository.findById(id);
        } else {
            optionalPermissionSet = assignablePermissionSetJpaRepository.findByName(identifier);
        }

        if (!optionalPermissionSet.isPresent()) {
            LOGGER.warn("Permission set not exists");
            throw getNotFoundException(
                CommandErrorCodes.ERR_ACC_090.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_090.getErrorCode()
            );
        }
        return optionalPermissionSet.get();
    }

    private void checkIfBusinessFunctionPrivilegesPairAreValid(
        List<PresentationPermissionSetItem> permissions
    ) {
        if (permissions.stream()
            .anyMatch(item -> !businessFunctionCache.haveValidPrivileges(item.getFunctionId(), item.getPrivileges()))) {
            throw getBadRequestException(ERR_ACC_088.getErrorMessage(), ERR_ACC_088.getErrorCode());
        }
    }

    private void checkIfPermissionSetNameAlreadyExists(PresentationPermissionSet persistencePermissionSet) {
        LOGGER.info("Check if assignable permission set with name {} already exists.",
            persistencePermissionSet.getName());
        if (assignablePermissionSetJpaRepository.existsByName(persistencePermissionSet.getName())) {
            LOGGER.warn("Assignable permission set with name {} already exists.", persistencePermissionSet.getName());
            throw getBadRequestException(ERR_ACC_087.getErrorMessage(), ERR_ACC_087.getErrorCode());
        }
    }
}
