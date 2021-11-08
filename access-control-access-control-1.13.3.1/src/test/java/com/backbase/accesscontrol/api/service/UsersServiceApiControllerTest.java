package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ArrangementPrivilegesGetResponseBodyToArrangementPrivilegesGetResponseBodyMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.BatchResponseItemExtendedToBatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.PrivilegesGetResponseBodyToPrivilegesGetResponseBodyMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.UserPermissionsSummaryGetResponseBodyToUserPermissionsSummaryGetResponseBodyMapper;
import com.backbase.accesscontrol.service.facades.UsersService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UsersServiceApiControllerTest {

    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private UsersService usersService;
    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private UsersServiceApiController testy;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(
                BatchResponseItemExtendedToBatchResponseItemExtendedMapper.class)),
            spy(Mappers.getMapper(
                UserPermissionsSummaryGetResponseBodyToUserPermissionsSummaryGetResponseBodyMapper.class)),
            spy(Mappers.getMapper(
                PrivilegesGetResponseBodyToPrivilegesGetResponseBodyMapper.class)),
            spy(Mappers
                .getMapper(ArrangementPrivilegesGetResponseBodyToArrangementPrivilegesGetResponseBodyMapper.class))
        ));

    @Test
    public void shouldReturnInternalRequestWithAListOfAllPrivileges() {
        String userName = "username";
        String serviceAgreementId = "SA-001";
        String functionName = "functionName";
        String resourceName = "resourceName";

        when(usersService.getPrivileges(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Lists.newArrayList(new PrivilegesGetResponseBody().withPrivilege("test")));

        List<com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody> response = testy
            .getPrivileges(userName, functionName, resourceName, serviceAgreementId).getBody();

        verify(usersService, times(1)).getPrivileges(eq(userName), eq(serviceAgreementId),
            eq(functionName), eq(resourceName));

        assertEquals(Lists.newArrayList(
            new com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody().privilege("test")
                .additions(new HashMap<>())),
            response);
    }

    @Test
    public void shouldGetAllUserPrivilegesWithServiceAgreementFromUserContext() {
        String userId = "username";
        String serviceAgreementId = "SA-001";
        String functionName = "functionName";
        String resourceName = "resourceName";

        mockGetServiceAgreementIdAndUserIdFromContext(serviceAgreementId, userId);
        when(usersService.getPrivileges(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Lists.newArrayList(new PrivilegesGetResponseBody().withPrivilege("test")));

        List<com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody> response = testy
            .getUserPrivileges(functionName, resourceName).getBody();

        verify(usersService).getPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName));

        assertEquals(Lists.newArrayList(
            new com.backbase.accesscontrol.service.rest.spec.model.PrivilegesGetResponseBody().privilege("test")
                .additions(new HashMap<>())),
            response);
    }

    @Test
    public void shouldReturnInternalRequestWithListOfAllArrangementPrivileges() {
        String userName = "username";
        String serviceAgreementId = "SA-001";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String privilege = "privilegeName";

        when(usersService
            .getArrangementPrivileges(any(DataItemPermissionsSearchParametersHolder.class)))
            .thenReturn(Lists.newArrayList(new ArrangementPrivilegesGetResponseBody()));

        List<com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody> response = testy
            .getArrangementPrivileges(userName, functionName, resourceName, serviceAgreementId, privilege).getBody();

        verify(usersService)
            .getArrangementPrivileges(eq(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userName)
                .withServiceAgreementId(serviceAgreementId)
                .withFunctionName(functionName)
                .withResourceName(resourceName)
                .withPrivilege(privilege)));

        assertEquals(Lists.newArrayList(
            new com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody()
                .additions(new HashMap<>())), response);
    }

    @Test
    public void shouldReturnInternalRequestWithListOfAllArrangementPrivilegesForMasterServiceAgreement() {
        String userName = "userId";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String privilege = "privilegeName";
        String legalEntityId = "le1";

        when(usersService
            .getArrangementPrivileges(any(DataItemPermissionsSearchParametersHolder.class)))
            .thenReturn(Lists.newArrayList(new ArrangementPrivilegesGetResponseBody()));

        com.backbase.dbs.user.api.client.v2.model.GetUser userGetResponseBody = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        userGetResponseBody.setId("userId");
        userGetResponseBody.setLegalEntityId(legalEntityId);
        when(userManagementService.getUserByInternalId(anyString()))
            .thenReturn(userGetResponseBody);

        List<com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody> response = testy
            .getArrangementPrivileges(userName, functionName, resourceName, null, privilege).getBody();

        verify(usersService)
            .getArrangementPrivileges(eq(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userName)
                .withLegalEntityId(legalEntityId)
                .withFunctionName(functionName)
                .withResourceName(resourceName)
                .withPrivilege(privilege)));

        assertEquals(Lists.newArrayList(
            new com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody()
                .additions(new HashMap<>())), response);
    }

    @Test
    public void shouldGetAllArrangementPrivilegesWithServiceAgreementFromUserContext() {
        String userId = "username";
        String serviceAgreementId = "SA-001";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String privilege = "privilegeName";

        mockGetServiceAgreementIdAndUserIdFromContext(serviceAgreementId, userId);
        when(usersService
            .getArrangementPrivileges(any(DataItemPermissionsSearchParametersHolder.class)))
            .thenReturn(Lists.newArrayList(new ArrangementPrivilegesGetResponseBody()));

        List<com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody> response = testy
            .getArrangementUserPrivileges(functionName, resourceName, privilege).getBody();

        verify(usersService)
            .getArrangementPrivileges(eq(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withFunctionName(functionName)
                .withLegalEntityId("le")
                .withResourceName(resourceName)
                .withPrivilege(privilege)));

        assertEquals(Lists.newArrayList(
            new com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody()
                .additions(new HashMap<>())), response);
    }

    @Test
    public void shouldReturnVoidInternalRequestWhenUserPermissionCheckIsCalled() {
        String userName = "username";
        String serviceAgreementId = "SA-001";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String privilege = "privilegeName";

        doNothing().when(usersService)
            .getUserPermissionCheck(anyString(), anyString(), anyString(), anyString(),
                anyString());

        testy
            .getUserPermissionCheck(userName, functionName, resourceName, privilege, serviceAgreementId);

        verify(usersService)
            .getUserPermissionCheck(eq(userName), eq(serviceAgreementId), eq(functionName), eq(resourceName),
                eq(privilege));

    }

    @Test
    public void shouldCheckIfUserHasPermissions() {
        String userId = "userId";
        String serviceAgreementId = "SA-001";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String privilege = "privilegeName";

        doNothing().when(usersService)
            .getUserPermissionCheck(eq(userId), eq(serviceAgreementId), eq(functionName),
                eq(resourceName), eq(privilege));
        when(userContextUtil.getServiceAgreementId())
            .thenReturn(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "le"));

        testy.getCheckUserPermission(functionName, resourceName, privilege);

        verify(usersService)
            .getUserPermissionCheck(eq(userId), eq(serviceAgreementId), eq(functionName),
                eq(resourceName), eq(privilege));
    }

    @Test
    public void shouldReturnPermissionSummaryForUserIdAndServiceAgreementId() {
        when(usersService.getUserPermissionsSummary())
            .thenReturn(Lists.newArrayList(new UserPermissionsSummaryGetResponseBody()));

        List<com.backbase.accesscontrol.service.rest.spec.model.UserPermissionsSummaryGetResponseBody> response = testy
            .getUserPermissionsSummary().getBody();

        verify(usersService).getUserPermissionsSummary();
        com.backbase.accesscontrol.service.rest.spec.model.UserPermissionsSummaryGetResponseBody expectedResponse = new com.backbase.accesscontrol.service.rest.spec.model.UserPermissionsSummaryGetResponseBody();

        assertEquals(expectedResponse.getFunction(), response.get(0).getFunction());
        assertEquals(expectedResponse.getResource(), response.get(0).getResource());
        assertNull(response.get(0).getResource());
        assertEquals(0, expectedResponse.getPermissions().size());
    }

    @Test
    public void shouldCheckArrangementPermission() {
        String userId = "userId";
        String serviceAgreementId = "SA-001";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String arrangementId = "resourceName";
        String privilege = "privilegeName";

        doNothing().when(usersService)
            .getArrangementPermissionCheck(any(DataItemPermissionsSearchParametersHolder.class), eq(arrangementId));

        testy.getArrangementPermissionCheck(arrangementId, userId, resourceName, functionName,
            privilege, serviceAgreementId);

        verify(usersService).getArrangementPermissionCheck(eq(new DataItemPermissionsSearchParametersHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withFunctionName(functionName)
            .withResourceName(resourceName)
            .withPrivilege(privilege)), eq(arrangementId));
    }

    @Test
    public void shouldCheckArrangementPermissionWhenSAIsNotProvided() {
        String userId = "userId";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String arrangementId = "resourceName";
        String privilege = "privilegeName";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setId(userId);
        user.setLegalEntityId("le1");

        when(userManagementService.getUserByInternalId(anyString()))
            .thenReturn(user);

        testy
            .getArrangementPermissionCheck(arrangementId, userId, resourceName, functionName, privilege, null);

        verify(usersService).getArrangementPermissionCheck(eq(new DataItemPermissionsSearchParametersHolder()
            .withUserId(userId)
            .withLegalEntityId("le1")
            .withFunctionName(functionName)
            .withResourceName(resourceName)
            .withPrivilege(privilege)), eq(arrangementId));
    }

    @Test
    public void shouldCallPutAssignUserPermissions() {

        when(usersService.saveBulkUserPermissions(anyList()))
            .thenReturn(Lists.newArrayList(new BatchResponseItemExtended()));

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> result = testy
            .putAssignUserPermissions(new ArrayList<>()).getBody();

        verify(usersService).saveBulkUserPermissions(eq(new ArrayList<>()));

        assertEquals(
            Lists.newArrayList(new com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended()
                .errors(new ArrayList<>()).additions(new HashMap<>())),
            result);
    }

    private void mockGetServiceAgreementIdAndUserIdFromContext(String serviceAgreementId, String userId) {
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "le"));
    }
}