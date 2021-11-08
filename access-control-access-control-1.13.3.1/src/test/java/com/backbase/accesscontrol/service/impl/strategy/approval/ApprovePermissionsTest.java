package com.backbase.accesscontrol.service.impl.strategy.approval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.mappers.ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApprovePermissionsTest {

    @InjectMocks
    private ApprovePermissions approvePermissions;

    @Mock
    private PermissionService permissionService;

    @Mock
    private AccessControlApprovalJpaRepository repository;

    @Mock
    protected EventBus eventBus;

    @Mock
    private ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper userContextPermissionsMapper;

    @Test
    public void shouldReturnDataGroupAsKey() {
        assertThat(approvePermissions.getKey().approvalAction, is(ApprovalAction.EDIT));
        assertThat(approvePermissions.getKey().approvalCategory, is(ApprovalCategory.ASSIGN_PERMISSIONS));
    }

    @Test
    public void shouldCallSaveDataGroupOnCreatePendingItem() {

        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroups
            = new ApprovalUserContextAssignFunctionGroup()
            .withFunctionGroupId("fgId")
            .withDataGroups(Sets.newHashSet("dg"));
        ApprovalUserContext approvalRequest = new ApprovalUserContext()
            .withLegalEntityId("le")
            .withServiceAgreementId("sa")
            .withUserId("user")
            .withApprovalUserContextAssignFunctionGroups(Sets.newHashSet(approvalUserContextAssignFunctionGroups))
            .withApprovalId("approvalId");
        approvePermissions.execute(approvalRequest);

        verify(repository).delete(eq(approvalRequest));
        verify(permissionService, times(1))
            .assignUserContextPermissions(anyString(), anyString(), anyString(), any());
    }


}