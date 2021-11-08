package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PrivilegesGetResponseBodyToPrivilegesGetResponseBodyMapper
    implements
    AbstractPayloadConverter<PrivilegesGetResponseBody,
        com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PrivilegesGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody.class
                .getCanonicalName());
    }
}
