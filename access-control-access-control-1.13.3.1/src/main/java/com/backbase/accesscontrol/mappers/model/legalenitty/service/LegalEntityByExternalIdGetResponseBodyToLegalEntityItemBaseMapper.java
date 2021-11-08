package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemBase;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityByExternalIdGetResponseBodyToLegalEntityItemBaseMapper
    implements AbstractPayloadConverter<LegalEntityByExternalIdGetResponseBody, LegalEntityItemBase> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityByExternalIdGetResponseBody.class.getCanonicalName(),
            LegalEntityItemBase.class.getCanonicalName());
    }
}
