package com.backbase.accesscontrol.mappers.model.accessgroup.service.aps;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationInternalIdResponseToPresentationIdMapper implements
    AbstractPayloadConverter<PresentationInternalIdResponse, PresentationId> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationInternalIdResponse.class.getCanonicalName(),
            PresentationId.class.getCanonicalName());
    }
}
