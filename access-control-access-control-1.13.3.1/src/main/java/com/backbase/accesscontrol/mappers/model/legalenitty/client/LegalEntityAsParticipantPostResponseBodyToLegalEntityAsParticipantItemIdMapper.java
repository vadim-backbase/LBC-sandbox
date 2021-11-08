package com.backbase.accesscontrol.mappers.model.legalenitty.client;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantItemId;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
                nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
                nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public abstract class LegalEntityAsParticipantPostResponseBodyToLegalEntityAsParticipantItemIdMapper implements
                AbstractPayloadConverter<LegalEntityAsParticipantPostResponseBody, LegalEntityAsParticipantItemId> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityAsParticipantPostResponseBody.class.getCanonicalName(),
                        LegalEntityAsParticipantItemId.class.getCanonicalName());
    }

}
