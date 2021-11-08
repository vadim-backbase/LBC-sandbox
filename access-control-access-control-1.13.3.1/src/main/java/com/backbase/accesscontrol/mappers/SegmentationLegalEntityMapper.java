package com.backbase.accesscontrol.mappers;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SegmentationLegalEntityMapper {

    List<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody> toPresentation(
        List<SegmentationGetResponseBodyQuery> segmentationGetResponseBodyList);
}
