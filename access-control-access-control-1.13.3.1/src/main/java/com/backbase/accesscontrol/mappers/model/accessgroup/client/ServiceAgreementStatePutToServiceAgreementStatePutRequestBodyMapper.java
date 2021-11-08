package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementStatePut;
import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementStatePutRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementStatePutToServiceAgreementStatePutRequestBodyMapper
    implements
    AbstractPayloadConverter<ServiceAgreementStatePut,
        ServiceAgreementStatePutRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(ServiceAgreementStatePut.class
            .getCanonicalName(),
            ServiceAgreementStatePutRequestBody.class
                .getCanonicalName());
    }
}
