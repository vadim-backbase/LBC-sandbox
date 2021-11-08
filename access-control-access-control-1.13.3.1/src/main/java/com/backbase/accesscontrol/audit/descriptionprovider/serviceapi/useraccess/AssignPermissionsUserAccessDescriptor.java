package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.useraccess;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.EXTERNAL_USER_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.google.common.collect.Lists.reverse;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationAssignUserPermissions;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupDataGroup;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AssignPermissionsUserAccessDescriptor extends AbstractDescriptionProvider {

    private static final String UPDATE_PERMISSIONS_PREFIX = "Update Permissions";
    private static final String INITIATE_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Initiated | for user %s in service agreement %s";
    private static final String SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Successful | for user %s in service agreement %s";
    private static final String FAILED_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Failed | for user %s in service agreement %s";


    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.ASSIGN_USER_PERMISSIONS)
            .withEventAction(EventAction.UPDATE_PERMISSIONS);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<PresentationAssignUserPermissions> presentationAssignUserPermissions = getArgument(
            joinPoint, List.class);

        List<AuditMessage> auditMessages = new ArrayList<>();

        for (PresentationAssignUserPermissions presentationAssignUserPermission : presentationAssignUserPermissions) {
            auditMessages.addAll(createInitEventDatumPermissions(presentationAssignUserPermission));
        }

        return auditMessages;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {

        List<AuditMessage> auditMessages = new ArrayList<>();

        List<BatchResponseItemExtended> responses = ((ResponseEntity<List<BatchResponseItemExtended>>) actionResult)
            .getBody();
        List<PresentationAssignUserPermissions> requests = getArgument(joinPoint, List.class);

        for (int i = responses.size() - 1; i >= 0; i--) {
            if (!responses.get(i).getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                List<AuditMessage> failedEventDatumPermissions = createFailedEventDatumPermissions(requests.get(i),
                    responses.get(i));
                auditMessages.addAll(reverse(failedEventDatumPermissions));
            } else {
                List<AuditMessage> successEventDatumPermissions = createSuccessEventDatumPermissions(requests.get(i));
                auditMessages.addAll(reverse(successEventDatumPermissions));
            }
        }

        return reverse(auditMessages);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        List<PresentationAssignUserPermissions> presentationAssignUserPermissions = getArgument(joinPoint, List.class);
        List<String> messageIds = new ArrayList<>();

        for (PresentationAssignUserPermissions presentationAssignUserPermission : presentationAssignUserPermissions) {
            String messageId = UUID.randomUUID().toString();

            int numberOfItems = getNumberOfItems(presentationAssignUserPermission);

            IntStream.range(0, numberOfItems).mapToObj(i -> messageId).forEach(messageIds::add);
        }

        return messageIds;
    }

    private List<AuditMessage> createInitEventDatumPermissions(
        PresentationAssignUserPermissions presentationAssignUserPermission) {
        return createEventDatumPermissions(presentationAssignUserPermission,
            Status.INITIATED, INITIATE_UPDATE_PERMISSIONS_DESCRIPTION);
    }

    private List<AuditMessage> createFailedEventDatumPermissions(
        PresentationAssignUserPermissions presentationAssignUserPermission,
        BatchResponseItemExtended batchResponseItem) {

        List<AuditMessage> auditMessages = createEventDatumPermissions(presentationAssignUserPermission,
            Status.FAILED, FAILED_UPDATE_PERMISSIONS_DESCRIPTION);

        auditMessages.forEach(auditMessage ->
            auditMessage.withEventMetaDatum(ERROR_CODE,
                nonNull(batchResponseItem.getStatus()) ? batchResponseItem.getStatus().toString() : "")
                .withEventMetaDatum(ERROR_MESSAGE,
                    !batchResponseItem.getErrors().isEmpty() ? batchResponseItem.getErrors().get(0) : "")
        );

        return auditMessages;
    }

    private List<AuditMessage> createSuccessEventDatumPermissions(
        PresentationAssignUserPermissions presentationFunctionGroupPutRequestBody) {
        return createEventDatumPermissions(presentationFunctionGroupPutRequestBody, Status.SUCCESSFUL,
            SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION);
    }

    private List<AuditMessage> createEventDatumPermissions(
        PresentationAssignUserPermissions presentationFunctionGroupPutRequestBody, Status status, String description) {
        List<PresentationFunctionGroupDataGroup> functionGroupDataGroups = presentationFunctionGroupPutRequestBody
            .getFunctionGroupDataGroups();

        if (nonNull(functionGroupDataGroups) && !functionGroupDataGroups.isEmpty()) {
            return functionGroupDataGroups
                .stream()
                .flatMap(pair -> {
                    if (isNull(pair.getDataGroupIdentifiers()) || pair.getDataGroupIdentifiers().isEmpty()) {
                        Map<String, String> metaData = createUserAndServiceAgreementMetaData(
                            presentationFunctionGroupPutRequestBody);
                        setFunctionGroupMetaData(metaData, pair.getFunctionGroupIdentifier());

                        return Stream
                            .of(getAuditMessage(presentationFunctionGroupPutRequestBody, metaData, status,
                                description));
                    } else {
                        return pair.getDataGroupIdentifiers()
                            .stream()
                            .map(dataGroup -> {
                                Map<String, String> metaData = createUserAndServiceAgreementMetaData(
                                    presentationFunctionGroupPutRequestBody);
                                setFunctionGroupDataGroupMetaData(metaData, pair.getFunctionGroupIdentifier(),
                                    dataGroup);
                                return getAuditMessage(presentationFunctionGroupPutRequestBody, metaData, status,
                                    description);
                            });
                    }

                }).collect(toList());
        }
        return Collections.singletonList(getAuditMessage(presentationFunctionGroupPutRequestBody,
            createUserAndServiceAgreementMetaData(presentationFunctionGroupPutRequestBody), status, description));
    }

    private int getNumberOfItems(PresentationAssignUserPermissions presentationAssignUserPermission) {
        int numberOfItems = presentationAssignUserPermission.getFunctionGroupDataGroups().stream()
            .map(fg -> CollectionUtils.isEmpty(fg.getDataGroupIdentifiers()) ? 1 : fg.getDataGroupIdentifiers().size())
            .reduce(0, Integer::sum);
        return numberOfItems == 0 ? 1 : numberOfItems;
    }

    private AuditMessage getAuditMessage(PresentationAssignUserPermissions presentationFunctionGroupPutRequestBody,
        Map<String, String> eventMetaData, Status status, String description) {
        return new AuditMessage()
            .withStatus(status)
            .withEventDescription(String
                .format(description, presentationFunctionGroupPutRequestBody.getExternalUserId(),
                    presentationFunctionGroupPutRequestBody.getExternalServiceAgreementId()))
            .withEventMetaData(eventMetaData);
    }

    private Map<String, String> createUserAndServiceAgreementMetaData(
        PresentationAssignUserPermissions presentationFunctionGroupPutRequestBody) {
        Map<String, String> eventMetadata = new HashMap<>();
        eventMetadata.put(EXTERNAL_USER_ID_FIELD_NAME, presentationFunctionGroupPutRequestBody.getExternalUserId());
        eventMetadata.put(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
            presentationFunctionGroupPutRequestBody.getExternalServiceAgreementId());
        return eventMetadata;
    }

    private void setFunctionGroupDataGroupMetaData(Map<String, String> metaData, PresentationIdentifier fgIdentifier,
        PresentationIdentifier dgIdentifier) {

        setFunctionGroupMetaData(metaData, fgIdentifier);

        if (Objects.nonNull(dgIdentifier.getNameIdentifier())) {
            metaData.put(DATA_GROUP_NAME_FIELD_NAME, dgIdentifier.getNameIdentifier().getName());
        } else {
            metaData.put(DATA_GROUP_ID_FIELD_NAME, dgIdentifier.getIdIdentifier());
        }
    }

    private void setFunctionGroupMetaData(Map<String, String> metaData, PresentationIdentifier fgIdentifier) {

        if (Objects.nonNull(fgIdentifier.getNameIdentifier())) {
            metaData.put(FUNCTION_GROUP_NAME_FIELD_NAME, fgIdentifier.getNameIdentifier().getName());
        } else {
            metaData.put(FUNCTION_GROUP_ID_FIELD_NAME, fgIdentifier.getIdIdentifier());
        }
    }
}
