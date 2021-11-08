package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.dto.ServiceAgreementDto;
import com.backbase.accesscontrol.dto.ServiceAgreementParticipantDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceAgreementDtoMapper {

    ServiceAgreementDto fromServiceAgreementPostRequestBody(
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody);

    ServiceAgreementDto fromServiceAgreementSave(ServiceAgreementSave serviceAgreementSave);

    ServiceAgreementParticipantDto participantToServiceAgreementParticipantDto(Participant participant);

}
