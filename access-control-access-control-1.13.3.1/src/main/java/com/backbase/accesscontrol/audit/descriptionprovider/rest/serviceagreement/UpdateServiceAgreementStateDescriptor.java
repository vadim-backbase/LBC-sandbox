package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementStatePut;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceAgreementStateDescriptor extends AbstractDescriptionProvider {

    private static final String SERVICE_AGREEMENT_ID = "serviceAgreementId";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_STATE)
            .withEventAction(EventAction.UPDATE_STATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementStatePut request = DescriptorUtils.getArgument(joinPoint, ServiceAgreementStatePut.class);
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, SERVICE_AGREEMENT_ID);
        return Collections.singletonList(createBaseMessage(request, serviceAgreementId, Status.INITIATED));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        ServiceAgreementStatePut request = DescriptorUtils.getArgument(joinPoint, ServiceAgreementStatePut.class);
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, SERVICE_AGREEMENT_ID);

        AuditMessage baseMessage = createBaseMessage(request, serviceAgreementId, Status.SUCCESSFUL);

        return Collections.singletonList(baseMessage);
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementStatePut request = DescriptorUtils.getArgument(joinPoint, ServiceAgreementStatePut.class);
        String serviceAgreementId = DescriptorUtils.getPathParameter(joinPoint, SERVICE_AGREEMENT_ID);

        AuditMessage baseMessage = createBaseMessage(request, serviceAgreementId, Status.FAILED);
        return Collections.singletonList(baseMessage);
    }

    private String getDescription(Status status, String saName) {
        return "Update | Service Agreement State | " + status + " | State " + saName;
    }

    private AuditMessage createBaseMessage(ServiceAgreementStatePut body, String serviceAgreementId,
        Status status) {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId);
        metaData.put(DescriptorFieldNames.SERVICE_AGREEMENT_STATUS_FIELD_NAME, body.getState().toString());

        return new AuditMessage()
            .withStatus(status)
            .withEventMetaData(metaData)
            .withEventDescription(getDescription(status, body.getState().toString()));
    }
}
