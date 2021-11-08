package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class DeleteDataGroupApprovalHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, ApprovalDto, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataGroupApprovalHandler.class);

    private DataGroupService dataGroupService;

    public DeleteDataGroupApprovalHandler(EventBus eventBus, DataGroupService dataGroupService) {
        super(eventBus);
        this.dataGroupService = dataGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder, ApprovalDto approval) {
        String dataGroupId = parameterHolder.getParameter();
        dataGroupService.deleteDataGroupApproval(dataGroupId, approval.getApprovalId());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        ApprovalDto approval,
        Void response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        ApprovalDto approval,
        Exception failure) {
        return null;
    }
}
