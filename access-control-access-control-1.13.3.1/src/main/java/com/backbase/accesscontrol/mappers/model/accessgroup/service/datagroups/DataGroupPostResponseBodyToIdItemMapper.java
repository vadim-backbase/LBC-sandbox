package com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DataGroupPostResponseBodyToIdItemMapper implements
    AbstractPayloadConverter<DataGroupsPostResponseBody, IdItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            DataGroupsPostResponseBody.class.getCanonicalName(),
            IdItem.class.getCanonicalName());
    }
}
