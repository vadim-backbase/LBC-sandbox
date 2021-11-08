package com.backbase.accesscontrol.mappers.model.legalenitty.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SearchSubEntitiesParameters;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SearchSubEntitiesParametersToSearchSubEntitiesParametersMapper
    implements
    AbstractPayloadConverter<com.backbase.accesscontrol.client.rest.spec.model.SearchSubEntitiesParameters, SearchSubEntitiesParameters> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.client.rest.spec.model.SearchSubEntitiesParameters.class.getCanonicalName(),
            SearchSubEntitiesParameters.class.getCanonicalName());
    }
}
