package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ADMIN_USER_APS_IDS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ADMIN_USER_APS_NAMES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.EXTERNAL_PARTICIPANT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.MASTER_SERVICE_AGREEMENT_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PARTICIPANT_SHARING_USERS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.REGULAR_USER_APS_IDS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.REGULAR_USER_APS_NAMES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_STATUS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.populateSuccessUserApsIdentifiersMetadata;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.populateUserApsIdentifiersMetadata;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.accesscontrol.service.rest.spec.model.ParticipantIngest;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers;
import com.backbase.accesscontrol.service.rest.spec.model.ServicesAgreementIngest;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CreateServiceAgreementsDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    public CreateServiceAgreementsDescriptor(
        DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_SERVICE)
            .withEventAction(EventAction.CREATE);
    }

    private ServicesAgreementIngest getServiceAgreementIngestPostRequestBody(
        ProceedingJoinPoint joinPoint) {
        return getArgument(joinPoint, ServicesAgreementIngest.class);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        ServicesAgreementIngest requestBody = getServiceAgreementIngestPostRequestBody(joinPoint);
        IdItem responseBody = ((ResponseEntity<IdItem>) actionResult).getBody();
        return requestBody.getParticipantsToIngest().stream()
            .map(participant -> createMessage(participant, requestBody, responseBody))
            .collect(toList());
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        ServicesAgreementIngest requestBody = getServiceAgreementIngestPostRequestBody(joinPoint);
        return requestBody.getParticipantsToIngest().stream()
            .map(participant -> createBaseAuditMessage(requestBody, Status.INITIATED))
            .collect(toList());
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        ServicesAgreementIngest requestBody = getServiceAgreementIngestPostRequestBody(joinPoint);
        return requestBody.getParticipantsToIngest().stream()
            .map(participant -> createBaseAuditMessage(requestBody, Status.FAILED))
            .collect(toList());

    }

    private AuditMessage createMessage(ParticipantIngest participant, ServicesAgreementIngest requestBody,
        IdItem responseBody) {
        try {
            AuditMessage auditMessage = createAuditMessage(requestBody, Status.SUCCESSFUL);
            auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, responseBody.getId());
            auditMessage.withEventMetaDatum(EXTERNAL_PARTICIPANT_ID_FIELD_NAME, participant.getExternalId());

            if (nonNull(participant.getSharingUsers())) {
                auditMessage
                    .withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, participant.getSharingUsers().toString());
            }

            if (nonNull(participant.getSharingAccounts())) {
                auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME,
                    participant.getSharingAccounts().toString());
            }

            PresentationUserApsIdentifiers regularUserAps = requestBody.getRegularUserAps();
            PresentationUserApsIdentifiers adminUserAps = requestBody.getAdminUserAps();
            populateSuccessUserApsIdentifiersMetadata(auditMessage, regularUserAps, REGULAR_USER_APS_IDS_FIELD_NAME,
                REGULAR_USER_APS_NAMES_FIELD_NAME);
            populateSuccessUserApsIdentifiersMetadata(auditMessage, adminUserAps, ADMIN_USER_APS_IDS_FIELD_NAME,
                ADMIN_USER_APS_NAMES_FIELD_NAME);

            return auditMessage;
        } catch (Exception e) {
            return new AuditMessage().withStatus(Status.FAILED);
        }
    }

    private AuditMessage createBaseAuditMessage(ServicesAgreementIngest requestBody, Status status) {
        try {
            AuditMessage auditMessage = createAuditMessage(requestBody, status);

            populateUserApsIdentifiersMetadata(auditMessage, requestBody.getRegularUserAps(),
                REGULAR_USER_APS_IDS_FIELD_NAME, REGULAR_USER_APS_NAMES_FIELD_NAME);

            populateUserApsIdentifiersMetadata(auditMessage, requestBody.getAdminUserAps(),
                ADMIN_USER_APS_IDS_FIELD_NAME, ADMIN_USER_APS_NAMES_FIELD_NAME);

            return auditMessage;
        } catch (Exception e) {
            return new AuditMessage().withStatus(status);
        }
    }

    private AuditMessage createAuditMessage(ServicesAgreementIngest requestBody, Status status) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, requestBody.getDescription());
        metaData.put(SERVICE_AGREEMENT_NAME_FIELD_NAME, requestBody.getName());
        metaData.put(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, requestBody.getExternalId());

        if (nonNull(requestBody.getIsMaster())) {
            metaData.put(MASTER_SERVICE_AGREEMENT_FIELD_NAME, requestBody.getIsMaster().toString());
        }

        if (nonNull(requestBody.getStatus())) {
            metaData.put(SERVICE_AGREEMENT_STATUS_FIELD_NAME, requestBody.getStatus().toString());
        }

        metaData.put(START_DATE_TIME_FIELD_NAME,
            dateTimeService.getStringDateTime(requestBody.getValidFromDate(), requestBody.getValidFromTime()));
        metaData.put(END_DATE_TIME_FIELD_NAME,
            dateTimeService.getStringDateTime(requestBody.getValidUntilDate(), requestBody.getValidUntilTime()));

        return new AuditMessage().withStatus(status)
            .withEventMetaData(metaData)
            .withEventDescription(getDescription(requestBody.getName(), status));
    }

    private String getDescription(String name, Status status) {
        return EventAction.CREATE.getActionEvent() + " | Service Agreement | " + status
            + " | name " + name;
    }
}
