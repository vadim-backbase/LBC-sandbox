package com.backbase.accesscontrol.mappers.model.query.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ParticipantToParticipantMapper
    implements
    AbstractPayloadConverter<Participant, ServiceAgreementParticipantsGetResponseBody> {

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(Participant.class.getCanonicalName(), ServiceAgreementParticipantsGetResponseBody.class
            .getCanonicalName());
    }
}
