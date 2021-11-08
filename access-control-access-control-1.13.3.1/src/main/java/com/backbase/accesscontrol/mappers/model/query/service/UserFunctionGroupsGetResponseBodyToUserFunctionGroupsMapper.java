package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.UserFunctionGroups;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.UserFunctionGroupsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserFunctionGroupsGetResponseBodyToUserFunctionGroupsMapper
    implements AbstractPayloadConverter<UserFunctionGroupsGetResponseBody, UserFunctionGroups> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(UserFunctionGroupsGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.UserFunctionGroups.class.getCanonicalName());
    }
}
