package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.UserAccessEntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserAccessEntitlementsResourceToEntitlementsResourceMapper
    implements AbstractPayloadConverter<UserAccessEntitlementsResource, EntitlementsResource> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(UserAccessEntitlementsResource.class.getCanonicalName(),
            EntitlementsResource.class.getCanonicalName());
    }
}
