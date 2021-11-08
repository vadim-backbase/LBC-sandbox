package com.backbase.accesscontrol.mappers.model.accessgroup.service.aps;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetResponseItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationPermissionSetResponseItemMapper implements
    AbstractPayloadConverter<PresentationPermissionSetResponseItem, com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetResponseItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationPermissionSetResponseItem.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetResponseItem.class
                .getCanonicalName());
    }
}
