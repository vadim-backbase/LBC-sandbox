package com.backbase.accesscontrol.business.persistence.aps;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdatePermissionSetHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, PresentationPermissionSetItemPut, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePermissionSetHandler.class);
    private PermissionSetService permissionSetService;

    public UpdatePermissionSetHandler(EventBus eventBus, PermissionSetService permissionSetService) {
        super(eventBus);
        this.permissionSetService = permissionSetService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String executeRequest(EmptyParameterHolder parameterHolder,
        PresentationPermissionSetItemPut requestData) {
        LOGGER.info("Handling execute request for update permission set {}", requestData);
        return permissionSetService.update(requestData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ServiceAgreementEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        PresentationPermissionSetItemPut request,
        String response) {
        LOGGER
            .info("Creating success event for updated assignable permission set with external service agreement id {}",
                request.getExternalServiceAgreementId());
        return new ServiceAgreementEvent().withId(response).withAction(Action.UPDATE);
    }

    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder, PresentationPermissionSetItemPut request,
        Exception failure) {
        return null;
    }
}
