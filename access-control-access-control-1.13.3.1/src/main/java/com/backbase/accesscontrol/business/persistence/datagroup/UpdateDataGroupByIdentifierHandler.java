package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class UpdateDataGroupByIdentifierHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, PresentationSingleDataGroupPutRequestBody, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroupByIdentifierHandler.class);

    private DataGroupService dataGroupService;

    public UpdateDataGroupByIdentifierHandler(
        EventBus eventBus, DataGroupService dataGroupService) {
        super(eventBus);
        this.dataGroupService = dataGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String executeRequest(EmptyParameterHolder parameterHolder,
        PresentationSingleDataGroupPutRequestBody requestData) {
        LOGGER.info("Trying to update data group by identifier {}", requestData.getDataGroupIdentifier());
        return dataGroupService.update(requestData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataGroupEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        PresentationSingleDataGroupPutRequestBody request, String dataGroupId) {
        return new DataGroupEvent().withId(dataGroupId)
            .withAction(Action.UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder,
        PresentationSingleDataGroupPutRequestBody request, Exception failure) {
        return null;
    }
}
