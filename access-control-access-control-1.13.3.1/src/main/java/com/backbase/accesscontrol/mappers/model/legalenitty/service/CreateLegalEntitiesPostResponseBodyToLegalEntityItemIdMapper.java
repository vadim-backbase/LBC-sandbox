package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemId;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CreateLegalEntitiesPostResponseBodyToLegalEntityItemIdMapper
    implements AbstractPayloadConverter<CreateLegalEntitiesPostResponseBody, LegalEntityItemId> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(CreateLegalEntitiesPostResponseBody.class.getCanonicalName(),
            LegalEntityItemId.class.getCanonicalName());
    }
}
