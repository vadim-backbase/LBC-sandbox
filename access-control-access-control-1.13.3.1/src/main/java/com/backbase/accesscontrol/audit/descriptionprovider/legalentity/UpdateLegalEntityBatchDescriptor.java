package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.google.common.collect.Lists.reverse;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component

public class UpdateLegalEntityBatchDescriptor extends AbstractDescriptionProvider {

    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String PARENT_EXTERNAL_LEGAL_ENTITY_ID = "Parent External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";
    private static final String ERROR_CODE = "Error code";
    private static final String ERROR_MESSAGE = "Error message";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.LEGAL_ENTITY_BATCH)
            .withEventAction(EventAction.UPDATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<AuditMessage> auditMessages = new ArrayList<>();

        List<LegalEntityPut> requests = getArgument(joinPoint, List.class);

        requests.forEach(object -> {
            try {
                AuditMessage auditMessage = new AuditMessage()
                    .withStatus(Status.INITIATED)
                    .withEventDescription(getInitDescription(object))
                    .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, object.getLegalEntity().getExternalId())
                    .withEventMetaDatum(LEGAL_ENTITY_NAME, object.getLegalEntity().getName())
                    .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID,
                        object.getLegalEntity().getParentExternalId())
                    .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                        nonNull(object.getLegalEntity().getType()) ? object.getLegalEntity().getType().toString()
                            : "");
                auditMessages.add(auditMessage);

            } catch (Exception e) {
                auditMessages.add(new AuditMessage().withStatus(Status.INITIATED));
            }
        });

        return auditMessages;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {

        List<BatchResponseItem> responseData = ((ResponseEntity<List<BatchResponseItem>>) actionResult).getBody();
        List<AuditMessage> auditMessages = new ArrayList<>();

        List<LegalEntityPut> requests = getArgument(joinPoint, List.class);

        for (int i = responseData.size() - 1; i >= 0; i--) {
            try {
                if (!responseData.get(i).getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                    AuditMessage failedEventDatumLegalEntity = createFailedEventDatumLegalEntity(requests.get(i),
                        responseData.get(i));
                    auditMessages.add(failedEventDatumLegalEntity);
                } else {
                    AuditMessage successEventDatumLegalEntity = createSuccessEventDatumLegalEntity(requests.get(i));
                    auditMessages.add(successEventDatumLegalEntity);
                }
            } catch (Exception e) {
                auditMessages.add(new AuditMessage().withStatus(Status.FAILED));
            }
        }
        return reverse(auditMessages);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        List<LegalEntityPut> requests = getArgument(joinPoint, List.class);
        List<String> messageIds = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            messageIds.add(UUID.randomUUID().toString());
        }

        return messageIds;
    }

    private AuditMessage createSuccessEventDatumLegalEntity(LegalEntityPut legalEntityPut) {
        return new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getSuccessDescription(legalEntityPut))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntityPut.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntityPut.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntityPut.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntityPut.getLegalEntity().getType()) ? legalEntityPut.getLegalEntity().getType()
                    .toString() : "");
    }

    private AuditMessage createFailedEventDatumLegalEntity(LegalEntityPut legalEntityPut,
        BatchResponseItem responseData) {
        return new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getFailedDescription(legalEntityPut))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntityPut.getLegalEntity().getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntityPut.getLegalEntity().getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntityPut.getLegalEntity().getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntityPut.getLegalEntity().getType()) ? legalEntityPut.getLegalEntity().getType()
                    .toString() : "")
            .withEventMetaDatum(ERROR_CODE,
                Objects.nonNull(responseData.getStatus()) ? responseData.getStatus().toString() : "")
            .withEventMetaDatum(ERROR_MESSAGE,
                CollectionUtils.isNotEmpty(responseData.getErrors()) ? responseData.getErrors().get(0) : "");
    }

    private String getInitDescription(LegalEntityPut legalEntityPut) {
        return getDescription(legalEntityPut, Status.INITIATED);
    }

    private String getSuccessDescription(LegalEntityPut legalEntityPut) {
        return getDescription(legalEntityPut, Status.SUCCESSFUL);
    }

    private String getFailedDescription(LegalEntityPut legalEntityPut) {
        return getDescription(legalEntityPut, Status.FAILED);
    }

    private String getDescription(LegalEntityPut legalEntityPut, Status status) {
        return EventAction.UPDATE.getActionEvent() + " | Legal Entity | " + status
            + " | name " + legalEntityPut.getLegalEntity().getName()
            + ", type " + legalEntityPut.getLegalEntity().getType() + ", external ID "
            + legalEntityPut.getLegalEntity().getExternalId();
    }
}
