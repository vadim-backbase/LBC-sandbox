package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupDetails;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIds;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SearchDataGroupsMapper {

    PresentationServiceAgreementIds toPresentation(ServiceAgreement serviceAgreement);

    @Mapping(target = "additions", ignore = true)
    PresentationDataGroupDetails toPresentation(DataGroup dataGroup);
}
