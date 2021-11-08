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
public class ApproveApprovalHandlerTest {

    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private ApproveApprovalHandler approveApprovalHandler;

    @Test
    public void shouldApproveApproval() {

        String approvalId = UUID.randomUUID().toString();
        doNothing().when(approvalService).approveApprovalRequest(approvalId);

        approveApprovalHandler
            .executeRequest(new SingleParameterHolder<>(approvalId), null);
        verify(approvalService, times(1)).approveApprovalRequest(approvalId);
    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        String approvalId = UUID.randomUUID().toString();

        Event successEvent = approveApprovalHandler
            .createSuccessEvent(new SingleParameterHolder<>(approvalId), null, null);
        assertNull(successEvent);
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";
        String approvalId = UUID.randomUUID().toString();
        Event failureEvent = approveApprovalHandler
            .createFailureEvent(new SingleParameterHolder<>(approvalId), null, new RuntimeException(errorMessage));

        assertNull(failureEvent);
    }
}