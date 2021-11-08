package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.RootLegalEntityGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RootLegalEntityGetResponseBodyToLegalEntityItemBaseMapper
    implements AbstractPayloadConverter<RootLegalEntityGetResponseBody, LegalEntityItemBase> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(RootLegalEntityGetResponseBody.class.getCanonicalName(),
            LegalEntityItemBase.class.getCanonicalName());
    }
}
