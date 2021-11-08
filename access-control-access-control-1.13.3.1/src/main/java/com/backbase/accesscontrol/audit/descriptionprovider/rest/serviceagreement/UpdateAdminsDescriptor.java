package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.client.rest.spec.model.UpdateAdmins;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdateAdminsDescriptor extends AbstractDescriptionProvider {

    private static final String DESCRIPTION = "Update Admins | Service Agreement | %s | service agreement ID %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_ADMINS)
            .withEventAction(EventAction.UPDATE_ADMINS);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");

        return singletonList(new AuditMessage().withStatus(Status.INITIATED)
            .withEventDescription(getDescription(Status.INITIATED, serviceAgreementId))
            .withEventMetaDatum(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        UpdateAdmins adminsPutRequestBody = DescriptorUtils.getArgument(joinPoint, UpdateAdmins.class);
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");

        return adminsPutRequestBody.getParticipants().stream().flatMap(item ->
            item.getAdmins().stream().map(adminId ->
                new AuditMessage()
                    .withStatus(Status.SUCCESSFUL)
                    .withEventDescription(getDescription(Status.SUCCESSFUL, serviceAgreementId))
                    .withEventMetaDatum(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId)
                    .withEventMetaDatum(DescriptorFieldNames.PARTICIPANT_ID_FIELD_NAME, item.getId())
                    .withEventMetaDatum(DescriptorFieldNames.ADMIN_ID_FIELD_NAME, adminId)
            )).collect(Collectors.toList());

    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, "id");

        return singletonList(new AuditMessage().withStatus(Status.FAILED)
            .withEventDescription(getDescription(Status.FAILED, serviceAgreementId))
            .withEventMetaDatum(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId));
    }

    private String getDescription(Status status, String serviceAgreementId) {
        return String.format(DESCRIPTION, status, serviceAgreementId);
    }
}
