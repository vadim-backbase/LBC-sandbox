package com.backbase.accesscontrol.mappers;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PersistenceApprovalPermissionsPresentationApprovalPermissionMapper {

    PresentationApprovalPermissions sourceToDestination(PersistenceApprovalPermissions persistenceApprovalPermissions);

    default List<PresentationGenericObjectId> map(List<String> value) {
        return value.stream().map(s -> new PresentationGenericObjectId().withId(s)).collect(Collectors.toList());
    }

}
