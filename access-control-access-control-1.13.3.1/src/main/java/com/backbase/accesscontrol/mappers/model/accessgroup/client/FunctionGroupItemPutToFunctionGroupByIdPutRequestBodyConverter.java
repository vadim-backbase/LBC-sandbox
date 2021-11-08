package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FunctionGroupItemPutToFunctionGroupByIdPutRequestBodyConverter
    implements
    AbstractPayloadConverter<
        com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItemPut,
        FunctionGroupByIdPutRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItemPut.class
                .getCanonicalName(),
            FunctionGroupByIdPutRequestBody.class
                .getCanonicalName());
    }
}
