package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class DeleteFunctionGroupApprovalDescriptor extends DeleteFunctionGroupDescriptor {

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP_APPROVAL)
            .withEventAction(EventAction.DELETE_PENDING);
    }

    @Override
    protected String getDescription(String id, Status status) {
        return EventAction.DELETE_PENDING.getActionEvent() + " | Function Group | " + status
            + " | ID " + id;
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(getArgument(joinPoint, String.class, 1));
    }
}
