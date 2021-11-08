package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupsIds;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.BulkSearchDataGroupsPostRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DataGroupsIdsToBulkSearchDataGroupsPostRequestBodyMapper
    implements AbstractPayloadConverter<DataGroupsIds, BulkSearchDataGroupsPostRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(DataGroupsIds.class.getCanonicalName(),
            BulkSearchDataGroupsPostRequestBody.class.getCanonicalName());
    }
}
