package com.backbase.accesscontrol.mappers.model.legalenitty.client;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantCreateItem;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityAsParticipantCreateItemToLegalEntityAsParticipantPostRequestBodyMapper implements
                AbstractPayloadConverter<LegalEntityAsParticipantCreateItem, LegalEntityAsParticipantPostRequestBody> {
    
    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityAsParticipantCreateItem.class.getCanonicalName(),
                        LegalEntityAsParticipantPostRequestBody.class.getCanonicalName());
    }

}