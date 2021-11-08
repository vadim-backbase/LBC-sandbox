package com.backbase.accesscontrol.business.persistence.approvals;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.buildingblocks.persistence.model.Event;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RejectApprovalHandlerTest {

    @Mock
    private ApprovalService approvalService;
    @InjectMocks
    private RejectApprovalHandler rejectApprovalHandler;

    @Test
    public void shouldRejectApproval() {
        String approvalId = UUID.randomUUID().toString();
        doNothing().when(approvalService).rejectApprovalRequest(approvalId);

        rejectApprovalHandler
            .executeRequest(new SingleParameterHolder<>(approvalId), null);
        verify(approvalService, times(1)).rejectApprovalRequest(approvalId);
    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        String approvalId = UUID.randomUUID().toString();
        Event successEvent = rejectApprovalHandler
            .createSuccessEvent(new SingleParameterHolder<>(approvalId), null, null);
        assertNull(successEvent);
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";
        String approvalId = UUID.randomUUID().toString();
        Event failureEvent = rejectApprovalHandler.createFailureEvent(new SingleParameterHolder<>(approvalId), null, new RuntimeException(errorMessage));

        assertNull(failureEvent);
    }
}