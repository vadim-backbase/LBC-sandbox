package com.backbase.accesscontrol.mappers.model.legalenitty.client;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public abstract class LegalEntityCreateItemToPresentationCreateLegalEntityItemPostRequestBodyMapper
    implements AbstractPayloadConverter<LegalEntityCreateItem, PresentationCreateLegalEntityItemPostRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityCreateItem.class.getCanonicalName(),
            PresentationCreateLegalEntityItemPostRequestBody.class.getCanonicalName());
    }
}
