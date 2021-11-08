package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementItemToServiceAgreementItemQueryMapper
    implements AbstractPayloadConverter<ServiceAgreementItem,
    com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItemQuery> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(ServiceAgreementItem.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItemQuery.class.getCanonicalName());
    }
}
