package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.dto.AssignUserPermissionsData;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.accesscontrol.service.batch.permission.UserPermissionBatchUpdate;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationFunctionGroupDataGroup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionBatchUpdateTest {

    @InjectMocks
    private UserPermissionBatchUpdate userPermissionBatchUpdate;

    @Mock
    private PermissionService permissionService;

    @Mock
    private EventBus eventBus;

    @Mock
    private Validator validator;

    @Test
    public void shouldUpdatePermissions() {

        AssignUserPermissionsData permissionsPutRequestBody1 = createPermissionsPutRequestBody("id.user1", "extSa-01",
            "le-01", emptyList());

        AssignUserPermissionsData permissionsPutRequestBody2 = createPermissionsPutRequestBody("id.user2", "extSa-02",
            "le-02", emptyList());

        AssignUserPermissionsData permissionsPutRequestBody3 = createPermissionsPutRequestBody("id.user3", "extSa-03",
            "le-03", emptyList());

        List<AssignUserPermissionsData> requestData = asList(permissionsPutRequestBody1,
            permissionsPutRequestBody2, permissionsPutRequestBody3);

        mockUpdateUserPermissions(permissionsPutRequestBody1);
        mockUpdatePermissionsWithException(permissionsPutRequestBody2,
            getBadRequestException("message.error", "code.error"));
        mockUpdateUserPermissions(permissionsPutRequestBody3);

        List<ResponseItemExtended> responseItemExtended = userPermissionBatchUpdate.processBatchItems(requestData);

        verify(permissionService, times(3))
            .updateUserPermission(any(AssignUserPermissionsData.class));

        assertEquals(3, responseItemExtended.size());

        assertEquals(ItemStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemExtended.get(1).getStatus());
        assertEquals("id.user2", responseItemExtended.get(1).getResourceId());
        assertEquals("message.error", responseItemExtended.get(1).getErrors().get(0));

        assertEquals(ItemStatusCode.HTTP_STATUS_OK, responseItemExtended.get(0).getStatus());
        assertEquals(ItemStatusCode.HTTP_STATUS_OK, responseItemExtended.get(2).getStatus());
    }

    private void mockUpdateUserPermissions(AssignUserPermissionsData permissionsPutRequestBody1) {
        when(permissionService.updateUserPermission(eq(permissionsPutRequestBody1)))
            .thenReturn("id");
    }

    private void mockUpdatePermissionsWithException(AssignUserPermissionsData permissionsPutRequestBody,
        Exception exception) {

        doThrow(exception).when(permissionService).updateUserPermission(eq(permissionsPutRequestBody));
    }

    private AssignUserPermissionsData createPermissionsPutRequestBody(String userId,
        String externalServiceAgreementId, String legalEntityId,
        List<PresentationFunctionGroupDataGroup> functionDataPairs) {

        String externalUserId = "externalUserId";

        com.backbase.dbs.user.api.client.v2.model.GetUser userResponse = new GetUser();
        userResponse.setId(userId);
        userResponse.setExternalId(externalUserId);
        userResponse.setLegalEntityId(legalEntityId);

        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> externalIdToUserMap = new HashMap<>();
        externalIdToUserMap.put(externalUserId, userResponse);

        PresentationAssignUserPermissions assignUserPermissions = new PresentationAssignUserPermissions()
            .withExternalUserId(externalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withFunctionGroupDataGroups(functionDataPairs);

        return new AssignUserPermissionsData(assignUserPermissions, externalIdToUserMap);
    }

}
