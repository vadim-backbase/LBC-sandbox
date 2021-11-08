package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_TYPE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_UPDATED_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupUpdate;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdateDataGroupServiceDescriptor extends AbstractDescriptionProvider {

    private static final String UPDATE_DATA_GROUP_PREFIX = "Update | Data Group | ";
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

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP_SERVICE)
            .withEventAction(EventAction.UPDATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationDataGroupUpdate data = getArgument(joinPoint, PresentationDataGroupUpdate.class);

        return singletonList(createAuditMessage(data));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        PresentationDataGroupUpdate data = getArgument(joinPoint, PresentationDataGroupUpdate.class);

        return singletonList(createAuditMessage(data,
            SUCCESSFUL_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
            SUCCESSFUL_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID, Status.SUCCESSFUL));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationDataGroupUpdate data = getArgument(joinPoint, PresentationDataGroupUpdate.class);

        return singletonList(createAuditMessage(data,
            FAILED_UPDATE_DATA_GROUP_DESCRIPTION_BY_NAMED_ID,
            FAILED_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID, Status.FAILED));
    }

    private AuditMessage createAuditMessage(PresentationDataGroupUpdate data) {
        try {
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
                    .withEventMetaDatum(DATA_GROUP_UPDATED_NAME_FIELD_NAME, data.getName())
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, data.getDescription());
            } else {
                return new AuditMessage()
                    .withStatus(Status.INITIATED)
                    .withEventDescription(String.format(
                        INITIATE_UPDATE_DATA_GROUP_DESCRIPTION_BY_ID,
                        identifier.getIdIdentifier()))
                    .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME,
                        identifier.getIdIdentifier())
                    .withEventMetaDatum(DATA_GROUP_UPDATED_NAME_FIELD_NAME, data.getName())
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, data.getDescription());
            }
        } catch (Exception e) {
            return new AuditMessage()
                .withStatus(Status.INITIATED);
        }
    }

    private AuditMessage createAuditMessage(PresentationDataGroupUpdate data,
        String namedIdentifierDescription,
        String idIdentifierDescription, Status status) {

        PresentationIdentifier identifier = data.getDataGroupIdentifier();
        try {
            if (Objects.nonNull(identifier.getNameIdentifier())) {
                return new AuditMessage()
                    .withStatus(status)
                    .withEventDescription(String.format(
                        namedIdentifierDescription,
                        identifier.getNameIdentifier().getName(),
                        identifier.getNameIdentifier().getExternalServiceAgreementId()))
                    .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                        identifier.getNameIdentifier().getExternalServiceAgreementId())
                    .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, identifier.getNameIdentifier().getName())
                    .withEventMetaDatum(DATA_GROUP_UPDATED_NAME_FIELD_NAME, data.getName())
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, data.getDescription());
            } else {
                return new AuditMessage()
                    .withStatus(status)
                    .withEventDescription(String.format(
                        idIdentifierDescription,
                        identifier.getIdIdentifier()))
                    .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME,
                        identifier.getIdIdentifier())
                    .withEventMetaDatum(DATA_GROUP_UPDATED_NAME_FIELD_NAME, data.getName())
                    .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, data.getType())
                    .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, data.getDescription());
            }
        } catch (Exception e) {
            return new AuditMessage()
                .withStatus(status);
        }
    }
}
