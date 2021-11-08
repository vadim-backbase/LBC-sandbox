package com.backbase.accesscontrol.business.persistence.aps;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.dto.IdentifierPair;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class DeletePermissionSetHandler extends LeanGenericEventEmitter<IdentifierPair, Void, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeletePermissionSetHandler.class);

    private PermissionSetService permissionSetService;

    public DeletePermissionSetHandler(EventBus eventBus, PermissionSetService permissionSetService) {
        super(eventBus);
        this.permissionSetService = permissionSetService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long executeRequest(IdentifierPair parameterHolder, Void requestData) {
        return permissionSetService.delete(parameterHolder.getIdentifierType(), parameterHolder.getIdentifier());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AssignablePermissionSetEvent createSuccessEvent(IdentifierPair parameterHolder, Void request,
        Long response) {
        LOGGER.info("Creating success event for delete assignable permission set with identifiers {}", parameterHolder);
        return new AssignablePermissionSetEvent().withId(BigDecimal.valueOf(response)).withAction(Action.DELETE);
    }

    @Override
    protected Event createFailureEvent(IdentifierPair parameterHolder, Void request, Exception failure) {
        return null;
    }

}
