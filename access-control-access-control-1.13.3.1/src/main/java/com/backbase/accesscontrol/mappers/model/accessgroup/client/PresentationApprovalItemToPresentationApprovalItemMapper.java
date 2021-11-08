package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationApprovalItemToPresentationApprovalItemMapper
    implements
    AbstractPayloadConverter<PresentationApprovalItem
        , com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationApprovalItem.class
                .getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalItem.class.getCanonicalName());
    }
}
