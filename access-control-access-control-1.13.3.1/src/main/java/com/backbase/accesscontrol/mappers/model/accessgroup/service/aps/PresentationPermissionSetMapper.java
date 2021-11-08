package com.backbase.accesscontrol.mappers.model.accessgroup.service.aps;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItem;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationPermissionSetMapper implements
    AbstractPayloadConverter<com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSet, PresentationPermissionSet> {

    protected abstract PresentationPermissionSetItem presentationPermissionSetItemToPresentationPermissionSetItem(
        com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetItem presentationPermissionSetItem);

    protected List<PresentationPermissionSetItem> presentationPermissionSetItemListToPresentationPermissionSetItemList(
        List<com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetItem> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<PresentationPermissionSetItem> list1 = new ArrayList<>(list.size());
        for (com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetItem presentationPermissionSetItem : list) {
            list1.add(presentationPermissionSetItemToPresentationPermissionSetItem(presentationPermissionSetItem));
        }
        return list1;
    }

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSet.class.getCanonicalName(),
            PresentationPermissionSet.class.getCanonicalName());
    }
}
