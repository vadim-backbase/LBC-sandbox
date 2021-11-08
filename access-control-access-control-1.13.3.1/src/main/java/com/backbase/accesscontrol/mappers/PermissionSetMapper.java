package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetResponseItem;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

//TODO Add tests for other converting functions in PermissionSetMapperTest
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PermissionSetMapper {

    @Autowired
    protected BusinessFunctionCache businessFunctionCache;

    /**
     * Mapper.
     *
     * @param persistencePermissionSet {@link PresentationPermissionSet}
     * @return {@link AssignablePermissionSet}
     */
    public abstract AssignablePermissionSet toDbModel(PresentationPermissionSet persistencePermissionSet);

    /**
     * Get permissions mapper.
     *
     * @param permissions list of {@link PresentationPermissionSetItem}
     * @return set of strings names of the permissions
     */
    protected Set<String> getPermissions(List<PresentationPermissionSetItem> permissions) {
        return permissions.stream().flatMap(
            item -> businessFunctionCache.getByFunctionAndPrivilege(item.getFunctionId(), item.getPrivileges())
                .stream()).collect(Collectors.toSet());
    }

    /**
     * Mapper.
     *
     * @param list of {@link AssignablePermissionSet}
     * @return PresentationPermissionSetResponseItem
     */
    public abstract List<PresentationPermissionSetResponseItem> sourceToDestination(List<AssignablePermissionSet> list);

    /**
     * Mapper.
     *
     * @param applicableFunctionPrivilegeIds privilege ids
     * @return list of {@link PresentationPermissionItem}
     */
    protected List<PresentationPermissionItem> convertPermissionItems(Set<String> applicableFunctionPrivilegeIds) {
        return businessFunctionCache.getApplicableFunctionPrivileges(applicableFunctionPrivilegeIds).stream()
            .collect(Collectors.groupingBy(ApplicableFunctionPrivilege::getBusinessFunction, Collectors.toList()))
            .entrySet().stream().map(entity -> new PresentationPermissionItem()
                .withFunctionId(entity.getKey().getId())
                .withResourceName(entity.getKey().getResourceName())
                .withFunctionName(entity.getKey().getFunctionName())
                .withPrivileges(entity.getValue().stream().map(ApplicableFunctionPrivilege::getPrivilegeName)
                    .collect(Collectors.toList()))).collect(Collectors.toList());
    }

}
