package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.BUSINESS_FUNCTION_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_TYPE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PRIVILEGES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.WordUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class CreateFunctionGroupDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    public CreateFunctionGroupDescriptor(
        DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        FunctionGroupsPostResponseBody functionGroupResponse =
            (FunctionGroupsPostResponseBody) actionResult;
        return getAuditMessages(joinPoint, functionGroupResponse.getId());
    }

    protected List<AuditMessage> getAuditMessages(ProceedingJoinPoint joinPoint, String actionResultId) {

        FunctionGroupBase functionBase = getArgument(joinPoint, FunctionGroupBase.class);
        List<AuditMessage> auditMessages = new ArrayList<>();

        AuditMessage baseAuditMessage = createBaseAuditMessage(Status.SUCCESSFUL, functionBase);

        if (functionBase.getPermissions() == null || functionBase.getPermissions().isEmpty()) {
            auditMessages.add(baseAuditMessage);
        } else {
            functionBase.getPermissions()
                .forEach(permission -> {
                    AuditMessage message = createBaseAuditMessage(Status.SUCCESSFUL, functionBase);
                    message
                        .withEventMetaDatum(BUSINESS_FUNCTION_ID_FIELD_NAME, permission.getFunctionId())
                        .withEventMetaDatum(PRIVILEGES_FIELD_NAME, getPrivileges(permission));
                    if (!isNull(actionResultId)) {
                        message.withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, actionResultId);
                    }
                    auditMessages.add(message);
                });
        }
        return auditMessages;
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        FunctionGroupBase functionBase = getArgument(joinPoint, FunctionGroupBase.class);
        return singletonList(createBaseAuditMessage(Status.INITIATED, functionBase));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        FunctionGroupBase functionBase = getArgument(joinPoint, FunctionGroupBase.class);
        return singletonList(createBaseAuditMessage(Status.FAILED, functionBase));

    }

    protected String getPrivileges(Permission permission) {
        return permission.getAssignedPrivileges().stream().map(Privilege::getPrivilege)
            .collect(joining(","));
    }

    protected AuditMessage createBaseAuditMessage(Status status, FunctionGroupBase functionBase) {
        try {
            return new AuditMessage()
                .withEventDescription(getDescription(functionBase, status))
                .withStatus(status)
                .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, functionBase.getName())
                .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, functionBase.getDescription())
                .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, functionBase.getServiceAgreementId())
                .withEventMetaDatum(START_DATE_TIME_FIELD_NAME,
                    (dateTimeService
                        .getStringDateTime(functionBase.getValidFromDate(), functionBase.getValidFromTime())))
                .withEventMetaDatum(END_DATE_TIME_FIELD_NAME,
                    (dateTimeService
                        .getStringDateTime(functionBase.getValidUntilDate(), functionBase.getValidUntilTime())))
                .withEventMetaDatum(FUNCTION_GROUP_TYPE_FIELD_NAME, functionBase.getType().toString());
        } catch (Exception e) {
            return new AuditMessage().withStatus(status);
        }
    }

    protected String getDescription(FunctionGroupBase functionGroupPostRequestBody, Status status) {
        return EventAction.CREATE.getActionEvent() + " | Function Group "
            + WordUtils.capitalizeFully(functionGroupPostRequestBody.getType().toString())
            + " | " + status
            + " | name " + functionGroupPostRequestBody.getName()
            + ", service agreement ID " + functionGroupPostRequestBody.getServiceAgreementId();
    }

}
