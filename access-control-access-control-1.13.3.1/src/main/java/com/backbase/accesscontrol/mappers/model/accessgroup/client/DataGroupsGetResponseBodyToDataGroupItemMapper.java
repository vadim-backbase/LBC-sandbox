package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DataGroupsGetResponseBodyToDataGroupItemMapper
    implements
    AbstractPayloadConverter<DataGroupsGetResponseBody
        , com.backbase.accesscontrol.client.rest.spec.model.DataGroupItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            DataGroupsGetResponseBody.class
                .getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.DataGroupItem.class.getCanonicalName());
    }
}
