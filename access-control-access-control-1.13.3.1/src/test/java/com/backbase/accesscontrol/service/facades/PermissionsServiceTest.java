package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_111;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_116;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_113;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.persistence.useraccess.AssignUserContextPermissionsApprovalHandler;
import com.backbase.accesscontrol.business.persistence.useraccess.AssignUserContextPermissionsHandler;
import com.backbase.accesscontrol.domain.dto.PersistenceUserContextPermissionsApproval;
import com.backbase.accesscontrol.domain.dto.PersistentUserContextPermissionsPutRequestBody;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.dto.parameterholder.UserIdServiceAgreementIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.UserPermissionsApprovalParameterHolder;
import com.backbase.accesscontrol.mappers.PresentationFunctionDataGroupItemsListAssignUserContextPermissionsMapper;
import com.backbase.accesscontrol.mappers.PresentationFunctionDataGroupItemsPersistenceUserContextPermissionsApprovalMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import java.util.ArrayList;
import java.util.List;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionsServiceTest {

    @InjectMocks
    private PermissionsService permissionsService;
    @Mock
    private AssignUserContextPermissionsHandler assignUserContextPermissionsHandler;
    @Mock
    private AssignUserContextPermissionsApprovalHandler assignUserContextPermissionsApprovalHandler;

    @Spy
    PresentationFunctionDataGroupItemsListAssignUserContextPermissionsMapper userContextPermissionsMapper = Mappers
        .getMapper(PresentationFunctionDataGroupItemsListAssignUserContextPermissionsMapper.class);

    @Spy
    private PresentationFunctionDataGroupItemsPersistenceUserContextPermissionsApprovalMapper userContextPermissionsApprovalMapper = Mappers
        .getMapper(PresentationFunctionDataGroupItemsPersistenceUserContextPermissionsApprovalMapper.class);

    @Test
    public void savePermissions() {
        String userId = "u1";
        String saId = "sa1";
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("u1");
        user1.setLegalEntityId("le");

        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgid = new ArrayList<>();
        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgid.add(genericId);
        PresentationFunctionDataGroup i = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
            .withDataGroupIds(dgid);
        items.add(i);
        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        doNothing().when(assignUserContextPermissionsHandler)
            .handleRequest(any(UserIdServiceAgreementIdParameterHolder.class),
                any(PersistentUserContextPermissionsPutRequestBody.class));

        permissionsService.savePermissions(request, user1, saId, userId);

        UserIdServiceAgreementIdParameterHolder holder = new UserIdServiceAgreementIdParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(saId);
        PersistentUserContextPermissionsPutRequestBody body = new PersistentUserContextPermissionsPutRequestBody();
        body.setUserLegalEntityId(user1.getLegalEntityId());
        UserContextPermissions permissions = new UserContextPermissions();
        permissions.setFunctionGroupId("fg");
        permissions.setDataGroupIds(singleton("dg1"));
        body.setPermissions(singleton(permissions));

        verify(assignUserContextPermissionsHandler).handleRequest(refEq(holder), refEq(body));

    }

    @Test
    public void savePermissionsThrowsExceptionWhenFgWithAndWithoutCombinations() {
        String userId = "u1";
        String saId = "sa1";
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("u1");
        user1.setLegalEntityId("le");

        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(genericId);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
            .withDataGroupIds(dgIds);

        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg");

        items.add(item01);
        items.add(item02);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.savePermissions(request, user1, saId, userId));

        verify(assignUserContextPermissionsHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_111.getErrorMessage(), ERR_AG_111.getErrorCode()));
    }

    @Test
    public void savePermissionsThrowsExceptionWhenFgWithoutAndWithCombinations() {
        String userId = "u1";
        String saId = "sa1";
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("u1");
        user1.setLegalEntityId("le");

        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(genericId);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
            .withDataGroupIds(dgIds);

        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg");

        items.add(item02);
        items.add(item01);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.savePermissions(request, user1, saId, userId));

        verify(assignUserContextPermissionsHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_111.getErrorMessage(), ERR_AG_111.getErrorCode()));
    }

    @Test
    public void savePermissionsThrowsBadRequestWhenDataGroupDuplicatesInSameFunctionGroupObject() {
        String userId = "u1";
        String saId = "sa1";
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("u1");
        user1.setLegalEntityId("le");

        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId dgId1 = new PresentationGenericObjectId().withId("dg1");
        PresentationGenericObjectId dgId2 = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(dgId1);
        dgIds.add(dgId2);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
                .withDataGroupIds(dgIds);

        items.add(item01);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> permissionsService.savePermissions(request, user1, saId, userId));

        verify(assignUserContextPermissionsHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_116.getErrorMessage(), ERR_AG_116.getErrorCode()));
    }

    @Test
    public void savePermissionsThrowsBadRequestWhenDataGroupDuplicatesInDifferentFunctionGroupObjects() {
        String userId = "u1";
        String saId = "sa1";
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("u1");
        user1.setLegalEntityId("le");

        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId dgId1 = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(dgId1);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
                .withDataGroupIds(dgIds);
        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
                .withDataGroupIds(dgIds);

        items.add(item01);
        items.add(item02);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> permissionsService.savePermissions(request, user1, saId, userId));

        verify(assignUserContextPermissionsHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_116.getErrorMessage(), ERR_AG_116.getErrorCode()));
    }

    @Test
    public void savePermissionsThrowsBadRequestWhenSelfApprovalPolicyWithSameBusinessFunctionRepeatedPerCombination() {
        String userId = "u1";
        String saId = "sa1";
        String businessFunction = "Assign Permissions";
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("u1");
        user1.setLegalEntityId("le");

        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        SelfApprovalPolicy selfApprovalPolicy1 = new SelfApprovalPolicy();
        selfApprovalPolicy1.setBusinessFunctionName(businessFunction);
        selfApprovalPolicy1.setCanSelfApprove(true);

        SelfApprovalPolicy selfApprovalPolicy2 = new SelfApprovalPolicy();
        selfApprovalPolicy2.setBusinessFunctionName(businessFunction);
        selfApprovalPolicy2.setCanSelfApprove(true);

        PresentationGenericObjectId dgId1 = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(dgId1);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg1")
                .withDataGroupIds(dgIds).withSelfApprovalPolicies(List.of(selfApprovalPolicy1, selfApprovalPolicy2));
        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg2")
                .withDataGroupIds(dgIds);

        items.add(item01);
        items.add(item02);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> permissionsService.savePermissions(request, user1, saId, userId));

        verify(assignUserContextPermissionsHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(String.format(ERR_ACC_113.getErrorMessage(), businessFunction),
                ERR_ACC_113.getErrorCode()));
    }

    @Test
    public void savePermissionsToApproval() {

        String userId = "u1";
        String saId = "sa1";
        String legalEntityId = "le";
        String approvalId = "appId";
        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgid = new ArrayList<>();
        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgid.add(genericId);
        PresentationFunctionDataGroup i = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
            .withDataGroupIds(dgid);
        items.add(i);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        doNothing().when(assignUserContextPermissionsApprovalHandler)
            .handleRequest(any(UserPermissionsApprovalParameterHolder.class),
                any(PersistenceUserContextPermissionsApproval.class));

        permissionsService.savePermissionsToApproval(request, saId, userId, legalEntityId, approvalId);

        UserPermissionsApprovalParameterHolder holder = new UserPermissionsApprovalParameterHolder()
            .withApprovalId(approvalId)
            .withLegalEntityId(legalEntityId)
            .withUserId(userId)
            .withServiceAgreementId(saId);

        PersistenceUserContextPermissionsApproval body = new PersistenceUserContextPermissionsApproval();
        UserContextPermissions permissions = new UserContextPermissions();
        permissions.setFunctionGroupId("fg");
        permissions.setDataGroupIds(singleton("dg1"));
        body.setPermissions(singleton(permissions));

        verify(assignUserContextPermissionsApprovalHandler).handleRequest(refEq(holder), refEq(body));
    }

    @Test
    public void savePermissionsToApprovalThrowsExceptionWhenFgWithAndWithoutCombinations() {

        String userId = "u1";
        String saId = "sa1";
        String legalEntityId = "le";
        String approvalId = "appId";
        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(genericId);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
            .withDataGroupIds(dgIds);

        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg");

        items.add(item01);
        items.add(item02);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.savePermissionsToApproval(request, saId, userId, legalEntityId, approvalId));

        verify(assignUserContextPermissionsApprovalHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_111.getErrorMessage(), ERR_AG_111.getErrorCode()));
    }

    @Test
    public void savePermissionsToApprovalThrowsExceptionWhenFgWithoutAndWithCombinations() {

        String userId = "u1";
        String saId = "sa1";
        String legalEntityId = "le";
        String approvalId = "appId";
        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(genericId);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg")
            .withDataGroupIds(dgIds);

        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg");

        items.add(item02);
        items.add(item01);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> permissionsService.savePermissionsToApproval(request, saId, userId, legalEntityId, approvalId));

        verify(assignUserContextPermissionsApprovalHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_111.getErrorMessage(), ERR_AG_111.getErrorCode()));
    }

    @Test
    public void savePermissionsToApprovalThrowsBadRequestWhenDataGroupDuplicatesInSameFunctionGroupObject() {
        String userId = "u1";
        String saId = "sa1";
        String legalEntityId = "le";
        String approvalId = "appId";
        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId dgId1 = new PresentationGenericObjectId().withId("dg1");
        PresentationGenericObjectId dgId2 = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(dgId1);
        dgIds.add(dgId2);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg1")
                .withDataGroupIds(dgIds);

        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg2");

        items.add(item01);
        items.add(item02);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> permissionsService.savePermissionsToApproval(request, saId, userId, legalEntityId, approvalId));

        verify(assignUserContextPermissionsApprovalHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_116.getErrorMessage(), ERR_AG_116.getErrorCode()));
    }

    @Test
    public void savePermissionsToApprovalThrowsBadRequestWhenDataGroupDuplicatesInDifferentFunctionGroupObjects() {
        String userId = "u1";
        String saId = "sa1";
        String legalEntityId = "le";
        String approvalId = "appId";
        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(genericId);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg1")
                .withDataGroupIds(dgIds);

        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg1")
                .withDataGroupIds(dgIds);

        items.add(item01);
        items.add(item02);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> permissionsService.savePermissionsToApproval(request, saId, userId, legalEntityId, approvalId));

        verify(assignUserContextPermissionsApprovalHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_116.getErrorMessage(), ERR_AG_116.getErrorCode()));
    }

    @Test
    public void savePermissionsToApprovalThrowsBadRequestWhenSelfApprovalPolicyWithSameBusinessFunctionRepeatedPerCombination() {
        String userId = "u1";
        String saId = "sa1";
        String legalEntityId = "le";
        String approvalId = "appId";
        String businessFunction = "Assign Permissions";
        List<PresentationFunctionDataGroup> items = new ArrayList<>();
        List<PresentationGenericObjectId> dgIds = new ArrayList<>();

        SelfApprovalPolicy selfApprovalPolicy1 = new SelfApprovalPolicy();
        selfApprovalPolicy1.setBusinessFunctionName(businessFunction);
        selfApprovalPolicy1.setCanSelfApprove(true);

        SelfApprovalPolicy selfApprovalPolicy2 = new SelfApprovalPolicy();
        selfApprovalPolicy2.setBusinessFunctionName(businessFunction);
        selfApprovalPolicy2.setCanSelfApprove(true);

        PresentationGenericObjectId genericId = new PresentationGenericObjectId().withId("dg1");
        dgIds.add(genericId);
        PresentationFunctionDataGroup item01 = new PresentationFunctionDataGroup().withFunctionGroupId("fg1")
                .withDataGroupIds(dgIds).withSelfApprovalPolicies(List.of(selfApprovalPolicy1, selfApprovalPolicy2));

        PresentationFunctionDataGroup item02 = new PresentationFunctionDataGroup().withFunctionGroupId("fg2");

        items.add(item01);
        items.add(item02);

        PresentationFunctionDataGroupItems request = new PresentationFunctionDataGroupItems().withItems(items);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> permissionsService.savePermissionsToApproval(request, saId, userId, legalEntityId, approvalId));

        verify(assignUserContextPermissionsApprovalHandler, times(0)).handleRequest(any(), any());

        assertThat(exception, new BadRequestErrorMatcher(String.format(ERR_ACC_113.getErrorMessage(), businessFunction),
                ERR_ACC_113.getErrorCode()));
    }
}
