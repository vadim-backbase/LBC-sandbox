package com.backbase.accesscontrol.business.persistence.serviceagreement;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.ServiceAgreementData;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.ServiceAgreementIngestService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IngestServiceAgreementHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, ServiceAgreementData<ServiceAgreementIngestPostRequestBody>,
                ServiceAgreementIngestPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestServiceAgreementHandler.class);

    private ServiceAgreementIngestService serviceAgreementIngestService;

    public IngestServiceAgreementHandler(EventBus eventBus,
        ServiceAgreementIngestService serviceAgreementIngestService) {
        super(eventBus);
        this.serviceAgreementIngestService = serviceAgreementIngestService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementIngestPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> requestData) {
        LOGGER.info("Ingesting service agreement request {}, map {}", requestData.getRequest(),
            requestData.getUsersByExternalId());
        String id = serviceAgreementIngestService.ingestServiceAgreement(requestData);
        LOGGER.info("Ingested service agreement id {}", id);
        return new ServiceAgreementIngestPostResponseBody()
            .withId(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> request,
        ServiceAgreementIngestPostResponseBody response) {
        return new ServiceAgreementEvent().withId(response.getId()).withAction(Action.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder,
        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> request, Exception failure) {
        return null;
    }
}
