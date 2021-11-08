package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntitiesBatchDelete;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DeleteLegalEntityBatchDescriptor extends AbstractDescriptionProvider {

    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String ERROR_CODE = "Error code";
    private static final String ERROR_MESSAGE = "Error message";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.LEGAL_ENTITY_BATCH)
            .withEventAction(EventAction.DELETE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<AuditMessage> auditMessages = new ArrayList<>();

        LegalEntitiesBatchDelete requests = DescriptorUtils.getArgument(joinPoint, LegalEntitiesBatchDelete.class);

        requests.getExternalIds().forEach(externalId -> {
            AuditMessage auditMessage = new AuditMessage()
                .withStatus(Status.INITIATED)
                .withEventDescription(getInitDescription(externalId))
                .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId);
            auditMessages.add(auditMessage);
        });
        return auditMessages;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<BatchResponseItem> responseData = ((ResponseEntity<List<BatchResponseItem>>) actionResult).getBody();
        List<AuditMessage> auditMessages = new ArrayList<>();

        for (BatchResponseItem aResponseData : responseData) {
            if (!aResponseData.getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                AuditMessage failedEventDatumLegalEntity = createFailedEventDatumLegalEntity(
                    aResponseData.getResourceId(), aResponseData);
                auditMessages.add(failedEventDatumLegalEntity);
            } else {
                AuditMessage successEventDatumLegalEntity = createSuccessEventDatumLegalEntity(
                    aResponseData.getResourceId());
                auditMessages.add(successEventDatumLegalEntity);
            }
        }
        return auditMessages;
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        LegalEntitiesBatchDelete requests = DescriptorUtils.getArgument(joinPoint, LegalEntitiesBatchDelete.class);
        int size = requests.getExternalIds().size();

        List<String> messageIds = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            messageIds.add(UUID.randomUUID().toString());
        }

        return messageIds;
    }

    private AuditMessage createSuccessEventDatumLegalEntity(String externalId) {
        return new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getSuccessDescription(externalId))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId);
    }

    private AuditMessage createFailedEventDatumLegalEntity(String externalId, BatchResponseItem batchResponseItem) {
        return new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getFailedDescription(externalId))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId)
            .withEventMetaDatum(ERROR_CODE,
                Objects.nonNull(batchResponseItem.getStatus()) ? batchResponseItem.getStatus().toString() : "")
            .withEventMetaDatum(ERROR_MESSAGE,
                !batchResponseItem.getErrors().isEmpty() ? batchResponseItem.getErrors().get(0) : "");
    }

    private String getInitDescription(String externalId) {
        return getDescription(externalId, Status.INITIATED);
    }

    private String getSuccessDescription(String externalId) {
        return getDescription(externalId, Status.SUCCESSFUL);
    }

    private String getFailedDescription(String externalId) {
        return getDescription(externalId, Status.FAILED);
    }

    private String getDescription(String externalId, Status status) {
        return EventAction.DELETE.getActionEvent() + " | Legal Entity | " + status
            + " | external ID " + externalId;
    }
}
