package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ArrangementPrivilegesGetResponseBodyQueryConverter
    implements
    AbstractPayloadConverter<ArrangementPrivilegesGetResponseBody,
        com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            ArrangementPrivilegesGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody.class
                .getCanonicalName());
    }
}
