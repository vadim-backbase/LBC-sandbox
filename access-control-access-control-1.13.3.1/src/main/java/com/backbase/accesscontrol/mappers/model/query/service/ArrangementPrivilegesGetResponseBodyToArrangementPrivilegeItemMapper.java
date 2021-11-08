package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegeItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ArrangementPrivilegesGetResponseBodyToArrangementPrivilegeItemMapper
    implements AbstractPayloadConverter<ArrangementPrivilegesGetResponseBody, ArrangementPrivilegeItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(ArrangementPrivilegesGetResponseBody.class.getCanonicalName(),
            ArrangementPrivilegeItem.class.getCanonicalName());
    }
}
