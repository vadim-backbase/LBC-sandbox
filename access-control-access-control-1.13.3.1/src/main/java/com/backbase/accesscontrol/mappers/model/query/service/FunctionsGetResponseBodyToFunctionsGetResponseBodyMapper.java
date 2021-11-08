package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FunctionsGetResponseBodyToFunctionsGetResponseBodyMapper
    implements AbstractPayloadConverter<FunctionsGetResponseBody,
    com.backbase.accesscontrol.service.rest.spec.model.FunctionsGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(FunctionsGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.FunctionsGetResponseBody.class.getCanonicalName());
    }
}
