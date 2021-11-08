package com.backbase.accesscontrol.mappers;

import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface DataGroupItemMapper {

    DataGroupBase convertFromBase(
        com.backbase.accesscontrol.client.rest.spec.model.DataGroupItemBase dataGroupItemBase);
}
