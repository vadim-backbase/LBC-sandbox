package com.backbase.accesscontrol.mappers.model.accessgroup.service.functiongroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIngestFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationIngestFunctionGroupToPresentationFunctionGroupMapper implements
    AbstractPayloadConverter<PresentationIngestFunctionGroup, PresentationFunctionGroup> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationIngestFunctionGroup.class.getCanonicalName(),
            PresentationFunctionGroup.class.getCanonicalName());
    }
}
