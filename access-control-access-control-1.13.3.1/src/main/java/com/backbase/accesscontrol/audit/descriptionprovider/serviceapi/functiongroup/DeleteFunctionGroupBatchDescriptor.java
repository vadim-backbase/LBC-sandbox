package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.google.common.collect.Lists.reverse;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DeleteFunctionGroupBatchDescriptor extends AbstractDescriptionProvider {

    private static final String DELETE_FUNCTION_GROUP_PREFIX = "Delete | Function Group | ";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP_BATCH)
            .withEventAction(EventAction.DELETE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        List<PresentationIdentifier> requests = getArgument(joinPoint, List.class);
        requests.forEach(item -> auditMessages.add(createBaseAuditMessage(item, Status.INITIATED)));
        return auditMessages;
    }

    private AuditMessage createBaseAuditMessage(PresentationIdentifier item, Status status) {
        AuditMessage auditMessage = new AuditMessage()
            .withStatus(status);
        if (Objects.isNull(item.getNameIdentifier())) {
            return auditMessage
                .withEventDescription(DELETE_FUNCTION_GROUP_PREFIX
                    + status
                    + " | ID "
                    + item.getIdIdentifier())
                .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, item.getIdIdentifier());
        }
        return auditMessage
            .withEventDescription(DELETE_FUNCTION_GROUP_PREFIX
                + status
                + " | name "
                + item.getNameIdentifier().getName() + ", external service agreement ID "
                + item.getNameIdentifier().getExternalServiceAgreementId())
            .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, item.getNameIdentifier().getName())
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                item.getNameIdentifier().getExternalServiceAgreementId());
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        List<PresentationIdentifier> requests = getArgument(joinPoint, List.class);

        return IntStream.range(0, requests.size())
            .mapToObj(i -> UUID.randomUUID().toString())
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        List<BatchResponseItemExtended> responses = ((ResponseEntity<List<BatchResponseItemExtended>>) actionResult)
            .getBody();
        List<PresentationIdentifier> requests = getArgument(joinPoint, List.class);

        for (int i = responses.size() - 1; i >= 0; i--) {
            if (!responses.get(i).getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                AuditMessage failedEventDatumInternal = createBaseAuditMessage(requests.get(i), Status.FAILED);
                failedEventDatumInternal.putEventMetaDatum(ERROR_CODE, responses.get(i).getStatus().toString());
                responses.get(i).getErrors()
                    .stream()
                    .limit(1)
                    .forEach(err -> failedEventDatumInternal.putEventMetaDatum(ERROR_MESSAGE, err));
                auditMessages.add(failedEventDatumInternal);
            } else {
                AuditMessage successEventDatumInternal = createBaseAuditMessage(requests.get(i),
                    Status.SUCCESSFUL);
                auditMessages.add(successEventDatumInternal);
            }
        }
        return reverse(auditMessages);
    }
}
