package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemId;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AddLegalEntityDescriptor extends AbstractDescriptionProvider {

    private static final String LEGAL_ENTITY_ID = "Legal Entity ID";
    private static final String EXTERNAL_LEGAL_ENTITY_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String PARENT_EXTERNAL_LEGAL_ENTITY_ID = "Parent External Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.LEGAL_ENTITY_ADD)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        LegalEntityCreateItem legalEntitiesPostRequestBody = DescriptorUtils
            .getArgument(joinPoint, LegalEntityCreateItem.class);
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
        LegalEntityCreateItem legalEntitiesPostRequestBody = DescriptorUtils
            .getArgument(joinPoint, LegalEntityCreateItem.class);
        LegalEntityItemId responseActionResult = ((ResponseEntity<LegalEntityItemId>) actionResult)
            .getBody();

        return singletonList(new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getSuccessDescription(legalEntitiesPostRequestBody))
            .withEventMetaDatum(LEGAL_ENTITY_ID, responseActionResult.getId())
            .withEventMetaDatum(EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getName())
            .withEventMetaDatum(PARENT_EXTERNAL_LEGAL_ENTITY_ID, legalEntitiesPostRequestBody.getParentExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE,
                nonNull(legalEntitiesPostRequestBody.getType()) ? legalEntitiesPostRequestBody.getType().toString()
                    : ""));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        LegalEntityCreateItem legalEntitiesPostRequestBody = DescriptorUtils
            .getArgument(joinPoint, LegalEntityCreateItem.class);
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

    private String getInitDescription(LegalEntityCreateItem createLegalEntitiesPostRequestBody) {
        return getDescription(createLegalEntitiesPostRequestBody, Status.INITIATED);
    }

    private String getSuccessDescription(LegalEntityCreateItem createLegalEntitiesPostRequestBody) {
        return getDescription(createLegalEntitiesPostRequestBody, Status.SUCCESSFUL);
    }

    private String getFailedDescription(LegalEntityCreateItem createLegalEntitiesPostRequestBody) {
        return getDescription(createLegalEntitiesPostRequestBody, Status.FAILED);
    }

    private String getDescription(LegalEntityCreateItem createLegalEntitiesPostRequestBody,
        Status status) {
        return EventAction.CREATE.getActionEvent() + " | Legal Entity | " + status
            + " | name " + createLegalEntitiesPostRequestBody.getName()
            + ", type " + createLegalEntitiesPostRequestBody.getType() + ", external ID "
            + createLegalEntitiesPostRequestBody.getExternalId();
    }
}
