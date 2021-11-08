package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ContextLegalEntities;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ContextLegalEntitiesToContextLegalEntitiesMapper
    implements AbstractPayloadConverter<ContextLegalEntities,
    com.backbase.accesscontrol.service.rest.spec.model.ContextLegalEntities> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(ContextLegalEntities.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.ContextLegalEntities.class.getCanonicalName());
    }
}
