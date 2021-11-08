package com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationSearchDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationSearchDataGroupRequestMapper implements
    AbstractPayloadConverter<PresentationSearchDataGroupsRequest, PresentationGetDataGroupsRequest> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(PresentationSearchDataGroupsRequest.class.getCanonicalName(),
            PresentationGetDataGroupsRequest.class.getCanonicalName());
    }
}
