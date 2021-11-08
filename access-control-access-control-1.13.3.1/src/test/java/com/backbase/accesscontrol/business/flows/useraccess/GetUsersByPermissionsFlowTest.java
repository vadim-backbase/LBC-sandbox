package com.backbase.accesscontrol.business.flows.useraccess;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserAccessPAndPService;
import com.backbase.accesscontrol.dto.GetUsersByPermissionsParameters;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.UserFunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersByPermissionsResponseBody;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetUsersByPermissionsFlowTest {

    @Mock
    private UserAccessPAndPService userAccessPAndPService;
    @InjectMocks
    private GetUsersByPermissionsFlow getUsersByPermissionsFlow;

    @Test
    public void shouldExecuteFlow() {

        GetUsersByPermissionsParameters searchParameters = new GetUsersByPermissionsParameters("said",
            "Assign Permissions", "view", "CUSTOMERS", "dataitemid");

        UserFunctionGroupsGetResponseBody userFunctionGroupsGetResponseBody1 = new UserFunctionGroupsGetResponseBody()
            .withUserId("uid1")
            .withFunctionGroupIds(new LinkedHashSet<>(Arrays.asList("fg1", "fg2")));

        UserFunctionGroupsGetResponseBody userFunctionGroupsGetResponseBody2 = new UserFunctionGroupsGetResponseBody()
            .withUserId("uid2")
            .withFunctionGroupIds(new LinkedHashSet<>(Arrays.asList("fg3", "fg4")));

        UsersByPermissionsResponseBody usersByPermissionsResponseBody = new UsersByPermissionsResponseBody()
            .withUserIds(Arrays.asList("uid1", "uid2"));

        when(userAccessPAndPService.getUsersByPermissions(refEq(searchParameters)))
            .thenReturn(Arrays.asList(userFunctionGroupsGetResponseBody1, userFunctionGroupsGetResponseBody2));

        UsersByPermissionsResponseBody res = getUsersByPermissionsFlow.start(searchParameters);

        assertEquals(usersByPermissionsResponseBody, res);

    }

}