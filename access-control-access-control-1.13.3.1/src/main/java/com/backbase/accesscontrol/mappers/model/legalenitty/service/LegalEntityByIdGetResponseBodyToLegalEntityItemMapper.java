package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityByIdGetResponseBodyToLegalEntityItemMapper
    implements AbstractPayloadConverter<LegalEntityByIdGetResponseBody, LegalEntityItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityByIdGetResponseBody.class.getCanonicalName(),
            LegalEntityItem.class.getCanonicalName());
    }
}
