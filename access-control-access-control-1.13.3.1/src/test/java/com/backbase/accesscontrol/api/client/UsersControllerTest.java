package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_065;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.client.rest.spec.model.UsersByPermission;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.GetUsersByPermissionsParameters;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ArrangementPrivilegesGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationUserDataItemPermissionConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PrivilegesGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UserPermissionsSummaryGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UsersByPermissionConverter;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.UsersFlowService;
import com.backbase.accesscontrol.service.facades.UsersService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationDataItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersByPermissionsResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UsersControllerTest {

    @Mock
    private UsersService usersServiceImpl;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private ValidationConfig validationConfig;
    @Mock
    private PermissionValidationService permissionValidationService;
    @Mock
    private UsersFlowService usersFlowService;

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(ArrangementPrivilegesGetResponseBodyConverter.class)),
            spy(Mappers.getMapper(PresentationUserDataItemPermissionConverter.class)),
            spy(Mappers.getMapper(UserPermissionsSummaryGetResponseBodyConverter.class)),
            spy(Mappers.getMapper(PrivilegesGetResponseBodyConverter.class)),
            spy(Mappers.getMapper(UsersByPermissionConverter.class))
        ));

    @InjectMocks
    private UsersController usersController;

    @Test
    public void getUserPrivilegesWithServiceAgreementFromUserContext() {
        String privilegeView = "view";
        String userId = "1";
        String serviceAgreementId = "001";
        String functionName = "function";
        String resourceName = "resource";

        List<PrivilegesGetResponseBody> privileges = Lists.newArrayList(new PrivilegesGetResponseBody()
            .withPrivilege(privilegeView));

        mockGetServiceAgreementIdFromContext(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "le"));
        when(usersServiceImpl
            .getPrivileges(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(privileges);
        List<com.backbase.accesscontrol.client.rest.spec.model.PrivilegesGetResponseBody> returnedPrivileges = usersController
            .getUserPrivileges(functionName, resourceName).getBody();

        verify(usersServiceImpl)
            .getPrivileges(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(usersServiceImpl);
        assertEquals(1, returnedPrivileges.size());
        assertEquals(privilegeView, returnedPrivileges.get(0).getPrivilege());
    }

    @Test
    public void getArrangementPrivilegesWithServiceAgreementFromUserContext() {
        String privilegeView = "view";
        String arrangementId = "251";
        String userId = "1";
        String serviceAgreementId = "001";
        String functionName = "function";
        String resourceName = "resource";

        ArrangementPrivilegesGetResponseBody arrangementPrivilegesGetResponseBody = new ArrangementPrivilegesGetResponseBody()
            .withArrangementId(arrangementId);
        List<ArrangementPrivilegesGetResponseBody> privileges = new ArrayList<>();
        privileges.add(arrangementPrivilegesGetResponseBody);

        mockGetServiceAgreementIdFromContext(serviceAgreementId);
        DataItemPermissionsSearchParametersHolder wantedRequest = new DataItemPermissionsSearchParametersHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withFunctionName(functionName)
            .withResourceName(resourceName)
            .withLegalEntityId("leid")
            .withPrivilege(privilegeView);

        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "leid"));

        when(usersServiceImpl
            .getArrangementPrivileges(refEq(wantedRequest)))
            .thenReturn(privileges);

        List<com.backbase.accesscontrol.client.rest.spec.model.ArrangementPrivilegesGetResponseBody> arrangementPrivileges = usersController
            .getArrangementUserPrivileges(functionName, resourceName, privilegeView).getBody();

        verify(usersServiceImpl, times(1))
            .getArrangementPrivileges(refEq(wantedRequest));
        verifyNoMoreInteractions(usersServiceImpl);

        assertEquals(1, arrangementPrivileges.size());
        assertEquals(arrangementId, arrangementPrivileges.get(0).getArrangementId());
    }

    @Test
    public void getUserPrivilegesSummary() {

        String functionName = "functionName";
        String resourceName = "resourceName";
        HashMap<String, Boolean> permissions = new HashMap<>();
        permissions.put("view", true);
        UserPermissionsSummaryGetResponseBody userPermissionsSummaryGetResponseBody = new UserPermissionsSummaryGetResponseBody()
            .withFunction(functionName)
            .withResource(resourceName)
            .withPermissions(permissions);
        List<UserPermissionsSummaryGetResponseBody> summaryGetResponseBodies = new ArrayList<>();
        summaryGetResponseBodies.add(userPermissionsSummaryGetResponseBody);

        when(usersServiceImpl.getUserPermissionsSummary()).thenReturn(summaryGetResponseBodies);

        List<com.backbase.accesscontrol.client.rest.spec.model.UserPermissionsSummaryGetResponseBody> userPermissionsSummary = usersController
            .getUserPermissionsSummary().getBody();

        verify(usersServiceImpl, times(1)).getUserPermissionsSummary();
        verifyNoMoreInteractions(usersServiceImpl);

        assertEquals(1, userPermissionsSummary.size());
        assertEquals(functionName, userPermissionsSummary.get(0).getFunction());
        assertEquals(resourceName, userPermissionsSummary.get(0).getResource());
        assertTrue(userPermissionsSummary.get(0).getPermissions().containsKey("view"));
    }

    @Test
    public void checkUserPermissions() {
        String privilegeView = "view";
        String userId = "1";
        String serviceAgreementId = "001";
        String functionName = "function";
        String resourceName = "resource";

        doNothing().when(usersServiceImpl)
            .getUserPermissionCheck(eq(userId), eq(serviceAgreementId), eq(functionName),
                eq(resourceName), eq(privilegeView));
        mockGetServiceAgreementIdFromContext(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "le"));

        usersController.getCheckUserPermission(functionName, resourceName, privilegeView);

        verify(usersServiceImpl, times(1))
            .getUserPermissionCheck(eq(userId), eq(serviceAgreementId), eq(functionName),
                eq(resourceName), eq(privilegeView));
        verifyNoMoreInteractions(usersServiceImpl);
    }

    @Test
    public void shouldGetAllDataItemPrivileges() {
        String privilegeView = "view";
        String dataItemType = "ARRANGEMENTS";
        String dataItemId = "ARR-01";
        String userId = "1";
        String serviceAgreementId = "001";
        String functionName = "function";
        String resourceName = "resource";

        List<com.backbase.accesscontrol.client.rest.spec.model.PresentationUserDataItemPermission> expectedResponse = singletonList(
            new com.backbase.accesscontrol.client.rest.spec.model.PresentationUserDataItemPermission()
                .additions(new HashMap<>())
                .permissions(
                    singletonList(new com.backbase.accesscontrol.client.rest.spec.model.PresentationUserPermission()
                        .additions(new HashMap<>())
                        .businessFunction(functionName)
                        .resource(resourceName)
                        .privileges(singletonList(privilegeView))))
                .dataItem(new com.backbase.accesscontrol.client.rest.spec.model.PresentationDataItem()
                    .additions(new HashMap<>())
                    .dataType(dataItemType)
                    .id(dataItemId))
        );

        PresentationUserDataItemPermission presentationUserDataItemPermission = new PresentationUserDataItemPermission()
            .withPermissions(singletonList(new PresentationUserPermission()
                .withBusinessFunction(functionName)
                .withResource(resourceName)
                .withPrivileges(singletonList(privilegeView))))
            .withDataItem(new PresentationDataItem()
                .withDataType(dataItemType)
                .withId(dataItemId));

        mockGetServiceAgreementIdFromContext(serviceAgreementId);

        when(usersServiceImpl.getDataItemPrivileges(eq(new DataItemPermissionsSearchParametersHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId)
            .withFunctionName(functionName)
            .withResourceName(resourceName)
            .withPrivilege(privilegeView)), eq(dataItemType), eq(dataItemId)))
            .thenReturn(singletonList(presentationUserDataItemPermission));
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "le"));

        doNothing()
            .when(validationConfig).validateDataGroupType(dataItemType);
        List<com.backbase.accesscontrol.client.rest.spec.model.PresentationUserDataItemPermission> dataItemPermissions = usersController
            .getDataItemPermissionsContext(functionName, resourceName, privilegeView, dataItemType, dataItemId)
            .getBody();

        verify(usersServiceImpl, times(1)).getDataItemPrivileges(
            eq(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withFunctionName(functionName)
                .withResourceName(resourceName)
                .withPrivilege(privilegeView)), eq(dataItemType),
            eq(dataItemId));
        verifyNoMoreInteractions(usersServiceImpl);

        assertEquals(1, dataItemPermissions.size());
        assertEquals(expectedResponse.get(0).getDataItem().getId(), dataItemPermissions.get(0).getDataItem().getId());
    }

    @Test
    public void shouldThrowForbiddenWhenNoLoggedUser() {
        String privilegeView = "view";
        String dataItemType = "ARRANGEMENTS";
        String dataItemId = "ARR-01";
        String serviceAgreementId = "001";
        String functionName = "function";
        String resourceName = "resource";

        mockGetServiceAgreementIdFromContext(serviceAgreementId);

        when(userContextUtil.getUserContextDetails())
            .thenThrow(getForbiddenException(AccessGroupErrorCodes.ERR_AG_071.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_071.getErrorCode()));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> usersController
            .getDataItemPermissionsContext(functionName, resourceName, privilegeView, dataItemType, dataItemId));

        assertThat(exception, new ForbiddenErrorMatcher(AccessGroupErrorCodes.ERR_AG_071.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_071.getErrorCode()));
    }

    @Test
    public void shouldGetUsersByPermission() {

        String filterByServiceAgreementId = "said1";
        String functionName = "Assign Permissions";
        String privilege = "view";
        String dataGroupType = "CUSTOMERS";
        String dataItemId = "dataitemid";

        UsersByPermissionsResponseBody usersByPermissionsResponseBody = new UsersByPermissionsResponseBody()
            .withUserIds(Arrays.asList("uid1", "uid2"));

        GetUsersByPermissionsParameters parameters = new GetUsersByPermissionsParameters(filterByServiceAgreementId,
            functionName, privilege, dataGroupType, dataItemId);
        when(usersFlowService.getUsersByPermissions(parameters)).thenReturn(usersByPermissionsResponseBody);

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(filterByServiceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        UsersByPermission usersByPermissions = usersController
            .getUsersByPermissions(functionName, filterByServiceAgreementId, privilege, dataGroupType, dataItemId)
            .getBody();

        verify(permissionValidationService)
            .validateAccessToServiceAgreementResource(filterByServiceAgreementId, AccessResourceType.USER_AND_ACCOUNT);
        verify(usersFlowService, times(1)).getUsersByPermissions(refEq(parameters));
        verifyNoMoreInteractions(usersFlowService);

        assertEquals(2, usersByPermissions.getUserIds().size());
        assertEquals(Arrays.asList("uid1", "uid2"), usersByPermissions.getUserIds());
    }

    @Test
    public void shouldGetUsersByPermissionWithServiceAgreementFromUserContext() {

        String filterBySaIdFromContext = "said1";
        String functionName = "Assign Permissions";
        String privilege = "view";
        String dataGroupType = "CUSTOMERS";
        String dataItemId = "dataitemid";

        UsersByPermissionsResponseBody usersByPermissionsResponseBody = new UsersByPermissionsResponseBody()
            .withUserIds(Arrays.asList("uid1", "uid2"));

        GetUsersByPermissionsParameters parameters = new GetUsersByPermissionsParameters(filterBySaIdFromContext,
            functionName, privilege, dataGroupType, dataItemId);
        mockGetServiceAgreementIdFromContext(filterBySaIdFromContext);
        when(usersFlowService.getUsersByPermissions(parameters)).thenReturn(usersByPermissionsResponseBody);

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(filterBySaIdFromContext, AccessResourceType.USER_AND_ACCOUNT);

        UsersByPermission usersByPermissions = usersController
            .getUsersByPermissions(functionName, filterBySaIdFromContext, privilege, dataGroupType, dataItemId)
            .getBody();

        verify(permissionValidationService)
            .validateAccessToServiceAgreementResource(filterBySaIdFromContext, AccessResourceType.USER_AND_ACCOUNT);
        verify(usersFlowService, times(1)).getUsersByPermissions(refEq(parameters));
        verifyNoMoreInteractions(usersFlowService);

        assertEquals(2, usersByPermissions.getUserIds().size());
        assertEquals(Arrays.asList("uid1", "uid2"), usersByPermissions.getUserIds());
    }

    @Test
    public void shouldThrowErrorOnGetUsersByPermission() {

        String filterByServiceAgreementId = "said1";
        String functionName = "Assign Permissions";
        String privilege = "view";
        String dataGroupType = "CUSTOMERS";
        String dataItemId = "dataitemid";

        UsersByPermissionsResponseBody usersByPermissionsResponseBody = new UsersByPermissionsResponseBody()
            .withUserIds(Arrays.asList("uid1", "uid2"));

        GetUsersByPermissionsParameters parameters = new GetUsersByPermissionsParameters(filterByServiceAgreementId,
            functionName, privilege, dataGroupType, dataItemId);
        when(usersFlowService.getUsersByPermissions(parameters)).thenReturn(usersByPermissionsResponseBody);

        doThrow(getForbiddenException(
            AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_032.getErrorCode()))
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(filterByServiceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> usersController
            .getUsersByPermissions(functionName, filterByServiceAgreementId, privilege, dataGroupType, dataItemId));

        assertThat(exception, new ForbiddenErrorMatcher(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_032.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorOnGetUsersByPermissionWhenDataGroupTypeIsNullAndDataItemIdIsNot() {

        String filterByServiceAgreementId = "said1";
        String functionName = "Assign Permissions";
        String privilege = "view";
        String dataItemId = "dataitemid";

        BadRequestException exception = assertThrows(BadRequestException.class, () -> usersController
            .getUsersByPermissions(filterByServiceAgreementId, functionName, privilege, null, dataItemId));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_065.getErrorMessage(), ERR_ACQ_065.getErrorCode()));
    }

    @Test
    public void shouldThrowErrorOnGetUsersByPermissionWhenDataItemIdIsIsNullAndDataGroupTypeIsNot() {

        String filterByServiceAgreementId = "said1";
        String functionName = "Assign Permissions";
        String privilege = "view";
        String dataGroupType = "CUSTOMERS";

        BadRequestException exception = assertThrows(BadRequestException.class, () -> usersController
            .getUsersByPermissions(filterByServiceAgreementId, functionName, privilege, dataGroupType, null));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_065.getErrorMessage(), ERR_ACQ_065.getErrorCode()));
    }

    private void mockGetServiceAgreementIdFromContext(String serviceAgreementId) {
        when(userContextUtil.getServiceAgreementId())
            .thenReturn(serviceAgreementId);
    }
}