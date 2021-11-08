package com.backbase.accesscontrol.business.persistence.approvals;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import org.springframework.stereotype.Component;

@Component
public class RejectApprovalHandler extends LeanGenericEventEmitter<SingleParameterHolder<String>, Void, Void> {

    private ApprovalService approvalService;

    public RejectApprovalHandler(EventBus eventBus, ApprovalService approvalService) {
        super(eventBus);
        this.approvalService = approvalService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder, Void requestData) {
        approvalService.rejectApprovalRequest(parameterHolder.getParameter());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(SingleParameterHolder<String> parameterHolder, Void request, Void response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        Void request, Exception failure) {
        return null;
    }
}

