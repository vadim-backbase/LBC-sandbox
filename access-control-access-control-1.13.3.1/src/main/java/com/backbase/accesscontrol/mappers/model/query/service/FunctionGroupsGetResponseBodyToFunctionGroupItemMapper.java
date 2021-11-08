package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.FunctionGroupItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FunctionGroupsGetResponseBodyToFunctionGroupItemMapper
    implements AbstractPayloadConverter<FunctionGroupsGetResponseBody, FunctionGroupItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(FunctionGroupsGetResponseBody.class.getCanonicalName(),
            FunctionGroupItem.class.getCanonicalName());
    }
}
