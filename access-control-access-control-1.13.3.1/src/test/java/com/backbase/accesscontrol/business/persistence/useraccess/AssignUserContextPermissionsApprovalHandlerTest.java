package com.backbase.accesscontrol.business.persistence.useraccess;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.domain.dto.PersistenceUserContextPermissionsApproval;
import com.backbase.accesscontrol.dto.parameterholder.UserPermissionsApprovalParameterHolder;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignUserContextPermissionsApprovalHandlerTest {

    @InjectMocks
    private AssignUserContextPermissionsApprovalHandler assignUserContextPermissionsApprovalHandler;

    @Mock
    private PermissionService permissionService;

    @Test
    public void shouldCallPermissionServiceWithNormalizedInputData() {
        String userId = "user";
        String serviceAgreementId = "sa-01";
        String legalEntityId = "le-01";
        String approvalId = "a-01";

        UserPermissionsApprovalParameterHolder parameterHolder = new UserPermissionsApprovalParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withLegalEntityId(legalEntityId)
            .withApprovalId(approvalId);
        UserContextPermissions userContextPermissions1 = new UserContextPermissions();
        userContextPermissions1.setFunctionGroupId("fg1");
        userContextPermissions1.setDataGroupIds(Sets.newHashSet("dg1"));
        UserContextPermissions userContextPermissions2 = new UserContextPermissions();
        userContextPermissions2.setFunctionGroupId("fg1");
        userContextPermissions2.setDataGroupIds(Sets.newHashSet("dg2"));
        UserContextPermissions userContextPermissions3 = new UserContextPermissions();
        userContextPermissions2.setFunctionGroupId("fg2");
        userContextPermissions2.setDataGroupIds(Sets.newHashSet((String) null));

        Set<UserContextPermissions> requestItems = Sets.newHashSet(
            userContextPermissions1,
            userContextPermissions2,
            userContextPermissions3,
            null);

        PersistenceUserContextPermissionsApproval request = new PersistenceUserContextPermissionsApproval();
        request.setPermissions(requestItems);
        UserContextPermissions item1 = new UserContextPermissions();
        item1.setFunctionGroupId("fg1");
        item1.setDataGroupIds(Sets.newHashSet("dg1", "dg2"));
        UserContextPermissions item2 = new UserContextPermissions();
        item2.setFunctionGroupId("fg1");
        Set<UserContextPermissions> normalizedRequest = Sets.newHashSet(item1, item2);

        doNothing().when(permissionService).assignUserContextPermissionsApproval(
            eq(serviceAgreementId),
            eq(userId), eq(legalEntityId),
            eq(approvalId),
            refEq(normalizedRequest));
        assignUserContextPermissionsApprovalHandler.executeRequest(parameterHolder, request);

        verify(permissionService)
            .assignUserContextPermissionsApproval(eq(serviceAgreementId), eq(userId), eq(legalEntityId),
                eq(approvalId), refEq(normalizedRequest));
    }

    @Test
    public void createSuccessEvent() {
        String userId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        String legalEntityId = UUID.randomUUID().toString();
        String approvalId = UUID.randomUUID().toString();

        UserPermissionsApprovalParameterHolder parameterHolder = new UserPermissionsApprovalParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withLegalEntityId(legalEntityId)
            .withApprovalId(approvalId);

        Event successEvent = assignUserContextPermissionsApprovalHandler
            .createSuccessEvent(parameterHolder, new PersistenceUserContextPermissionsApproval(), null);
        assertNull(successEvent);
    }

    @Test
    public void createFailureEvent() {
        String userId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        String legalEntityId = UUID.randomUUID().toString();
        String approvalId = UUID.randomUUID().toString();

        UserPermissionsApprovalParameterHolder parameterHolder = new UserPermissionsApprovalParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withLegalEntityId(legalEntityId)
            .withApprovalId(approvalId);

        Exception exception = new Exception("Error message");

        Event failedEvent = assignUserContextPermissionsApprovalHandler
            .createFailureEvent(parameterHolder, new PersistenceUserContextPermissionsApproval(), exception);

        assertNull(failedEvent);
    }
}
