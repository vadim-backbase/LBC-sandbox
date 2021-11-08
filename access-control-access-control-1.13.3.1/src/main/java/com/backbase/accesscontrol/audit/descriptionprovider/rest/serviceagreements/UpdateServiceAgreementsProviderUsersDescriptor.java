package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreements;

import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceAgreementsProviderUsersDescriptor extends AbstractDescriptionProvider {

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENTS_USERS_ADD)
            .withEventAction(EventAction.ADD_USERS);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        UsersForServiceAgreement request =
            DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");

        return request.getUsers().stream()
            .map(user -> createBaseUpdateMessage(serviceAgreementId, user, Status.INITIATED))
            .collect(toList());
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        UsersForServiceAgreement request =
            DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");

        return request.getUsers().stream()
            .map(user -> createBaseUpdateMessage(serviceAgreementId, user, Status.SUCCESSFUL))
            .collect(toList());
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        UsersForServiceAgreement request =
            DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");

        return request.getUsers().stream()
            .map(user -> createBaseUpdateMessage(serviceAgreementId, user, Status.FAILED))
            .collect(toList());
    }

    private AuditMessage createBaseUpdateMessage(String serviceAgreementId, String userId, Status status) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId);
        metaData.put(DescriptorFieldNames.USER_ID_FIELD_NAME, userId);
        return new AuditMessage().withStatus(status).withEventDescription(getDescription(serviceAgreementId, status))
            .withEventMetaData(metaData);
    }

    private String getDescription(String serviceAgreementId, Status status) {
        return "Add User | Service Agreement | " + status + " | service agreement ID " + serviceAgreementId;

    }
}
