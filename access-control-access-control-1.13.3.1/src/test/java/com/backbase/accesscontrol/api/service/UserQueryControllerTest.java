package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_017;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_065;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.accesscontrol.mappers.ArrangementPrivilegesMapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.ArrangementPrivilegesGetResponseBodyQueryConverter;
import com.backbase.accesscontrol.mappers.model.query.service.ArrangementPrivilegesGetResponseBodyToArrangementPrivilegeItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ContextLegalEntitiesToContextLegalEntitiesMapper;
import com.backbase.accesscontrol.mappers.model.query.service.PersistenceApprovalPermissionsToPersistenceApprovalPermissionsMapper;
import com.backbase.accesscontrol.mappers.model.query.service.PersistenceUserDataItemPermissionToPersistenceUserDataItemPermissionMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserAccessEntitlementsResourceToEntitlementsResourceMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserAccessLegalEntitiesToLegalEntityResourceMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserAccessServiceAgreementToEntitlementsResourceMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserFunctionGroupsGetResponseBodyToUserFunctionGroupsMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.accesscontrol.service.impl.UserAccessFunctionGroupService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ContextLegalEntities;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.LegalEntityResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceDataItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ServiceAgreementResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.UserFunctionGroupsGetResponseBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserQueryControllerTest {

    @Mock
    private UserAccessPrivilegeService userAccessPrivilegeService;
    @Mock
    private UserAccessPermissionCheckService userAccessPermissionCheckService;
    @Mock
    private ApprovalService approvalService;
    @Mock
    private UserAccessFunctionGroupService userAccessFunctionGroupService;
    @InjectMocks
    private UserQueryController userQueryController;
    @Spy
    private ArrangementPrivilegesMapper arrangementPrivilegesMapper = Mappers
        .getMapper(ArrangementPrivilegesMapper.class);
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(ArrangementPrivilegesGetResponseBodyToArrangementPrivilegeItemMapper.class)),
            spy(Mappers.getMapper(ArrangementPrivilegesGetResponseBodyQueryConverter.class)),
            spy(Mappers.getMapper(PersistenceUserDataItemPermissionToPersistenceUserDataItemPermissionMapper.class)),
            spy(Mappers.getMapper(UserAccessEntitlementsResourceToEntitlementsResourceMapper.class)),
            spy(Mappers.getMapper(UserAccessServiceAgreementToEntitlementsResourceMapper.class)),
            spy(Mappers.getMapper(UserAccessLegalEntitiesToLegalEntityResourceMapper.class)),
            spy(Mappers.getMapper(PersistenceApprovalPermissionsToPersistenceApprovalPermissionsMapper.class)),
            spy(Mappers.getMapper(ContextLegalEntitiesToContextLegalEntitiesMapper.class)),
            spy(Mappers.getMapper(UserFunctionGroupsGetResponseBodyToUserFunctionGroupsMapper.class)))
        );

    @Test
    public void shouldInvokeGetArrangementPrivilegesAndCopyHeaders() {
        String resourceName = "resourceName";
        String functionName = "functionName";
        String userId = "100004";
        String serviceAgreementId = "sa-001";

        ArrangementPrivilegesDto arrangementPrivilegesDto = new ArrangementPrivilegesDto()
            .withArrangementId("arrangementId")
            .withPrivileges(asList(new Privilege()
                .withPrivilege("execute")));

        when(userAccessPrivilegeService
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName), eq(""),
                eq(null), eq("")))
            .thenReturn(Collections.singletonList(arrangementPrivilegesDto));

        List<ArrangementPrivilegesGetResponseBody> arrangementPrivileges =
            userQueryController
                .getArrangementPrivilegesQuery(functionName,
                    resourceName, userId, serviceAgreementId, null, "", "").getBody();

        assertEquals(1, arrangementPrivileges.size());
        assertEquals(arrangementPrivilegesDto.getPrivileges().get(0).getPrivilege(),
            arrangementPrivileges.get(0).getPrivileges().get(0).getPrivilege());
    }

    @Test
    public void shouldReturnListOfUsersFunctionGroups() {
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String privilege = "execute";
        String dataGroupType = "CONTACTS";
        String dataItemId = "itemId";

        Map<String, Set<String>> userFunctionGroupMap = new HashMap<>();
        userFunctionGroupMap.put("userId1", Sets.newHashSet("funcId1", "funcId2"));
        userFunctionGroupMap.put("userId2", Sets.newHashSet("funcId3"));

        UserFunctionGroupsGetResponseBody userFunctionGroupsGetResponseBody1 = new UserFunctionGroupsGetResponseBody()
            .withUserId("userId1").withFunctionGroupIds(Sets.newHashSet("funcId1", "funcId2"));
        UserFunctionGroupsGetResponseBody userFunctionGroupsGetResponseBody2 = new UserFunctionGroupsGetResponseBody()
            .withUserId("userId2").withFunctionGroupIds(Sets.newHashSet("funcId3"));

        List<UserFunctionGroupsGetResponseBody> userFunctionGroupsGetResponseBodyList = asList(
            userFunctionGroupsGetResponseBody1, userFunctionGroupsGetResponseBody2);

        when(userAccessFunctionGroupService
            .getUsersFunctionGroups(eq(serviceAgreementId), eq(functionName), eq(privilege), eq(dataGroupType),
                eq(dataItemId))).thenReturn(userFunctionGroupMap);

        List<com.backbase.accesscontrol.service.rest.spec.model.UserFunctionGroups> usersFunctionGroups = userQueryController
            .getUsersFunctionGroups(serviceAgreementId, functionName, privilege, dataGroupType, dataItemId).getBody();

        verify(userAccessFunctionGroupService)
            .getUsersFunctionGroups(eq(serviceAgreementId), eq(functionName), eq(privilege), eq(dataGroupType),
                eq(dataItemId));
        assertEquals(userFunctionGroupsGetResponseBodyList.size(), usersFunctionGroups.size());
    }

    @Test
    public void shouldReturnListOfUsersFunctionGroupsWhenDataGroupTypeAndItemIdNotProvided() {
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String privilege = "execute";
        String dataGroupType = null;
        String dataItemId = null;

        Map<String, Set<String>> userFunctionGroupMap = new HashMap<>();
        userFunctionGroupMap.put("userId1", Sets.newHashSet("funcId1", "funcId2"));
        userFunctionGroupMap.put("userId2", Sets.newHashSet("funcId3"));

        UserFunctionGroupsGetResponseBody userFunctionGroupsGetResponseBody1 = new UserFunctionGroupsGetResponseBody()
            .withUserId("userId1").withFunctionGroupIds(Sets.newHashSet("funcId1", "funcId2"));
        UserFunctionGroupsGetResponseBody userFunctionGroupsGetResponseBody2 = new UserFunctionGroupsGetResponseBody()
            .withUserId("userId2").withFunctionGroupIds(Sets.newHashSet("funcId3"));

        List<UserFunctionGroupsGetResponseBody> userFunctionGroupsGetResponseBodyList = asList(
            userFunctionGroupsGetResponseBody1, userFunctionGroupsGetResponseBody2);

        when(userAccessFunctionGroupService
            .getUsersFunctionGroups(eq(serviceAgreementId), eq(functionName), eq(privilege), eq(dataGroupType),
                eq(dataItemId))).thenReturn(userFunctionGroupMap);

        List<com.backbase.accesscontrol.service.rest.spec.model.UserFunctionGroups> usersFunctionGroups = userQueryController
            .getUsersFunctionGroups(serviceAgreementId, functionName, privilege, dataGroupType, dataItemId).getBody();

        verify(userAccessFunctionGroupService)
            .getUsersFunctionGroups(eq(serviceAgreementId), eq(functionName), eq(privilege), eq(dataGroupType),
                eq(dataItemId));
        assertEquals(userFunctionGroupsGetResponseBodyList.size(), usersFunctionGroups.size());
    }

    @Test
    public void shouldThrowBadRequestWhenSendingDataItemTypeWithoutDataItemId() {
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String privilege = "execute";
        String dataGroupType = "CONTACTS";
        String dataItemId = null;

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userQueryController
            .getUsersFunctionGroups(serviceAgreementId, functionName, privilege, dataGroupType, dataItemId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_065.getErrorMessage(), ERR_ACQ_065.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenSendingDataItemIdWithoutDataItemType() {
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String privilege = "execute";
        String dataGroupType = null;
        String dataItemId = "itemId";

        BadRequestException exception = assertThrows(BadRequestException.class, () -> userQueryController
            .getUsersFunctionGroups(serviceAgreementId, functionName, privilege, dataGroupType, dataItemId));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_065.getErrorMessage(), ERR_ACQ_065.getErrorCode()));
    }

    @Test
    @Deprecated
    public void shouldReturnListOfDataItemPrivileges() {
        String serviceAgreementId = "id.sa";
        String userId = "id.user";
        String resource = "Resource";
        String function = "Function";
        String functionId = "1020";
        String functionCode = "Function code";
        String privileges = "view";
        String dataType = "CONTACTS";
        String itemId = "itemId";
        List<PersistenceUserDataItemPermission> mockResponse =
            asList(
                new PersistenceUserDataItemPermission()
                    .withDataItem(new PersistenceDataItem()
                        .withDataType(dataType)
                        .withId(itemId))
                    .withPermissions(asList(new PersistenceUserPermission()
                        .withBusinessFunction(function)
                        .withFunctionId(functionId)
                        .withFunctionCode(functionCode)
                        .withResource(resource)
                        .withPrivileges(asList(privileges)))));

        when(userAccessPrivilegeService
            .getUserDataItemsPrivileges(userId, serviceAgreementId, resource, function, privileges, dataType, itemId))
            .thenReturn(mockResponse);

        List<com.backbase.accesscontrol.service.rest.spec.model.PersistenceUserDataItemPermission> responseFromListener =
            userQueryController.getDataItemPermissions(userId, serviceAgreementId,
                function, resource, privileges, dataType, itemId).getBody();

        assertEquals(responseFromListener.get(0).getDataItem().getId(), mockResponse.get(0).getDataItem().getId());
    }

    @Test
    public void shouldCallCheckUserAccessToEntitlementsResource() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String legalEntityResource = "LE-02";
        com.backbase.accesscontrol.service.rest.spec.model.UserAccessEntitlementsResource data =
            new com.backbase.accesscontrol.service.rest.spec.model.UserAccessEntitlementsResource();
        data.setAccessResourceType(com.backbase.accesscontrol.service.rest.spec.model.AccessResourceType.USER);
        data.setUserLegalEntityId(userLegalEntity);
        data.setContextServiceAgreementId(contextServiceAgreementId);
        data.setLegalEntityIds(asList(legalEntityResource));

        EntitlementsResource dataTransform = new EntitlementsResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withAccessResourceType(AccessResourceType.USER)
            .withLegalEntityIds(asList(legalEntityResource));

        ContextLegalEntities response = new ContextLegalEntities()
            .withLegalEntities(Lists.newArrayList(legalEntityResource));

        when(userAccessPermissionCheckService.checkUserAccessToEntitlementsResources(dataTransform))
            .thenReturn(response);

        com.backbase.accesscontrol.service.rest.spec.model.ContextLegalEntities contextLegalEntities = userQueryController
            .postUserAccessToEntitlementsResource(data).getBody();

        verify(userAccessPermissionCheckService).checkUserAccessToEntitlementsResources(dataTransform);
        assertEquals(response.getLegalEntities(), contextLegalEntities.getLegalEntities());
    }

    @Test
    public void shouldCallCheckUserAccessToServiceAgreement() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String serviceAgreement = "SA-02";

        ServiceAgreementResource data = new ServiceAgreementResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withAccessResourceType(AccessResourceType.USER)
            .withServiceAgreementId(serviceAgreement);

        com.backbase.accesscontrol.service.rest.spec.model.UserAccessServiceAgreement userAccessServiceAgreement =
            new com.backbase.accesscontrol.service.rest.spec.model.UserAccessServiceAgreement();
        userAccessServiceAgreement.setUserLegalEntityId(userLegalEntity);
        userAccessServiceAgreement.setContextServiceAgreementId(contextServiceAgreementId);
        userAccessServiceAgreement.setServiceAgreementId(serviceAgreement);
        userAccessServiceAgreement.setAccessResourceType(
            com.backbase.accesscontrol.service.rest.spec.model.AccessResourceType.USER);

        doNothing().when(userAccessPermissionCheckService).checkUserAccessToServiceAgreement(data);

        userQueryController
            .postUserAccessToServiceAgreement(userAccessServiceAgreement);

        verify(userAccessPermissionCheckService).checkUserAccessToServiceAgreement(data);
    }

    @Test
    public void shouldCallGetLegalEntitiesThatUserHasAccessTo() {
        String userLegalEntity = "LE-01";
        String contextServiceAgreementId = "SA-01";
        LegalEntityResource data = new LegalEntityResource()
            .withUserLegalEntityId(userLegalEntity)
            .withContextServiceAgreementId(contextServiceAgreementId)
            .withAccessResourceType(AccessResourceType.USER);
        com.backbase.accesscontrol.service.rest.spec.model.UserAccessLegalEntities userAccessLegalEntities =
            new com.backbase.accesscontrol.service.rest.spec.model.UserAccessLegalEntities();
        userAccessLegalEntities.setUserLegalEntityId(userLegalEntity);
        userAccessLegalEntities.setContextServiceAgreementId(contextServiceAgreementId);
        userAccessLegalEntities.setAccessResourceType(
            com.backbase.accesscontrol.service.rest.spec.model.AccessResourceType.USER);

        ContextLegalEntities response = new ContextLegalEntities()
            .withLegalEntities(asList("LE-01"));

        when(userAccessPermissionCheckService.getLegalEntitiesThatUserHasAccessTo(data))
            .thenReturn(response);

        com.backbase.accesscontrol.service.rest.spec.model.ContextLegalEntities contextLegalEntities = userQueryController
            .postLegalEntitiesInContext(userAccessLegalEntities).getBody();

        verify(userAccessPermissionCheckService).getLegalEntitiesThatUserHasAccessTo(data);
        assertEquals(response.getLegalEntities(), contextLegalEntities.getLegalEntities());
    }

    @Test
    public void getPersistenceApprovalPermissionsTest() {

        String userId = "userId";
        String serviceAgreementId = "serviceAgreementId";
        String approvalId = "approvalId";

        PersistenceApprovalPermissions persistenceApprovalPermissions
            = new PersistenceApprovalPermissions()
            .withApprovalId(approvalId);

        when(approvalService.getPersistenceApprovalPermissions(eq(userId), eq(serviceAgreementId)))
            .thenReturn(persistenceApprovalPermissions);

        com.backbase.accesscontrol.service.rest.spec.model.PersistenceApprovalPermissions approvalPermissions =
            userQueryController
                .getPersistenceApprovalPermissions(userId, serviceAgreementId).getBody();

        Assert.assertNotNull(approvalPermissions);
        Assert.assertEquals(approvalId, approvalPermissions.getApprovalId());
        verify(approvalService).getPersistenceApprovalPermissions(userId, serviceAgreementId);
    }

    @Test
    public void getUserPermissionCheck() {

        String userId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String resourceName = "Entitlements";
        String privileges = "execute,read";

        userQueryController
            .getUserPermissionCheckQuery(userId, resourceName, functionName,
                privileges, serviceAgreementId);

        verify(userAccessPermissionCheckService).checkUserPermission(
            eq(userId),
            eq(serviceAgreementId),
            eq(functionName),
            eq(resourceName),
            eq(privileges));
    }

    @Test
    public void shouldThrowForbiddenExceptionOnGetUserPermissionCheck() {

        String userId = "userId";
        String serviceAgreementId = "saId";
        String functionName = "Entitlements";
        String resourceName = "Entitlements";
        String privileges = "execute,read";

        doThrow(getForbiddenException(ERR_ACQ_017.getErrorMessage(), ERR_ACQ_017.getErrorCode()))
            .when(userAccessPermissionCheckService)
            .checkUserPermission(eq(userId),
                eq(serviceAgreementId),
                eq(functionName),
                eq(resourceName),
                eq(privileges));

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> userQueryController
            .getUserPermissionCheckQuery(userId, resourceName, functionName,
                privileges, serviceAgreementId));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_ACQ_017.getErrorMessage(), ERR_ACQ_017.getErrorCode()));
    }
}
