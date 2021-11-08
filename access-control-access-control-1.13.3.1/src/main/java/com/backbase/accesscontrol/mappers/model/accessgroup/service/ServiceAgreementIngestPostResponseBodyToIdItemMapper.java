package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementIngestPostResponseBodyToIdItemMapper
    implements
    AbstractPayloadConverter<ServiceAgreementIngestPostResponseBody,
        com.backbase.accesscontrol.service.rest.spec.model.IdItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            ServiceAgreementIngestPostResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.IdItem.class
                .getCanonicalName());
    }
}
