package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateFunctionGroupHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, FunctionGroupByIdPutRequestBody, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFunctionGroupHandler.class);
    private FunctionGroupService functionGroupService;
    private FunctionGroupMapper functionGroupMapper;

    public UpdateFunctionGroupHandler(EventBus eventBus, FunctionGroupService functionGroupService,
        FunctionGroupMapper functionGroupMapper) {
        super(eventBus);
        this.functionGroupService = functionGroupService;
        this.functionGroupMapper = functionGroupMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder, FunctionGroupByIdPutRequestBody requestData) {
        String id = parameterHolder.getParameter();
        LOGGER.info("Updating function group with ID {}", id);

        functionGroupService.updateFunctionGroup(id, functionGroupMapper.presentationToFunctionGroupBase(requestData));
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FunctionGroupEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        FunctionGroupByIdPutRequestBody request, Void response) {
        return new FunctionGroupEvent().withId(parameterHolder.getParameter()).withAction(Action.UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        FunctionGroupByIdPutRequestBody request, Exception failure) {
        return null;
    }


}
