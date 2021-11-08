package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public abstract class ServiceAgreementPutToServiceAgreementPutRequestBodyMapper
    implements AbstractPayloadConverter<com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementPut,
    ServiceAgreementPutRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementPut.class.getCanonicalName(),
            ServiceAgreementPutRequestBody.class.getCanonicalName());
    }
}
