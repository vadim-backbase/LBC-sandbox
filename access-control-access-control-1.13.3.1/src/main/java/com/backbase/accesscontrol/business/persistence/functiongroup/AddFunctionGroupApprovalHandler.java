package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddFunctionGroupApprovalHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, FunctionGroupApprovalBase, FunctionGroupsPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddFunctionGroupApprovalHandler.class);

    private final FunctionGroupService functionGroupService;

    public AddFunctionGroupApprovalHandler(EventBus eventBus, FunctionGroupService functionGroupService) {
        super(eventBus);
        this.functionGroupService = functionGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FunctionGroupsPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        FunctionGroupApprovalBase requestData) {
        LOGGER.info("Handling execute request for saving function group with approval ON {}", requestData);
        String savedFunctionGroupId = functionGroupService.addFunctionGroupApproval(requestData);
        return new FunctionGroupsPostResponseBody()
            .withId(savedFunctionGroupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(EmptyParameterHolder parameterHolder,
        FunctionGroupApprovalBase request,
        FunctionGroupsPostResponseBody response) {
        return null;
    }

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request The request.
     * @param failure The exception that was thrown during request execution.
     * @return The event to fire.
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder, FunctionGroupApprovalBase request,
        Exception failure) {
        return null;
    }

}