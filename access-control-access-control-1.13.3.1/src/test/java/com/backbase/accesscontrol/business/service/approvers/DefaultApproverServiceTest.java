package com.backbase.accesscontrol.business.service.approvers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.persistence.approvals.ApproveApprovalHandler;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultApproverServiceTest {

    @InjectMocks
    private DefaultApproverService defaultApproverService;
    @Mock
    private ApproveApprovalHandler approveApprovalHandler;

    @Test
    public void testManageApproveDeleteApprovalType() {
        String approvalId = "approvalId";
        String serviceAgreementId = "serviceAgreementId";
        String userId = "userId";
        doNothing().when(approveApprovalHandler).handleRequest(any(SingleParameterHolder.class), isNull());
        defaultApproverService.manageApprove(approvalId, serviceAgreementId, userId);

        ArgumentCaptor<SingleParameterHolder<String>> captor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        verify(approveApprovalHandler).handleRequest(captor.capture(), isNull());
        assertEquals(approvalId, captor.getValue().getParameter());
    }
}
