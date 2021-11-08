package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class UpdateServiceAgreementAdminsHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, AdminsPutRequestBody, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceAgreementAdminsHandler.class);

    private ServiceAgreementAdminService serviceAgreementAdminService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    public UpdateServiceAgreementAdminsHandler(EventBus eventBus,
        ServiceAgreementAdminService serviceAgreementAdminService,
        ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService) {
        super(eventBus);
        this.serviceAgreementAdminService = serviceAgreementAdminService;
        this.serviceAgreementBusinessRulesService = serviceAgreementBusinessRulesService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder, AdminsPutRequestBody requestData) {
        String serviceAgreementId = parameterHolder.getParameter();
        LOGGER.info("Trying to update admins {} on service agreement", serviceAgreementId);
        if (serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(serviceAgreementId)) {
            throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
        }

        serviceAgreementBusinessRulesService.checkPendingValidationsInServiceAgreement(serviceAgreementId);
        serviceAgreementBusinessRulesService
            .checkPendingDeleteOfFunctionOrDataGroupInServiceAgreement(serviceAgreementId);

        serviceAgreementAdminService.updateAdmins(serviceAgreementId, requestData);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(SingleParameterHolder<String> parameterHolder, AdminsPutRequestBody request,
        Void response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder, AdminsPutRequestBody request,
        Exception failure) {
        return null;
    }

}
