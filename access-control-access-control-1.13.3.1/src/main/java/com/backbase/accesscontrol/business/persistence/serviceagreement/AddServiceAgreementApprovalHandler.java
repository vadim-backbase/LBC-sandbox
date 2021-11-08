package com.backbase.accesscontrol.business.persistence.serviceagreement;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.LegalEntityIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddServiceAgreementApprovalHandler extends
    LeanGenericEventEmitter<LegalEntityIdApprovalIdParameterHolder, ServiceAgreementPostRequestBody, ServiceAgreementPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddServiceAgreementApprovalHandler.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    public AddServiceAgreementApprovalHandler(EventBus eventBus,
        PersistenceServiceAgreementService persistenceServiceAgreementService) {
        super(eventBus);
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementPostResponseBody executeRequest(LegalEntityIdApprovalIdParameterHolder parameterHolder,
        ServiceAgreementPostRequestBody requestData) {
        LOGGER.info("Trying to save Service Agreement with approval ON {} ", requestData.getName());
        String approvalId = persistenceServiceAgreementService
            .saveServiceAgreementApproval(requestData, parameterHolder.getLegalEntityId(),
                parameterHolder.getApprovalId());
        return new ServiceAgreementPostResponseBody().withId(approvalId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(LegalEntityIdApprovalIdParameterHolder parameterHolder,
        ServiceAgreementPostRequestBody request, ServiceAgreementPostResponseBody response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(LegalEntityIdApprovalIdParameterHolder parameterHolder,
        ServiceAgreementPostRequestBody request, Exception failure) {
        return null;
    }

}
