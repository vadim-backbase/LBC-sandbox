package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.UserAccessServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ServiceAgreementResource;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserAccessServiceAgreementToEntitlementsResourceMapper
    implements AbstractPayloadConverter<UserAccessServiceAgreement, ServiceAgreementResource> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(UserAccessServiceAgreement.class.getCanonicalName(),
            ServiceAgreementResource.class.getCanonicalName());
    }
}
