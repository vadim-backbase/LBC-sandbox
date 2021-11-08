package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddDataGroupApprovalHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, DataGroupBase, DataGroupsPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddDataGroupApprovalHandler.class);

    private DataGroupService dataGroupService;

    public AddDataGroupApprovalHandler(EventBus eventBus, DataGroupService dataGroupService) {
        super(eventBus);
        this.dataGroupService = dataGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataGroupsPostResponseBody executeRequest(SingleParameterHolder<String> parameterHolder,
        DataGroupBase requestData) {
        LOGGER.info("Handling execute request for saving data group with approval ON {}", requestData);
        String savedDataGroupId = dataGroupService.saveDataGroupApproval(requestData, parameterHolder.getParameter());
        return new DataGroupsPostResponseBody()
            .withId(savedDataGroupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        DataGroupBase dataGroupBase, DataGroupsPostResponseBody response) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        DataGroupBase dataGroupBase, Exception failure) {
        LOGGER.info("Creating failed event for saving data group approval with data {}", dataGroupBase);
        return null;
    }
}
