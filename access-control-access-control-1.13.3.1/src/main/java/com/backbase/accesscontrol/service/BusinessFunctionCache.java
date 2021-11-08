package com.backbase.accesscontrol.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.repository.ApplicableFunctionPrivilegeJpaRepository;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BusinessFunctionCache {

    private Map<String, ApplicableFunctionPrivilege> applicableFunctionPrivilegeMap;

    private Map<String, List<ApplicableFunctionPrivilege>> businessFunctionIdPrivilegeListMapping;

    private Map<String, BusinessFunction> businessFunctionByNameMapping;

    /**
     * Custom constructor.
     *
     * @param applicableFunctionPrivilegeJpaRepository {@link ApplicableFunctionPrivilegeJpaRepository}
     */
    public BusinessFunctionCache(ApplicableFunctionPrivilegeJpaRepository applicableFunctionPrivilegeJpaRepository) {
        loadApplicableFunctionPrivileges(applicableFunctionPrivilegeJpaRepository);
    }

    private void loadApplicableFunctionPrivileges(
        ApplicableFunctionPrivilegeJpaRepository applicableFunctionPrivilegeJpaRepository) {
        log.info("Loading business functions and privileges");

        applicableFunctionPrivilegeMap = applicableFunctionPrivilegeJpaRepository.findAll().stream()
            .collect(Collectors.toMap(ApplicableFunctionPrivilege::getId, item -> item));

        businessFunctionIdPrivilegeListMapping = applicableFunctionPrivilegeMap.values().stream().collect(Collectors
            .groupingBy(applicableFunctionPrivilege -> applicableFunctionPrivilege.getBusinessFunction().getId()));

        businessFunctionByNameMapping = applicableFunctionPrivilegeMap.values().stream()
                .map(ApplicableFunctionPrivilege::getBusinessFunction)
                .distinct()
                .collect(Collectors.toMap(BusinessFunction::getFunctionName, item -> item));
    }

    /**
     * Check if privileges can be assigned to a specified business function id.
     *
     * @param functionId Business function id
     * @param privileges List of privileges
     * @return true if privileges can be assigned to the specified business function id, otherwise false
     */
    public boolean haveValidPrivileges(String functionId, Collection<String> privileges) {
        List<ApplicableFunctionPrivilege> applicableFunctionPrivileges = businessFunctionIdPrivilegeListMapping
            .get(functionId);

        if (isNull(applicableFunctionPrivileges)) {
            return false;
        }

        if (isNull(privileges)) {
            return true;
        }

        return applicableFunctionPrivileges.stream()
            .map(item -> item.getPrivilege().getName()).collect(Collectors.toList()).containsAll(privileges);

    }

    /**
     * For specified function name will return matching business function.
     *
     * @param functionName Business function name
     * @return optional of matching business function, empty if not found
     */
    public Optional<BusinessFunction> getBusinessFunctionByFunctionName(String functionName) {
        return Optional.ofNullable(businessFunctionByNameMapping.get(functionName));
    }

    /**
     * For specified function id and privilege with return applicable function privilege id.
     *
     * @param functionId Business function id
     * @param privileges List of privileges
     * @return list of applicable function privileges id for function id / privilege pair
     */
    public List<String> getByFunctionAndPrivilege(String functionId, Collection<String> privileges) {
        log.info("Getting applicable function privileges");
        return businessFunctionIdPrivilegeListMapping.get(functionId).stream()
            .filter(item -> privileges.contains(item.getPrivilege().getName()))
            .map(ApplicableFunctionPrivilege::getId)
            .collect(Collectors.toList());
    }

    /**
     * For specified ids will return the corresponding objects.
     *
     * @param ids collection of applicable function privileges ids
     * @return set of {@link ApplicableFunctionPrivilege} for specified ids
     */
    public Set<ApplicableFunctionPrivilege> getApplicableFunctionPrivileges(Collection<String> ids) {
        return applicableFunctionPrivilegeMap.entrySet().stream()
            .filter(entity -> ids.contains(entity.getKey()))
            .map(Entry::getValue)
            .collect(Collectors.toSet());
    }

    /**
     * Gets all applicable function privileges.
     *
     * @return list of {@link ApplicableFunctionPrivilege}
     */
    public List<ApplicableFunctionPrivilege> getAllApplicableFunctionPrivileges() {
        return new ArrayList<>(applicableFunctionPrivilegeMap.values());
    }


    /**
     * Retrieves ApplicableFunctionPrivilege by name and function group.
     *
     * @param businessFunctionId     id of business function
     * @param assignedPrivilegeNames name of privilege
     * @return List of {@link ApplicableFunctionPrivilege}
     */
    public List<ApplicableFunctionPrivilege> findAllByBusinessFunctionIdAndPrivilegeNameIn(
        String businessFunctionId, List<String> assignedPrivilegeNames) {
        return businessFunctionIdPrivilegeListMapping.get(businessFunctionId).stream()
            .filter(e -> assignedPrivilegeNames.contains(e.getPrivilegeName()))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves ApplicableFunctionPrivilege by business function id and privilege name.
     *
     * @param functionId - business function id
     * @param privilege  - privilege name
     * @return {@link ApplicableFunctionPrivilege}
     */
    public ApplicableFunctionPrivilege getByFunctionIdAndPrivilege(String functionId, String privilege) {

        return findAllByBusinessFunctionIdAndPrivilegeNameIn(functionId, Lists.newArrayList(privilege))
            .stream().findFirst().orElse(null);
    }

    /**
     * Get ApplicableFunctionPrivilege by id.
     *
     * @param id of applicable function privilege
     * @return {@link ApplicableFunctionPrivilege}
     */
    public ApplicableFunctionPrivilege getApplicableFunctionPrivilegeById(String id) {
        return applicableFunctionPrivilegeMap.get(id);
    }

    /**
     * Get ApplicableFunctionPrivilege by business function id.
     *
     * @param id of applicable function privilege
     * @return list of  {@link ApplicableFunctionPrivilege}
     */
    public List<ApplicableFunctionPrivilege> getApplicableFunctionPrivilegeByBusinessFunctionId(String id) {
        return getAllApplicableFunctionPrivileges().stream()
            .filter(e -> e.getBusinessFunction().getId().equals(id))
            .collect(Collectors.toList());
    }

    /**
     * For specified function name and resource name and privileges will return applicable function privilege ids.
     *
     * @param functionName Business function name
     * @param resourceName Business resource name
     * @param privileges   List of privileges
     * @return set of applicable function privileges ids
     */
    public Set<String> getByFunctionNameOrResourceNameOrPrivilegesOptional(String functionName, String resourceName,
        Collection<String> privileges) {
        Stream<ApplicableFunctionPrivilege> result = applicableFunctionPrivilegeMap.values().stream();

        if (nonNull(functionName)) {
            result = result.filter(i -> i.getBusinessFunction().getFunctionName().equals(functionName));
        }

        if (nonNull(resourceName)) {
            result = result.filter(i -> i.getBusinessFunction().getResourceName().equals(resourceName));
        }

        if (CollectionUtils.isNotEmpty(privileges)) {
            result = result.filter(i -> privileges.contains(i.getPrivilege().getName()));
        }
        return result.map(ApplicableFunctionPrivilege::getId)
            .collect(Collectors.toSet());
    }

    /**
     * For specified function names, resource name and privileges will return applicable function privilege ids.
     *
     * @param functionNames Business function names
     * @param resourceName  Business resource name
     * @param privileges    List of privileges
     * @return set of applicable function privileges ids
     */
    public Set<String> getByFunctionNamesOrResourceNameOrPrivileges(Collection<String> functionNames,
        String resourceName, Collection<String> privileges) {

        Stream<ApplicableFunctionPrivilege> result = applicableFunctionPrivilegeMap.values().stream();

        if(CollectionUtils.isNotEmpty(functionNames)){
            result = result.filter(afp -> functionNames.contains(afp.getBusinessFunction().getFunctionName()));
        }

        if (nonNull(resourceName)) {
            result = result.filter(i -> i.getBusinessFunction().getResourceName().equals(resourceName));
        }

        if (CollectionUtils.isNotEmpty(privileges)) {
            result = result.filter(i -> privileges.contains(i.getPrivilege().getName()));
        }

        return result
            .map(ApplicableFunctionPrivilege::getId)
            .collect(Collectors.toSet());
    }
}
