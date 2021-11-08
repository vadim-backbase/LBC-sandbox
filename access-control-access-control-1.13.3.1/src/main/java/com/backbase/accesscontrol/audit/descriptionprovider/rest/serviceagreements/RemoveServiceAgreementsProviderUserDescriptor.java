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
public class RemoveServiceAgreementsProviderUserDescriptor extends AbstractDescriptionProvider {

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENTS_USERS_REMOVE)
            .withEventAction(EventAction.REMOVE_USERS);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");
        UsersForServiceAgreement request =
            DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);

        return request.getUsers().stream()
            .map(user -> createBaseRemoveMessage(user, serviceAgreementId, Status.INITIATED))
            .collect(toList());
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");
        UsersForServiceAgreement request =
            DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);

        return request.getUsers().stream()
            .map(user -> createBaseRemoveMessage(user, serviceAgreementId, Status.FAILED))
            .collect(toList());
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");
        UsersForServiceAgreement request =
            DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);

        return request.getUsers().stream()
            .map(user -> createBaseRemoveMessage(user, serviceAgreementId, Status.SUCCESSFUL))
            .collect(toList());
    }

    private AuditMessage createBaseRemoveMessage(String userId, String serviceAgreementId, Status status) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId);
        metaData.put(DescriptorFieldNames.USER_ID_FIELD_NAME, userId);
        return new AuditMessage()
            .withStatus(status)
            .withEventMetaData(metaData)
            .withEventDescription(getDescription(serviceAgreementId, status));
    }

    private String getDescription(String serviceAgreementId, Status status) {
        return "Remove User | Service Agreement | " + status + " | service agreement ID " + serviceAgreementId;
    }
}
