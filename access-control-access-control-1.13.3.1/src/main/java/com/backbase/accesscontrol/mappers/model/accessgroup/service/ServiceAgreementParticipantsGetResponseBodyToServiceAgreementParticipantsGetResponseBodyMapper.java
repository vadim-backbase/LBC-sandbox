package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementParticipantsGetResponseBodyToServiceAgreementParticipantsGetResponseBodyMapper
    implements
    AbstractPayloadConverter<ServiceAgreementParticipantsGetResponseBody,
        com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            ServiceAgreementParticipantsGetResponseBody.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody.class
                .getCanonicalName());
    }
}
