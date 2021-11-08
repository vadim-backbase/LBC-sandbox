package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.dto.PersistenceUserContextPermissionsApproval;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface PresentationFunctionDataGroupItemsPersistenceUserContextPermissionsApprovalMapper {

    @Mapping(target = "permissions", source = "items")
    PersistenceUserContextPermissionsApproval map(PresentationFunctionDataGroupItems presentationFunctionDataGroupItems);

    default Set<String> getDataGroupIds(List<PresentationGenericObjectId> dataGroupIds) {
        return dataGroupIds
            .stream()
            .filter(dataGroup -> dataGroup != null && dataGroup.getId() != null)
            .map(PresentationGenericObjectId::getId)
            .collect(Collectors.toSet());
    }
}
