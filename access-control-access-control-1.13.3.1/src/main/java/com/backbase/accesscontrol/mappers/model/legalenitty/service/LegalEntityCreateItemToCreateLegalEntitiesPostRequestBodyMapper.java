package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public abstract class LegalEntityCreateItemToCreateLegalEntitiesPostRequestBodyMapper
    implements AbstractPayloadConverter<LegalEntityCreateItem, CreateLegalEntitiesPostRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityCreateItem.class.getCanonicalName(),
            CreateLegalEntitiesPostRequestBody.class.getCanonicalName());
    }
}