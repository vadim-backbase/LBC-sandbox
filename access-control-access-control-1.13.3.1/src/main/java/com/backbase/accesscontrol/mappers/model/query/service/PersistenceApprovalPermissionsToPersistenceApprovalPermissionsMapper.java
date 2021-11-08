package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PersistenceApprovalPermissionsToPersistenceApprovalPermissionsMapper
    implements AbstractPayloadConverter<PersistenceApprovalPermissions,
    com.backbase.accesscontrol.service.rest.spec.model.PersistenceApprovalPermissions> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(PersistenceApprovalPermissions.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.PersistenceApprovalPermissions.class.getCanonicalName());
    }
}
