package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApproveDeleteDataGroupTest {

    @InjectMocks
    private ApproveDeleteDataGroup approveDeleteDataGroup;

    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private AccessControlApprovalJpaRepository repository;
    @Mock
    protected EventBus eventBus;

    @Test
    public void shouldReturnDataGroupAsKey() {
        assertThat(approveDeleteDataGroup.getKey().approvalAction, is(ApprovalAction.DELETE));
        assertThat(approveDeleteDataGroup.getKey().approvalCategory, is(ApprovalCategory.MANAGE_DATA_GROUPS));
    }

    @Test
    public void shouldCallSaveDataGroupOnCreatePendingItem() {

        ApprovalDataGroup approvalDataGroup = new ApprovalDataGroup();
        approvalDataGroup.setDataGroupId("123");

        approveDeleteDataGroup.execute(approvalDataGroup);

        verify(repository).delete(eq(approvalDataGroup));
        verify(dataGroupService, times(1)).delete(anyString());
    }

}