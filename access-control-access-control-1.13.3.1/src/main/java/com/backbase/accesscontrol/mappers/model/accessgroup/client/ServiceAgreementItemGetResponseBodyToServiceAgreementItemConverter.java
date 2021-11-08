package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementItemGetResponseBodyToServiceAgreementItemConverter
    implements
    AbstractPayloadConverter<
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody,
        com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementItem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody.class
                .getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementItem.class
                .getCanonicalName());
    }
}
