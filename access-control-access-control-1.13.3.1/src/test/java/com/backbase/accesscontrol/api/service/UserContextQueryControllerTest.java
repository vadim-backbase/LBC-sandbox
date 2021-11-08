package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_113;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_115;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.mappers.UserContextPermissionsMapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.UserContextsGetResponseBodyToGetContextMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.facades.UserContextFlowService;
import com.backbase.accesscontrol.service.facades.UserContextServiceFacade;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupData;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemIds;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import com.backbase.accesscontrol.service.rest.spec.model.GetContexts;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionData;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionDataGroup;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionDataGroupData;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserContextQueryControllerTest {

    @InjectMocks
    private UserContextQueryController userContextQueryController;

    @Mock
    private UserContextServiceFacade userContextServiceFacade;

    @Mock
    private UserContextsGetResponseBody userContextsGetResponseBody;

    @Mock
    private UserContextFlowService userContextFlowService;

    @Mock
    private UserContextUtil userContextUtil;

    @Mock
    private ServiceAgreementIdProvider serviceAgreementIdProvider;

    @Mock
    private ValidationConfig validationConfig;

    @Spy
    private final UserContextPermissionsMapper userContextPermissionsMapper = Mappers
        .getMapper(UserContextPermissionsMapper.class);

    @Spy
    private final PayloadConverter payloadConverter =
        new PayloadConverter(singletonList(
            spy(Mappers.getMapper(UserContextsGetResponseBodyToGetContextMapper.class)))
        );


    @Test
    void shouldGetUserContextsByUserId() {
        String userId = "u1";
        String query = "";
        Integer size = 10;
        Integer from = 0;

        when(userContextServiceFacade.getUserContextsByUserId(
            userId, "", 0, 10)).thenReturn(userContextsGetResponseBody);

        GetContexts request = userContextQueryController
            .getUserContexts(userId, null, query, from, size).getBody();
        assertNotNull(request);
        verify(userContextServiceFacade).getUserContextsByUserId(userId, "", 0, 10);
    }

    @Test
    void shouldThrowExceptionOnInvalidUserContext() {
        String userId = "u1";
        String serviceAgreementId = "sa1";

        doNothing().when(userContextServiceFacade).validateUserContext(
            userId, serviceAgreementId);

        userContextQueryController
            .getUserContextValidation(userId, serviceAgreementId);
        verify(userContextServiceFacade).validateUserContext(userId, serviceAgreementId);
    }

    @Test
    void testGetUserContextPermissions() {
        String serviceAgreementId = "saId";
        String userId = "userId";
        String extUserId = "extUserId";

        doNothing().when(validationConfig).validateDataGroupType(anyString());
        when(userContextUtil.getAuthenticatedUserName()).thenReturn(extUserId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "leId"));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(serviceAgreementId));

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(asList("ARRANGEMENTS", "PAYEES"))
            .functionNames(asList("SEPA CT", "Manage Service Agreements"))
            .resourceName("Payments")
            .privileges(asList("view", "edit"));

        com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest flowRequest = new com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest()
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

        com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup flowResponse = new com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup()
            .permissionsData(singletonList(new com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroup()
                .permissions(asList(new com.backbase.accesscontrol.client.rest.spec.model.PermissionData()
                        .resourceName("Payments")
                        .functionName("SEPA CT")
                        .privileges(asList("view", "edit")),
                    new com.backbase.accesscontrol.client.rest.spec.model.PermissionData()
                        .resourceName("Payments")
                        .functionName("Manage Service Agreements")
                        .privileges(asList("view", "edit"))))
                .dataGroups(asList(
                    singletonList(new com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData()
                        .dataGroupType("ARRANGEMENTS")
                        .dataGroupIds(singletonList("dgId01"))),
                    asList(new com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData()
                            .dataGroupType("ARRANGEMENTS")
                            .dataGroupIds(singletonList("dgId02")),
                        new com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData()
                            .dataGroupType("PAYEES")
                            .dataGroupIds(singletonList("dgId03")))))))
            .dataGroupsData(asList(new com.backbase.accesscontrol.client.rest.spec.model.DataGroupData()
                    .dataGroupId("dgId01")
                    .dataItemIds(asList("item01", "item02")),
                new com.backbase.accesscontrol.client.rest.spec.model.DataGroupData()
                    .dataGroupId("dgId02")
                    .dataItemIds(asList("item03", "item04")),
                new com.backbase.accesscontrol.client.rest.spec.model.DataGroupData()
                    .dataGroupId("dgId03")
                    .dataItemIds(asList("item05", "item06"))));

        when(userContextFlowService
            .getUserContextPermissions(userId, serviceAgreementId, flowRequest))
            .thenReturn(flowResponse);

        com.backbase.accesscontrol.service.rest.spec.model.PermissionsDataGroup response = userContextQueryController
            .getUserContextPermissions(permissionsRequest).getBody();

        verify(userContextFlowService)
            .getUserContextPermissions(userId, serviceAgreementId, flowRequest);

        assertEquals(mockResponse, response);
    }

    @Test
    void testFallbackToMSAWhenGetUserContextPermissionsCalled() {
        String serviceAgreementId = "saId";
        String userId = "userId";
        String extUserId = "extUserId";

        doNothing().when(validationConfig).validateDataGroupType(anyString());
        when(userContextUtil.getAuthenticatedUserName()).thenReturn(extUserId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "leId"));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.empty());
        when(serviceAgreementIdProvider
            .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(extUserId))
            .thenReturn(serviceAgreementId);

        when(userContextFlowService
            .getUserContextPermissions(eq(userId), eq(serviceAgreementId), any()))
            .thenReturn(new com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup()
                .permissionsData(emptyList()).dataGroupsData(emptyList()));

        userContextQueryController
            .getUserContextPermissions(new PermissionsRequest().dataGroupTypes(singletonList("ARRANGEMENTS")))
            .getBody();

        verify(userContextFlowService)
            .getUserContextPermissions(eq(userId), eq(serviceAgreementId), any());
    }

    @Test
    void testGetDataItemsPermissions() {
        String userId = "u1";
        String serviceAgreementId = "sa1";
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        dataItemsPermissions.setFunctionName("SEPA");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2));
        when(userContextUtil.getAuthenticatedUserName()).thenReturn("externalUserName");
        when(serviceAgreementIdProvider
            .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext("externalUserName"))
            .thenReturn(serviceAgreementId);
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.empty());
        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto(userId, "le"));
        doNothing().when(userContextServiceFacade).getDataItemsPermissions(anySet(), eq(dataItemsPermissions),
            eq(userId), eq(serviceAgreementId));
        doNothing().when(validationConfig).validateDataGroupType("ARRANGEMENTS");
        doNothing().when(validationConfig).validateDataGroupType("PAYEES");
        userContextQueryController
            .getDataItemsPermissions(dataItemsPermissions);
        verify(userContextServiceFacade).getDataItemsPermissions(anySet(), eq(dataItemsPermissions),
            eq(userId), eq(serviceAgreementId));
    }

    @Test
    void testGetDataItemsPermissionsBadRequestExceptionWhenNullDataItem() {

        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, null));
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            userContextQueryController
                .getDataItemsPermissions(dataItemsPermissions));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_115.getErrorMessage(), ERR_AG_115.getErrorCode()));
    }

    @Test
    void testShouldThrowBadRequestWhenGetDataItemsPermissionsHasNotUniqueItem() {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("PAYEE_ID_1");
        dataItemIds3.setItemType("PAYEES");
        dataItemsPermissions.setFunctionName("SEPA");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));
        doNothing().when(validationConfig).validateDataGroupType("ARRANGEMENTS");
        doNothing().when(validationConfig).validateDataGroupType("PAYEES");
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            userContextQueryController
                .getDataItemsPermissions(dataItemsPermissions));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_113.getErrorMessage(), ERR_AG_113.getErrorCode()));
    }

    @Test
    void testShouldThrowBadRequestWhenGetDataItemsPermissionsHasNotUniqueType() {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("ACCOUNT_ID_1");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("PAYEE_ID_1");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("PAYEE_ID_2");
        dataItemIds3.setItemType("PAYEES");
        dataItemsPermissions.setFunctionName("SEPA");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));
        doNothing().when(validationConfig).validateDataGroupType("ARRANGEMENTS");
        doNothing().when(validationConfig).validateDataGroupType("PAYEES");
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            userContextQueryController
                .getDataItemsPermissions(dataItemsPermissions));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_113.getErrorMessage(), ERR_AG_113.getErrorCode()));
    }
}
