package com.backbase.accesscontrol.mappers.model.accessgroup.client;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UsersForServiceAgreementToPresentationUsersForServiceAgreementRequestBodyConverter
    implements
    AbstractPayloadConverter<
        com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement,
        PresentationUsersForServiceAgreementRequestBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement.class
                .getCanonicalName(), PresentationUsersForServiceAgreementRequestBody.class
            .getCanonicalName());
    }
}
