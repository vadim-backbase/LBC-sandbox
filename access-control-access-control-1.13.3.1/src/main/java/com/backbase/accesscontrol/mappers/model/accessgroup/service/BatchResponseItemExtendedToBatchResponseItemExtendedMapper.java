package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BatchResponseItemExtendedToBatchResponseItemExtendedMapper
    implements
    AbstractPayloadConverter<BatchResponseItemExtended,
        com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            BatchResponseItemExtended.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.class
                .getCanonicalName());
    }
}
