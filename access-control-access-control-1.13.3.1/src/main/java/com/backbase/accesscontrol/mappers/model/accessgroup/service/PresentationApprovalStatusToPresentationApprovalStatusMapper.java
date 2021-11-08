package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationApprovalStatusToPresentationApprovalStatusMapper
    implements
    AbstractPayloadConverter<PresentationApprovalStatus,
        com.backbase.accesscontrol.service.rest.spec.model.PresentationApprovalStatus> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationApprovalStatus.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.PresentationApprovalStatus.class
                .getCanonicalName());
    }
}
