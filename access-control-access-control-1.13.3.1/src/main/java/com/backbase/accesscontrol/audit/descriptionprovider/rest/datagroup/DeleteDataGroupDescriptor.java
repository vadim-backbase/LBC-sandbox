package com.backbase.accesscontrol.audit.descriptionprovider.rest.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class DeleteDataGroupDescriptor extends AbstractDescriptionProvider {

    private static final String DELETE_DATA_GROUP_PREFIX = "Delete | Data Group | ";
    protected String initialDescription = DELETE_DATA_GROUP_PREFIX + "Initiated | ID %s";
    protected String successfulDescription = DELETE_DATA_GROUP_PREFIX + "Successful | ID %s";
    protected String failedDescription = DELETE_DATA_GROUP_PREFIX + "Failed | ID %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP)
            .withEventAction(EventAction.DELETE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        String dataGroupId = DescriptorUtils.getArgument(joinPoint, String.class);

        AuditMessage auditMessage = createAuditMessage(dataGroupId, initialDescription);

        auditMessage.setStatus(Status.INITIATED);

        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {

        String dataGroupId = DescriptorUtils.getArgument(joinPoint, String.class);

        AuditMessage auditMessage = createAuditMessage(dataGroupId, successfulDescription);

        auditMessage.setStatus(Status.SUCCESSFUL);

        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        String dataGroupId = DescriptorUtils.getArgument(joinPoint, String.class);

        AuditMessage auditMessage = createAuditMessage(dataGroupId, failedDescription);

        auditMessage.setStatus(Status.FAILED);

        return singletonList(auditMessage);
    }

    private AuditMessage createAuditMessage(String dataGroupId, String description) {
        return new AuditMessage()
            .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME, dataGroupId)
            .withEventDescription(String.format(
                description,
                dataGroupId));
    }
}