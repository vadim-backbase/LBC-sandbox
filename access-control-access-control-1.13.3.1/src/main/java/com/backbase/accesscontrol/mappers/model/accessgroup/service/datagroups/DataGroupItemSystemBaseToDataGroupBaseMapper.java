package com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupItemSystemBase;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public abstract class DataGroupItemSystemBaseToDataGroupBaseMapper implements
    AbstractPayloadConverter<DataGroupItemSystemBase, DataGroupBase> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            DataGroupItemSystemBase.class.getCanonicalName(),
            DataGroupBase.class.getCanonicalName());
    }
}
