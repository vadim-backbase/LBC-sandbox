package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import java.util.List;
import org.apache.commons.lang.WordUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class CreateFunctionGroupApprovalDescriptor extends CreateFunctionGroupDescriptor {

    public CreateFunctionGroupApprovalDescriptor(
        DateTimeService dateTimeService) {
        super(dateTimeService);
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP_APPROVAL)
            .withEventAction(EventAction.CREATE_PENDING);
    }

    @Override
    protected String getDescription(FunctionGroupBase functionGroupPostRequestBody, Status status) {
        return EventAction.CREATE_PENDING.getActionEvent() + " | Function Group "
            + WordUtils.capitalizeFully(functionGroupPostRequestBody.getType().toString())
            + " | " + status
            + " | name " + functionGroupPostRequestBody.getName()
            + ", service agreement ID " + functionGroupPostRequestBody.getServiceAgreementId();
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        return getAuditMessages(joinPoint, null);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(DescriptorUtils.getArgument(joinPoint, String.class, 2));
    }
}
