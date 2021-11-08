package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.EXTERNAL_USER_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.google.common.collect.Lists.reverse;

import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUserPair;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUsersBatchUpdate;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class UpdateUsersBase extends AbstractDescriptionProvider {

    List<AuditMessage> getInitEventData(ProceedingJoinPoint joinPoint, String entityUpdated) {
        List<AuditMessage> auditMessagesAddRemoveUsers = new ArrayList<>();
        PresentationServiceAgreementUsersBatchUpdate presentationServiceAgreementUsersUpdate = getArgument(joinPoint,
            PresentationServiceAgreementUsersBatchUpdate.class);
        String action = presentationServiceAgreementUsersUpdate.getAction().toString();
        List<PresentationServiceAgreementUserPair> requests = presentationServiceAgreementUsersUpdate.getUsers();
        for (int i = requests.size() - 1; i >= 0; i--) {
            AuditMessage auditMessage = createInitiatedEventAddRemoveUserServiceAgreement(entityUpdated, action,
                requests.get(i));
            auditMessagesAddRemoveUsers.add(auditMessage);
        }
        return reverse(auditMessagesAddRemoveUsers);
    }

    AuditMessage createBaseEventAddRemoveUsersServiceAgreement(String entityUpdated,
        BatchResponseItemExtended responseItemExtended, Status status) {
        return new AuditMessage()
            .withStatus(status)
            .withEventDescription(getDescription(entityUpdated, responseItemExtended.getAction().toString(),
                responseItemExtended.getExternalServiceAgreementId(), responseItemExtended.getResourceId(),
                status))
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                responseItemExtended.getExternalServiceAgreementId())
            .withEventMetaDatum(EXTERNAL_USER_ID_FIELD_NAME, responseItemExtended.getResourceId());
    }

    AuditMessage createInitiatedEventAddRemoveUserServiceAgreement(String entityUpdated, String action,
        PresentationServiceAgreementUserPair presentationServiceAgreementUserPair) {
        return new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                getDescription(entityUpdated, action,
                    presentationServiceAgreementUserPair.getExternalServiceAgreementId(),
                    presentationServiceAgreementUserPair.getExternalUserId(), Status.INITIATED))
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                presentationServiceAgreementUserPair.getExternalServiceAgreementId())
            .withEventMetaDatum(EXTERNAL_USER_ID_FIELD_NAME, presentationServiceAgreementUserPair.getExternalUserId());
    }


    private String getDescription(String entityUpdated, String action, String extternalServiceAgreementId,
        String externalUserId,
        Status status) {
        return StringUtils.capitalize(action) + " " + entityUpdated + " | Service Agreement | " + status
            + " | external service agreement ID "
            + extternalServiceAgreementId
            + ", external user ID " + externalUserId;

    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        PresentationServiceAgreementUsersBatchUpdate presentationServiceAgreementUsersUpdate = getArgument(joinPoint,
            PresentationServiceAgreementUsersBatchUpdate.class);
        List<PresentationServiceAgreementUserPair> requests = presentationServiceAgreementUsersUpdate.getUsers();
        List<String> messageIds = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            messageIds.add(UUID.randomUUID().toString());

        }
        return messageIds;
    }

}
