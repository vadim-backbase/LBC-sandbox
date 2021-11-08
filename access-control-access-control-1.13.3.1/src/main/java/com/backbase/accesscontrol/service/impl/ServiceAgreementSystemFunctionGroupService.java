package com.backbase.accesscontrol.service.impl;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.FunctionGroupService;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServiceAgreementSystemFunctionGroupService {

    private static final String SYSTEM_FUNCTION_GROUP_NAME = "SYSTEM_FUNCTION_GROUP";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementSystemFunctionGroupService.class);

    private FunctionGroupService functionGroupService;
    private BusinessFunctionCache businessFunctionCache;
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    private EntityManager entityManager;
    private FunctionGroupJpaRepository functionGroupJpaRepository;


    /**
     * Creates an object containing service agreement and system function group for that service agreement.
     *
     * @param serviceAgreement - service agreement object
     * @return {@link ServiceAgreementFunctionGroups} object containing service agreement and system function groups
     */
    public ServiceAgreementFunctionGroups getServiceAgreementFunctionGroups(ServiceAgreement serviceAgreement) {

        return new ServiceAgreementFunctionGroups(
            getOrCreateSystemFunctionGroup(serviceAgreement),
            serviceAgreement);
    }

    private String getOrCreateSystemFunctionGroup(ServiceAgreement serviceAgreement) {
        Optional<FunctionGroup> systemFunctionGroup = getSystemFunctionGroupByName(
            serviceAgreement, SYSTEM_FUNCTION_GROUP_NAME);

        return systemFunctionGroup
            .map(FunctionGroup::getId)
            .orElseGet(() -> createSystemFunctionGroupAndGetId(serviceAgreement));
    }

    private Optional<FunctionGroup> getSystemFunctionGroupByName(
        ServiceAgreement serviceAgreement, String functionGroupName) {

        PersistenceUtil persistenceUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (persistenceUtil.isLoaded(serviceAgreement, "functionGroups")) {
            LOGGER.debug("Getting function group from service agreement function groups collection.");
            return serviceAgreement.getFunctionGroups().stream()
                .filter(functionGroup -> functionGroup.getName().equals(functionGroupName)).findFirst();
        } else {
            LOGGER.debug("Getting function group from repository.");
            return functionGroupJpaRepository.findByNameAndServiceAgreementId(SYSTEM_FUNCTION_GROUP_NAME,
                serviceAgreement.getId());
        }
    }

    private String createSystemFunctionGroupAndGetId(ServiceAgreement serviceAgreement) {
        Set<ApplicableFunctionPrivilege> allApplicableFunctionPrivileges = businessFunctionCache
            .getApplicableFunctionPrivileges(assignablePermissionSetJpaRepository
            .findAllByAssignedAsAdminToServiceAgreement(serviceAgreement.getId()).stream()
                    .flatMap(aps -> aps.getPermissions().stream()).collect(Collectors.toSet()));

        List<Permission> permissions = createPermissionsToAssign(allApplicableFunctionPrivileges);

        return functionGroupService.addSystemFunctionGroup(serviceAgreement, SYSTEM_FUNCTION_GROUP_NAME, permissions);
    }

    private List<Permission> createPermissionsToAssign(
        Set<ApplicableFunctionPrivilege> allApplicableFunctionPrivileges) {
        return allApplicableFunctionPrivileges.stream()
            .collect(Collectors.groupingBy(ApplicableFunctionPrivilege::getBusinessFunction))
            .entrySet().stream()
            .map(this::createPermission)
            .collect(Collectors.toList());
    }

    private Permission createPermission(Entry<BusinessFunction, List<ApplicableFunctionPrivilege>> function) {
        return new Permission()
            .withFunctionId(function.getKey().getId())
            .withAssignedPrivileges(function.getValue().stream()
                .map(applicableFunctionPrivilege -> new PrivilegeDto()
                    .withPrivilege(applicableFunctionPrivilege.getPrivilege().getName()))
                .collect(Collectors.toList()));
    }
}