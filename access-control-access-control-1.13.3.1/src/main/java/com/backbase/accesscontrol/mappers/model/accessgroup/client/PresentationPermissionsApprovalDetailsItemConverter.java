package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationPermissionsApprovalDetailsItemConverter
    implements
    AbstractPayloadConverter<
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem,
        com.backbase.accesscontrol.client.rest.spec.model.PresentationPermissionsApprovalDetailsItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem.class
                .getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.PresentationPermissionsApprovalDetailsItem.class
                .getCanonicalName());
    }
}
