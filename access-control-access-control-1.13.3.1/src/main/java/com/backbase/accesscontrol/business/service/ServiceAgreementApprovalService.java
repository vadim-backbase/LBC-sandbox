package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_042;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.business.persistence.serviceagreement.AddServiceAgreementApprovalHandler;
import com.backbase.accesscontrol.business.persistence.serviceagreement.AddServiceAgreementHandler;
import com.backbase.accesscontrol.business.persistence.serviceagreement.EditServiceAgreementApprovalHandler;
import com.backbase.accesscontrol.business.persistence.serviceagreement.EditServiceAgreementHandler;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementValidator;
import com.backbase.accesscontrol.dto.parameterholder.LegalEntityIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.ServiceAgreementIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.mappers.ServiceAgreementDtoMapper;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A service class that communicates with the persistence services via handler and returns responses.
 */
@Service
@AllArgsConstructor
public class ServiceAgreementApprovalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementApprovalService.class);

    private AddServiceAgreementHandler addServiceAgreementHandler;
    private AddServiceAgreementApprovalHandler addServiceAgreementApprovalHandler;
    private EditServiceAgreementHandler editServiceAgreementHandler;
    private EditServiceAgreementApprovalHandler editServiceAgreementApprovalHandler;

    private ServiceAgreementValidator serviceAgreementValidator;
    private Validator validator;

    private ServiceAgreementDtoMapper serviceAgreementDtoMapper;

    /**
     * Method for creating service agreement. This method simply creates service agreement.
     *
     * @param serviceAgreementPostRequestBody presentation payload {@link ServiceAgreementPostResponseBody}
     * @param legalEntityId                   creators legal entity id
     * @return {@link ServiceAgreementPostResponseBody}
     */
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.SERVICE_AGREEMENT)
    public ServiceAgreementPostResponseBody createServiceAgreement(
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody, String legalEntityId) {

        serviceAgreementValidator.validatePayload(
            serviceAgreementDtoMapper.fromServiceAgreementPostRequestBody(serviceAgreementPostRequestBody));

        return addServiceAgreementHandler
            .handleRequest(new SingleParameterHolder<>(legalEntityId), serviceAgreementPostRequestBody);
    }

    /**
     * Create approval service agreement record in database. Temporary record for service agreement.
     *
     * @param serviceAgreementPostRequestBody serviceAgreementPostRequestBody object
     * @param legalEntityId                   creators legal entity id
     * @return {@link ServiceAgreementPostResponseBody}
     */
    @AuditEvent(eventAction = EventAction.CREATE_PENDING, objectType = AuditObjectType.SERVICE_AGREEMENT_APPROVAL)
    public ServiceAgreementPostResponseBody createServiceAgreementWithApproval(
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody, String legalEntityId, String approvalId) {

        serviceAgreementValidator.validatePayload(
            serviceAgreementDtoMapper.fromServiceAgreementPostRequestBody(serviceAgreementPostRequestBody));

        return addServiceAgreementApprovalHandler
            .handleRequest(new LegalEntityIdApprovalIdParameterHolder(legalEntityId, approvalId),
                serviceAgreementPostRequestBody);
    }

    @AuditEvent(eventAction = EventAction.UPDATE_PENDING, objectType = AuditObjectType.SERVICE_AGREEMENT_APPROVAL)
    public void updateServiceAgreementWithApproval(ServiceAgreementSave serviceAgreementSave, String serviceAgreementId,
        String approvalId) {

        validate(serviceAgreementSave);

        editServiceAgreementApprovalHandler
            .handleRequest(new ServiceAgreementIdApprovalIdParameterHolder(serviceAgreementId, approvalId),
                serviceAgreementSave);
    }


    @AuditEvent(eventAction = EventAction.UPDATE, objectType = AuditObjectType.SAVE_SERVICE_AGREEMENT)
    public void updateServiceAgreement(ServiceAgreementSave serviceAgreementSave, String serviceAgreementId) {

        validate(serviceAgreementSave);

        editServiceAgreementHandler
            .handleRequest(new SingleParameterHolder<>(serviceAgreementId), serviceAgreementSave);
    }

    private void validate(ServiceAgreementSave serviceAgreementSave) {

        Set<ConstraintViolation<ServiceAgreementSave>> violations = validator.validate(serviceAgreementSave);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                .map(violation -> violation.getPropertyPath().toString() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));

            LOGGER.warn("Violations are {}", errorMsg);
            throw getBadRequestException(ERR_ACQ_042.getErrorMessage() + " " + errorMsg, ERR_ACQ_042.getErrorCode());
        }

        serviceAgreementValidator
            .validatePayload(serviceAgreementDtoMapper.fromServiceAgreementSave(serviceAgreementSave));
    }
}
