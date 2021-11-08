package com.backbase.accesscontrol.business.persistence.datagroup;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupApprovalHandlerTest {

    @Mock
    private DataGroupService dataGroupService;
    @InjectMocks
    private UpdateDataGroupApprovalHandler updateDataGroupApprovalHandler;
    @Mock
    private EventBus eventBus;

    @Test
    public void shouldSuccessfullyInvokeUpdate() {
        DataGroupByIdPutRequestBody dataGroupApprovalUpdate = new DataGroupByIdPutRequestBody();

        doNothing().when(dataGroupService).updateDataGroupApproval(any(DataGroupByIdPutRequestBody.class));
        ApprovalDto dto = new ApprovalDto();
        dto.setApprovalId("approvalId");
        updateDataGroupApprovalHandler
            .executeRequest(new SingleParameterHolder<>(dto), dataGroupApprovalUpdate);

        verify(eventBus, times(0)).emitEvent(any(EnvelopedEvent.class));
        verify(dataGroupService).updateDataGroupApproval(any(DataGroupByIdPutRequestBody.class));
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";
        DataGroupByIdPutRequestBody dataGroupApprovalUpdate = new DataGroupByIdPutRequestBody();
        Event failureEvent = updateDataGroupApprovalHandler
            .createFailureEvent(null, dataGroupApprovalUpdate, new RuntimeException(errorMessage));
        assertNull(failureEvent);
    }
}