package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import org.springframework.stereotype.Component;

@Component
public class DeleteDataGroupHandler extends LeanGenericEventEmitter<SingleParameterHolder<String>, Void, Void> {

    private DataGroupService dataGroupService;

    public DeleteDataGroupHandler(EventBus eventBus, DataGroupService dataGroupService) {
        super(eventBus);
        this.dataGroupService = dataGroupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Void executeRequest(SingleParameterHolder<String> parameterHolder, Void requestData) {
        dataGroupService.delete(parameterHolder.getParameter());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataGroupEvent createSuccessEvent(SingleParameterHolder<String> parameterHolder, Void request,
        Void response) {
        return new DataGroupEvent().withId(parameterHolder.getParameter()).withAction(Action.DELETE);
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
