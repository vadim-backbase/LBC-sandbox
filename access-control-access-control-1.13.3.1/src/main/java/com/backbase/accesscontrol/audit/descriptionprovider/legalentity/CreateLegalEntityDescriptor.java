package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemId;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CreateLegalEntityDescriptor extends AbstractDescriptionProvider {

    private static final String LEGAL_ENTITY_ID = "Legal Entity ID";
    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String PARENT_EXTERNAL_LEGAL_ENTITY_ID = "Parent External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.LEGAL_ENTITY)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        LegalEntityCreateItem legalEntitiesPostRequestBody = getArgument(
            joinPoint, LegalEntityCreateItem.class);
        return singletonList(new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(getInitDescription(legalEntitiesPostRequestBody))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntitiesPostRequestBody.getType()) ? legalEntitiesPostRequestBody.getType().toString()
                    : ""));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        LegalEntityCreateItem legalEntitiesPostRequestBody = getArgument(joinPoint, LegalEntityCreateItem.class);
        LegalEntityItemId legalEntitiesPostResponseBody = ((ResponseEntity<LegalEntityItemId>) actionResult)
            .getBody();

        return singletonList(new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getSuccessDescription(legalEntitiesPostRequestBody))
            .withEventMetaDatum(LEGAL_ENTITY_ID, legalEntitiesPostResponseBody.getId())
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntitiesPostRequestBody.getType()) ? legalEntitiesPostRequestBody.getType().toString()
                    : ""));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem legalEntitiesPostRequestBody = getArgument(
            joinPoint,
            com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem.class);
        return singletonList(new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getFailedDescription(legalEntitiesPostRequestBody))
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntitiesPostRequestBody.getType()) ? legalEntitiesPostRequestBody.getType().toString()
                    : ""));
    }

    private String getInitDescription(
        com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem legalEntitiesPostRequestBody) {
        return getDescription(legalEntitiesPostRequestBody, Status.INITIATED);
    }

    private String getSuccessDescription(
        com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem legalEntitiesPostRequestBody) {
        return getDescription(legalEntitiesPostRequestBody, Status.SUCCESSFUL);
    }

    private String getFailedDescription(
        com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem legalEntitiesPostRequestBody) {
        return getDescription(legalEntitiesPostRequestBody, Status.FAILED);
    }

    private String getDescription(
        com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem legalEntitiesPostRequestBody,
        Status status) {
        return EventAction.CREATE.getActionEvent() + " | Legal Entity | " + status
            + " | name " + legalEntitiesPostRequestBody.getName()
            + ", type " + legalEntitiesPostRequestBody.getType() + ", external ID "
            + legalEntitiesPostRequestBody.getExternalId();
    }
}
