package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementExternalIdGetResponseBodyToServiceAgreementItemMapper
    implements
    AbstractPayloadConverter<ServiceAgreementExternalIdGetResponseBody,
        com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            ServiceAgreementExternalIdGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItem.class
                .getCanonicalName());
    }
}
