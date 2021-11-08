package com.backbase.accesscontrol.mappers.model.accessgroup.service;

import com.backbase.accesscontrol.mappers.model.AbstractPayloadConverter;
import com.backbase.accesscontrol.mappers.model.ConverterKey;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PresentationParticipantBatchUpdateToPresentationParticipantsPutMapper
    implements
    AbstractPayloadConverter<com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantBatchUpdate,
        PresentationParticipantsPut> {

    @Mapping(source = "sharingUsers", target = "sharingUsers", defaultValue = "false")
    @Mapping(source = "sharingAccounts", target = "sharingAccounts", defaultValue = "false")
    protected abstract PresentationParticipantPutBody presentationParticipantPutBodyToPresentationParticipantPutBody(com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantPutBody presentationParticipantPutBody);

    protected List<PresentationParticipantPutBody> presentationParticipantPutBodyListToPresentationParticipantPutBodyList(List<com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantPutBody> list) {
        if ( list == null ) {
            return new ArrayList<>();
        }

        List<PresentationParticipantPutBody> list1 = new ArrayList<>( list.size() );
        for ( com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantPutBody presentationParticipantPutBody : list ) {
            list1.add( presentationParticipantPutBodyToPresentationParticipantPutBody( presentationParticipantPutBody ) );
        }

        return list1;
    }

    @Override
    public ConverterKey getConverterKey() {
        return new ConverterKey(
            com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantBatchUpdate.class
                .getCanonicalName(),
            PresentationParticipantsPut.class.getCanonicalName());
    }
}
