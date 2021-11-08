package com.backbase.accesscontrol.mappers.model.legalenitty.client;

import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityExternalDataItem;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LegalEntityExternalDataToLegalEntityExternalDataItemMapper
    implements AbstractPayloadConverter<LegalEntityExternalData, LegalEntityExternalDataItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(LegalEntityExternalData.class.getCanonicalName(),
            LegalEntityExternalDataItem.class.getCanonicalName());
    }
}
