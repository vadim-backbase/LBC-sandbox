package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_STATUS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementPut;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class IngestUpdateServiceAgreementDescriptor extends AbstractDescriptionProvider {

    private DateTimeService dateTimeService;

    /**
     * Autowire constructor.
     */
    public IngestUpdateServiceAgreementDescriptor(
        DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_SERVICE)
            .withEventAction(EventAction.UPDATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementPut request = getArgument(joinPoint, ServiceAgreementPut.class);
        String serviceAgreementId = getArgument(joinPoint, String.class);
        try {
            return singletonList(new AuditMessage()
                .withStatus(Status.INITIATED)
                .withEventDescription(
                    getDescription(Status.INITIATED.toString(),
                        request.getName()))
                .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId)
                .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, request.getExternalId())
                .withEventMetaDatum(SERVICE_AGREEMENT_NAME_FIELD_NAME, request.getName())
                .withEventMetaDatum(SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, request.getDescription())
                .withEventMetaDatum(SERVICE_AGREEMENT_STATUS_FIELD_NAME,
                    Objects.nonNull(request.getStatus()) ? request.getStatus().toString() : "")
                .withEventMetaDatum(START_DATE_TIME_FIELD_NAME,
                    dateTimeService.getStringDateTime(request.getValidFromDate(), request.getValidFromTime()))
                .withEventMetaDatum(END_DATE_TIME_FIELD_NAME,
                    dateTimeService.getStringDateTime(request.getValidUntilDate(), request.getValidUntilTime()))
            );
        } catch (Exception e) {
            return singletonList(new AuditMessage().withStatus(Status.INITIATED));
        }
    }

    private String getDescription(String status, String name) {
        return "Update | Service Agreement | " + status + " | name " + name;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        try {
            ServiceAgreementPut request = getArgument(joinPoint, ServiceAgreementPut.class);
            String serviceAgreementId = getArgument(joinPoint, String.class);

            Map<String, String> metaData = new HashMap<>();
            metaData.put(SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId);
            addPropertyToMetaData(metaData, SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, request.getExternalId());
            addPropertyToMetaData(metaData, SERVICE_AGREEMENT_NAME_FIELD_NAME, request.getName());
            addPropertyToMetaData(metaData, SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, request.getDescription());

            if (nonNull(request.getStatus())) {
                metaData.put(SERVICE_AGREEMENT_STATUS_FIELD_NAME, request.getStatus().toString());
            }

            metaData.put(START_DATE_TIME_FIELD_NAME,
                dateTimeService.getStringDateTime(request.getValidFromDate(), request.getValidFromTime()));
            metaData.put(END_DATE_TIME_FIELD_NAME,
                dateTimeService.getStringDateTime(request.getValidUntilDate(), request.getValidUntilTime()));

            return singletonList(new AuditMessage()
                .withStatus(Status.SUCCESSFUL)
                .withEventMetaData(metaData)
                .withEventDescription(getDescription(Status.SUCCESSFUL.toString(), request.getName()))
            );
        } catch (Exception e) {
            return singletonList(new AuditMessage().withStatus(Status.FAILED));
        }
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        ServiceAgreementPut request = getArgument(joinPoint, ServiceAgreementPut.class);
        String serviceAgreementId = getArgument(joinPoint, String.class);
        try {
            return singletonList(new AuditMessage()
                .withStatus(Status.FAILED)
                .withEventDescription(
                    getDescription(Status.FAILED.toString(),
                        request.getName()))
                .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, serviceAgreementId)
                .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, request.getExternalId())
                .withEventMetaDatum(SERVICE_AGREEMENT_NAME_FIELD_NAME, request.getName())
                .withEventMetaDatum(SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, request.getDescription())
                .withEventMetaDatum(SERVICE_AGREEMENT_STATUS_FIELD_NAME,
                    Objects.nonNull(request.getStatus()) ? request.getStatus().toString() : "")
                .withEventMetaDatum(START_DATE_TIME_FIELD_NAME,
                    dateTimeService.getStringDateTime(request.getValidFromDate(), request.getValidFromTime()))
                .withEventMetaDatum(END_DATE_TIME_FIELD_NAME,
                    dateTimeService.getStringDateTime(request.getValidUntilDate(), request.getValidUntilTime()))
            );
        } catch (Exception e) {
            return singletonList(new AuditMessage().withStatus(Status.FAILED));
        }
    }

    private void addPropertyToMetaData(Map<String, String> metaData,
        String propertyName, String propertyValue) {
        if (nonNull(propertyValue)) {
            metaData.put(propertyName, propertyValue);
        }
    }
}
