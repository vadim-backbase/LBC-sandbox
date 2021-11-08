package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationUserDataItemPermissionConverter
    implements
    AbstractPayloadConverter<PresentationUserDataItemPermission,
        com.backbase.accesscontrol.client.rest.spec.model.PresentationUserDataItemPermission> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationUserDataItemPermission.class.getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.PresentationUserDataItemPermission.class
                .getCanonicalName());
    }
}
