package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ResponseItemMapper
    implements
    AbstractPayloadConverter<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem, BatchResponseItem> {

    public abstract BatchResponseItem.StatusEnum convertStatus(
        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseStatusCode input);

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem.class.getCanonicalName(),
            BatchResponseItem.class.getCanonicalName());
    }
}
