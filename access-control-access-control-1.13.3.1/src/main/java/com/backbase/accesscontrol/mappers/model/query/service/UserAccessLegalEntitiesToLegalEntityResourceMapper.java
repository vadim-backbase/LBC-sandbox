package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.UserAccessLegalEntities;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.LegalEntityResource;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserAccessLegalEntitiesToLegalEntityResourceMapper
    implements AbstractPayloadConverter<UserAccessLegalEntities, LegalEntityResource> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(UserAccessLegalEntities.class.getCanonicalName(),
            LegalEntityResource.class.getCanonicalName());
    }
}
