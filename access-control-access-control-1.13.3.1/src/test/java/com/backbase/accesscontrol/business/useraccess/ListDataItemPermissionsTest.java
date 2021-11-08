package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.matchers.MatcherUtil.getDataItemMatcher;
import static com.backbase.accesscontrol.matchers.MatcherUtil.getDataItemPermissionMatcher;
import static com.backbase.accesscontrol.matchers.MatcherUtil.getPermissionsMatcher;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceDataItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListDataItemPermissionsTest {

    @Mock
    private UserAccessPrivilegeService userAccessPrivilegeService;

    @Spy
    private ObjectConverter objectConverter = new ObjectConverter(spy(ObjectMapper.class));

    @InjectMocks
    private ListDataItemPermissions listDataItemPermissions;

    @Test
    public void shouldPassIfGetAllFunctionsIsInvoked() {
        String userId = "001";
        String serviceAgreementId = "SA-01";
        String dataItemType = "ARRANGEMENTS";
        String dataItemId = "ARR-01";
        String businessFunction = "Manage Data Groups";
        String resourceName = "Data Group";
        String privilegeView = "view";
        List<String> privileges = Collections.singletonList(privilegeView);
        PersistenceUserDataItemPermission userDataItemPermission = new PersistenceUserDataItemPermission()
            .withDataItem(new PersistenceDataItem()
                .withDataType(dataItemType)
                .withId(dataItemId))
            .withPermissions(Collections.singletonList(new PersistenceUserPermission()
                .withBusinessFunction(businessFunction)
                .withResource(resourceName)
                .withPrivileges(privileges)));
        List<PersistenceUserDataItemPermission> getResponseBodyList = Collections.singletonList(userDataItemPermission);

        when(userAccessPrivilegeService
            .getUserDataItemsPrivileges(eq(userId), eq(serviceAgreementId), eq(resourceName), eq(businessFunction),
                eq(privilegeView), eq(dataItemType), eq(dataItemId))).thenReturn(getResponseBodyList);

        InternalRequest<DataItemPermissionsSearchParametersHolder> request = getInternalRequest(
            new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withFunctionName(businessFunction)
                .withResourceName(resourceName)
                .withPrivilege(privilegeView));

        List<PresentationUserDataItemPermission> responsePrivileges = listDataItemPermissions
            .getDataItemPrivileges(request, dataItemType, dataItemId).getData();

        verify(userAccessPrivilegeService)
            .getUserDataItemsPrivileges(eq(userId), eq(serviceAgreementId), eq(resourceName), eq(businessFunction),
                eq(privilegeView), eq(dataItemType), eq(dataItemId));

        assertThat(responsePrivileges, contains(
            getDataItemPermissionMatcher(getDataItemMatcher(is(dataItemId), is(dataItemType)),
                contains(
                    getPermissionsMatcher(is(resourceName), is(businessFunction), hasItem(privilegeView))
                )
            )
        ));
    }
}
