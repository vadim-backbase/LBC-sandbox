package com.backbase.accesscontrol.business.persistence.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoveUsersFromServiceAgreementHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, List<UsersDto>, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveUsersFromServiceAgreementHandler.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;


    public RemoveUsersFromServiceAgreementHandler(
        EventBus eventBus, PersistenceServiceAgreementService persistenceServiceAgreementService,
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
        List<UsersDto> requestData) {
        String serviceAgreementId = parameterHolder.getParameter();
        LOGGER.info("Trying to remove participant's users for Service Agreement {} ", serviceAgreementId);
        if (serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(serviceAgreementId)) {
            throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
        }
        serviceAgreementBusinessRulesService.checkPendingValidationsInServiceAgreement(serviceAgreementId);
        serviceAgreementBusinessRulesService
            .checkPendingDeleteOfFunctionOrDataGroupInServiceAgreement(serviceAgreementId);
        persistenceServiceAgreementService.removeUsersFromServiceAgreement(serviceAgreementId, requestData);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        List<UsersDto> request, Void response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        List<UsersDto> request, Exception failure) {
        return null;
    }
}
