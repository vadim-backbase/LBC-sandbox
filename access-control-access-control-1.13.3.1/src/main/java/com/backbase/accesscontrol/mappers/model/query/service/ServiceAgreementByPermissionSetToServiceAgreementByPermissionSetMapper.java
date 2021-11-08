package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementByPermissionSet;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementByPermissionSetToServiceAgreementByPermissionSetMapper
    implements AbstractPayloadConverter<ServiceAgreementByPermissionSet,
    com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementByPermissionSet> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(ServiceAgreementByPermissionSet.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementByPermissionSet.class.getCanonicalName());
    }
}
