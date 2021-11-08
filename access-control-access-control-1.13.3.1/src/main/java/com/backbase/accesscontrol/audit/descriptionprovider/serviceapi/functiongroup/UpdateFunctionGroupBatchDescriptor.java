package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.BUSINESS_FUNCTION_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PRIVILEGES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.google.common.collect.Lists.reverse;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionFunctionGroupUpdate;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UpdateFunctionGroupBatchDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    public UpdateFunctionGroupBatchDescriptor(
        DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP_BATCH)
            .withEventAction(EventAction.UPDATE);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        List<BatchResponseItemExtended> responses = ((ResponseEntity<List<BatchResponseItemExtended>>) actionResult)
            .getBody();
        List<PresentationFunctionGroupPutRequestBody> requests = getArgument(joinPoint, List.class);
        for (int i = responses.size() - 1; i >= 0; i--) {
            try {
                if (!responses.get(i).getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                    List<PresentationPermissionFunctionGroupUpdate> permissions = requests.get(i)
                        .getFunctionGroup().getPermissions();
                    AuditMessage failedEventDatumFunctionGroup = createFailedEventDatumFunctionGroup(requests.get(i),
                        responses.get(i));

                    auditMessages.addAll(
                        Collections.nCopies(permissions.isEmpty() ? 1 : permissions.size(),
                            failedEventDatumFunctionGroup)
                    );

                } else {
                    List<AuditMessage> successEventDatumFunctionGroup = createSuccessEventDatumFunctionGroup(
                        requests.get(i));
                    auditMessages.addAll(successEventDatumFunctionGroup);
                }
            } catch (Exception e) {
                auditMessages.add(new AuditMessage().withStatus(Status.FAILED));
            }
        }

        return reverse(auditMessages);
    }


    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        List<PresentationFunctionGroupPutRequestBody> requests = getArgument(joinPoint, List.class);

        requests.forEach(request -> {
            try {
                List<PresentationPermissionFunctionGroupUpdate> permissions = Optional
                    .of(request
                        .getFunctionGroup().getPermissions()).get();
                AuditMessage auditMessage = createBaseAuditMessage(request, Status.INITIATED);
                if (permissions.isEmpty()) {
                    auditMessages.add(auditMessage);
                } else {
                    permissions
                        .forEach(permission -> {
                            AuditMessage message = createBaseAuditMessage(request,
                                Status.INITIATED);
                            message
                                .withEventMetaDatum(BUSINESS_FUNCTION_NAME_FIELD_NAME, permission.getFunctionName())
                                .withEventMetaDatum(PRIVILEGES_FIELD_NAME, getPrivileges(permission));
                            auditMessages.add(message);
                        });

                }
            } catch (Exception e) {
                auditMessages.add(new AuditMessage().withStatus(Status.INITIATED));
            }
        });

        return auditMessages;
    }

    private List<AuditMessage> createSuccessEventDatumFunctionGroup(
        PresentationFunctionGroupPutRequestBody presentationFunctionGroupPutRequestBody) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        List<PresentationPermissionFunctionGroupUpdate> permissions = Optional
            .of(presentationFunctionGroupPutRequestBody
                .getFunctionGroup().getPermissions()).get();

        AuditMessage baseAuditMessage = createBaseAuditMessage(
            presentationFunctionGroupPutRequestBody, Status.SUCCESSFUL);

        if (permissions.isEmpty()) {
            auditMessages.add(baseAuditMessage);
        } else {
            permissions
                .forEach(permission -> {
                    AuditMessage message = createBaseAuditMessage(presentationFunctionGroupPutRequestBody,
                        Status.SUCCESSFUL);
                    message
                        .withEventMetaDatum(BUSINESS_FUNCTION_NAME_FIELD_NAME, permission.getFunctionName())
                        .withEventMetaDatum(PRIVILEGES_FIELD_NAME, getPrivileges(permission));
                    auditMessages.add(message);
                });
        }
        return auditMessages;

    }

    private AuditMessage createBaseAuditMessage(
        PresentationFunctionGroupPutRequestBody presentationFunctionGroupPutRequestBody, Status status) {
        try {
            AuditMessage auditMessage = new AuditMessage()
                .withEventDescription(
                    getDescription(presentationFunctionGroupPutRequestBody.getIdentifier(), status))
                .withStatus(status)
                .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME,
                    presentationFunctionGroupPutRequestBody.getFunctionGroup().getName())
                .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME,
                    presentationFunctionGroupPutRequestBody.getFunctionGroup().getDescription())
                .withEventMetaDatum(START_DATE_TIME_FIELD_NAME,
                    dateTimeService
                        .getStringDateTime(
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidFromDate(),
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidFromTime()))
                .withEventMetaDatum(END_DATE_TIME_FIELD_NAME,
                    dateTimeService
                        .getStringDateTime(
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidUntilDate(),
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidUntilTime()));
            setFunctionGroupIdentifier(auditMessage, presentationFunctionGroupPutRequestBody.getIdentifier());
            return auditMessage;
        } catch (Exception e) {
            return new AuditMessage().withStatus(status);
        }

    }

    private void setFunctionGroupIdentifier(AuditMessage auditMessage, PresentationIdentifier identifier) {
        if (Objects.nonNull(identifier.getNameIdentifier())) {
            auditMessage.withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, identifier.getNameIdentifier().getName())
                .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                    identifier.getNameIdentifier().getExternalServiceAgreementId());
        } else {
            auditMessage.withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, identifier.getIdIdentifier());
        }
    }

    private String getPrivileges(PresentationPermissionFunctionGroupUpdate permission) {
        return String.join(",", permission.getPrivileges());
    }

    private AuditMessage createFailedEventDatumFunctionGroup(
        PresentationFunctionGroupPutRequestBody presentationFunctionGroupPutRequestBody,
        BatchResponseItemExtended batchResponseItem) {
        try {
            AuditMessage auditMessage = new AuditMessage()
                .withEventDescription(
                    getDescription(presentationFunctionGroupPutRequestBody.getIdentifier(), Status.FAILED))
                .withStatus(Status.FAILED)
                .withEventMetaDatum(UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME,
                    presentationFunctionGroupPutRequestBody.getFunctionGroup().getName())
                .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME,
                    presentationFunctionGroupPutRequestBody.getFunctionGroup().getDescription())
                .withEventMetaDatum(START_DATE_TIME_FIELD_NAME,
                    dateTimeService
                        .getStringDateTime(
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidFromDate(),
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidFromTime()))
                .withEventMetaDatum(END_DATE_TIME_FIELD_NAME,
                    dateTimeService
                        .getStringDateTime(
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidUntilDate(),
                            presentationFunctionGroupPutRequestBody.getFunctionGroup().getValidUntilTime()))
                .withEventMetaDatum(ERROR_CODE, batchResponseItem.getStatus().toString())
                .withEventMetaDatum(ERROR_MESSAGE,
                    batchResponseItem.getErrors().isEmpty() ? "" : batchResponseItem.getErrors().get(0));
            setFunctionGroupIdentifier(auditMessage, presentationFunctionGroupPutRequestBody.getIdentifier());
            return auditMessage;

        } catch (Exception e) {
            return new AuditMessage().withStatus(Status.FAILED);
        }
    }

    private String getDescription(PresentationIdentifier identifier, Status status) {
        if (Objects.nonNull(identifier.getNameIdentifier())) {
            return EventAction.UPDATE.getActionEvent() + " | Function Group | " + status
                + " | name " + identifier.getNameIdentifier().getName()
                + ", external service agreement ID " + identifier.getNameIdentifier().getExternalServiceAgreementId();
        } else {
            return EventAction.UPDATE.getActionEvent() + " | Function Group | " + status
                + " | ID " + identifier.getIdIdentifier();
        }

    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        List<PresentationFunctionGroupPutRequestBody> requests = getArgument(joinPoint, List.class);
        List<String> messageIds = new ArrayList<>();
        for (PresentationFunctionGroupPutRequestBody request : requests) {
            String messageId = UUID.randomUUID().toString();
            List<PresentationPermissionFunctionGroupUpdate> permissions = Optional
                .of(request
                    .getFunctionGroup().getPermissions()).get();

            messageIds = Collections.nCopies(permissions.isEmpty() ? 1 : permissions.size(), messageId);

        }
        return messageIds;
    }
}
