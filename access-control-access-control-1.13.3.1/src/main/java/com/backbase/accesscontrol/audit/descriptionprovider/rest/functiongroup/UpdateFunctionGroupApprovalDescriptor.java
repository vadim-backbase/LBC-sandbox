package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdateFunctionGroupApprovalDescriptor extends UpdateFunctionGroupDescriptor {


    public UpdateFunctionGroupApprovalDescriptor(
        DateTimeService dateTimeService) {
        super(dateTimeService);
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP_APPROVAL)
            .withEventAction(EventAction.UPDATE_PENDING);
    }

    @Override
    protected String getDescription(Status status, String functionGroupId) {
        return EventAction.UPDATE_PENDING.getActionEvent() + " | Function Group | " + status
            + " | ID " + functionGroupId;
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(DescriptorUtils.getArgument(joinPoint, String.class, 1));
    }

}
