package com.backbase.accesscontrol.business.persistence.serviceagreement;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EditServiceAgreementHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, ServiceAgreementSave, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditServiceAgreementHandler.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;


    public EditServiceAgreementHandler(EventBus eventBus,
        PersistenceServiceAgreementService persistenceServiceAgreementService) {
        super(eventBus);
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementSave requestData) {
        String serviceAgreementId = parameterHolder.getParameter();
        LOGGER.info("Trying to update ServiceAgreements {} ", serviceAgreementId);
        persistenceServiceAgreementService.save(serviceAgreementId, requestData);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementSave request,
        Void response) {
        return new ServiceAgreementEvent().withId(parameterHolder.getParameter()).withAction(Action.UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementSave request, Exception failure) {
        return null;
    }
}
