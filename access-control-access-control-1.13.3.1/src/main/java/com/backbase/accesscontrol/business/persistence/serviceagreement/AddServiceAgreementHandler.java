package com.backbase.accesscontrol.business.persistence.serviceagreement;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddServiceAgreementHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, ServiceAgreementPostRequestBody, ServiceAgreementPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddServiceAgreementHandler.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    public AddServiceAgreementHandler(EventBus eventBus,
        PersistenceServiceAgreementService persistenceServiceAgreementService) {
        super(eventBus);
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementPostResponseBody executeRequest(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementPostRequestBody requestData) {
        LOGGER.info("Trying to save Service Agreement {} ", requestData.getName());
        ServiceAgreement serviceAgreement = persistenceServiceAgreementService
            .save(requestData, parameterHolder.getParameter());
        return new ServiceAgreementPostResponseBody().withId(serviceAgreement.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementPostRequestBody request, ServiceAgreementPostResponseBody response) {
        return new ServiceAgreementEvent().withId(response.getId()).withAction(Action.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementPostRequestBody request,
        Exception failure) {
        return null;
    }

}
