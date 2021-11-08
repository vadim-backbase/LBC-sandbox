package com.backbase.accesscontrol.business.service.approvers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.approvals.ApproveApprovalHandler;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManageFunctionGroupDeleteApproverTest {

    @InjectMocks
    private ManageFunctionGroupDeleteApprover manageFunctionGroupDeleteApprover;
    @Mock
    private AccessControlApprovalService accessControlApprovalService;
    @Mock
    private ApproveApprovalHandler approveApprovalHandler;

    @Test
    public void testManageApproveDeleteApprovalType() {
        String approvalId = "approvalId";
        String serviceAgreementId = "serviceAgreementId";
        String userId = "userId";
        String fgId = "fgId";
        String oldApprovalTypeId = "type id";

        when(accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(new PresentationFunctionGroupApprovalDetailsItem()
                .withFunctionGroupId(fgId));
        doNothing().when(approveApprovalHandler).handleRequest(any(SingleParameterHolder.class), isNull());
        when(accessControlApprovalService
            .getApprovalTypeIdFromApprovals(eq(fgId))).thenReturn(oldApprovalTypeId);

        manageFunctionGroupDeleteApprover.manageApprove(approvalId, serviceAgreementId, userId);
        verify(accessControlApprovalService, times(1))
            .deleteApprovalType(eq(fgId));

        ArgumentCaptor<SingleParameterHolder<String>> captor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        verify(approveApprovalHandler).handleRequest(captor.capture(), isNull());
        assertEquals(approvalId, captor.getValue().getParameter());
    }
}
