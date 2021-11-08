package com.backbase.accesscontrol.mappers.model.legalenitty.client;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityItemId;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityByIdGetResponseBodyToLegalEntityItemIdMapper
    implements AbstractPayloadConverter<LegalEntityByIdGetResponseBody, LegalEntityItemId> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityByIdGetResponseBody.class.getCanonicalName(),
            LegalEntityItemId.class.getCanonicalName());
    }
}
