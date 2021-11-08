package com.backbase.accesscontrol.business.service;

import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class UserContextEventGenerationService {

    private EventBus eventBus;

    @Autowired
    public UserContextEventGenerationService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void generateUserContextEvents(Function<String, List<UserContextProjection>> getUserContextProjections,
                                          String groupId) {
       List<UserContextProjection> userContextProjections = getUserContextProjections.apply(groupId);
        for (UserContextProjection userContextProjection : userContextProjections) {
           EnvelopedEvent<Event> userContextEnvelop = new EnvelopedEvent<>();
           userContextEnvelop.setEvent(new UserContextEvent()
                   .withServiceAgreementId(userContextProjection.getServiceAgreementId())
                   .withUserId(userContextProjection.getUserId()));
           eventBus.emitEvent(userContextEnvelop);
       }
    }
}
