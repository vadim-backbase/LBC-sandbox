package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.service.UserContextEventGenerationService;
import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataGroupEventHandlerTest {
    @Mock
    private EventBus eventBus;

    @Mock
    private UserContextService userContextService;

    @Captor
    private ArgumentCaptor<EnvelopedEvent<Event>> eventCaptor;

    private DataGroupEventHandler dataGroupEventHandler;

    @Before
    public void setUp() {
        UserContextProjection userContext1 = new UserContextProjection("userId1", "agreementId1");
        UserContextProjection userContext2 = new UserContextProjection("userId2", "agreementId2");
        when(userContextService.getUserContextListByDataGroupId("dataGroupId"))
                .thenReturn(Arrays.asList(userContext1, userContext2));
        UserContextEventGenerationService userContextEventGenerationService =
                new UserContextEventGenerationService(eventBus);
        dataGroupEventHandler = new DataGroupEventHandler(userContextService, userContextEventGenerationService);
    }

    @Test
    public void successHandleDataGroupEvent() {
        EnvelopedEvent<DataGroupEvent> dataGroupEnvelop = new EnvelopedEvent<>();
        DataGroupEvent dataGroupEvent = new DataGroupEvent().withAction(Action.UPDATE).withId("dataGroupId");
        dataGroupEnvelop.setEvent(dataGroupEvent);
        dataGroupEnvelop.setOriginatorContext(null);
        dataGroupEventHandler.handle(dataGroupEnvelop);
        verify(eventBus, times(2)).emitEvent(eventCaptor.capture());

        List<String> userIds = eventCaptor.getAllValues()
                .stream().map(envelopEvent -> envelopEvent.getEvent())
                .map(event -> ((UserContextEvent)event).getUserId()).collect(Collectors.toList());
         assertThat(userIds).containsExactlyInAnyOrder("userId1", "userId2");
    }

    @Test
    public void successHandleDataGroupEventWithActionDelete() {
        EnvelopedEvent<DataGroupEvent> dataGroupEnvelop = new EnvelopedEvent<>();
        DataGroupEvent dataGroupEvent = new DataGroupEvent().withAction(Action.DELETE).withId("dataGroupId");
        dataGroupEnvelop.setEvent(dataGroupEvent);
        dataGroupEnvelop.setOriginatorContext(null);
        dataGroupEventHandler.handle(dataGroupEnvelop);
        verify(eventBus, times(0)).emitEvent(any());
    }

    @Test
    public void handleDataGroupEventWithEmptyEnvelop() {
        EnvelopedEvent<DataGroupEvent> dataGroupEnvelop = new EnvelopedEvent<>();
        dataGroupEnvelop.setEvent(null);
        dataGroupEnvelop.setOriginatorContext(null);
        dataGroupEventHandler.handle(dataGroupEnvelop);
        verify(eventBus, times(0)).emitEvent(any());
    }

    @Test
    public void handleDataGroupEventWithDataGroupIdNull() {
        EnvelopedEvent<DataGroupEvent> dataGroupEnvelop = new EnvelopedEvent<>();
        DataGroupEvent dataGroupEvent = new DataGroupEvent().withAction(Action.DELETE).withId(null);
        dataGroupEnvelop.setEvent(dataGroupEvent);
        dataGroupEnvelop.setOriginatorContext(null);
        dataGroupEventHandler.handle(dataGroupEnvelop);
        verify(eventBus, times(0)).emitEvent(any());
    }
}
