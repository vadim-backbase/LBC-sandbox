package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class DeleteFunctionGroupDescriptor extends AbstractDescriptionProvider {

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP)
            .withEventAction(EventAction.DELETE);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        String functionGroupId = getArgument(joinPoint, String.class);
        return singletonList(new AuditMessage()
            .withEventDescription(getDescription(functionGroupId, Status.SUCCESSFUL))
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, functionGroupId)
        );
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        String functionGroupId = getArgument(joinPoint, String.class);
        return singletonList(new AuditMessage()
            .withEventDescription(getDescription(functionGroupId, Status.INITIATED))
            .withStatus(Status.INITIATED)
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, functionGroupId)
        );
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        String functionGroupId = getArgument(joinPoint, String.class);
        return singletonList(new AuditMessage()
            .withEventDescription(getDescription(functionGroupId, Status.FAILED))
            .withStatus(Status.FAILED)
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, functionGroupId)
        );
    }

    protected String getDescription(String id, Status status) {
        return EventAction.DELETE.getActionEvent() + " | Function Group | " + status
            + " | ID " + id;
    }
}
