package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
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
public class ApproveDeleteFunctionGroupPersistenceTest {

    @InjectMocks
    private ApproveDeleteFunctionGroup approveDeleteFunctionGroup;
    @Mock
    private FunctionGroupService functionGroupService;
    @Mock
    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Mock
    protected EventBus eventBus;

    @Test
    public void testShouldReturnFunctionGroupAsKey() {
        assertThat(approveDeleteFunctionGroup.getKey().approvalAction, is(ApprovalAction.DELETE));
        assertThat(approveDeleteFunctionGroup.getKey().approvalCategory, is(ApprovalCategory.MANAGE_FUNCTION_GROUPS));
    }

    @Test
    public void testShouldCallSaveDataGroupOnCreatePendingItem() {

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setFunctionGroupId("123");

        approveDeleteFunctionGroup.execute(approvalFunctionGroup);

        verify(accessControlApprovalJpaRepository).delete(eq(approvalFunctionGroup));
        verify(functionGroupService, times(1)).deleteFunctionGroup(anyString());
    }
}
