package com.backbase.accesscontrol.audit.descriptionprovider.rest.approvals;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ApproveApprovalDescriptor extends ApprovalDescriptor {

    private static final String DESCRIPTION = "Approve pending operation | %s | for user %s in service agreement %s";

    public ApproveApprovalDescriptor(UserContextUtil userContextUtil) {
        super(userContextUtil);
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.APPROVAL)
            .withEventAction(EventAction.APPROVE);
    }

    protected String getDescription(Status status, String userId, String serviceAgreementId) {
        return String.format(DESCRIPTION, status, userId, serviceAgreementId);
    }

    @Override
    protected Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(DescriptorFieldNames.OUTCOME_FIELD_NAME, ApprovalStatus.APPROVED.toString());
        return metadata;
    }
}
