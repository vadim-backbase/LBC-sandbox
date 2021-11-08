package com.backbase.accesscontrol.mappers.model.legalenitty.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityPutToLegalEntityPutMapper
    implements AbstractPayloadConverter<LegalEntityPut,
    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityPut.class.getCanonicalName(),
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut.class.getCanonicalName());
    }

    public abstract LegalEntity toLegalEntity(LegalEntityCreateItem legalEntityCreateItem);
}
