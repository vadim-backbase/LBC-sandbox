package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementUsersQuery;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementUsersGetResponseBodyToServiceAgreementUsersMapper
    implements AbstractPayloadConverter<ServiceAgreementUsersGetResponseBody, ServiceAgreementUsersQuery> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(ServiceAgreementUsersGetResponseBody.class.getCanonicalName(),
            ServiceAgreementUsersQuery.class.getCanonicalName());
    }
}
