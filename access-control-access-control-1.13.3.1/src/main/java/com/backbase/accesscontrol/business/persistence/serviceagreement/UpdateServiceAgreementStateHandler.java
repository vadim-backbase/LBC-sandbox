package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementStatePutRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceAgreementStateHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, ServiceAgreementStatePutRequestBody, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceAgreementStateHandler.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    public UpdateServiceAgreementStateHandler(EventBus eventBus,
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
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementStatePutRequestBody requestData) {
        LOGGER.info("Trying to update ServiceAgreements State");
        ServiceAgreementState state = ServiceAgreementState.fromString(requestData.getState().toString());
        String serviceAgreementId = parameterHolder.getParameter();
        ServiceAgreementItem serviceAgreement =
            persistenceServiceAgreementService.getServiceAgreementResponseBodyById(serviceAgreementId);
        if (state == ServiceAgreementState.DISABLED && serviceAgreementBusinessRulesService
            .isServiceAgreementRootMasterServiceAgreement(serviceAgreement)) {
            LOGGER
                .warn("Service Agreement is master Service Agreement and can not be disabled.");
            throw getBadRequestException(AccessGroupErrorCodes.ERR_AG_070.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_070.getErrorCode());
        }
        persistenceServiceAgreementService.updateServiceAgreementState(serviceAgreementId, state);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementStatePutRequestBody request, Void response) {
        return new ServiceAgreementEvent()
            .withAction(Action.UPDATE)
            .withId(parameterHolder.getParameter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        ServiceAgreementStatePutRequestBody request, Exception failure) {
        return null;
    }
}
