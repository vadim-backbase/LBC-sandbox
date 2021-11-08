package com.backbase.accesscontrol.audit.descriptionprovider.permissions;

import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdatePermissionsApprovalDescriptor extends UpdatePermissionsDescriptor {

    private static final AuditEventAction auditEventAction = new AuditEventAction()
        .withEventAction(EventAction.REQUEST_PERMISSIONS_UPDATE)
        .withObjectType(AuditObjectType.UPDATE_USER_PERMISSIONS_APPROVAL);

    private static final String UPDATE_PERMISSIONS_PREFIX = "Request Permissions Update";
    private static final String INITIATE_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Initiated | for user %s in service agreement %s";
    private static final String SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Successful | for user %s in service agreement %s";
    private static final String FAILED_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Failed | for user %s in service agreement %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return auditEventAction;
    }

    @Override
    protected UserContextData getUserContextData(ProceedingJoinPoint joinPoint) {
        return new UserContextData((String) joinPoint.getArgs()[1], (String) joinPoint.getArgs()[2]);
    }

    @Override
    protected String getInitDescription(String userId, String serviceAgreementId) {
        return String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, userId, serviceAgreementId);
    }

    @Override
    protected String getSuccessDescription(String userId, String serviceAgreementId) {
        return String.format(SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION, userId, serviceAgreementId);
    }

    @Override
    protected String getFailedDescription(String userId, String serviceAgreementId) {
        return String.format(FAILED_UPDATE_PERMISSIONS_DESCRIPTION, userId, serviceAgreementId);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(DescriptorUtils.getArgument(joinPoint, String.class, 3));
    }
}
