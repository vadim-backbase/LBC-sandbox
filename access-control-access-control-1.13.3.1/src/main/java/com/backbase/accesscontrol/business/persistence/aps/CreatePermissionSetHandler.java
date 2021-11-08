package com.backbase.accesscontrol.business.persistence.aps;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class CreatePermissionSetHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, PresentationPermissionSet, BigDecimal> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePermissionSetHandler.class);

    private PermissionSetService permissionSetService;

    public CreatePermissionSetHandler(EventBus eventBus, PermissionSetService permissionSetService) {
        super(eventBus);
        this.permissionSetService = permissionSetService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BigDecimal executeRequest(EmptyParameterHolder parameterHolder,
        PresentationPermissionSet persistencePermissionSet) {
        LOGGER.info("Handling execute request for create permission set {}", persistencePermissionSet);
        return permissionSetService.save(persistencePermissionSet);
    }

    @Override
    protected AssignablePermissionSetEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        PresentationPermissionSet request,
        BigDecimal response) {
        LOGGER.info("Creating success event for created assignable permission set with id {}", response);
        return new AssignablePermissionSetEvent().withId(response).withAction(Action.ADD);
    }

    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder, PresentationPermissionSet request,
        Exception failure) {
        return null;
    }


}
