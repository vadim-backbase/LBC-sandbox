package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.GetContexts;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserContextsGetResponseBodyToGetContextMapper
    implements AbstractPayloadConverter<UserContextsGetResponseBody, GetContexts> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(UserContextsGetResponseBody.class.getCanonicalName(),
            GetContexts.class.getCanonicalName());
    }
}
