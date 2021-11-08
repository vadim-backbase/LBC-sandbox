package com.backbase.accesscontrol.mappers.model.legalenitty.client;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityItem;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityForUserGetResponseBodyToLegalEntityItemMapper
    implements AbstractPayloadConverter<LegalEntityForUserGetResponseBody, LegalEntityItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityForUserGetResponseBody.class.getCanonicalName(),
            LegalEntityItem.class.getCanonicalName());
    }
}
