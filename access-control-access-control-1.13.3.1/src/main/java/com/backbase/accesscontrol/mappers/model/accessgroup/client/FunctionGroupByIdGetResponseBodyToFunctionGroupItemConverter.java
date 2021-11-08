package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FunctionGroupByIdGetResponseBodyToFunctionGroupItemConverter
    implements
    AbstractPayloadConverter<
        FunctionGroupByIdGetResponseBody,
        com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            FunctionGroupByIdGetResponseBody.class
                .getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItem.class
                .getCanonicalName());
    }
}
