package com.backbase.accesscontrol.business.persistence.datagroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddDataGroupApprovalHandlerTest {

    @Mock
    private DataGroupService dataGroupService;

    @InjectMocks
    private AddDataGroupApprovalHandler addDataGroupApprovalHandler;
    @Mock
    private EventBus eventBus;

    @Test
    public void shouldSuccessfullyInvokeSave() {
        DataGroupBase dataGroupApprovalCreate = new DataGroupBase();

        String approvalId = UUID.randomUUID().toString();
        when(dataGroupService.saveDataGroupApproval(eq(dataGroupApprovalCreate), eq(approvalId)))
            .thenReturn(approvalId);

        DataGroupsPostResponseBody data = addDataGroupApprovalHandler
            .handleRequest(new SingleParameterHolder<>(approvalId), dataGroupApprovalCreate);

        verify(dataGroupService).saveDataGroupApproval(eq(dataGroupApprovalCreate), eq(approvalId));
        verify(eventBus, times(0)).emitEvent(any(EnvelopedEvent.class));
        assertEquals(approvalId, data.getId());
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";

        DataGroupBase dataGroupBase = new DataGroupBase();
        Event failureEvent = addDataGroupApprovalHandler
            .createFailureEvent(new SingleParameterHolder<>("id"), dataGroupBase, new RuntimeException(errorMessage));
        assertNull(failureEvent);
    }
}