package com.backbase.accesscontrol.mappers.model.accessgroup.service.functiongroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationFunctionGroupPutRequestBodyMapper implements
    AbstractPayloadConverter<PresentationFunctionGroupPutRequestBody, com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationFunctionGroupPutRequestBody.class.getCanonicalName(),
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody.class
                .getCanonicalName());
    }
}
