package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class EditServiceAgreementDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    /**
     * Autowire constructor.
     */
    public EditServiceAgreementDescriptor(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SAVE_SERVICE_AGREEMENT)
            .withEventAction(EventAction.UPDATE);
    }

    protected String getDescription(String name, Status status) {
        return EventAction.UPDATE.getActionEvent() + " | Service Agreement | " + status
            + " | name " + name;
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementSave request = DescriptorUtils.getArgument(joinPoint, ServiceAgreementSave.class);
        String serviceAgreementId = DescriptorUtils.getArgument(joinPoint, String.class, 0);

        return request.getParticipants().stream()
            .map(participant ->
                createMessage(participant, request, serviceAgreementId, Status.INITIATED))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        ServiceAgreementSave request = DescriptorUtils.getArgument(joinPoint, ServiceAgreementSave.class);
        String serviceAgreementId = DescriptorUtils.getArgument(joinPoint, String.class, 0);

        return request.getParticipants().stream()
            .map(participant -> createMessage(participant, request, serviceAgreementId,
                Status.SUCCESSFUL))
            .collect(Collectors.toList());
    }

    private AuditMessage createMessage(
        Participant participant, ServiceAgreementSave request, String serviceAgreementId, Status status) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, request.getExternalId());
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId);
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_NAME_FIELD_NAME, request.getName());
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, request.getDescription());
        metaData.put(DescriptorFieldNames.PARTICIPANT_ID_FIELD_NAME, participant.getId());
        metaData.put(DescriptorFieldNames.MASTER_SERVICE_AGREEMENT_FIELD_NAME,
            Objects.isNull(request.getIsMaster()) ? "false" : request.getIsMaster().toString());

        if (Objects.nonNull(request.getStatus())) {
            metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_STATUS_FIELD_NAME, request.getStatus().toString());
        }
        if (Objects.nonNull(participant.getSharingUsers())) {
            metaData.put(DescriptorFieldNames.PARTICIPANT_SHARING_USERS_FIELD_NAME, String.valueOf(participant.getSharingUsers()));
        }
        if (Objects.nonNull((participant.getSharingAccounts()))) {
            metaData.put(DescriptorFieldNames.PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME, String.valueOf(participant.getSharingAccounts()));
        }

        metaData.put(DescriptorFieldNames.START_DATE_TIME_FIELD_NAME,
            dateTimeService.getStringDateTime(request.getValidFromDate(), request.getValidFromTime()));
        metaData.put(DescriptorFieldNames.END_DATE_TIME_FIELD_NAME,
            dateTimeService.getStringDateTime(request.getValidUntilDate(), request.getValidUntilTime()));

        return new AuditMessage()
            .withStatus(status)
            .withEventDescription(getDescription(request.getName(), status))
            .withEventMetaData(metaData);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementSave request = DescriptorUtils.getArgument(joinPoint, ServiceAgreementSave.class);
        String serviceAgreementId = DescriptorUtils.getArgument(joinPoint, String.class, 0);

        return request.getParticipants().stream()
            .map(participant ->
                createMessage(participant, request, serviceAgreementId, Status.FAILED))
            .collect(Collectors.toList());
    }
}
