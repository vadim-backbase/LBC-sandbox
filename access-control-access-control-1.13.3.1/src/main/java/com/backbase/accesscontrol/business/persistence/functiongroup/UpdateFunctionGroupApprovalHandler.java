package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.FunctionGroupIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateFunctionGroupApprovalHandler extends
    LeanGenericEventEmitter<FunctionGroupIdApprovalIdParameterHolder, FunctionGroupByIdPutRequestBody, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFunctionGroupApprovalHandler.class);

    private FunctionGroupService functionGroupService;

    public UpdateFunctionGroupApprovalHandler(EventBus eventBus, FunctionGroupService functionGroupService) {
        super(eventBus);
        this.functionGroupService = functionGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(FunctionGroupIdApprovalIdParameterHolder parameterHolder,
        FunctionGroupByIdPutRequestBody requestData) {
        LOGGER.info("Handling execute request for updating function group with approval ON {}", requestData);
        functionGroupService.updateFunctionGroupApproval(requestData, parameterHolder.getFunctionGroupId(),
            parameterHolder.getApprovalId());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(FunctionGroupIdApprovalIdParameterHolder parameterHolder,
        FunctionGroupByIdPutRequestBody request, Void response) {
        return null;
    }

    @Override
    protected Event createFailureEvent(FunctionGroupIdApprovalIdParameterHolder parameterHolder,
        FunctionGroupByIdPutRequestBody request, Exception failure) {
        return null;
    }

}