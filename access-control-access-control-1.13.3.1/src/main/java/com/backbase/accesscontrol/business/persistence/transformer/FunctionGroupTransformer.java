package com.backbase.accesscontrol.business.persistence.transformer;


import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupBase;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FunctionGroupTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupTransformer.class);
    private final BusinessFunctionCache businessFunctionCache;

    /**
     * Transform Function Group from domain in appropriate Function Group response body.
     *
     * @param clazz         - class type to transform in
     * @param functionGroup - function group to be transformed
     * @param <T>           - type which extends {@link FunctionGroupBase}
     * @return specific type of function group
     */
    public <T extends FunctionGroupBase> T transformFunctionGroup(Class<T> clazz, FunctionGroup functionGroup) {
        T newInstance = createInstance(clazz);
        newInstance
            .withDescription(functionGroup.getDescription())
            .withName(functionGroup.getName())
            .withType(FunctionGroupBase.Type.fromValue(functionGroup.getType().toString()))
            .withServiceAgreementId(getServiceAgreementId(functionGroup))
            .withPermissions(this.getPermission(functionGroup))
            .withValidFrom(functionGroup.getStartDate())
            .withValidUntil(functionGroup.getEndDate());
        return newInstance;
    }

    private String getServiceAgreementId(FunctionGroup functionGroup) {
        return Optional.ofNullable(functionGroup.getServiceAgreement())
            .map(ServiceAgreement::getId)
            .orElse(null);
    }

    @SuppressWarnings("squid:S2139")
    private <T extends FunctionGroupBase> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            LOGGER.warn("Unable to create response body", e);
            throw getInternalServerErrorException(e.getMessage());
        }
    }

    /**
     * Gets list of permissions for function group.
     *
     * @param functionGroup - data object of type {@link FunctionGroup}
     * @return list of {@link Permission}
     */
    public List<Permission> getPermission(FunctionGroup functionGroup) {

        return businessFunctionCache.getApplicableFunctionPrivileges(
            functionGroup.getPermissionsStream()
                .map(GroupedFunctionPrivilege::getApplicableFunctionPrivilegeId)
                .collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.groupingBy(aFP -> aFP.getBusinessFunction().getId()))
            .entrySet().stream().map(function -> new Permission()
                .withFunctionId(function.getKey())
                .withAssignedPrivileges(getAssignedPrivileges(function.getValue()))
            ).collect(Collectors.toList());
    }

    private List<Privilege> getAssignedPrivileges(List<ApplicableFunctionPrivilege> applicableFunctionPrivileges) {
        return applicableFunctionPrivileges.stream()
            .map(applicableFunctionPrivilege -> new Privilege()
                .withPrivilege(applicableFunctionPrivilege.getPrivilege().getName()))
            .collect(Collectors.toList());
    }
}
