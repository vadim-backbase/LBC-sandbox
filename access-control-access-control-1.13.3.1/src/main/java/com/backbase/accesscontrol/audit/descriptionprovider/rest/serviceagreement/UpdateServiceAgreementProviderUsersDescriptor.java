package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceAgreementProviderUsersDescriptor extends AbstractDescriptionProvider {

    private UserContextUtil userContextUtil;

    @Autowired
    public UpdateServiceAgreementProviderUsersDescriptor(UserContextUtil userContextUtil) {
        this.userContextUtil = userContextUtil;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_USERS_ADD)
            .withEventAction(EventAction.ADD_USERS);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        UsersForServiceAgreement request = DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        return request.getUsers().stream()
            .map(user -> createBaseUpdateMessage(serviceAgreementId, user, Status.INITIATED))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        UsersForServiceAgreement request = DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        return request.getUsers().stream()
            .map(user -> createBaseUpdateMessage(serviceAgreementId, user, Status.FAILED))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        UsersForServiceAgreement request = DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        return request.getUsers().stream()
            .map(user -> createBaseUpdateMessage(serviceAgreementId, user, Status.SUCCESSFUL))
            .collect(Collectors.toList());
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
