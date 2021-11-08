package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UserAssignedFunctionGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserAssignedFunctionGroupToUserAssignedFunctionGroupMapper implements
    AbstractPayloadConverter<UserAssignedFunctionGroupResponse, com.backbase.accesscontrol.service.rest.spec.model.UserAssignedFunctionGroupResponse> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(UserAssignedFunctionGroupResponse.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.UserAssignedFunctionGroupResponse.class.getCanonicalName());
    }
}
