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
public class RemoveServiceAgreementProviderUserDescriptor extends AbstractDescriptionProvider {

    private UserContextUtil userContextUtil;

    @Autowired
    public RemoveServiceAgreementProviderUserDescriptor(UserContextUtil userContextUtil) {
        this.userContextUtil = userContextUtil;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_USERS_REMOVE)
            .withEventAction(EventAction.REMOVE_USERS);
    }


    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        UsersForServiceAgreement request = DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        return request.getUsers().stream()
            .map(user -> createBaseRemoveMessage(user, serviceAgreementId, Status.INITIATED))
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        UsersForServiceAgreement request = DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        return request.getUsers().stream()
            .map(user -> createBaseRemoveMessage(user, serviceAgreementId, Status.FAILED))
            .collect(Collectors.toList());
    }


    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        UsersForServiceAgreement request = DescriptorUtils.getArgument(joinPoint, UsersForServiceAgreement.class);
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        return request.getUsers().stream()
            .map(user -> createBaseRemoveMessage(user, serviceAgreementId, Status.SUCCESSFUL))
            .collect(Collectors.toList());
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
