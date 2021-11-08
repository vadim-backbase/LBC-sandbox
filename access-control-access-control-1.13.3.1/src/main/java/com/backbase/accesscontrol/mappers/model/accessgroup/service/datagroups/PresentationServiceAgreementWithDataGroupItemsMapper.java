package com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementWithDataGroupsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationServiceAgreementWithDataGroupItemsMapper implements
    AbstractPayloadConverter<PresentationServiceAgreementWithDataGroups, PresentationServiceAgreementWithDataGroupsItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            PresentationServiceAgreementWithDataGroups.class.getCanonicalName(),
            PresentationServiceAgreementWithDataGroupsItem.class.getCanonicalName());
    }
}
