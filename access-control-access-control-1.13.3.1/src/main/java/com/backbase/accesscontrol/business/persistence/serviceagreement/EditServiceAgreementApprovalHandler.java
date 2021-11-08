package com.backbase.accesscontrol.business.persistence.serviceagreement;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.parameterholder.ServiceAgreementIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EditServiceAgreementApprovalHandler extends
    LeanGenericEventEmitter<ServiceAgreementIdApprovalIdParameterHolder, ServiceAgreementSave, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditServiceAgreementApprovalHandler.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    public EditServiceAgreementApprovalHandler(EventBus eventBus,
        PersistenceServiceAgreementService persistenceServiceAgreementService,
        ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService) {
        super(eventBus);
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
        this.serviceAgreementBusinessRulesService = serviceAgreementBusinessRulesService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(ServiceAgreementIdApprovalIdParameterHolder parameterHolder,
        ServiceAgreementSave requestData) {
        String serviceAgreementId = parameterHolder.getServiceAgreementId();
        LOGGER.info("Trying to update ServiceAgreements {} ", serviceAgreementId);
        serviceAgreementBusinessRulesService.checkPendingValidationsInServiceAgreement(serviceAgreementId);
        serviceAgreementBusinessRulesService
            .checkPendingDeleteOfFunctionOrDataGroupInServiceAgreement(serviceAgreementId);
        persistenceServiceAgreementService
            .updateServiceAgreementApproval(requestData, parameterHolder.getServiceAgreementId(),
                parameterHolder.getApprovalId());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(ServiceAgreementIdApprovalIdParameterHolder parameterHolder,
        ServiceAgreementSave request, Void response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(ServiceAgreementIdApprovalIdParameterHolder parameterHolder,
        ServiceAgreementSave request, Exception failure) {
        return null;
    }
}
