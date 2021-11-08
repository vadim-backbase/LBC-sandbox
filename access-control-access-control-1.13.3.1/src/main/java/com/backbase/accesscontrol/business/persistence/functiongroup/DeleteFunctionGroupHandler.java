package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeleteFunctionGroupHandler extends LeanGenericEventEmitter<SingleParameterHolder<String>, Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFunctionGroupHandler.class);
    private FunctionGroupService functionGroupService;

    public DeleteFunctionGroupHandler(EventBus eventBus, FunctionGroupService functionGroupService) {
        super(eventBus);
        this.functionGroupService = functionGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder, Void requestData) {
        LOGGER.info("Trying to delete function group with id {}", parameterHolder.getParameter());

        functionGroupService.deleteFunctionGroup(parameterHolder.getParameter());
        return requestData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FunctionGroupEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        Void request, Void commandResult) {
        return new FunctionGroupEvent().withId(parameterHolder.getParameter()).withAction(Action.DELETE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder, Void request, Exception failure) {
        return null;
    }
}
