package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementByPermissionSet;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceAgreementByPermissionSetMapper {

    /**
     * Mapper.
     *
     * @param serviceAgreements list of {@link ServiceAgreement}
     * @return list of {@link ServiceAgreementByPermissionSet}
     */
    List<ServiceAgreementByPermissionSet> sourceToDestination(List<ServiceAgreement> serviceAgreements);

    /**
     * Mapper.
     *
     * @param serviceAgreements {@link ServiceAgreement}
     * @return {@link ServiceAgreementByPermissionSet}
     */
    @Mapping(target = "isMaster", source = "master")
    @Mapping(target = "regularUserAps",
        expression = "java(getApsIds(serviceAgreements.getPermissionSetsRegular()))")
    @Mapping(target = "adminUserAps", expression = "java(getApsIds(serviceAgreements.getPermissionSetsAdmin()))")
    ServiceAgreementByPermissionSet sourceToDestination(ServiceAgreement serviceAgreements);

    /**
     * Converts permission sets to sets of ids.
     *
     * @param permissionSets Set of permissions sets.
     * @return set of ids of provided permission sets.
     */
    default Set<BigDecimal> getApsIds(Set<AssignablePermissionSet> permissionSets) {
        return permissionSets.stream()
            .map(permissionSet -> new BigDecimal(permissionSet.getId()))
            .collect(Collectors.toSet());
    }

}
