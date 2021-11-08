package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.dto.IdentifierDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataGroupDomainMapper {

    @Mapping(target = "dataGroupId", source = "id")
    @Mapping(target = "id", ignore = true)
    ApprovalDataGroupDetails approvalDataGroupDetailsToApprovalDataGroupDetails(
        DataGroupByIdPutRequestBody dataGroupApprovalUpdate);

    IdentifierDto presentationToIdentifierDto(PresentationIdentifier dataGroupIdentifier);
}
