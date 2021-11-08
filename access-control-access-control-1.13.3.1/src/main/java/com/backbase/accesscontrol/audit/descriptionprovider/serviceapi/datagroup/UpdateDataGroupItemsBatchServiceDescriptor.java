package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_TYPE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_ITEM_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_ITEM_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.TYPE_OF_CHANGE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static org.apache.commons.lang3.StringUtils.capitalize;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupItemPutRequestBody;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UpdateDataGroupItemsBatchServiceDescriptor extends AbstractDescriptionProvider {

    private static final String UPDATE_DATA_GROUP_PREFIX = "Update | Data Group Items | ";
    private static final String INITIATE_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID = UPDATE_DATA_GROUP_PREFIX
        + "Initiated | name %s, external service agreement ID %s";
    private static final String INITIATE_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID = UPDATE_DATA_GROUP_PREFIX
        + "Initiated | ID %s";
    private static final String SUCCESSFUL_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID = UPDATE_DATA_GROUP_PREFIX
        + "Successful | name %s, external service agreement ID %s";
    private static final String SUCCESSFUL_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID = UPDATE_DATA_GROUP_PREFIX
        + "Successful | ID %s";
    private static final String FAILED_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID = UPDATE_DATA_GROUP_PREFIX
        + "Failed | name %s, external service agreement ID %s";
    private static final String FAILED_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID = UPDATE_DATA_GROUP_PREFIX
        + "Failed | ID %s";
    private static final String ADD_REMOVE_ITEMS = "%s items";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP_ITEMS)
            .withEventAction(EventAction.UPDATE);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        List<PresentationDataGroupItemPutRequestBody> requests = getArgument(joinPoint, List.class);

        return requests.stream().map(request -> new ArrayList<>(Collections
            .nCopies(request.getDataItems().size(), UUID.randomUUID().toString())))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        List<PresentationDataGroupItemPutRequestBody> requests = getArgument(joinPoint, List.class);

        List<AuditMessage> result = new ArrayList<>();
        for (PresentationDataGroupItemPutRequestBody request : requests) {
            result.addAll(request.getDataItems()
                .stream()
                .map(item -> createAuditMessage(request, item))
                .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<BatchResponseItemExtended> responses = ((ResponseEntity<List<BatchResponseItemExtended>>) actionResult)
            .getBody();
        List<PresentationDataGroupItemPutRequestBody> requests = getArgument(joinPoint, List.class);

        List<AuditMessage> result = new ArrayList<>();
        for (BatchResponseItemExtended response : responses) {
            PresentationDataGroupItemPutRequestBody request = requests.get(responses.indexOf(response));
            if (response.getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                result.addAll(request.getDataItems().stream().map(item -> createAuditMessage(response, request, item,
                    SUCCESSFUL_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
                    SUCCESSFUL_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID, Status.SUCCESSFUL)).collect(Collectors.toList()));
            } else {
                List<AuditMessage> messages = request.getDataItems()
                    .stream()
                    .map(item -> createAuditMessage(response, request, item,
                        FAILED_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
                        FAILED_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID, Status.FAILED)).collect(Collectors.toList());
                for (AuditMessage message : messages) {
                    message.putEventMetaDatum(ERROR_CODE, response.getStatus().toString());
                    response.getErrors()
                        .stream()
                        .limit(1)
                        .forEach(err -> message.putEventMetaDatum(ERROR_MESSAGE, err));
                }
                result.addAll(messages);
            }
        }
        return result;
    }

    private AuditMessage createAuditMessage(PresentationDataGroupItemPutRequestBody data,
        PresentationItemIdentifier item) {

        try {
            String itemKey = DATA_ITEM_ID_FIELD_NAME;
            String itemValue = item.getInternalIdIdentifier();

            if (Objects.nonNull(item.getExternalIdIdentifier())) {
                itemKey = DATA_ITEM_EXTERNAL_ID_FIELD_NAME;
                itemValue = item.getExternalIdIdentifier();
            }
            PresentationIdentifier identifier = data.getDataGroupIdentifier();
            if (Strings.isNullOrEmpty(identifier.getIdIdentifier())) {
                return new AuditMessage()
                    .withStatus(Status.INITIATED)
                    .withEventDescription(String.format(
                        INITIATE_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
                        identifier.getNameIdentifier().getName(),
                        identifier.getNameIdentifier().getExternalServiceAgreementId()))
                    .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                        identifier.getNameIdentifier().getExternalServiceAgreementId())
                    .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, identifier.getNameIdentifier().getName())
                    .withEventMetaDatum(TYPE_OF_CHANGE_FIELD_NAME,
                        String.format(ADD_REMOVE_ITEMS, capitalize(data.getAction().toString())))
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(itemKey, itemValue);
            } else {
                return new AuditMessage()
                    .withStatus(Status.INITIATED)
                    .withEventDescription(String.format(
                        INITIATE_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID,
                        identifier.getIdIdentifier()))
                    .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME,
                        identifier.getIdIdentifier())
                    .withEventMetaDatum(TYPE_OF_CHANGE_FIELD_NAME,
                        String.format(ADD_REMOVE_ITEMS, capitalize(data.getAction().toString())))
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(itemKey, itemValue);
            }
        } catch (Exception e) {
            return new AuditMessage()
                .withStatus(Status.INITIATED);
        }
    }

    private AuditMessage createAuditMessage(BatchResponseItemExtended response,
        PresentationDataGroupItemPutRequestBody data,
        PresentationItemIdentifier item,
        String namedIdentifierDescription,
        String idIdentifierDescription, Status status) {

        try {
            String itemKey = DATA_ITEM_ID_FIELD_NAME;
            String itemValue = item.getInternalIdIdentifier();

            if (Objects.nonNull(item.getExternalIdIdentifier())) {
                itemKey = DATA_ITEM_EXTERNAL_ID_FIELD_NAME;
                itemValue = item.getExternalIdIdentifier();
            }
            if (!Strings.isNullOrEmpty(response.getExternalServiceAgreementId())) {
                return new AuditMessage()
                    .withStatus(status)
                    .withEventDescription(String.format(
                        namedIdentifierDescription,
                        response.getResourceId(),
                        response.getExternalServiceAgreementId()))
                    .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                        response.getExternalServiceAgreementId())
                    .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, response.getResourceId())
                    .withEventMetaDatum(TYPE_OF_CHANGE_FIELD_NAME,
                        String.format(ADD_REMOVE_ITEMS, capitalize(data.getAction().toString())))
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(itemKey, itemValue);
            } else {
                return new AuditMessage()
                    .withStatus(status)
                    .withEventDescription(String.format(
                        idIdentifierDescription,
                        response.getResourceId()))
                    .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME,
                        response.getResourceId())
                    .withEventMetaDatum(TYPE_OF_CHANGE_FIELD_NAME,
                        String.format(ADD_REMOVE_ITEMS, capitalize(data.getAction().toString())))
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(itemKey, itemValue);
            }
        } catch (Exception e) {
            return new AuditMessage()
                .withStatus(status);
        }
    }
}
