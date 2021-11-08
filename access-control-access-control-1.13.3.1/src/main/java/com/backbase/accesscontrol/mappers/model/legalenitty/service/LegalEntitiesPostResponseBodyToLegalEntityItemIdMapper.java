package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemId;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntitiesPostResponseBodyToLegalEntityItemIdMapper
    implements
    AbstractPayloadConverter<LegalEntitiesPostResponseBody, LegalEntityItemId> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntitiesPostResponseBody.class.getCanonicalName(),
            LegalEntityItemId.class.getCanonicalName());
    }
}
