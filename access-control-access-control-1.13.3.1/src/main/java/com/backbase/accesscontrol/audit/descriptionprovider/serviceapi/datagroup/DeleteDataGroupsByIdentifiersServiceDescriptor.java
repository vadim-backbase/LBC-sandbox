package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DeleteDataGroupsByIdentifiersServiceDescriptor extends AbstractDescriptionProvider {

    private static final String DELETE_DATA_GROUP_PREFIX = "Delete | Data Group | ";
    private static final String INITIATE_DELETE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID = DELETE_DATA_GROUP_PREFIX
        + "Initiated | name %s, external service agreement ID %s";
    private static final String INITIATE_DELETE_DATA_GROUP_DESCRIPTION_BY_ID = DELETE_DATA_GROUP_PREFIX
        + "Initiated | ID %s";
    private static final String SUCCESSFUL_DELETE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID = DELETE_DATA_GROUP_PREFIX
        + "Successful | name %s, external service agreement ID %s";
    private static final String SUCCESSFUL_DELETE_DATA_GROUP_DESCRIPTION_BY_ID = DELETE_DATA_GROUP_PREFIX
        + "Successful | ID %s";
    private static final String FAILED_DELETE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID = DELETE_DATA_GROUP_PREFIX
        + "Failed | name %s, external service agreement ID %s";
    private static final String FAILED_DELETE_DATA_GROUP_DESCRIPTION_BY_ID = DELETE_DATA_GROUP_PREFIX
        + "Failed | ID %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP_BATCH_SERVICE)
            .withEventAction(EventAction.DELETE);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        List<PresentationIdentifier> requests = getArgument(joinPoint, List.class);

        return IntStream.range(0, requests.size())
            .mapToObj(i -> UUID.randomUUID().toString())
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<PresentationIdentifier> requests = getArgument(joinPoint, List.class);

        List<AuditMessage> result = new ArrayList<>();
        for (PresentationIdentifier identifier : requests) {
            result.add(createAuditMessage(identifier));
        }
        return result;
    }

    private AuditMessage createAuditMessage(PresentationIdentifier identifier) {
        try {
            if (Strings.isNullOrEmpty(identifier.getIdIdentifier())) {
                return new AuditMessage()
                    .withStatus(Status.INITIATED)
                    .withEventDescription(String.format(
                        INITIATE_DELETE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
                        identifier.getNameIdentifier().getName(),
                        identifier.getNameIdentifier().getExternalServiceAgreementId()))
                    .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                        identifier.getNameIdentifier().getExternalServiceAgreementId())
                    .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, identifier.getNameIdentifier().getName());
            } else {
                return new AuditMessage()
                    .withStatus(Status.INITIATED)
                    .withEventDescription(String.format(
                        INITIATE_DELETE_DATA_GROUP_DESCRIPTION_BY_ID,
                        identifier.getIdIdentifier()))
                    .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME,
                        identifier.getIdIdentifier());
            }
        } catch (Exception e) {
            return new AuditMessage()
                .withStatus(Status.INITIATED);
        }
    }

    private AuditMessage createAuditMessage(BatchResponseItemExtended identifier, String namedIdentifierDescription,
        String idIdentifierDescription, Status status) {

        try {
            if (!Strings.isNullOrEmpty(identifier.getExternalServiceAgreementId())) {
                return new AuditMessage()
                    .withStatus(status)
                    .withEventDescription(String.format(
                        namedIdentifierDescription,
                        identifier.getResourceId(),
                        identifier.getExternalServiceAgreementId()))
                    .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                        identifier.getExternalServiceAgreementId())
                    .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, identifier.getResourceId());
            } else {
                return new AuditMessage()
                    .withStatus(status)
                    .withEventDescription(String.format(
                        idIdentifierDescription,
                        identifier.getResourceId()))
                    .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME,
                        identifier.getResourceId());
            }
        } catch (Exception e) {
            return new AuditMessage()
                .withStatus(status);
        }
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<BatchResponseItemExtended> responses = ((ResponseEntity<List<BatchResponseItemExtended>>) actionResult)
            .getBody();

        List<AuditMessage> result = new ArrayList<>();
        for (BatchResponseItemExtended response : responses) {
            if (response.getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                result.add(createAuditMessage(response,
                    SUCCESSFUL_DELETE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
                    SUCCESSFUL_DELETE_DATA_GROUP_DESCRIPTION_BY_ID, Status.SUCCESSFUL));
            } else {
                AuditMessage message = createAuditMessage(response,
                    FAILED_DELETE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
                    FAILED_DELETE_DATA_GROUP_DESCRIPTION_BY_ID, Status.FAILED);
                message.putEventMetaDatum(ERROR_CODE, response.getStatus().toString());
                response.getErrors()
                    .stream()
                    .limit(1)
                    .forEach(err -> message.putEventMetaDatum(ERROR_MESSAGE, err));
                result.add(message);
            }
        }
        return result;
    }
}