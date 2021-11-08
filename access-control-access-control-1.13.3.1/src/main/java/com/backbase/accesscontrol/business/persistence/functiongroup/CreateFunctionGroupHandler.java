package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateFunctionGroupHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, FunctionGroupBase, FunctionGroupsPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFunctionGroupHandler.class);

    private FunctionGroupService functionGroupService;

    public CreateFunctionGroupHandler(EventBus eventBus, FunctionGroupService functionGroupService) {
        super(eventBus);
        this.functionGroupService = functionGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FunctionGroupsPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        FunctionGroupBase requestData) {
        LOGGER.info("Creating function group {} from event handler.", requestData.getName());
        String functionGroupId = functionGroupService.addFunctionGroup(requestData);
        return new FunctionGroupsPostResponseBody().withId(functionGroupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FunctionGroupEvent createSuccessEvent(EmptyParameterHolder parameterHolder, FunctionGroupBase request,
        FunctionGroupsPostResponseBody response) {
        return new FunctionGroupEvent().withId(response.getId()).withAction(Action.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder, FunctionGroupBase request,
        Exception failure) {
        return null;
    }
}