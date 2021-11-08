package com.backbase.accesscontrol.mappers.model.accessgroup.service.aps;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationPermissionSetPutToPresentationPermissionSetItemPutMapper implements
    AbstractPayloadConverter<PresentationPermissionSetPut, PresentationPermissionSetItemPut> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationPermissionSetPut.class.getCanonicalName(),
            PresentationPermissionSetItemPut.class.getCanonicalName());
    }
}
