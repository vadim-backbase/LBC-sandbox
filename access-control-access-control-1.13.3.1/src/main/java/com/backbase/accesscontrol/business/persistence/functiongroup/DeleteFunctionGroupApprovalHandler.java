package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeleteFunctionGroupApprovalHandler
    extends LeanGenericEventEmitter<SingleParameterHolder<String>, ApprovalDto, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFunctionGroupApprovalHandler.class);

    private final FunctionGroupService functionGroupService;

    public DeleteFunctionGroupApprovalHandler(
        EventBus eventBus, FunctionGroupService functionGroupService) {
        super(eventBus);
        this.functionGroupService = functionGroupService;
    }

    /**
     * Execute the actual request.
     *
     * @param parameterHolder contains function group id
     * @param requestData the request
     * @return The result of the request.
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder, ApprovalDto requestData) {

        LOGGER.info("Handling execute request for deleting function group with approval ON {}", requestData);
        functionGroupService.deleteApprovalFunctionGroup(parameterHolder.getParameter(), requestData);
        return null;
    }

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request The request.
     * @param response result from the executed request.
     * @return The event to fire.
     */
    @Override
    protected Event createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        ApprovalDto request, Void response) {
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
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        ApprovalDto request, Exception failure) {
        return null;
    }
}
