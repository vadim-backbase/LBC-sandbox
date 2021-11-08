package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.ListServiceAgreements;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreements;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PersistenceServiceAgreementsToListServiceAgreementsMapper
    implements AbstractPayloadConverter<PersistenceServiceAgreements, ListServiceAgreements> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(PersistenceServiceAgreements.class.getCanonicalName(),
            ListServiceAgreements.class.getCanonicalName());
    }
}
