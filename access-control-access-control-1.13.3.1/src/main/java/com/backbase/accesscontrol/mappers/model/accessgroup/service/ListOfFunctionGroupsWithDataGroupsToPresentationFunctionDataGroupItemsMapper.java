package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ListOfFunctionGroupsWithDataGroupsToPresentationFunctionDataGroupItemsMapper
    implements
    AbstractPayloadConverter<com.backbase.accesscontrol.service.rest.spec.model.ListOfFunctionGroupsWithDataGroups,
        PresentationFunctionDataGroupItems> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.service.rest.spec.model.ListOfFunctionGroupsWithDataGroups.class
                .getCanonicalName(),
            PresentationFunctionDataGroupItems.class.getCanonicalName());
    }
}
