package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.dto.ExtendedResponseItem;
import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseStatusCode;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BatchResponseItemMapper {

    List<BatchResponseItem> toLegalEntityPresentation(List<ResponseItem> item);

    default BatchResponseStatusCode toLegalEntityStatus(ItemStatusCode status) {
        return BatchResponseStatusCode.fromValue(status.toString());
    }

    List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem> toPresentation(
        List<ResponseItem> items);

    List<BatchResponseItemExtended> toExtendedPresentationList(List<ExtendedResponseItem> items);

    BatchResponseItemExtended toExtendedPresentation(ExtendedResponseItem item);

    default com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode toStatus(ItemStatusCode status) {
        return com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode.fromValue(status.toString());
    }
}
