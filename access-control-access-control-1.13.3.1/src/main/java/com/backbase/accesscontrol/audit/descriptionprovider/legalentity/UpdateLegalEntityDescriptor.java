package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityUpdateItem;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdateLegalEntityDescriptor extends AbstractDescriptionProvider {

    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.LEGAL_ENTITY)
            .withEventAction(EventAction.UPDATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        LegalEntityUpdateItem legalEntityRequest = DescriptorUtils.getArgument(joinPoint, LegalEntityUpdateItem.class);
        String externalId = DescriptorUtils.getArgument(joinPoint, String.class);

        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(getInitDescription(externalId, legalEntityRequest))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId)
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntityRequest.getType()) ? legalEntityRequest.getType().toString() : "");
        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        LegalEntityUpdateItem legalEntityRequest = DescriptorUtils
            .getArgument(joinPoint, LegalEntityUpdateItem.class);
        String externalId = DescriptorUtils.getArgument(joinPoint, String.class);
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getSuccessDescription(externalId, legalEntityRequest))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId)
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntityRequest.getType()) ? legalEntityRequest.getType().toString() : "");
        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        LegalEntityUpdateItem legalEntityRequest = DescriptorUtils
            .getArgument(joinPoint, LegalEntityUpdateItem.class);
        String externalId = DescriptorUtils.getArgument(joinPoint, String.class);
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getFailedDescription(externalId, legalEntityRequest))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, externalId)
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntityRequest.getType()) ? legalEntityRequest.getType().toString() : "");
        return singletonList(auditMessage);
    }

    private String getInitDescription(String externalId, LegalEntityUpdateItem legalEntityByExternalIdPutRequestBody) {
        return getDescription(externalId, legalEntityByExternalIdPutRequestBody, Status.INITIATED);
    }

    private String getSuccessDescription(String externalId, LegalEntityUpdateItem legalEntityUpdateItem) {
        return getDescription(externalId, legalEntityUpdateItem, Status.SUCCESSFUL);
    }

    private String getFailedDescription(String externalId, LegalEntityUpdateItem legalEntityUpdateItem) {
        return getDescription(externalId, legalEntityUpdateItem, Status.FAILED);
    }

    private String getDescription(String externalId, LegalEntityUpdateItem legalEntityUpdateItem, Status status) {
        return EventAction.UPDATE.getActionEvent() + " | Legal Entity | " + status
            + " | type " + legalEntityUpdateItem.getType()
            + ", external ID " + externalId;
    }
}
