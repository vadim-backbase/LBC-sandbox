package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UpdateAdminsToAdminsPutRequestBodyConverter
    implements
    AbstractPayloadConverter<
        com.backbase.accesscontrol.client.rest.spec.model.UpdateAdmins,
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.client.rest.spec.model.UpdateAdmins.class
                .getCanonicalName(),
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody.class
                .getCanonicalName());
    }
}
