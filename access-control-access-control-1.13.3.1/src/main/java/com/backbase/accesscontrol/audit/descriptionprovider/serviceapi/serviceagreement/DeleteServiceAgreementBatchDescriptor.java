package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.google.common.collect.Lists.reverse;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementBatchDelete;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DeleteServiceAgreementBatchDescriptor extends AbstractDescriptionProvider {

    private static final String DELETE_SERVICE_AGREEMENT_PREFIX = "Delete | Service Agreement | ";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_BATCH_SERVICE)
            .withEventAction(EventAction.DELETE);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        ServiceAgreementBatchDelete requests = getArgument(joinPoint, ServiceAgreementBatchDelete.class);

        return IntStream.range(0, requests.getServiceAgreementIdentifiers().size())
            .mapToObj(i -> UUID.randomUUID().toString())
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        ServiceAgreementBatchDelete requests = getArgument(joinPoint,
            ServiceAgreementBatchDelete.class);
        for (int i = 0; i < requests.getServiceAgreementIdentifiers().size(); i++) {
            AuditMessage auditMessage = createBaseEventMetaData(Status.INITIATED,
                requests.getServiceAgreementIdentifiers().get(i));
            auditMessages.add(auditMessage);
        }
        return auditMessages;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<BatchResponseItem> responseData = ((ResponseEntity<List<BatchResponseItem>>) actionResult).getBody();
        List<AuditMessage> auditMessages = new ArrayList<>();

        ServiceAgreementBatchDelete requests = getArgument(joinPoint,
            ServiceAgreementBatchDelete.class);

        List<PresentationServiceAgreementIdentifier> identifiers = new ArrayList<>(
            requests.getServiceAgreementIdentifiers());

        for (int i = responseData.size() - 1; i >= 0; i--) {
            if (!responseData.get(i).getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                AuditMessage failedMessage = createBaseEventMetaData(Status.FAILED,
                    identifiers.get(i));
                failedMessage.putEventMetaDatum(ERROR_CODE, responseData.get(i).getStatus().toString());
                responseData.get(i).getErrors()
                    .stream()
                    .limit(1)
                    .forEach(err -> failedMessage.putEventMetaDatum(ERROR_MESSAGE, err));
                auditMessages.add(failedMessage);
            } else {
                AuditMessage successEventDatumLegalEntity = createBaseEventMetaData(Status.SUCCESSFUL,
                    identifiers.get(i));
                auditMessages.add(successEventDatumLegalEntity);
            }
        }
        return reverse(auditMessages);
    }

    private AuditMessage createBaseEventMetaData(Status status,
        com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementIdentifier identifier) {
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(status);
        return getProvidedIdentifier(status, identifier, auditMessage);
    }

    private AuditMessage getProvidedIdentifier(Status status,
        PresentationServiceAgreementIdentifier identifier,
        AuditMessage auditMessage) {
        try {
            if (isNotEmpty(identifier.getIdIdentifier())) {
                return auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, identifier.getIdIdentifier())
                    .withEventDescription(
                        DELETE_SERVICE_AGREEMENT_PREFIX + status + " | ID " + identifier.getIdIdentifier());
            } else if (isNotEmpty(identifier.getNameIdentifier())) {
                return auditMessage
                    .withEventMetaDatum(SERVICE_AGREEMENT_NAME_FIELD_NAME, identifier.getNameIdentifier())
                    .withEventDescription(
                        DELETE_SERVICE_AGREEMENT_PREFIX + status + " | name " + identifier.getNameIdentifier());
            } else if (isNotEmpty(identifier.getExternalIdIdentifier())) {
                return auditMessage
                    .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, identifier.getExternalIdIdentifier())
                    .withEventDescription(
                        DELETE_SERVICE_AGREEMENT_PREFIX + status + " | external ID " + identifier
                            .getExternalIdIdentifier());
            }
        } catch (Exception e) {
            return new AuditMessage().withStatus(status);
        }
        return auditMessage;
    }
}
