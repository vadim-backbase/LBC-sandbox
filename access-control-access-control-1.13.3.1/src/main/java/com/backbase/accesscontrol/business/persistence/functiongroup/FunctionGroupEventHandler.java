package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.service.UserContextEventGenerationService;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FunctionGroupEventHandler implements EventHandler<FunctionGroupEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupEventHandler.class);

    private final UserContextService userContextService;

    private final UserContextEventGenerationService userContextEventGenerationService;

    @Autowired
    public FunctionGroupEventHandler(UserContextService userContextService,
                                 UserContextEventGenerationService userContextEventGenerationService) {
        this.userContextService = userContextService;
        this.userContextEventGenerationService = userContextEventGenerationService;
    }

    @Override
    public void handle(EnvelopedEvent<FunctionGroupEvent> envelopedEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Invoked handler {}", this.getClass().getName());
        }
        FunctionGroupEvent functionGroupEvent = envelopedEvent.getEvent();
        if (functionGroupEvent == null || functionGroupEvent.getId() == null) {
            LOGGER.info("Event is invalid {}. Skipped handling", envelopedEvent);
            return;
        }

        if(functionGroupEvent.getAction() == Action.DELETE) {
            return;
        }

        userContextEventGenerationService.generateUserContextEvents(
                userContextService::getUserContextListByFunctionGroupId, functionGroupEvent.getId());
    }
}
