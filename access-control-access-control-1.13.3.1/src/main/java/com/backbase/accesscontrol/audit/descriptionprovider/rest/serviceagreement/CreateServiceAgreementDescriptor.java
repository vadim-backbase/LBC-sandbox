package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static java.util.Objects.nonNull;

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
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class CreateServiceAgreementDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    /**
     * Autowire constructor.
     */
    public CreateServiceAgreementDescriptor(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementPostRequestBody request = getServiceAgreementPostRequestBody(joinPoint);
        return request.getParticipants().stream()
            .map(participant ->
                createSuccessMessage(participant, request, Status.INITIATED))
            .collect(Collectors.toList());
    }

    protected ServiceAgreementPostRequestBody getServiceAgreementPostRequestBody(ProceedingJoinPoint joinPoint) {
        return DescriptorUtils.getArgument(joinPoint, ServiceAgreementPostRequestBody.class);
    }

    protected String getDescription(String name, Status status) {
        return EventAction.CREATE.getActionEvent() + " | Service Agreement | " + status
            + " | name " + name;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        ServiceAgreementPostRequestBody request = getServiceAgreementPostRequestBody(joinPoint);
        ServiceAgreementPostResponseBody responseBody = (ServiceAgreementPostResponseBody) actionResult;
        return request.getParticipants().stream()
            .map(participant -> {
                AuditMessage successMessage = createSuccessMessage(participant, request, Status.SUCCESSFUL);
                successMessage.withEventMetaDatum(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, responseBody.getId());
                return successMessage;
            })
            .collect(Collectors.toList());
    }

    protected AuditMessage createSuccessMessage(Participant participant, ServiceAgreementPostRequestBody request,
        Status status) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(DescriptorFieldNames.MASTER_SERVICE_AGREEMENT_FIELD_NAME, "false");
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_NAME_FIELD_NAME, request.getName());
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, request.getDescription());
        metaData.put(DescriptorFieldNames.PARTICIPANT_ID_FIELD_NAME, participant.getId());
        if (Objects.nonNull(participant.getSharingUsers())) {
            metaData.put(DescriptorFieldNames.PARTICIPANT_SHARING_USERS_FIELD_NAME, String.valueOf(participant.getSharingUsers()));
        }
        if (Objects.nonNull(participant.getSharingAccounts())) {
            metaData.put(DescriptorFieldNames.PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME, String.valueOf(participant.getSharingAccounts()));
        }
        if (nonNull(request.getStatus())) {
            metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_STATUS_FIELD_NAME, request.getStatus().toString());
        }
        metaData.put(DescriptorFieldNames.START_DATE_TIME_FIELD_NAME,
            dateTimeService.getStringDateTime(request.getValidFromDate(), request.getValidFromTime()));
        metaData.put(DescriptorFieldNames.END_DATE_TIME_FIELD_NAME,
            dateTimeService.getStringDateTime(request.getValidUntilDate(), request.getValidUntilTime()));
        return new AuditMessage()
            .withStatus(status)
            .withEventMetaData(metaData)
            .withEventDescription(getDescription(request.getName(), status));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementPostRequestBody request = getServiceAgreementPostRequestBody(joinPoint);
        return request.getParticipants().stream()
            .map(participant ->
                createSuccessMessage(participant, request, Status.FAILED))
            .collect(Collectors.toList());
    }
}