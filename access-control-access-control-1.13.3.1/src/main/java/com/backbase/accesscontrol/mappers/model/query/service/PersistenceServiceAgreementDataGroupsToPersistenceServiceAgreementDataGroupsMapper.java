package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreementDataGroups;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PersistenceServiceAgreementDataGroupsToPersistenceServiceAgreementDataGroupsMapper
    implements AbstractPayloadConverter<PersistenceServiceAgreementDataGroups,
    com.backbase.accesscontrol.service.rest.spec.model.PersistenceServiceAgreementDataGroups> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(PersistenceServiceAgreementDataGroups.class.getCanonicalName(),
            com.backbase.accesscontrol.service.rest.spec.model.PersistenceServiceAgreementDataGroups.class
                .getCanonicalName());
    }
}
