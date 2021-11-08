package com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupUpdate;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationDataGroupUpdateToPresentationSingleDataGroupPutRequestBodyMapper implements
    AbstractPayloadConverter<PresentationDataGroupUpdate, PresentationSingleDataGroupPutRequestBody> {

    protected abstract PresentationItemIdentifier presentationItemIdentifierToPresentationItemIdentifier(
        com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier presentationItemIdentifier);

    protected List<PresentationItemIdentifier> presentationItemIdentifierListToPresentationItemIdentifierList(
        List<com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier> list) {
        if (list == null) {
            return new ArrayList<>();
        }

        List<PresentationItemIdentifier> list1 = new ArrayList<>(list.size());
        for (com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier presentationItemIdentifier : list) {
            list1.add(presentationItemIdentifierToPresentationItemIdentifier(presentationItemIdentifier));
        }

        return list1;
    }

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationDataGroupUpdate.class.getCanonicalName(),
            PresentationSingleDataGroupPutRequestBody.class.getCanonicalName());
    }
}
