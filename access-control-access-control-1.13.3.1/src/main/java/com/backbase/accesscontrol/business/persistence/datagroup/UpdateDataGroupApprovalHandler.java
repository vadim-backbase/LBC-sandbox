package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateDataGroupApprovalHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<ApprovalDto>, DataGroupByIdPutRequestBody, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroupApprovalHandler.class);

    private DataGroupService dataGroupService;

    public UpdateDataGroupApprovalHandler(EventBus eventBus, DataGroupService dataGroupService) {
        super(eventBus);
        this.dataGroupService = dataGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<ApprovalDto> parameterHolder,
        DataGroupByIdPutRequestBody requestData) {
        LOGGER.info("Trying to update approval DataGroup with id {} and data {}", requestData.getId(),
            requestData);
        requestData.setApprovalId(parameterHolder.getParameter().getApprovalId());
        dataGroupService.updateDataGroupApproval(requestData);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(SingleParameterHolder parameterHolder,
        DataGroupByIdPutRequestBody dataGroup, Void response) {
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder parameterHolder,
        DataGroupByIdPutRequestBody dataGroup, Exception failure) {
        return null;
    }
}
