package com.backbase.accesscontrol.service.facades;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.flows.useraccess.GetPermissionDataGroupsFlow;
import com.backbase.accesscontrol.client.rest.spec.model.DataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.dto.UserContextDetailsPermissionRequestDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserContextFlowServiceTest {

    @Mock
    private GetPermissionDataGroupsFlow getPermissionDataGroupsFlow;

    @InjectMocks
    private UserContextFlowService userContextFlowService;

    @Test
    public void testGetUserContextPermissions() {
        String userId = "userId";
        String serviceAgreementId = "saId";

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(asList("ARRANGEMENTS", "PAYEES"))
            .functionNames(asList("SEPA CT", "Manage Service Agreements"))
            .resourceName("Payments")
            .privileges(asList("view", "edit"));

        PermissionsDataGroup mockResponse = new PermissionsDataGroup()
            .permissionsData(singletonList(new com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroup()
                .permissions(asList(new com.backbase.accesscontrol.client.rest.spec.model.PermissionData()
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

        when(getPermissionDataGroupsFlow.start(any(UserContextDetailsPermissionRequestDto.class)))
            .thenReturn(mockResponse);

        PermissionsDataGroup response = userContextFlowService
            .getUserContextPermissions(userId, serviceAgreementId, permissionsRequest);

        ArgumentCaptor<UserContextDetailsPermissionRequestDto> captor = ArgumentCaptor
            .forClass(UserContextDetailsPermissionRequestDto.class);

        verify(getPermissionDataGroupsFlow).start(captor.capture());

        UserContextDetailsPermissionRequestDto requestDto = captor.getValue();

        assertEquals(userId, requestDto.getUserId());
        assertEquals(serviceAgreementId, requestDto.getServiceAgreementId());
        assertEquals(permissionsRequest, requestDto.getPermissionsRequest());

        assertEquals(mockResponse, response);
    }
}