package com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationDataGroupItemPutRequestBodyMapper implements
    AbstractPayloadConverter<com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupItemPutRequestBody, PresentationDataGroupItemPutRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupItemPutRequestBody.class
                .getCanonicalName(),
            PresentationDataGroupItemPutRequestBody.class.getCanonicalName());
    }
}
