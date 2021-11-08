package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.aps;

import static com.backbase.accesscontrol.audit.AuditObjectType.PERMISSION_SET;
import static com.backbase.accesscontrol.audit.EventAction.DELETE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.backbase.audit.client.model.AuditMessage.Status.FAILED;
import static com.backbase.audit.client.model.AuditMessage.Status.INITIATED;
import static com.backbase.audit.client.model.AuditMessage.Status.SUCCESSFUL;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class DeletePermissionSetServiceDescriptor extends AbstractDescriptionProvider {

    private static final String ID_IDENTIFIER_TYPE = "id";
    private static final String DELETE_PERMISSION_SET_PREFIX = "Delete | Assignable Permission Set | ";
    private static final String INITIATE_DELETE_PERMISSION_SET_DESCRIPTION_BY_NAME = DELETE_PERMISSION_SET_PREFIX
        + "Initiated | name %s";
    private static final String INITIATE_DELETE_PERMISSION_SET_DESCRIPTION_BY_ID = DELETE_PERMISSION_SET_PREFIX
        + "Initiated | ID %s";
    private static final String SUCCESSFUL_DELETE_PERMISSION_SET_DESCRIPTION_BY_NAME = DELETE_PERMISSION_SET_PREFIX
        + "Successful | name %s";
    private static final String SUCCESSFUL_DELETE_PERMISSION_SET_DESCRIPTION_BY_ID = DELETE_PERMISSION_SET_PREFIX
        + "Successful | ID %s";
    private static final String FAILED_DELETE_PERMISSION_SET_DESCRIPTION_BY_NAME = DELETE_PERMISSION_SET_PREFIX
        + "Failed | name %s";
    private static final String FAILED_DELETE_PERMISSION_SET_DESCRIPTION_BY_ID = DELETE_PERMISSION_SET_PREFIX
        + "Failed | ID %s";
    public static final AuditEventAction AUDIT_EVENT_ACTION = new AuditEventAction()
        .withObjectType(PERMISSION_SET)
        .withEventAction(DELETE);

    @Override
    public AuditEventAction getAuditEventAction() {
        return AUDIT_EVENT_ACTION;
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        return createMessage(joinPoint, INITIATE_DELETE_PERMISSION_SET_DESCRIPTION_BY_ID,
            INITIATE_DELETE_PERMISSION_SET_DESCRIPTION_BY_NAME, INITIATED);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        return createMessage(joinPoint, FAILED_DELETE_PERMISSION_SET_DESCRIPTION_BY_ID,
            FAILED_DELETE_PERMISSION_SET_DESCRIPTION_BY_NAME, FAILED);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        return createMessage(joinPoint, SUCCESSFUL_DELETE_PERMISSION_SET_DESCRIPTION_BY_ID,
            SUCCESSFUL_DELETE_PERMISSION_SET_DESCRIPTION_BY_NAME, SUCCESSFUL);
    }

    private List<AuditMessage> createMessage(ProceedingJoinPoint joinPoint, String idDescription,
        String nameDescription, Status status) {

        String identifierType = getArgument(joinPoint, String.class, 0);
        String identifier = getArgument(joinPoint, String.class, 1);
        String description = String.format(nameDescription, identifier);
        if (identifierType.equals(ID_IDENTIFIER_TYPE)) {
            description = String.format(idDescription, identifier);
            identifierType = identifierType.toUpperCase();
        }

        return singletonList(new AuditMessage()
            .withStatus(status)
            .withEventDescription(description)
            .withEventMetaDatum(identifierType, identifier));
    }
}
