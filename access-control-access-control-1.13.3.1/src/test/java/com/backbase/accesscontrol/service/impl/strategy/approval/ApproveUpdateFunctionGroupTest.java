package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
public class ApproveUpdateFunctionGroupTest {

    @InjectMocks
    private ApproveUpdateFunctionGroup approveUpdateFunctionGroup;

    @Mock
    private FunctionGroupService functionGroupService;
    @Mock
    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Mock
    private ApprovalPermissionUtil approvalPermissionUtil;
    @Mock
    protected EventBus eventBus;


    @Test
    public void testShouldReturnFunctionGroupAsKey() {
        assertThat(approveUpdateFunctionGroup.getKey().approvalAction, is(ApprovalAction.EDIT));
        assertThat(approveUpdateFunctionGroup.getKey().approvalCategory, is(ApprovalCategory.MANAGE_FUNCTION_GROUPS));
    }

    @Test
    public void testShouldCallSaveFunctionGroupOnCreatePendingItem() {

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setServiceAgreementId("1");
        approvalFunctionGroup.setFunctionGroupId("fg1");
        approvalFunctionGroup.setApprovalId("approvalId");
        FunctionGroupBase data = new FunctionGroupBase().withServiceAgreementId("1");
        when(approvalPermissionUtil.convertAndReturnFunctionGroupBase(eq(approvalFunctionGroup)))
            .thenReturn(new FunctionGroupBase().withServiceAgreementId("1"));
        approveUpdateFunctionGroup.execute(approvalFunctionGroup);

        verify(accessControlApprovalJpaRepository).delete(eq(approvalFunctionGroup));
        verify(functionGroupService, times(1)).updateFunctionGroup(eq("fg1"), eq(data));
    }

}
