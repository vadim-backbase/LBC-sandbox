package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityUpdateItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityUpdateItemToLegalEntityByExternalIdPutRequestBodyMapper
    implements AbstractPayloadConverter<LegalEntityUpdateItem, LegalEntityByExternalIdPutRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityUpdateItem.class.getCanonicalName(),
            LegalEntityByExternalIdPutRequestBody.class.getCanonicalName());
    }
}