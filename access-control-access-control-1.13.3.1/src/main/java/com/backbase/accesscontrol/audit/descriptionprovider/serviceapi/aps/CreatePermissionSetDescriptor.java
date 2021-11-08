package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.aps;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ASSIGNABLE_PERMISSION_SET_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ASSIGNABLE_PERMISSION_SET_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ASSIGNABLE_PERMISSION_SET_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ASSIGNABLE_PERMISSION_SET_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationId;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSet;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetItem;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CreatePermissionSetDescriptor extends AbstractDescriptionProvider {

    private static final String CREATE_PERMISSION_SET_PREFIX = "Create | Assignable Permission Set | ";
    private static final String INITIATE_CREATE_PERMISSION_SET_DESCRIPTION = CREATE_PERMISSION_SET_PREFIX
        + "Initiated | name %s";
    private static final String SUCCESSFUL_CREATE_PERMISSION_SET_DESCRIPTION = CREATE_PERMISSION_SET_PREFIX
        + "Successful | name %s, ID %s";
    private static final String FAILED_CREATE_DATA_GROUP_DESCRIPTION = CREATE_PERMISSION_SET_PREFIX
        + "Failed | name %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.PERMISSION_SET)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationPermissionSet presentationPermissionSet = getArgument(joinPoint, PresentationPermissionSet.class);

        List<AuditMessage> auditMessages = new ArrayList<>();

        List<PresentationPermissionSetItem> permissions = presentationPermissionSet.getPermissions();

        if (isNull(permissions) || permissions.isEmpty()) {
            AuditMessage auditMessage = createAuditMessage(presentationPermissionSet, Status.INITIATED);
            auditMessage.withEventDescription(
                String.format(INITIATE_CREATE_PERMISSION_SET_DESCRIPTION, presentationPermissionSet.getName()));
            auditMessages.add(auditMessage);
        } else {
            for (PresentationPermissionSetItem presentationPermissionSetItem : permissions) {
                AuditMessage auditMessage = createAuditMessage(presentationPermissionSet, Status.INITIATED);
                auditMessage.withEventDescription(
                    String.format(INITIATE_CREATE_PERMISSION_SET_DESCRIPTION, presentationPermissionSet.getName()));

                if (nonNull(presentationPermissionSetItem)) {
                    auditMessage.withEventMetaDatum(ASSIGNABLE_PERMISSION_SET_FIELD_NAME,
                        getPrivilegesForFunctionGroupId(presentationPermissionSetItem));
                }

                auditMessages.add(auditMessage);
            }
        }

        return auditMessages;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        PresentationPermissionSet presentationPermissionSet = getArgument(joinPoint, PresentationPermissionSet.class);
        PresentationId presentationInternalIdResponse = ((ResponseEntity<PresentationId>) actionResult).getBody();

        List<AuditMessage> auditMessages = new ArrayList<>();

        List<PresentationPermissionSetItem> permissions = presentationPermissionSet.getPermissions();

        if (isNull(permissions) || permissions.isEmpty()) {
            AuditMessage auditMessage = createAuditMessage(presentationPermissionSet, Status.SUCCESSFUL);
            auditMessage.withEventDescription(
                String.format(SUCCESSFUL_CREATE_PERMISSION_SET_DESCRIPTION, presentationPermissionSet.getName(),
                    presentationInternalIdResponse.getId()))
                .withEventMetaDatum(ASSIGNABLE_PERMISSION_SET_ID_FIELD_NAME,
                    presentationInternalIdResponse.getId().toString());
            auditMessages.add(auditMessage);
        } else {
            for (PresentationPermissionSetItem presentationPermissionSetItem : permissions) {
                AuditMessage auditMessage = createAuditMessage(presentationPermissionSet, Status.SUCCESSFUL);
                auditMessage.withEventDescription(
                    String.format(SUCCESSFUL_CREATE_PERMISSION_SET_DESCRIPTION, presentationPermissionSet.getName(),
                        presentationInternalIdResponse.getId()))
                    .withEventMetaDatum(ASSIGNABLE_PERMISSION_SET_ID_FIELD_NAME,
                        presentationInternalIdResponse.getId().toString());

                if (nonNull(presentationPermissionSetItem)) {
                    auditMessage.withEventMetaDatum(ASSIGNABLE_PERMISSION_SET_FIELD_NAME,
                        getPrivilegesForFunctionGroupId(presentationPermissionSetItem));
                }

                auditMessages.add(auditMessage);
            }
        }

        return auditMessages;
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationPermissionSet presentationPermissionSet = getArgument(joinPoint, PresentationPermissionSet.class);

        List<AuditMessage> auditMessages = new ArrayList<>();

        List<PresentationPermissionSetItem> permissions = presentationPermissionSet.getPermissions();

        if (isNull(permissions) || permissions.isEmpty()) {
            AuditMessage auditMessage = createAuditMessage(presentationPermissionSet, Status.FAILED);
            auditMessage.withEventDescription(
                String.format(FAILED_CREATE_DATA_GROUP_DESCRIPTION, presentationPermissionSet.getName()));
            auditMessages.add(auditMessage);
        } else {
            for (PresentationPermissionSetItem presentationPermissionSetItem : permissions) {
                AuditMessage auditMessage = createAuditMessage(presentationPermissionSet, Status.FAILED);
                auditMessage.withEventDescription(
                    String.format(FAILED_CREATE_DATA_GROUP_DESCRIPTION, presentationPermissionSet.getName()));

                if (nonNull(presentationPermissionSetItem)) {
                    auditMessage.withEventMetaDatum(ASSIGNABLE_PERMISSION_SET_FIELD_NAME,
                        getPrivilegesForFunctionGroupId(presentationPermissionSetItem));
                }

                auditMessages.add(auditMessage);
            }
        }

        return auditMessages;
    }

    private AuditMessage createAuditMessage(PresentationPermissionSet presentationPermissionSet, Status status) {
        return new AuditMessage().withStatus(status)
            .withEventMetaDatum(ASSIGNABLE_PERMISSION_SET_NAME_FIELD_NAME, presentationPermissionSet.getName())
            .withEventMetaDatum(ASSIGNABLE_PERMISSION_SET_DESCRIPTION_FIELD_NAME,
                presentationPermissionSet.getDescription());
    }

    private String getPrivilegesForFunctionGroupId(PresentationPermissionSetItem presentationPermissionSetItem) {
        String functionIdPrivileges = presentationPermissionSetItem.getFunctionId() + " / ";

        if (nonNull(presentationPermissionSetItem.getPrivileges())) {
            functionIdPrivileges += presentationPermissionSetItem.getPrivileges().stream().map(a -> nonNull(a) ? a : "")
                .sorted().collect(Collectors.joining(", "));
        }
        return functionIdPrivileges;
    }
}
