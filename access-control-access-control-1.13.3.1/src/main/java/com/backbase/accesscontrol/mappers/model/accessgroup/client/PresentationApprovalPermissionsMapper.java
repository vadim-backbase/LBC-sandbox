package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationApprovalPermissionsMapper
    implements
    AbstractPayloadConverter<PresentationApprovalPermissions, com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalPermissions> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(PresentationApprovalPermissions.class.getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalPermissions.class.getCanonicalName());
    }
}
