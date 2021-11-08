package com.backbase.accesscontrol.audit.descriptionprovider.rest.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class DeleteDataGroupApprovalDescriptor extends DeleteDataGroupDescriptor {

    private static final String DELETE_DATA_GROUP_PREFIX = "Request Delete | Data Group | ";

    /**
     * DeleteDataGroupApprovalDescriptor default constructor.
     */
    public DeleteDataGroupApprovalDescriptor() {
        initialDescription = DELETE_DATA_GROUP_PREFIX + "Initiated | ID %s";
        successfulDescription = DELETE_DATA_GROUP_PREFIX + "Successful | ID %s";
        failedDescription = DELETE_DATA_GROUP_PREFIX + "Failed | ID %s";
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP_APPROVAL)
            .withEventAction(EventAction.DELETE_PENDING);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(getArgument(joinPoint, String.class, 1));
    }
}
