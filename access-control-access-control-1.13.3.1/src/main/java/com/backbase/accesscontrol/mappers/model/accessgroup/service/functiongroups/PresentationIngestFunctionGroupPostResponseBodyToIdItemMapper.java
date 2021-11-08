package com.backbase.accesscontrol.mappers.model.accessgroup.service.functiongroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationIngestFunctionGroupPostResponseBodyToIdItemMapper implements
    AbstractPayloadConverter<PresentationIngestFunctionGroupPostResponseBody, IdItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationIngestFunctionGroupPostResponseBody.class.getCanonicalName(),
            IdItem.class.getCanonicalName());
    }
}
