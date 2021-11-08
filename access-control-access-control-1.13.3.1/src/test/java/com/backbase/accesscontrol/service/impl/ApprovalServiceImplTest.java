package com.backbase.accesscontrol.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.util.PersistenceApprovalPermissionResponseFactory;
import com.backbase.accesscontrol.service.impl.strategy.approval.ApprovalFactory;
import com.backbase.accesscontrol.service.impl.strategy.approval.ApprovalItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissionsGetResponseBody;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApprovalServiceImplTest {

    private static final String SERVICE_AGREEMENT_ID = "serviceAgreementId";
    private static final String USER_ID = "userId";
    private static final String APPROVAL_ID = "approvalId";

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    @Mock
    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Mock
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    @Mock
    private UserContextService userContextService;
    @Mock
    private ApprovalFactory approvalFactory;
    @Mock
    private ApprovalItem approvalItem;
    @Mock
    private PersistenceApprovalPermissionResponseFactory persistenceApprovalPermissionResponseFactory;

    @Test
    void approveApprovalRequestTest() {
        String approvalId = APPROVAL_ID;
        String legalEntityId = "legalEntity";
        String fgId = "fgId";
        String dgId = "dgId";
        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(dgId);
        dataGroup.setName("dgName");
        dataGroup.setDescription("dgDesc");
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(fgId);
        functionGroup.setName("fgName");
        functionGroup.setDescription("fgDesc");

        Set<ApprovalUserContextAssignFunctionGroup> newState = new HashSet<>();
        ApprovalUserContextAssignFunctionGroup fg = new ApprovalUserContextAssignFunctionGroup()
            .withFunctionGroupId(fgId);
        newState.add(fg);
        AccessControlApproval approvalUserContext = new ApprovalUserContext()
            .withServiceAgreementId(SERVICE_AGREEMENT_ID)
            .withUserId(USER_ID)
            .withApprovalId(approvalId)
            .withLegalEntityId(legalEntityId)
            .withApprovalUserContextAssignFunctionGroups(newState);

        when(approvalFactory.getApprovalItem(any(), any())).thenReturn(approvalItem);

        when(accessControlApprovalJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.of(approvalUserContext));

        doNothing().when(approvalItem).execute(approvalUserContext);
        approvalService.approveApprovalRequest(approvalId);
        verify(accessControlApprovalJpaRepository, times(1)).findByApprovalId(approvalId);
        verify(approvalItem).execute(eq(approvalUserContext));
    }


    @Test
    void rejectApprovalRequestTest() {
        String approvalId = APPROVAL_ID;
        String legalEntityId = "legalEntity";
        String fgId = "fgId";
        String dgId = "dgId";
        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(dgId);
        dataGroup.setName("dgName");
        dataGroup.setDescription("dgDesc");
        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setId(fgId);
        functionGroup.setName("fgName");
        functionGroup.setDescription("fgDesc");

        Set<ApprovalUserContextAssignFunctionGroup> newState = new HashSet<>();
        ApprovalUserContextAssignFunctionGroup fg = new ApprovalUserContextAssignFunctionGroup()
            .withFunctionGroupId(fgId);
        newState.add(fg);
        AccessControlApproval approvalUserContext = new ApprovalUserContext()
            .withServiceAgreementId(SERVICE_AGREEMENT_ID)
            .withUserId(USER_ID)
            .withApprovalId(approvalId)
            .withLegalEntityId(legalEntityId)
            .withApprovalUserContextAssignFunctionGroups(newState);

        when(accessControlApprovalJpaRepository.findByApprovalId(approvalId))
            .thenReturn(Optional.of(approvalUserContext));
        doNothing().when(accessControlApprovalJpaRepository).delete(approvalUserContext);
        approvalService.rejectApprovalRequest(approvalId);
        verify(accessControlApprovalJpaRepository, times(1)).findByApprovalId(approvalId);
        verify(accessControlApprovalJpaRepository, times(1)).delete(approvalUserContext);
    }


    @Test
    void getPersistenceApprovalPermissionsWithoutFunctionGroupWithoutDataGroupWithApprovalPendingRequest() {

        mockUserAccessEmpty();
        mockApprovalUserContextWithApprovalId();

        PersistenceApprovalPermissions persistenceApprovalPermissions =
            approvalService.getPersistenceApprovalPermissions(USER_ID, SERVICE_AGREEMENT_ID);

        assertEquals(0, persistenceApprovalPermissions.getItems().size());
        assertEquals(APPROVAL_ID, persistenceApprovalPermissions.getApprovalId());

    }


    @Test
    void getPersistenceApprovalPermissionsWithoutFunctionGroupWithoutDataGroupWithoutApprovalPendingRequest() {

        mockUserAccessEmpty();
        mockApprovalUserContextWithEmpty();

        PersistenceApprovalPermissions persistenceApprovalPermissions =
            approvalService.getPersistenceApprovalPermissions(USER_ID, SERVICE_AGREEMENT_ID);

        assertEquals(0, persistenceApprovalPermissions.getItems().size());
        assertNull(persistenceApprovalPermissions.getApprovalId());

    }

    @Test
    void getPersistenceApprovalPermissionsWithoutApprovalPendingPermissions() {
        mockApprovalUserContextWithEmpty();

        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        UserContext userContext = new UserContext();
        userContext.setUserAssignedFunctionGroups(Set.of(assignedFunctionGroup));

        doReturn(Optional.of(userContext)).when(userContextService)
            .getUserContextByUserIdAndServiceAgreementIdWithFunctionAndDataGroupIds(USER_ID, SERVICE_AGREEMENT_ID);
        doReturn(Collections.singletonList(new PersistenceApprovalPermissionsGetResponseBody()))
            .when(persistenceApprovalPermissionResponseFactory)
            .createPersistenceApprovalPermissions(assignedFunctionGroup);

        PersistenceApprovalPermissions persistenceApprovalPermissions =
            approvalService.getPersistenceApprovalPermissions(USER_ID, SERVICE_AGREEMENT_ID);

        assertEquals(1, persistenceApprovalPermissions.getItems().size());
        assertNull(persistenceApprovalPermissions.getApprovalId());

        verify(persistenceApprovalPermissionResponseFactory).createPersistenceApprovalPermissions(assignedFunctionGroup);
    }

    private void mockApprovalUserContextWithEmpty() {
        when(approvalUserContextJpaRepository.findByUserIdAndServiceAgreementId(eq(USER_ID), eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(Optional.empty());
    }

    private void mockUserAccessEmpty() {
        when(userContextService
            .getUserContextByUserIdAndServiceAgreementIdWithFunctionAndDataGroupIds(eq(USER_ID),
                eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(Optional.empty());
    }

    private void mockApprovalUserContextWithApprovalId() {
        when(approvalUserContextJpaRepository.findByUserIdAndServiceAgreementId(eq(USER_ID), eq(SERVICE_AGREEMENT_ID)))
            .thenReturn(Optional.of(
                new ApprovalUserContext()
                    .withApprovalId(APPROVAL_ID)
            ));
    }
}