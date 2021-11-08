package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserContextServiceAgreementsGetResponseBodyToServiceagreementpartialitemConverter
    implements
    AbstractPayloadConverter<UserContextServiceAgreementsGetResponseBody,
        com.backbase.accesscontrol.client.rest.spec.model.Serviceagreementpartialitem> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            UserContextServiceAgreementsGetResponseBody.class
                .getCanonicalName(),
            com.backbase.accesscontrol.client.rest.spec.model.Serviceagreementpartialitem.class
                .getCanonicalName());
    }
}
