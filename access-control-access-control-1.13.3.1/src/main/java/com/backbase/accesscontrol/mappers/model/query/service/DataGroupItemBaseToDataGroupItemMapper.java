package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DataGroupItemBaseToDataGroupItemMapper
    implements AbstractPayloadConverter<DataGroupItemBase, DataGroupItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(DataGroupItemBase.class.getCanonicalName(), DataGroupItem.class.getCanonicalName());
    }
}
