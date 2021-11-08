package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_073;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementAdmin;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementRefJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.ServiceAgreementQueryService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementState;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementStateLegalEntities;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServiceAgreementQueryServiceImpl implements ServiceAgreementQueryService {

    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private ApprovalServiceAgreementRefJpaRepository approvalServiceAgreementRefJpaRepository;
    private DateTimeService dateTimeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementQueryServiceImpl.class);

    @Override
    public ServiceAgreementApprovalDetailsItem getByApprovalId(String approvalId) {
        LOGGER.info("Trying to get approval details for service agreement with approval id {}", approvalId);
        ApprovalServiceAgreementRef approvalServiceAgreementRef = getApprovalServiceAgreement(approvalId);
        ServiceAgreement serviceAgreement = null;
        if (nonNull(approvalServiceAgreementRef.getServiceAgreementId())) {
            serviceAgreement = serviceAgreementJpaRepository
                .findById(approvalServiceAgreementRef.getServiceAgreementId(), GraphConstants
                    .SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR).orElseThrow(() -> {
                    LOGGER.warn("Service agreement with id {} does not exists.",
                        approvalServiceAgreementRef.getServiceAgreementId());
                    return getBadRequestException(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode());
                });
        }

        return createResult(serviceAgreement, approvalServiceAgreementRef,
            approvalServiceAgreementRef.getApprovalAction());
    }

    private ServiceAgreementApprovalDetailsItem createResult(ServiceAgreement serviceAgreement,
        ApprovalServiceAgreementRef approvalServiceAgreementRef,
        ApprovalAction approvalAction) {
        ServiceAgreementApprovalDetailsItem result = new ServiceAgreementApprovalDetailsItem();

        result.setServiceAgreementId(approvalServiceAgreementRef.getServiceAgreementId());
        result.setAction(PresentationApprovalAction.valueOf(approvalServiceAgreementRef.getApprovalAction().name()));
        result.setApprovalId(approvalServiceAgreementRef.getApprovalId());

        if (approvalAction != ApprovalAction.DELETE) {
            ApprovalServiceAgreement approvalServiceAgreement = (ApprovalServiceAgreement) approvalServiceAgreementRef;

            ServiceAgreementState newState = createNewState(approvalServiceAgreement);
            result.setNewState(newState);

        }

        ServiceAgreementState oldState = createOldState(serviceAgreement);
        result.setOldState(oldState);

        result.setAction(PresentationApprovalAction.valueOf(approvalAction.name()));
        return result;
    }

    private ServiceAgreementState createNewState(ApprovalServiceAgreement approvalServiceAgreement) {
        Set<String> admins = new HashSet<>();
        Set<ServiceAgreementStateLegalEntities> legalEntities = new HashSet<>();
        for (ApprovalServiceAgreementParticipant participant : approvalServiceAgreement.getParticipants()) {
            admins.addAll(participant.getAdmins());
            legalEntities.add(new ServiceAgreementStateLegalEntities()
                .withId(participant.getLegalEntityId()).withContributeAccount(participant.isShareAccounts())
                .withContributeUsers(participant.isShareUsers()).withId(participant.getLegalEntityId())
                .withExternalId(participant.getLegalEntity().getExternalId())
                .withName(participant.getLegalEntity().getName()));
        }

        return new ServiceAgreementState()
            .withName(approvalServiceAgreement.getName())
            .withDescription(approvalServiceAgreement.getDescription())
            .withAdmins(admins)
            .withLegalEntities(legalEntities)
            .withValidFromDate(dateTimeService.getStringDateFromDate(approvalServiceAgreement.getStartDate()))
            .withValidFromTime(dateTimeService.getStringTimeFromDate(approvalServiceAgreement.getStartDate()))
            .withValidUntilDate(dateTimeService.getStringDateFromDate(approvalServiceAgreement.getEndDate()))
            .withValidUntilTime(dateTimeService.getStringTimeFromDate(approvalServiceAgreement.getEndDate()));
    }


    private ServiceAgreementState createOldState(ServiceAgreement serviceAgreement) {
        if (serviceAgreement == null) {
            return null;
        }
        Set<String> admins = new HashSet<>();
        Set<ServiceAgreementStateLegalEntities> legalEntities = new HashSet<>();

        for (Participant participant : serviceAgreement.getParticipants().values()) {
            admins.addAll(participant.getAdmins().values().stream().map(ServiceAgreementAdmin::getUserId)
                .collect(Collectors.toSet()));
            legalEntities.add(new ServiceAgreementStateLegalEntities()
                .withId(participant.getLegalEntity().getId()).withContributeAccount(participant.isShareAccounts())
                .withContributeUsers(participant.isShareUsers())
                .withExternalId(participant.getLegalEntity().getExternalId())
                .withName(participant.getLegalEntity().getName()));
        }
        return new ServiceAgreementState()
            .withName(serviceAgreement.getName())
            .withDescription(serviceAgreement.getDescription())
            .withAdmins(admins)
            .withLegalEntities(legalEntities)
            .withValidFromDate(dateTimeService.getStringDateFromDate(serviceAgreement.getStartDate()))
            .withValidFromTime(dateTimeService.getStringTimeFromDate(serviceAgreement.getStartDate()))
            .withValidUntilDate(dateTimeService.getStringDateFromDate(serviceAgreement.getEndDate()))
            .withValidUntilTime(dateTimeService.getStringTimeFromDate(serviceAgreement.getEndDate()));
    }

    private ApprovalServiceAgreementRef getApprovalServiceAgreement(String approvalId) {
        return approvalServiceAgreementRefJpaRepository
            .findByApprovalId(approvalId)
            .orElseThrow(() -> {
                LOGGER.warn("Approval with id {} does not exist", approvalId);
                return getNotFoundException(ERR_ACQ_073.getErrorMessage(), ERR_ACQ_073.getErrorCode());
            });
    }
}
