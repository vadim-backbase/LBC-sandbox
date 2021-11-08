package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementAdmins;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementAdminsGetResponseBodyToServiceAgreementAdminsMapper
    implements AbstractPayloadConverter<ServiceAgreementAdminsGetResponseBody, ServiceAgreementAdmins> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(ServiceAgreementAdminsGetResponseBody.class.getCanonicalName(),
            ServiceAgreementAdmins.class.getCanonicalName());
    }
}
