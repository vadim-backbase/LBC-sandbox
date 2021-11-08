package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PersistenceUserDataItemPermissionToPersistenceUserDataItemPermissionMapper
    implements AbstractPayloadConverter<PersistenceUserDataItemPermission,
    com.backbase.accesscontrol.service.rest.spec.model.PersistenceUserDataItemPermission> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(PersistenceUserDataItemPermission.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.PersistenceUserDataItemPermission.class
                .getCanonicalName());
    }
}
