package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItem;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SegmentationGetResponseBodyQueryToLegalEntityItemMapper
    implements AbstractPayloadConverter<SegmentationGetResponseBodyQuery, LegalEntityItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(SegmentationGetResponseBodyQuery.class.getCanonicalName(),
            LegalEntityItem.class.getCanonicalName());
    }
}
