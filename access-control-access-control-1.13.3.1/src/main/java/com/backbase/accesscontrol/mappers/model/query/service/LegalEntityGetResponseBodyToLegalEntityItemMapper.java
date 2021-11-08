package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityGetResponseBodyToLegalEntityItemMapper
    implements AbstractPayloadConverter<LegalEntityGetResponseBody, LegalEntityItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityGetResponseBody.class.getCanonicalName(),
            LegalEntityItem.class.getCanonicalName());
    }
}
