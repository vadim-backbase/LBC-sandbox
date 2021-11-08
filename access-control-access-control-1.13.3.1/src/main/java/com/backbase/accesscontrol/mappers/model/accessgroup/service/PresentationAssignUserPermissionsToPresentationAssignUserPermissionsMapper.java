package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationAssignUserPermissionsToPresentationAssignUserPermissionsMapper
    implements
    AbstractPayloadConverter<com.backbase.accesscontrol.service.rest.spec.model.PresentationAssignUserPermissions,
        PresentationAssignUserPermissions> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.service.rest.spec.model.PresentationAssignUserPermissions.class
                .getCanonicalName(),
            PresentationAssignUserPermissions.class.getCanonicalName());
    }
}
