package com.backbase.accesscontrol.business.persistence.functiongroup;

import com.backbase.accesscontrol.business.service.UserContextEventGenerationService;
import com.backbase.accesscontrol.domain.dto.UserContextProjection;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FunctionGroupEventHandlerTest {
    @Mock
    private EventBus eventBus;

    @Mock
    private UserContextService userContextService;

    @Captor
    private ArgumentCaptor<EnvelopedEvent<Event>> eventCaptor;

    private FunctionGroupEventHandler functionGroupEventHandler;

    @Before
    public void setUp() {
        UserContextProjection userContext1 = new UserContextProjection("userId1", "agreementId1");
        UserContextProjection userContext2 = new UserContextProjection("userId2", "agreementId2");
        when(userContextService.getUserContextListByFunctionGroupId("functionGroupId"))
                .thenReturn(Arrays.asList(userContext1, userContext2));
        UserContextEventGenerationService userContextEventGenerationService =
                new UserContextEventGenerationService(eventBus);
        functionGroupEventHandler = new FunctionGroupEventHandler(userContextService, userContextEventGenerationService);
    }

    @Test
    public void successHandleFunctionGroupEvent() {
        EnvelopedEvent<FunctionGroupEvent> functionGroupEnvelop = new EnvelopedEvent<>();
        FunctionGroupEvent functionGroupEvent = new FunctionGroupEvent().withAction(Action.UPDATE).withId("functionGroupId");
        functionGroupEnvelop.setEvent(functionGroupEvent);
        functionGroupEnvelop.setOriginatorContext(null);
        functionGroupEventHandler.handle(functionGroupEnvelop);
        verify(eventBus, times(2)).emitEvent(eventCaptor.capture());

        List<String> userIds = eventCaptor.getAllValues()
                .stream().map(envelopEvent -> envelopEvent.getEvent())
                .map(event -> ((UserContextEvent)event).getUserId()).collect(Collectors.toList());
        assertThat(userIds).containsExactlyInAnyOrder("userId1", "userId2");
    }

    @Test
    public void successHandleFunctionGroupEventWithActionDelete() {
        EnvelopedEvent<FunctionGroupEvent> functionGroupEnvelop = new EnvelopedEvent<>();
        FunctionGroupEvent functionGroupEvent = new FunctionGroupEvent().withAction(Action.DELETE).withId("functionGroupId");
        functionGroupEnvelop.setEvent(functionGroupEvent);
        functionGroupEnvelop.setOriginatorContext(null);
        functionGroupEventHandler.handle(functionGroupEnvelop);
        verify(eventBus, times(0)).emitEvent(any());
    }

    @Test
    public void handleFunctionGroupEventWithEmptyEnvelop() {
        EnvelopedEvent<FunctionGroupEvent> functionGroupEnvelop = new EnvelopedEvent<>();
        functionGroupEnvelop.setEvent(null);
        functionGroupEnvelop.setOriginatorContext(null);
        functionGroupEventHandler.handle(functionGroupEnvelop);
        verify(eventBus, times(0)).emitEvent(any());
    }

    @Test
    public void handleFunctionGroupEventWithFunctionGroupIdNull() {
        EnvelopedEvent<FunctionGroupEvent> functionGroupEnvelop = new EnvelopedEvent<>();
        FunctionGroupEvent functionGroupEvent = new FunctionGroupEvent().withAction(Action.DELETE).withId(null);
        functionGroupEnvelop.setEvent(functionGroupEvent);
        functionGroupEnvelop.setOriginatorContext(null);
        functionGroupEventHandler.handle(functionGroupEnvelop);
        verify(eventBus, times(0)).emitEvent(any());
    }
}
