package com.backbase.accesscontrol.audit.descriptionprovider.rest.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class CreateDataGroupApprovalDescriptor extends CreateDataGroupDescriptor {

    private static final String CREATE_DATA_GROUP_PREFIX = "Request Create | Data Group | ";

    /**
     * CreateDataGroupApprovalDescriptor default constructor.
     */
    public CreateDataGroupApprovalDescriptor() {

        initialDescription = CREATE_DATA_GROUP_PREFIX
            + "Initiated | name %s, service agreement ID %s, type %s";
        successfulDescription = CREATE_DATA_GROUP_PREFIX
            + "Successful | name %s, service agreement ID %s, type %s";
        failedDescription = CREATE_DATA_GROUP_PREFIX
            + "Failed | name %s, service agreement ID %s, type %s";
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP_APPROVAL)
            .withEventAction(EventAction.CREATE_PENDING);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(getArgument(joinPoint, String.class));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {

        DataGroupBase dataBase = getArgument(joinPoint, DataGroupBase.class);

        AuditMessage auditMessage = createAuditMessage(dataBase, successfulDescription);

        auditMessage.setStatus(Status.SUCCESSFUL);

        return singletonList(auditMessage);
    }

}
