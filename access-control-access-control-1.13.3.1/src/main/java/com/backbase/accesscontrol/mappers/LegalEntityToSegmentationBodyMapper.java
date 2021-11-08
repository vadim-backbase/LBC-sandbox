package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LegalEntityToSegmentationBodyMapper {

    List<SegmentationGetResponseBodyQuery> sourceToDestination(List<LegalEntity> legalEntity);

    /**
     * Mapper.
     *
     * @param legalEntity {@link LegalEntity}
     * @return {@link SegmentationGetResponseBodyQuery}
     */
    @Mapping(target = "isParent", expression = "java(java.util.Objects.isNull(legalEntity.getParent()))")
    SegmentationGetResponseBodyQuery sourceToDestination(LegalEntity legalEntity);

}
