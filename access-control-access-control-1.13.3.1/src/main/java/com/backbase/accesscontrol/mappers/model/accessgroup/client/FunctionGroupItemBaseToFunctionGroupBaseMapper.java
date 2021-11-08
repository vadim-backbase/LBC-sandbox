package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItemBase;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FunctionGroupItemBaseToFunctionGroupBaseMapper
    implements
    AbstractPayloadConverter<FunctionGroupItemBase, FunctionGroupBase> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(FunctionGroupItemBase.class.getCanonicalName(),
            FunctionGroupBase.class.getCanonicalName());
    }
}