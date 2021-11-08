package com.backbase.accesscontrol.business.flows.useraccess;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.DataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.dto.UserContextDetailsPermissionRequestDto;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetPermissionDataGroupsFlowTest {

    @Mock
    private UserAccessPrivilegeService userAccessPrivilegeService;

    @InjectMocks
    private GetPermissionDataGroupsFlow getPermissionDataGroupsFlow;

    @Test
    public void shouldExecuteFlow() {
        String userId = "userId";
        String serviceAgreementId = "saId";

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(asList("ARRANGEMENTS", "PAYEES"))
            .functionNames(asList("SEPA CT", "Manage Service Agreements"))
            .resourceName("Payments")
            .privileges(asList("view", "edit"));

        PermissionsDataGroup mockResponse = new PermissionsDataGroup()
            .permissionsData(singletonList(new PermissionDataGroup()
                .permissions(asList(new PermissionData()
                        .resourceName("Payments")
                        .functionName("SEPA CT")
                        .privileges(asList("view", "edit")),
                    new PermissionData()
                        .resourceName("Payments")
                        .functionName("Manage Service Agreements")
                        .privileges(asList("view", "edit"))))
                .dataGroups(asList(
                    singletonList(new PermissionDataGroupData()
                        .dataGroupType("ARRANGEMENTS")
                        .dataGroupIds(singletonList("dgId01"))),
                    asList(new PermissionDataGroupData()
                            .dataGroupType("ARRANGEMENTS")
                            .dataGroupIds(singletonList("dgId02")),
                        new PermissionDataGroupData()
                            .dataGroupType("PAYEES")
                            .dataGroupIds(singletonList("dgId03")))))))
            .dataGroupsData(asList(new DataGroupData()
                    .dataGroupId("dgId01")
                    .dataItemIds(asList("item01", "item02")),
                new DataGroupData()
                    .dataGroupId("dgId02")
                    .dataItemIds(asList("item03", "item04")),
                new DataGroupData()
                    .dataGroupId("dgId03")
                    .dataItemIds(asList("item05", "item06"))));

        when(userAccessPrivilegeService
            .getPermissionsDataGroup(eq(userId), eq(serviceAgreementId), eq(permissionsRequest)))
            .thenReturn(mockResponse);

        PermissionsDataGroup response = getPermissionDataGroupsFlow
            .start(new UserContextDetailsPermissionRequestDto(userId, serviceAgreementId, permissionsRequest));

        assertEquals(mockResponse, response);
    }
}