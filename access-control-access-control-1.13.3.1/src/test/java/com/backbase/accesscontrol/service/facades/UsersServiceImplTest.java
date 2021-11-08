package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.routes.useraccess.AssignUserPermissionsBatchRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.GetArrangementPrivilegesRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.GetUserPrivilegesSummaryRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ListDataItemPrivilegesRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ListPrivilegesRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ValidatePermissionsRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationDataItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UsersServiceImplTest {

    public static final String ID = "00001";

    @Mock
    private ListPrivilegesRouteProxy listPrivilegesRouteProxy;
    @Mock
    private ValidatePermissionsRouteProxy validatePermissionsRouteProxy;
    @Mock
    private GetArrangementPrivilegesRouteProxy getArrangementPrivilegesRouteProxy;
    @Mock
    private GetUserPrivilegesSummaryRouteProxy getUserPrivilegesSummaryRouteProxy;
    @Mock
    private AssignUserPermissionsBatchRouteProxy assignUserPermissionsBatchRouteProxy;
    @Mock
    private ListDataItemPrivilegesRouteProxy listDataItemPrivilegesRouteProxy;

    @InjectMocks
    private UsersServiceImpl usersService;
    @Spy
    private InternalRequestContext internalRequestContext = new DefaultInternalRequestContext();

    @Test
    public void shouldReturnInternalRequestWithAListOfAllPrivileges() {

        PrivilegesGetResponseBody functionsGetResponseBody = new PrivilegesGetResponseBody()
            .withPrivilege("read");

        InternalRequest<List<PrivilegesGetResponseBody>> returnedInternalRequest = getInternalRequest(
            Collections.singletonList(functionsGetResponseBody));

        when(listPrivilegesRouteProxy.getPrivileges(any(InternalRequest.class), anyString(), anyString(), anyString(),
            anyString()))
            .thenReturn(returnedInternalRequest);

        List<PrivilegesGetResponseBody> allFunctions = usersService
            .getPrivileges("001", "001", "Contacts", "Contacts");

        verify(listPrivilegesRouteProxy)
            .getPrivileges(any(InternalRequest.class), eq("001"), eq("001"), eq("Contacts"), eq("Contacts"));
        assertEquals(functionsGetResponseBody.getPrivilege(), allFunctions.get(0).getPrivilege());
    }

    @Test
    public void shouldGetArrangementPrivileges() {
        ArrangementPrivilegesGetResponseBody data = new ArrangementPrivilegesGetResponseBody()
            .withArrangementId("1")
            .withPrivileges(asList(
                new Privilege()
                    .withPrivilege("read"),
                new Privilege()
                    .withPrivilege("create")));

        InternalRequest<List<ArrangementPrivilegesGetResponseBody>> returnedInternalRequest = getInternalRequest(
            Collections.singletonList(data));

        InternalRequest<DataItemPermissionsSearchParametersHolder> request = getInternalRequest(
            new DataItemPermissionsSearchParametersHolder()
                    .withUserId("001")
                    .withServiceAgreementId("001")
                    .withFunctionName("Contacts")
                    .withResourceName("Contacts"),
                internalRequestContext);

        when(getArrangementPrivilegesRouteProxy
            .getArrangementPrivileges(refEq(request)))
            .thenReturn(returnedInternalRequest);

        List<ArrangementPrivilegesGetResponseBody> returnedArrangementPrivileges = usersService
            .getArrangementPrivileges(new DataItemPermissionsSearchParametersHolder()
                .withUserId("001")
                .withServiceAgreementId("001")
                .withFunctionName("Contacts")
                .withResourceName("Contacts"));

        assertEquals(data.getPrivileges().size(),
            returnedArrangementPrivileges.get(0).getPrivileges().size());
    }

    @Test
    public void shouldGetUserPrivilegesSummary() {
        HashMap<String, Boolean> permissions = new HashMap<>();
        permissions.put("view", true);
        UserPermissionsSummaryGetResponseBody userPermissionsSummaryGetResponseBody = new UserPermissionsSummaryGetResponseBody()
            .withResource("resourceName")
            .withFunction("functionName")
            .withPermissions(permissions);

        List<UserPermissionsSummaryGetResponseBody> allUserPermissionsSummary = new ArrayList<>();
        allUserPermissionsSummary.add(userPermissionsSummaryGetResponseBody);

        InternalRequest<List<UserPermissionsSummaryGetResponseBody>> returnedInternalRequest = getInternalRequest(
            allUserPermissionsSummary);

        when(getUserPrivilegesSummaryRouteProxy.getUserPrivilegesSummary(any(InternalRequest.class)))
            .thenReturn(returnedInternalRequest);

        List<UserPermissionsSummaryGetResponseBody> userPermissionsSummary =
            usersService.getUserPermissionsSummary();

        verify(getUserPrivilegesSummaryRouteProxy).getUserPrivilegesSummary(any(InternalRequest.class));

        assertEquals(allUserPermissionsSummary.size(), userPermissionsSummary.size());
    }

    @Test
    public void shouldReturnVoidInternalRequestWhenUserPermissionCheckIsCalled() {
        InternalRequest<Void> returnedInternalRequest = getInternalRequest(null);

        when(validatePermissionsRouteProxy
            .getUserPermissionCheck(any(InternalRequest.class), anyString(), anyString(), anyString(), anyString(),
                anyString()))
            .thenReturn(returnedInternalRequest);

        usersService
            .getUserPermissionCheck("001", "001", "Contacts", "Contacts", "read");

        verify(validatePermissionsRouteProxy)
            .getUserPermissionCheck(any(InternalRequest.class), eq("001"), eq("001"), eq("Contacts"), eq("Contacts"),
                eq("read"));
    }

    @Test
    public void shouldCallAssignUserPermissionsBatchRouteProxy() {
        InternalRequest<List<PresentationAssignUserPermissions>> testRequest = new InternalRequest<>(
            Lists.newArrayList(), internalRequestContext);

        when(assignUserPermissionsBatchRouteProxy.assignUserPermissionsBatch(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(new ArrayList<>(), internalRequestContext));

        usersService.saveBulkUserPermissions(Lists.newArrayList());

        verify(assignUserPermissionsBatchRouteProxy).assignUserPermissionsBatch(refEq(testRequest));
    }

    @Test
    public void shouldReturnDataItemPrivileges() {
        String userId = "001";
        String serviceAgreementId = "SA-01";
        String dataItemType = "ARRANGEMENTS";
        String dataItemId = "ARR-01";
        String businessFunction = "Manage Data Groups";
        String resourceName = "Data Group";
        String privilegeView = "view";
        List<String> privileges = Collections.singletonList(privilegeView);
        PresentationUserDataItemPermission userDataItemPermission = new PresentationUserDataItemPermission()
            .withDataItem(new PresentationDataItem()
                .withDataType(dataItemType)
                .withId(dataItemId))
            .withPermissions(Collections.singletonList(new PresentationUserPermission()
                .withBusinessFunction(businessFunction)
                .withResource(resourceName)
                .withPrivileges(privileges)));

        InternalRequest<List<PresentationUserDataItemPermission>> returnedInternalRequest = getInternalRequest(
            Collections.singletonList(userDataItemPermission));

        InternalRequest<DataItemPermissionsSearchParametersHolder> request = getInternalRequest(
            new DataItemPermissionsSearchParametersHolder()
                    .withUserId(userId)
                    .withServiceAgreementId(serviceAgreementId)
                    .withFunctionName(businessFunction)
                    .withResourceName(resourceName)
                    .withPrivilege(privilegeView),
                internalRequestContext);

        when(listDataItemPrivilegesRouteProxy
            .getDataItemPrivileges(any(InternalRequest.class), anyString(), anyString()))
            .thenReturn(returnedInternalRequest);

        List<PresentationUserDataItemPermission> permissionsResponse = usersService
            .getDataItemPrivileges(new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withFunctionName(businessFunction)
                .withResourceName(resourceName)
                .withPrivilege(privilegeView), dataItemType, dataItemId);
        verify(listDataItemPrivilegesRouteProxy).getDataItemPrivileges(eq(request), eq(dataItemType), eq(dataItemId));

        assertEquals(Collections.singletonList(userDataItemPermission), permissionsResponse);
    }
}