package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class GetUserToServiceAgreementUsersGetResponseBodyMapper
    implements
    AbstractPayloadConverter<GetUser, ServiceAgreementUsersGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.dbs.user.api.client.v2.model.GetUser.class
                .getCanonicalName(),
            ServiceAgreementUsersGetResponseBody.class.getCanonicalName());
    }
}
