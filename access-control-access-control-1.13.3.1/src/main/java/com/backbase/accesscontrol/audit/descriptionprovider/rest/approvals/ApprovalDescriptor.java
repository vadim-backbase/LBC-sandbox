package com.backbase.accesscontrol.audit.descriptionprovider.rest.approvals;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.APPROVAL_REQUEST_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getPathParameter;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class ApprovalDescriptor extends AbstractDescriptionProvider {

    private static final String ANONYMOUS = "anonymous";
    private static final String APPROVAL_ID_PATH_VARIABLE = "approvalId";

    private UserContextUtil userContextUtil;

    public ApprovalDescriptor(UserContextUtil userContextUtil) {
        this.userContextUtil = userContextUtil;
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        String approvalId = getPathParameter(joinPoint, APPROVAL_ID_PATH_VARIABLE);

        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(getDescription(Status.INITIATED, getUserId(), getServiceAgreementId()))
            .withEventMetaData(getMetadata());
        auditMessage.withEventMetaDatum(APPROVAL_REQUEST_ID_FIELD_NAME, approvalId);

        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        String approvalId = getPathParameter(joinPoint, APPROVAL_ID_PATH_VARIABLE);

        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(getDescription(Status.SUCCESSFUL, getUserId(), getServiceAgreementId()))
            .withEventMetaData(getMetadata());
        auditMessage.withEventMetaDatum(APPROVAL_REQUEST_ID_FIELD_NAME, approvalId);

        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        String approvalId = getPathParameter(joinPoint, APPROVAL_ID_PATH_VARIABLE);

        AuditMessage auditMessage = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(getDescription(Status.FAILED, getUserId(), getServiceAgreementId()))
            .withEventMetaData(getMetadata());
        auditMessage.withEventMetaDatum(APPROVAL_REQUEST_ID_FIELD_NAME, approvalId);

        return singletonList(auditMessage);
    }

    protected abstract String getDescription(Status status, String userId, String serviceAgreementId);

    protected Map<String, String> getMetadata() {
        return emptyMap();
    }

    protected String getUserId() {
        Optional<String> authenticatedUserName = userContextUtil.getOptionalAuthenticatedUserName();
        if (authenticatedUserName.isPresent()) {
            return userContextUtil.getUserContextDetails().getInternalUserId();
        } else {
            return ANONYMOUS;
        }
    }

    protected String getServiceAgreementId() {
        return userContextUtil.getServiceAgreementId();
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        String approvalId = getPathParameter(joinPoint, APPROVAL_ID_PATH_VARIABLE);

        return singletonList(approvalId);
    }
}
