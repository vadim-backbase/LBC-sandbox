package com.backbase.accesscontrol.business.persistence.datagroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDataGroupHandlerTest {

    @Mock
    private DataGroupService dataGroupService;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private DeleteDataGroupHandler deleteDataGroupHandler;

    private SingleParameterHolder<String> parameterHolder;

    @Before
    public void setUp() {
        parameterHolder = new SingleParameterHolder("parameterHolder");
    }

    @Test
    public void shouldDeleteDataGroup() {
        doNothing().when(dataGroupService).delete(eq(parameterHolder.getParameter()));
        deleteDataGroupHandler.executeRequest(parameterHolder, null);
        verify(dataGroupService).delete(eq(parameterHolder.getParameter()));

    }

    @Test
    public void shouldCreateExpectedFailedEvent() {
        Event failureEvent = deleteDataGroupHandler.createFailureEvent(parameterHolder,
            null, new RuntimeException("message"));

        assertNull(failureEvent);
    }

    @Test
    public void shouldCreateExpectedSuccessEvent() {
        DataGroupEvent successEvent = deleteDataGroupHandler
            .createSuccessEvent(parameterHolder, null, null);

        assertEquals(parameterHolder.getParameter(), successEvent.getId());
        assertEquals(Action.DELETE, successEvent.getAction());
    }

}