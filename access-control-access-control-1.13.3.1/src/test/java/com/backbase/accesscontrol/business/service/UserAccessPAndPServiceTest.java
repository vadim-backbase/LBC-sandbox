package com.backbase.accesscontrol.business.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.GetUsersByPermissionsParameters;
import com.backbase.accesscontrol.service.impl.UserAccessFunctionGroupService;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.UserFunctionGroupsGetResponseBody;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserAccessPAndPServiceTest {

    @Mock
    private UserAccessFunctionGroupService userAccessFunctionGroupService;
    @InjectMocks
    private UserAccessPAndPService userAccessPAndPService;

    @Test
    public void shouldGetUsersByPermissions() {
        String serviceAgreementId = "said1";
        String functionName = "Assign Permissions";
        String privilege = "view";
        String dataGroupType = "CUSTOMERS";
        String dataItemId = "dataitemid";

        GetUsersByPermissionsParameters parameters = new GetUsersByPermissionsParameters(serviceAgreementId,
            functionName, privilege, dataGroupType, dataItemId);
        mockGetUsersByPermissionsReturnTwoUsers(parameters);
        List<UserFunctionGroupsGetResponseBody> responseBodies =
            userAccessPAndPService.getUsersByPermissions(parameters);

        verify(userAccessFunctionGroupService).getUsersFunctionGroups(eq(serviceAgreementId),
            eq(functionName), eq(privilege), eq(dataGroupType), eq(dataItemId));
        assertEquals(2, responseBodies.size());
        assertThat(responseBodies.stream()
            .map(UserFunctionGroupsGetResponseBody::getUserId).collect(
                Collectors.toList()), containsInAnyOrder("USER1", "USER2"));
    }

    public void mockGetUsersByPermissionsReturnTwoUsers(GetUsersByPermissionsParameters parameters){

        Map<String, Set<String>> responseEntity = new HashMap<>();
        responseEntity.put("USER1", new HashSet<>(Arrays.asList("fg1", "fg2")));
        responseEntity.put("USER2", new HashSet<>(Arrays.asList("fg2", "fg3")));
        when(userAccessFunctionGroupService
            .getUsersFunctionGroups(eq(parameters.getServiceAgreementId()),
                eq(parameters.getFunctionName()),
                eq(parameters.getPrivilege()),
                eq(parameters.getDataGroupType()),
                eq(parameters.getDataItemId())))
            .thenReturn(responseEntity);
    }
}
