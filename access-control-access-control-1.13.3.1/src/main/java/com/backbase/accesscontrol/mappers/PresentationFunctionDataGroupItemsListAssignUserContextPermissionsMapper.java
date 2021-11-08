package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.dto.PersistentUserContextPermissionsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface PresentationFunctionDataGroupItemsListAssignUserContextPermissionsMapper {

    @Mapping(target = "userLegalEntityId", source = "userLegalEntityId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "permissions", source = "presentationFunctionDataGroupItems.items")
    PersistentUserContextPermissionsPutRequestBody map(PresentationFunctionDataGroupItems presentationFunctionDataGroupItems, String userLegalEntityId);

    default Set<String> getDataGroupIds(List<PresentationGenericObjectId> dataGroupIds) {
        return dataGroupIds
            .stream()
            .filter(dataGroup -> dataGroup != null && dataGroup.getId() != null)
            .map(PresentationGenericObjectId::getId)
            .collect(Collectors.toSet());
    }
}