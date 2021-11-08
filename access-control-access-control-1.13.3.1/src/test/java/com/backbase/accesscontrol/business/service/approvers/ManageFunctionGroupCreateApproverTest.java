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
import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManageFunctionGroupCreateApproverTest {

    @InjectMocks
    private ManageFunctionGroupCreateApprover manageFunctionGroupCreateApprover;
    @Mock
    private AccessControlApprovalService accessControlApprovalService;
    @Mock
    private FunctionGroupPAndPService functionGroupPAndPService;
    @Mock
    private ApproveApprovalHandler approveApprovalHandler;

    @Test
    public void testManageApproveDeleteApprovalType() {
        String approvalId = "approvalId";
        String serviceAgreementId = "serviceAgreementId";
        String userId = "userId";
        String fgId = "fgId";
        String name = "name";
        String newApprovalTypeId = "944c27c0-2808-457b-aa13-71ff07c5b536";

        when(accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(new PresentationFunctionGroupApprovalDetailsItem()
                .withServiceAgreementId(serviceAgreementId)
                .withNewState(new PresentationFunctionGroupState()
                    .withName(name)
                    .withApprovalTypeId(newApprovalTypeId)));
        doNothing().when(approveApprovalHandler).handleRequest(any(SingleParameterHolder.class), isNull());
        List<FunctionGroupsGetResponseBody> fgList = new ArrayList<>();
        fgList.add(new FunctionGroupsGetResponseBody()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)
            .withId(fgId));
        when(functionGroupPAndPService.getFunctionGroups(eq(serviceAgreementId)))
            .thenReturn(fgList);
        manageFunctionGroupCreateApprover.manageApprove(approvalId, serviceAgreementId, userId);
        verify(accessControlApprovalService, times(1))
            .createApprovalType(eq(fgId),eq(newApprovalTypeId));

        ArgumentCaptor<SingleParameterHolder<String>> captor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        verify(approveApprovalHandler).handleRequest(captor.capture(), isNull());
        assertEquals(approvalId, captor.getValue().getParameter());
    }

    @Test
    public void testManageApproveIfFunctionGroupNotPresent() {
        String approvalId = "approvalId";
        String serviceAgreementId = "serviceAgreementId";
        String userId = "userId";
        String fgId = "fgId";
        String name = "name";
        String newApprovalTypeId = "944c27c0-2808-457b-aa13-71ff07c5b536";

        when(accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(new PresentationFunctionGroupApprovalDetailsItem()
                .withServiceAgreementId(serviceAgreementId)
                .withNewState(new PresentationFunctionGroupState()
                    .withName(name)
                    .withApprovalTypeId(newApprovalTypeId)));
        doNothing().when(approveApprovalHandler).handleRequest(any(SingleParameterHolder.class), isNull());
        manageFunctionGroupCreateApprover.manageApprove(approvalId, serviceAgreementId, userId);
        verify(accessControlApprovalService, times(0))
            .createApprovalType(eq(fgId),eq(newApprovalTypeId));

        ArgumentCaptor<SingleParameterHolder<String>> captor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        verify(approveApprovalHandler).handleRequest(captor.capture(), isNull());
        assertEquals(approvalId, captor.getValue().getParameter());
    }
}
