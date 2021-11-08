package com.backbase.accesscontrol.business.persistence.useraccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.domain.dto.PersistentUserContextPermissionsPutRequestBody;
import com.backbase.accesscontrol.dto.parameterholder.UserIdServiceAgreementIdParameterHolder;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssignUserContextPermissionsHandlerTest {

    @Mock
    private PermissionService permissionService;
    @InjectMocks
    private AssignUserContextPermissionsHandler assignUserContextPermissionsHandler;

    @Test
    public void shouldCallPermissionServiceWithNormalizedInputData() {

        String userId = "user";
        String serviceAgreementId = "sa";
        String legalEntityId = "le";

        UserIdServiceAgreementIdParameterHolder parameterHolder = new UserIdServiceAgreementIdParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId);
        UserContextPermissions assignUserContextPermisionsPutRequestBody1 = new UserContextPermissions();
        assignUserContextPermisionsPutRequestBody1.setDataGroupIds(Sets.newHashSet("dg1"));
        UserContextPermissions assignUserContextPermisionsPutRequestBody2 = new UserContextPermissions();
        assignUserContextPermisionsPutRequestBody2.setFunctionGroupId("fg1");
        assignUserContextPermisionsPutRequestBody2.setDataGroupIds(Sets.newHashSet("dg2"));
        UserContextPermissions assignUserContextPermisionsPutRequestBody3 = new UserContextPermissions();
        assignUserContextPermisionsPutRequestBody3.setFunctionGroupId("fg2");
        assignUserContextPermisionsPutRequestBody3.setDataGroupIds(Sets.newHashSet((String) null));

        Set<UserContextPermissions> requestItems = Sets.newHashSet(
            assignUserContextPermisionsPutRequestBody1,
            assignUserContextPermisionsPutRequestBody2,
            assignUserContextPermisionsPutRequestBody3,
            null);

        PersistentUserContextPermissionsPutRequestBody request = new PersistentUserContextPermissionsPutRequestBody();
        request.setPermissions(requestItems);
        request.setUserLegalEntityId(legalEntityId);
        UserContextPermissions item = new UserContextPermissions();
        item.setFunctionGroupId("fg1");
        item.setDataGroupIds(Sets.newHashSet("dg1", "dg2"));
        UserContextPermissions item1 = new UserContextPermissions();
        item1.setFunctionGroupId("fg2");
        item1.setDataGroupIds(Sets.newHashSet("dg1", "dg2"));
        Set<UserContextPermissions> normaizedRequest = Sets.newHashSet(item, item1);

        doNothing().when(permissionService).assignUserContextPermissions(
            eq(serviceAgreementId),
            eq(userId), eq(legalEntityId),
            refEq(normaizedRequest));
        assignUserContextPermissionsHandler.executeRequest(parameterHolder, request);

        verify(permissionService)
            .assignUserContextPermissions(eq(serviceAgreementId), eq(userId), eq(legalEntityId),
                refEq(normaizedRequest));
    }

    @Test
    public void createSuccessEvent() {
        String userId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();

        UserIdServiceAgreementIdParameterHolder parameterHolder = new UserIdServiceAgreementIdParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId);

        UserContextEvent successEvent = assignUserContextPermissionsHandler
            .createSuccessEvent(parameterHolder, new PersistentUserContextPermissionsPutRequestBody(), null);
        assertEquals(userId, successEvent.getUserId());
        assertEquals(serviceAgreementId, successEvent.getServiceAgreementId());
    }

    @Test
    public void createFailureEvent() {
        String userId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();

        UserIdServiceAgreementIdParameterHolder parameterHolder = new UserIdServiceAgreementIdParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId);

        Exception exception = new Exception("Error message");

        Event failedEvent = assignUserContextPermissionsHandler
            .createFailureEvent(parameterHolder, new PersistentUserContextPermissionsPutRequestBody(), exception);

        assertNull(failedEvent);
    }
}
