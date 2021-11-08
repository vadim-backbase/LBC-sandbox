package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.aps;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ADMIN_USER_APS_IDS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ADMIN_USER_APS_NAMES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.REGULAR_USER_APS_IDS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.REGULAR_USER_APS_NAMES_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.populateSuccessUserApsIdentifiersMetadata;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.populateUserApsIdentifiersMetadata;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetPut;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdatePermissionSetDescriptor extends AbstractDescriptionProvider {

    private static final String UPDATE_PERMISSION_SET_PREFIX = "Update | "
        + "Associated Assignable Permission Sets to a Service Agreement | ";
    private static final String INITIATE_UPDATE_PERMISSION_SET_DESCRIPTION = UPDATE_PERMISSION_SET_PREFIX
        + "Initiated | External Service Agreement ID %s";
    private static final String SUCCESSFUL_UPDATE_PERMISSION_SET_DESCRIPTION = UPDATE_PERMISSION_SET_PREFIX
        + "Successful | External Service Agreement ID %s";
    private static final String FAILED_UPDATE_DATA_GROUP_DESCRIPTION = UPDATE_PERMISSION_SET_PREFIX
        + "Failed | External Service Agreement ID %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.PERMISSION_SET)
            .withEventAction(EventAction.UPDATE_ASSOCIATED_APS);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        PresentationPermissionSetPut requestBody = getArgument(joinPoint, PresentationPermissionSetPut.class);
        return Collections
            .singletonList(createSuccessMessage(requestBody));
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationPermissionSetPut requestBody = getArgument(joinPoint, PresentationPermissionSetPut.class);
        return Collections
            .singletonList(
                createBaseAuditMessage(requestBody, Status.INITIATED, INITIATE_UPDATE_PERMISSION_SET_DESCRIPTION));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        PresentationPermissionSetPut requestBody = getArgument(joinPoint, PresentationPermissionSetPut.class);
        return Collections
            .singletonList(createBaseAuditMessage(requestBody, Status.FAILED, FAILED_UPDATE_DATA_GROUP_DESCRIPTION));

    }

    private AuditMessage createSuccessMessage(PresentationPermissionSetPut requestBody) {
        Map<String, String> metaData = new HashMap<>();
        try {

            metaData.put(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, requestBody.getExternalServiceAgreementId());

            AuditMessage auditMessage = new AuditMessage().withStatus(Status.SUCCESSFUL)
                .withEventMetaData(metaData)
                .withEventDescription(String
                    .format(SUCCESSFUL_UPDATE_PERMISSION_SET_DESCRIPTION, requestBody.getExternalServiceAgreementId()));

            PresentationUserApsIdentifiers regularUserAps = requestBody.getRegularUserAps();
            PresentationUserApsIdentifiers adminUserAps = requestBody.getAdminUserAps();
            populateSuccessUserApsIdentifiersMetadata(auditMessage, regularUserAps, REGULAR_USER_APS_IDS_FIELD_NAME,
                REGULAR_USER_APS_NAMES_FIELD_NAME);
            populateSuccessUserApsIdentifiersMetadata(auditMessage, adminUserAps, ADMIN_USER_APS_IDS_FIELD_NAME,
                ADMIN_USER_APS_NAMES_FIELD_NAME);

            return auditMessage;
        } catch (Exception e) {
            return new AuditMessage().withStatus(Status.FAILED);
        }
    }

    private AuditMessage createBaseAuditMessage(
        PresentationPermissionSetPut requestBody, Status status, String description) {
        Map<String, String> metaData = new HashMap<>();
        try {
            metaData.put(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, requestBody.getExternalServiceAgreementId());

            AuditMessage auditMessage = new AuditMessage().withStatus(status)
                .withEventMetaData(metaData)
                .withEventDescription(String
                    .format(description, requestBody.getExternalServiceAgreementId()));

            populateUserApsIdentifiersMetadata(auditMessage, requestBody.getRegularUserAps(),
                REGULAR_USER_APS_IDS_FIELD_NAME, REGULAR_USER_APS_NAMES_FIELD_NAME);

            populateUserApsIdentifiersMetadata(auditMessage, requestBody.getAdminUserAps(),
                ADMIN_USER_APS_IDS_FIELD_NAME, ADMIN_USER_APS_NAMES_FIELD_NAME);

            return auditMessage;
        } catch (Exception e) {
            return new AuditMessage().withStatus(status);
        }
    }
}
