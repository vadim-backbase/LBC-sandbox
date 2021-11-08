package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.EXTERNAL_PARTICIPANT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PARTICIPANT_SHARING_USERS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.google.common.collect.Lists.reverse;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationAction;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantBatchUpdate;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantPutBody;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class IngestUpdateServiceAgreementParticipantsDescriptor extends AbstractDescriptionProvider {

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_PARTICIPANTS_UPDATE)
            .withEventAction(EventAction.UPDATE_PARTICIPANTS);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        PresentationParticipantBatchUpdate presentationParticipantsPut = getArgument(joinPoint,
            PresentationParticipantBatchUpdate.class);
        List<PresentationParticipantPutBody> participants = presentationParticipantsPut.getParticipants();

        for (int i = participants.size() - 1; i >= 0; i--) {
            AuditMessage auditMessage = createInitEventDatumParticipantsFromServiceAgreement(participants.get(i));
            auditMessages.add(auditMessage);
        }
        return reverse(auditMessages);
    }


    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        List<BatchResponseItemExtended> responses = ((ResponseEntity<List<BatchResponseItemExtended>>) actionResult)
            .getBody();
        PresentationParticipantBatchUpdate presentationParticipantsPut = getArgument(joinPoint,
            PresentationParticipantBatchUpdate.class);
        List<PresentationParticipantPutBody> participants = presentationParticipantsPut.getParticipants();

        for (int i = responses.size() - 1; i >= 0; i--) {
            if (responses.get(i).getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                AuditMessage successEventDatumRemoveParticipantsFromServiceAgreement =
                    createSuccessEventDatumRemoveParticipantsFromServiceAgreement(
                        responses.get(i), participants.get(i));
                auditMessages.add(successEventDatumRemoveParticipantsFromServiceAgreement);
            } else {
                AuditMessage failedEventDatumRemoveParticipantsFromServiceAgreement =
                    createFailedEventDatumRemoveParticipantsFromServiceAgreement(
                        responses.get(i), participants.get(i));
                auditMessages.add(failedEventDatumRemoveParticipantsFromServiceAgreement);
            }
        }
        return reverse(auditMessages);
    }

    private AuditMessage createSuccessEventDatumRemoveParticipantsFromServiceAgreement(
        BatchResponseItemExtended responseItemExtended,
        PresentationParticipantPutBody participant) {
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getDescription(participant, Status.SUCCESSFUL));
        auditMessage
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                responseItemExtended.getExternalServiceAgreementId());
        auditMessage.withEventMetaDatum(
            StringUtils.capitalize(participant.getAction().toString()) + " " + EXTERNAL_PARTICIPANT_ID_FIELD_NAME,
            participant.getExternalParticipantId());
        if (participant.getAction() == PresentationAction.ADD) {
            if (nonNull(participant.getSharingUsers())) {
                auditMessage
                    .withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, participant.getSharingUsers().toString());
            }
            if (nonNull(participant.getSharingAccounts())) {
                auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME,
                    participant.getSharingAccounts().toString());
            }
        }

        return auditMessage;
    }

    private AuditMessage createInitEventDatumParticipantsFromServiceAgreement(
        PresentationParticipantPutBody participant) {
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(getDescription(participant, Status.INITIATED));
        auditMessage
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, participant.getExternalServiceAgreementId());
        auditMessage.withEventMetaDatum(
            StringUtils.capitalize(participant.getAction().toString()) + " " + EXTERNAL_PARTICIPANT_ID_FIELD_NAME,
            participant.getExternalParticipantId());
        if (participant.getAction() == PresentationAction.ADD) {
            if (nonNull(participant.getSharingUsers())) {
                auditMessage
                    .withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, participant.getSharingUsers().toString());
            }
            if (nonNull(participant.getSharingAccounts())) {
                auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME,
                    participant.getSharingAccounts().toString());
            }
        }

        return auditMessage;
    }


    private AuditMessage createFailedEventDatumRemoveParticipantsFromServiceAgreement(
        BatchResponseItemExtended batchResponseItemExtended, PresentationParticipantPutBody participant) {
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getDescription(participant, Status.FAILED));
        auditMessage
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                batchResponseItemExtended.getExternalServiceAgreementId());
        auditMessage.withEventMetaDatum(
            StringUtils.capitalize(participant.getAction().toString()) + " " + EXTERNAL_PARTICIPANT_ID_FIELD_NAME,
            participant.getExternalParticipantId());
        if (participant.getAction() == PresentationAction.ADD) {
            if (nonNull(participant.getSharingUsers())) {
                auditMessage
                    .withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, participant.getSharingUsers().toString());
            }
            if (nonNull(participant.getSharingAccounts())) {
                auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME,
                    participant.getSharingAccounts().toString());
            }
        }
        auditMessage.withEventMetaDatum(ERROR_CODE, batchResponseItemExtended.getStatus().toString());
        auditMessage.withEventMetaDatum(ERROR_MESSAGE,
            batchResponseItemExtended.getErrors().isEmpty() ? "" : batchResponseItemExtended.getErrors().get(0));
        return auditMessage;
    }

    private String getDescription(PresentationParticipantPutBody presentationParticipantPutBody, Status status) {
        return StringUtils.capitalize(presentationParticipantPutBody.getAction().toString())
            + " Participant | Service Agreement | " + status
            + " | external service agreement ID " + presentationParticipantPutBody.getExternalServiceAgreementId()
            + ", external participant ID " + presentationParticipantPutBody.getExternalParticipantId();
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        PresentationParticipantBatchUpdate presentationParticipantsPut = getArgument(joinPoint,
            PresentationParticipantBatchUpdate.class);
        List<PresentationParticipantPutBody> participants = presentationParticipantsPut.getParticipants();

        List<String> messageIds = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            messageIds.add(UUID.randomUUID().toString());

        }
        return messageIds;
    }

}
