package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdateFunctionGroupDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    public UpdateFunctionGroupDescriptor(
        DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP)
            .withEventAction(EventAction.UPDATE);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        FunctionGroupByIdPutRequestBody functionBase =
            DescriptorUtils.getArgument(joinPoint, FunctionGroupByIdPutRequestBody.class);
        String functionGroupId = extractId(joinPoint);
        List<AuditMessage> auditMessages = new ArrayList<>();

        AuditMessage baseAuditMessage = createBaseAuditMessage(functionBase, Status.SUCCESSFUL, functionGroupId);
        baseAuditMessage.withEventMetaDatum(DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME, functionGroupId);
        if (functionBase.getPermissions() == null || functionBase.getPermissions().isEmpty()) {
            auditMessages.add(baseAuditMessage);
        } else {
            functionBase.getPermissions()
                .forEach(permission -> {
                    AuditMessage message = createBaseAuditMessage(functionBase, Status.SUCCESSFUL, functionGroupId);
                    message
                        .withEventMetaDatum(DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME, functionGroupId)
                        .withEventMetaDatum(DescriptorFieldNames.BUSINESS_FUNCTION_ID_FIELD_NAME, permission.getFunctionId())
                        .withEventMetaDatum(DescriptorFieldNames.PRIVILEGES_FIELD_NAME, getPrivilegesPerPermission(permission));
                    auditMessages.add(message);
                });
        }
        return auditMessages;
    }

    protected String extractId(ProceedingJoinPoint joinPoint) {
        return DescriptorUtils.getArgument(joinPoint, String.class, 0);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        FunctionGroupBase functionBase = DescriptorUtils.getArgument(joinPoint, FunctionGroupByIdPutRequestBody.class);
        String functionGroupId = extractId(joinPoint);
        return singletonList(createBaseAuditMessage(functionBase, Status.INITIATED, functionGroupId));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        FunctionGroupBase functionBase = DescriptorUtils.getArgument(joinPoint, FunctionGroupByIdPutRequestBody.class);
        String functionGroupId = extractId(joinPoint);
        return singletonList(createBaseAuditMessage(functionBase, Status.FAILED, functionGroupId));

    }

    private String getPrivilegesPerPermission(Permission permission) {
        return permission.getAssignedPrivileges().stream().map(Privilege::getPrivilege)
            .collect(joining(","));
    }

    private AuditMessage createBaseAuditMessage(
        FunctionGroupBase functionBase,
        Status status,
        String functionGroupId) {
        try {
            return new AuditMessage()
                .withStatus(status)
                .withEventDescription(getDescription(status, functionGroupId))
                .withEventMetaDatum(DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME, functionBase.getName())
                .withEventMetaDatum(DescriptorFieldNames.FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, functionBase.getDescription())
                .withEventMetaDatum(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, functionBase.getServiceAgreementId())
                .withEventMetaDatum(DescriptorFieldNames.START_DATE_TIME_FIELD_NAME,
                    (dateTimeService
                        .getStringDateTime(functionBase.getValidFromDate(), functionBase.getValidFromTime())))
                .withEventMetaDatum(DescriptorFieldNames.END_DATE_TIME_FIELD_NAME,
                    (dateTimeService
                        .getStringDateTime(functionBase.getValidUntilDate(), functionBase.getValidUntilTime())));
        } catch (Exception e) {
            return new AuditMessage().withStatus(status);
        }
    }

    protected String getDescription(Status status, String functionGroupId) {
        return EventAction.UPDATE.getActionEvent() + " | Function Group | " + status
            + " | ID " + functionGroupId;
    }

}
