package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.BUSINESS_FUNCTION_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_APS_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_APS_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_TYPE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PRIVILEGES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermission;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIngestFunctionGroup;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.WordUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class IngestFunctionGroupDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    public IngestFunctionGroupDescriptor(
        DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.FUNCTION_GROUP_INGEST)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationIngestFunctionGroup functionBase = getArgument(joinPoint, PresentationIngestFunctionGroup.class);
        return singletonList(createBaseAuditMessage(Status.INITIATED, functionBase));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        IdItem response = ((ResponseEntity<IdItem>) actionResult).getBody();
        PresentationIngestFunctionGroup functionBase = getArgument(joinPoint, PresentationIngestFunctionGroup.class);
        List<AuditMessage> auditMessages = new ArrayList<>();

        AuditMessage baseAuditMessage = createBaseAuditMessage(Status.SUCCESSFUL, functionBase);

        if (functionBase.getPermissions() == null || functionBase.getPermissions().isEmpty()) {
            baseAuditMessage
                .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, response.getId());
            auditMessages.add(baseAuditMessage);
        } else {
            functionBase.getPermissions()
                .forEach(permission -> {
                    AuditMessage message = createBaseAuditMessage(Status.SUCCESSFUL, functionBase);
                    message.withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME,
                        response.getId());
                    message
                        .withEventMetaDatum(BUSINESS_FUNCTION_ID_FIELD_NAME, permission.getFunctionId())
                        .withEventMetaDatum(PRIVILEGES_FIELD_NAME, getPrivilegesForPermission(permission));
                    auditMessages.add(message);
                });
        }
        return auditMessages;
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationIngestFunctionGroup functionBase = getArgument(joinPoint, PresentationIngestFunctionGroup.class);
        return singletonList(createBaseAuditMessage(Status.FAILED, functionBase));
    }

    private AuditMessage createBaseAuditMessage(Status status, PresentationIngestFunctionGroup functionBase) {
        try {
            AuditMessage auditMessage = new AuditMessage()
                .withStatus(status)
                .withEventDescription(getDescription(functionBase, status))
                .withEventMetaDatum(FUNCTION_GROUP_NAME_FIELD_NAME, functionBase.getName())
                .withEventMetaDatum(FUNCTION_GROUP_DESCRIPTION_FIELD_NAME, functionBase.getDescription())
                .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                    functionBase.getExternalServiceAgreementId())
                .withEventMetaDatum(START_DATE_TIME_FIELD_NAME,
                    dateTimeService.getStringDateTime(functionBase.getValidFromDate(), functionBase.getValidFromTime()))
                .withEventMetaDatum(END_DATE_TIME_FIELD_NAME,
                    dateTimeService
                        .getStringDateTime(functionBase.getValidUntilDate(), functionBase.getValidUntilTime()))
                .withEventMetaDatum(FUNCTION_GROUP_TYPE_FIELD_NAME, functionBase.getType().toString());
            if (Objects.nonNull(functionBase.getApsId())) {
                auditMessage.withEventMetaDatum(FUNCTION_GROUP_APS_ID_FIELD_NAME, functionBase.getApsId().toString());
            }
            if (!Strings.isNullOrEmpty(functionBase.getApsName())) {
                auditMessage.withEventMetaDatum(FUNCTION_GROUP_APS_NAME_FIELD_NAME, functionBase.getApsName());
            }
            return auditMessage;
        } catch (Exception e) {
            return new AuditMessage().withStatus(status);
        }
    }

    private String getDescription(PresentationIngestFunctionGroup functionGroupBase,
        Status status) {
        return "Create" + " | Function Group "
            + WordUtils.capitalizeFully(functionGroupBase.getType().toString())
            + " | "
            + status
            + " | name " + functionGroupBase.getName()
            + ", external service agreement ID " + functionGroupBase.getExternalServiceAgreementId();
    }

    private String getPrivilegesForPermission(PresentationPermission permission) {
        return String.join(",", permission.getPrivileges());
    }
}
