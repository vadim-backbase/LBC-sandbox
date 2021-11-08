package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_TYPE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupItemSystemBase;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.google.common.base.Strings;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CreateDataGroupServiceDescriptor extends AbstractDescriptionProvider {

    private static final String CREATE_DATA_GROUP_PREFIX = "Create | Data Group | ";
    private static final String INITIATE_CREATE_DATA_GROUP_DESCRIPTION_BY_EXTRNAL_SA_ID = CREATE_DATA_GROUP_PREFIX
        + "Initiated | name %s, external service agreement ID %s, type %s";
    private static final String INITIATE_CREATE_DATA_GROUP_DESCRIPTION_BY_INTERNAL_SA_ID = CREATE_DATA_GROUP_PREFIX
        + "Initiated | name %s, service agreement ID %s, type %s";
    private static final String SUCCESSFUL_CREATE_DATA_GROUP_DESCRIPTION_BY_EXTRNAL_SA_ID = CREATE_DATA_GROUP_PREFIX
        + "Successful | name %s, external service agreement ID %s, type %s";
    private static final String SUCCESSFUL_CREATE_DATA_GROUP_DESCRIPTION_BY_INTERNAL_SA_ID = CREATE_DATA_GROUP_PREFIX
        + "Successful | name %s, service agreement ID %s, type %s";
    private static final String FAILED_CREATE_DATA_GROUP_DESCRIPTION_BY_EXTRNAL_SA_ID = CREATE_DATA_GROUP_PREFIX
        + "Failed | name %s, external service agreement ID %s, type %s";
    private static final String FAILED_CREATE_DATA_GROUP_DESCRIPTION_BY_INTERNAL_SA_ID = CREATE_DATA_GROUP_PREFIX
        + "Failed | name %s, service agreement ID %s, type %s";

    @Override
    public AuditEventAction getAuditEventAction() {

        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP_SERVICE)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        DataGroupItemSystemBase dataBase = getArgument(joinPoint, DataGroupItemSystemBase.class);

        AuditMessage auditMessage = createAuditMessage(dataBase);

        auditMessage.setStatus(Status.INITIATED);

        assignServiceAgreementIdAndDescription(dataBase, auditMessage,
            INITIATE_CREATE_DATA_GROUP_DESCRIPTION_BY_EXTRNAL_SA_ID,
            INITIATE_CREATE_DATA_GROUP_DESCRIPTION_BY_INTERNAL_SA_ID);
        return singletonList(auditMessage);
    }

    private void assignServiceAgreementIdAndDescription(DataGroupItemSystemBase dataBase, AuditMessage auditMessage,
        String externalServiceAgrementIdDescription, String internalServiceAgreementIdDescription) {
        if (!Strings.isNullOrEmpty(dataBase.getExternalServiceAgreementId())) {
            auditMessage.setEventDescription(String.format(
                externalServiceAgrementIdDescription,
                dataBase.getName(),
                dataBase.getExternalServiceAgreementId(),
                dataBase.getType()));
            auditMessage.putEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME,
                dataBase.getExternalServiceAgreementId());
        } else {
            auditMessage.setEventDescription(String.format(
                internalServiceAgreementIdDescription,
                dataBase.getName(),
                dataBase.getServiceAgreementId(),
                dataBase.getType()));
            auditMessage.putEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME,
                dataBase.getServiceAgreementId());
        }
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        IdItem dataGroupsPostResponseBody = ((ResponseEntity<IdItem>) actionResult).getBody();
        DataGroupItemSystemBase dataBase = getArgument(joinPoint, DataGroupItemSystemBase.class);

        AuditMessage auditMessage = createAuditMessage(dataBase);

        auditMessage.setStatus(Status.SUCCESSFUL);
        auditMessage.putEventMetaDatum(DATA_GROUP_ID_FIELD_NAME, dataGroupsPostResponseBody.getId());

        assignServiceAgreementIdAndDescription(dataBase, auditMessage,
            SUCCESSFUL_CREATE_DATA_GROUP_DESCRIPTION_BY_EXTRNAL_SA_ID,
            SUCCESSFUL_CREATE_DATA_GROUP_DESCRIPTION_BY_INTERNAL_SA_ID);

        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        DataGroupItemSystemBase dataBase = getArgument(joinPoint, DataGroupItemSystemBase.class);
        AuditMessage auditMessage = createAuditMessage(dataBase);

        auditMessage.setStatus(Status.FAILED);
        assignServiceAgreementIdAndDescription(dataBase, auditMessage,
            FAILED_CREATE_DATA_GROUP_DESCRIPTION_BY_EXTRNAL_SA_ID,
            FAILED_CREATE_DATA_GROUP_DESCRIPTION_BY_INTERNAL_SA_ID);

        return singletonList(auditMessage);
    }

    private AuditMessage createAuditMessage(DataGroupItemSystemBase dataBase) {
        return new AuditMessage()
            .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, dataBase.getName())
            .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, dataBase.getDescription())
            .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, dataBase.getType());
    }
}
