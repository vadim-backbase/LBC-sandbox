package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class UpdateDataGroupHandler extends
    LeanGenericEventEmitter<SingleParameterHolder<String>, DataGroupByIdPutRequestBody, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroupHandler.class);

    private DataGroupService dataGroupService;

    public UpdateDataGroupHandler(EventBus eventBus, DataGroupService dataGroupService) {
        super(eventBus);
        this.dataGroupService = dataGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder,
        DataGroupByIdPutRequestBody requestData) {
        LOGGER.info("Trying to update DataGroup with id {} and data {}", parameterHolder.getParameter(),
            requestData);
        try {
            dataGroupService.update(parameterHolder.getParameter(), requestData);
        } catch (Exception err) {
            Throwable root = ExceptionUtils.getRootCause(err);
            if (root instanceof BadRequestException) {
                LOGGER.warn("Throwing bad request exception", err);
                throw (BadRequestException) root;
            }
            throw err;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataGroupEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder,
        DataGroupByIdPutRequestBody dataGroup, Void response) {
        return new DataGroupEvent().withId(parameterHolder.getParameter())
            .withAction(Action.UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(SingleParameterHolder<String> parameterHolder,
        DataGroupByIdPutRequestBody dataGroup, Exception failure) {
        return null;
    }
}
