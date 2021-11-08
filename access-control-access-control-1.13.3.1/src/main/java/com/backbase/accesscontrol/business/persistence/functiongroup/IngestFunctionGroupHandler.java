package com.backbase.accesscontrol.business.persistence.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.IngestFunctionGroupTransformService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class IngestFunctionGroupHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, FunctionGroupIngest,
        PresentationIngestFunctionGroupPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestFunctionGroupHandler.class);

    private IngestFunctionGroupTransformService ingestFunctionGroupTransformService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    public IngestFunctionGroupHandler(EventBus eventBus,
        IngestFunctionGroupTransformService ingestFunctionGroupTransformService,
        ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService) {
        super(eventBus);
        this.ingestFunctionGroupTransformService = ingestFunctionGroupTransformService;
        this.serviceAgreementBusinessRulesService = serviceAgreementBusinessRulesService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PresentationIngestFunctionGroupPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        FunctionGroupIngest requestData) {
        LOGGER.info("Creating function group {}", requestData);
        if (serviceAgreementBusinessRulesService
            .isServiceAgreementInPendingStateByExternalId(requestData.getExternalServiceAgreementId())) {

            throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
        }
        return new PresentationIngestFunctionGroupPostResponseBody()
            .withId(ingestFunctionGroupTransformService.addFunctionGroup(requestData));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FunctionGroupEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        FunctionGroupIngest request, PresentationIngestFunctionGroupPostResponseBody response) {
        return new FunctionGroupEvent().withId(response.getId()).withAction(Action.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder,
        FunctionGroupIngest request, Exception failure) {
        return null;
    }
}

