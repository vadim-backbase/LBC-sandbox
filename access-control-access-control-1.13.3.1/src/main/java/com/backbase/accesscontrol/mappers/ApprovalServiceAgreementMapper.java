package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ApprovalServiceAgreementMapper {

    @Autowired
    protected DateTimeService dateTimeService;

    /**
     * Creates approval service agreement from ServiceAgreementPostRequestBody.
     *
     * @param serviceAgreementPostRequestBody service agreement request body
     * @param creatorLegalEntityId            creator legal entity
     * @param approvalId                      approval id
     * @return {@link ApprovalServiceAgreement}
     */
    @Mapping(target = "startDate", expression =
        "java(dateTimeService.getStartDateFromDateAndTime("
            + "serviceAgreementPostRequestBody.getValidFromDate(),serviceAgreementPostRequestBody.getValidFromTime()))")
    @Mapping(target = "endDate", expression = "java(dateTimeService.getEndDateFromDateAndTime("
        + "serviceAgreementPostRequestBody.getValidUntilDate(), serviceAgreementPostRequestBody.getValidUntilTime()))")
    @Mapping(target = "master", constant = "false")
    @Mapping(target = "state", expression =
        "java(createStatusToServiceAgreementState(serviceAgreementPostRequestBody.getStatus()))")
    public abstract ApprovalServiceAgreement serviceAgreementPostRequestBodyToApprovalServiceAgreement(
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody,
        String creatorLegalEntityId, String approvalId);

    /**
     * Creates approval service agreement participant from Participant.
     *
     * @param participant service agreement participant
     * @return {@link ApprovalServiceAgreementParticipant}
     */
    @Mapping(target = "legalEntityId", source = "id")
    @Mapping(target = "shareUsers", source = "sharingUsers")
    @Mapping(target = "shareAccounts", source = "sharingAccounts")
    public abstract ApprovalServiceAgreementParticipant participantToApprovalServiceAgreementParticipant(
        Participant participant);

    /**
     * Creates service agreement state from create status.
     *
     * @param createStatus create status request
     * @return {@link ServiceAgreementState}
     */
    public ServiceAgreementState createStatusToServiceAgreementState(CreateStatus createStatus) {
        return ServiceAgreementState.fromString(createStatus.toString());
    }
}
