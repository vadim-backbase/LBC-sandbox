package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.service.UserContextEventGenerationService;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataGroupEventHandler implements EventHandler<DataGroupEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupEventHandler.class);

    private final UserContextService userContextService;

    private final UserContextEventGenerationService userContextEventGenerationService;

    @Autowired
    public DataGroupEventHandler(UserContextService userContextService,
                                 UserContextEventGenerationService userContextEventGenerationService) {
        this.userContextService = userContextService;
        this.userContextEventGenerationService = userContextEventGenerationService;
    }

    @Override
    public void handle(EnvelopedEvent<DataGroupEvent> envelopedEvent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Invoked handler {}", this.getClass().getName());
        }
        DataGroupEvent dataGroupEvent = envelopedEvent.getEvent();
        if (dataGroupEvent == null || dataGroupEvent.getId() == null) {
            LOGGER.info("Event is invalid {}. Skipped handling", envelopedEvent);
            return;
        }

        if (dataGroupEvent.getAction() == Action.DELETE) {
            return;
        }

        userContextEventGenerationService.generateUserContextEvents(
                userContextService::getUserContextListByDataGroupId, dataGroupEvent.getId());
    }
}
