package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ArrangementPrivilegesGetResponseBodyConverter
    implements
    AbstractPayloadConverter<ArrangementPrivilegesGetResponseBody, com.backbase.accesscontrol.client.rest.spec.model.ArrangementPrivilegesGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            ArrangementPrivilegesGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.ArrangementPrivilegesGetResponseBody.class
                .getCanonicalName());
    }
}
