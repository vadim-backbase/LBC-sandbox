package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApproveCreateDataGroupTest {

    @InjectMocks
    private ApproveCreateDataGroup approveCreateDataGroup;

    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private AccessControlApprovalJpaRepository repository;
    @Mock
    protected EventBus eventBus;

    @Test
    public void shouldReturnDataGroupAsKey() {
        assertThat(approveCreateDataGroup.getKey().approvalAction, is(ApprovalAction.CREATE));
        assertThat(approveCreateDataGroup.getKey().approvalCategory, is(ApprovalCategory.MANAGE_DATA_GROUPS));
    }

    @Test
    public void shouldCallSaveDataGroupOnCreatePendingItem() {

        AccessControlApproval approvalRequest = new ApprovalDataGroupDetails()
            .withApprovalId("approvalId");

        doNothing().when(repository).delete(eq(approvalRequest));

        approveCreateDataGroup.execute((ApprovalDataGroupDetails) approvalRequest);
        verify(repository).delete(eq(approvalRequest));

        verify(dataGroupService, times(1)).save(any(DataGroupBase.class));
    }

}