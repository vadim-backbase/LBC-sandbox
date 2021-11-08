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
public class UpdateDataGroupApprovalDescriptor extends UpdateDataGroupDescriptor {

    private static final String UPDATE_DATA_GROUP_PREFIX = "Request Update | Data Group | ";

    /**
     * UpdateDataGroupApprovalDescriptor default constructor.
     */
    public UpdateDataGroupApprovalDescriptor() {
        initialDescription = UPDATE_DATA_GROUP_PREFIX + "Initiated | name %s, service agreement ID %s, type %s";
        successfulDescription = UPDATE_DATA_GROUP_PREFIX + "Successful | name %s, service agreement ID %s, type %s";
        failedDescription = UPDATE_DATA_GROUP_PREFIX + "Failed | name %s, service agreement ID %s, type %s";
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP_APPROVAL)
            .withEventAction(EventAction.UPDATE_PENDING);
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(getArgument(joinPoint, String.class));
    }
}
