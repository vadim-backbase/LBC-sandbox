package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ServiceAgreementUsersGetResponseBodyTServiceAgreementUsersItemMapper
    implements
    AbstractPayloadConverter<com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementUsersGetResponseBody, ServiceAgreementUsersGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementUsersGetResponseBody.class
                .getCanonicalName(),
            ServiceAgreementUsersGetResponseBody.class.getCanonicalName());
    }
}
