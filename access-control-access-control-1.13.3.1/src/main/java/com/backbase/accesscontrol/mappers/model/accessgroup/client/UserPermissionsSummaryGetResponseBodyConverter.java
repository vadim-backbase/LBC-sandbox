package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserPermissionsSummaryGetResponseBodyConverter
    implements
    AbstractPayloadConverter<UserPermissionsSummaryGetResponseBody, com.backbase.accesscontrol.client.rest.spec.model.UserPermissionsSummaryGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            UserPermissionsSummaryGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.UserPermissionsSummaryGetResponseBody.class
                .getCanonicalName());
    }
}
