package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApproveCreateFunctionGroupTest {

    @InjectMocks
    private ApproveCreateFunctionGroup approveCreateFunctionGroup;

    @Mock
    protected EventBus eventBus;
    @Mock
    private FunctionGroupService functionGroupService;
    @Mock
    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Mock
    private ApprovalPermissionUtil approvalPermissionUtil;

    @Test
    public void testShouldReturnFunctionGroupAsKey() {
        assertThat(approveCreateFunctionGroup.getKey().approvalAction, is(ApprovalAction.CREATE));
        assertThat(approveCreateFunctionGroup.getKey().approvalCategory, is(ApprovalCategory.MANAGE_FUNCTION_GROUPS));
    }

    @Test
    public void testShouldSaveFunctionGroupOnCreatePendingItem() {

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setServiceAgreementId("1");
        AccessControlApproval approvalRequest = approvalFunctionGroup.withApprovalId("approvalId");

        when(approvalPermissionUtil.convertAndReturnFunctionGroupBase(eq(approvalFunctionGroup)))
            .thenReturn(new FunctionGroupBase());
        doNothing().when(accessControlApprovalJpaRepository).delete(eq(approvalRequest));

        approveCreateFunctionGroup.execute((ApprovalFunctionGroup) approvalRequest);
        verify(accessControlApprovalJpaRepository).delete(eq(approvalRequest));

        verify(functionGroupService, times(1)).addFunctionGroup(any(FunctionGroupBase.class));
    }

}