package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersByPermissionsResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UsersByPermissionConverter
    implements
    AbstractPayloadConverter<UsersByPermissionsResponseBody,
        com.backbase.accesscontrol.client.rest.spec.model.UsersByPermission> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            UsersByPermissionsResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.UsersByPermission.class
                .getCanonicalName());
    }
}
