package com.backbase.accesscontrol.util;

import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_010;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ApprovalServiceAgreementUtil {

    private LegalEntityJpaRepository legalEntityJpaRepository;
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;

    public ApprovalServiceAgreementUtil(
        LegalEntityJpaRepository legalEntityJpaRepository,
        AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository) {
        this.legalEntityJpaRepository = legalEntityJpaRepository;
        this.assignablePermissionSetJpaRepository = assignablePermissionSetJpaRepository;
    }

    public ServiceAgreement transformApprovalServiceAgreementToServiceAgreement(
        ApprovalServiceAgreement approvalRequest) {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(approvalRequest.getServiceAgreementId());
        serviceAgreement
            .setCreatorLegalEntity(legalEntityJpaRepository.findById(approvalRequest.getCreatorLegalEntityId())
                .orElseThrow(() -> getNotFoundException(ERR_ACC_010.getErrorMessage(), ERR_ACC_010.getErrorCode())));
        serviceAgreement.setExternalId(approvalRequest.getExternalId());
        serviceAgreement.setDescription(approvalRequest.getDescription());
        serviceAgreement.setMaster(approvalRequest.isMaster());
        serviceAgreement.setName(approvalRequest.getName());
        serviceAgreement.setStartDate(approvalRequest.getStartDate());
        serviceAgreement.setEndDate(approvalRequest.getEndDate());
        serviceAgreement.setPermissionSetsAdmin(
            assignablePermissionSetJpaRepository.findAllByIdIn(approvalRequest.getPermissionSetsAdmin()));
        serviceAgreement.setPermissionSetsRegular(
            assignablePermissionSetJpaRepository.findAllByIdIn(approvalRequest.getPermissionSetsRegular()));
        serviceAgreement.setState(approvalRequest.getState());
        serviceAgreement.setAdditions(approvalRequest.getAdditions());
        serviceAgreement
            .addParticipant(transformParticipants(approvalRequest.getParticipants()));
        return serviceAgreement;
    }

    private List<Participant> transformParticipants(Set<ApprovalServiceAgreementParticipant> participants) {
        return participants.stream().map(p -> {
            Participant participant = new Participant();
            participant.setLegalEntity(legalEntityJpaRepository.findById(p.getLegalEntityId())
                .orElseThrow(() -> getNotFoundException(ERR_ACC_010.getErrorMessage(), ERR_ACC_010.getErrorCode())));
            participant.setShareAccounts(p.isShareAccounts());
            participant.setShareUsers(p.isShareUsers());
            p.getAdmins().forEach(participant::addAdmin);
            return participant;
        }).collect(Collectors.toList());
    }
}
