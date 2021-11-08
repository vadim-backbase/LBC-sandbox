package com.backbase.accesscontrol.audit.descriptionprovider.rest.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_TYPE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class CreateDataGroupDescriptor extends AbstractDescriptionProvider {

    private static final String CREATE_DATA_GROUP_PREFIX = "Create | Data Group | ";
    protected String initialDescription = CREATE_DATA_GROUP_PREFIX
        + "Initiated | name %s, service agreement ID %s, type %s";
    protected String successfulDescription = CREATE_DATA_GROUP_PREFIX
        + "Successful | name %s, service agreement ID %s, type %s";
    protected String failedDescription = CREATE_DATA_GROUP_PREFIX
        + "Failed | name %s, service agreement ID %s, type %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.DATA_GROUP)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        DataGroupBase dataBase = getArgument(joinPoint, DataGroupBase.class);

        AuditMessage auditMessage = createAuditMessage(dataBase, initialDescription);

        auditMessage.setStatus(Status.INITIATED);

        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        DataGroupsPostResponseBody dataGroupsPostResponseBody =
            ((DataGroupsPostResponseBody) actionResult);
        DataGroupBase dataBase = getArgument(joinPoint, DataGroupBase.class);

        AuditMessage auditMessage = createAuditMessage(dataBase, successfulDescription);

        auditMessage.setStatus(Status.SUCCESSFUL);
        auditMessage.putEventMetaDatum(DATA_GROUP_ID_FIELD_NAME, dataGroupsPostResponseBody.getId());

        return singletonList(auditMessage);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        DataGroupBase dataBase = getArgument(joinPoint, DataGroupBase.class);

        AuditMessage auditMessage = createAuditMessage(dataBase, failedDescription);

        auditMessage.setStatus(Status.FAILED);

        return singletonList(auditMessage);
    }

    protected AuditMessage createAuditMessage(DataGroupBase dataBase, String description) {
        return new AuditMessage()
            .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, dataBase.getName())
            .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, dataBase.getDescription())
            .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, dataBase.getType())
            .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME,
                dataBase.getServiceAgreementId())
            .withEventDescription(String.format(
                description,
                dataBase.getName(),
                dataBase.getServiceAgreementId(),
                dataBase.getType()));
    }
}
