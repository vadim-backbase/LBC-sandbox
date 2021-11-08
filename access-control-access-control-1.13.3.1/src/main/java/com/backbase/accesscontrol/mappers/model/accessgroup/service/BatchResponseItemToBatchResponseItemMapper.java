package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BatchResponseItemToBatchResponseItemMapper
    implements
    AbstractPayloadConverter<BatchResponseItem,
        com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            BatchResponseItem.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.class
                .getCanonicalName());
    }
}
