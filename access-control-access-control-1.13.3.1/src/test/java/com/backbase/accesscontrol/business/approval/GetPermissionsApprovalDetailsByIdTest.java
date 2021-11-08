package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_090;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getVoidInternalRequest;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.dbs.approval.api.client.v2.model.PolicyDetailsDto;
import com.backbase.dbs.approval.api.client.v2.model.PolicyItemDetailsDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.RecordDto;
import com.backbase.dbs.approval.api.client.v2.model.RecordStatus;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsExtendedPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetPermissionsApprovalDetailsByIdTest {

    private final FutureTask mockedFuture = Mockito.mock(FutureTask.class);
    @InjectMocks
    private GetPermissionsApprovalDetailsById getApprovalDetailsById;
    @Mock
    private AccessControlApprovalService accessControlApprovalService;
    @Mock
    private UserManagementService userManagementService;

    @Test
    public void shouldGetApprovalDetailsById() throws ExecutionException, InterruptedException {
        String approvalId = "appId";
        String serviceAgreementId = "saId";
        String userId = "uId";

        PolicyDetailsDto presentationPolicyDto = new PolicyDetailsDto();
        presentationPolicyDto.setItems(singletonList(new PolicyItemDetailsDto()
            .numberOfApprovals(2)));

        PresentationApprovalDetailDto approvalDetails1 = new PresentationApprovalDetailDto()
            .createdAt(new Date())
            .function("ASSIGN PERMISSIONS")
            .userId(userId)
            .userFullName("fullName")
            .serviceAgreementId(serviceAgreementId)
            .records(singletonList(new RecordDto()
                .createdAt(new Date())
                .status(RecordStatus.APPROVED)
                .userId("approverId1")
                .userFullName("username1")))
            .policy(presentationPolicyDto);
        PresentationGetApprovalDetailResponse detailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(approvalDetails1);

        PresentationPermissionsApprovalDetailsItem detailsItem = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription("sa description")
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("saName")
            .withUserId(userId)
            .withModifiedFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsExtendedPair()
                .withDescription("fg desc 1")
                .withId("id1")
                .withName("fn name 1")
                .withNewDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId1")
                    .withDescription("dg desc 1")
                    .withName("dg name 1")))
                .withRemovedDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId2")
                    .withName("dg name 2")
                    .withDescription("dg desc 2")))
                .withUnmodifiedDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId3")
                    .withName("dg name 3")
                    .withDescription("dg desc 3")))))
            .withNewFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsPair()
                .withId("fgId4")
                .withName("fg name 4")
                .withDescription("fg desc 4")
                .withDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId4")
                    .withName("dg name 4")
                    .withDescription("dg desc 4")))))
            .withRemovedFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsPair()
                .withId("fgId5")
                .withName("fg name 5")
                .withDescription("fg desc 5")
                .withDataGroups(new ArrayList<>())))
            .withUnmodifiedFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsPair()
                .withId("fgId6")
                .withName("fg name 6")
                .withDescription("fg desc 6")
                .withDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId6")
                    .withName("dg name 6")
                    .withDescription("dg desc 6")))));
        when(accessControlApprovalService
            .getPersistenceApprovalPermissions(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(detailsItem);

        GetUser user = new GetUser();
        user.setId(userId);
        user.setFullName("fullName");
        when(userManagementService.getUserByInternalId(eq(userId)))
            .thenReturn(user);

        GetUser userBody = new GetUser();
        userBody.setId(userId);
        userBody.setFullName("fullName");
        userBody.setExternalId("u-ex-id");

        when(mockedFuture.get()).thenReturn(approvalDetails1);
        when(accessControlApprovalService
            .getPresentationApprovalDetailDto(
                eq(approvalId),
                eq(serviceAgreementId),
                eq(userId))).thenReturn(mockedFuture);
        PresentationPermissionsApprovalDetailsItem data = getApprovalDetailsById
            .getPermissionsApprovalById(getVoidInternalRequest(), approvalId, serviceAgreementId, userBody.getId())
            .getData();

        verify(accessControlApprovalService, times(1))
            .getPersistenceApprovalPermissions(eq(approvalId), eq(serviceAgreementId), eq(userId));

        assertEquals(detailsItem.getAction().toString(), data.getAction().toString());
        assertEquals(detailsItem.getCategory().toString(), data.getCategory().toString());
        assertEquals(detailsItem.getUserId(), data.getUserId());
        assertEquals(detailsItem.getServiceAgreementDescription(), data.getServiceAgreementDescription());
        assertEquals(detailsItem.getServiceAgreementId(), data.getServiceAgreementId());
        assertEquals(detailsItem.getServiceAgreementName(), data.getServiceAgreementName());
        assertEquals(detailsItem.getModifiedFunctionGroups().size(), data.getModifiedFunctionGroups().size());
        assertEquals(detailsItem.getNewFunctionGroups().size(), data.getNewFunctionGroups().size());
        assertEquals(detailsItem.getRemovedFunctionGroups().size(), data.getRemovedFunctionGroups().size());
        assertEquals(detailsItem.getUnmodifiedFunctionGroups().size(), data.getUnmodifiedFunctionGroups().size());
        assertEquals(detailResponse.getApprovalDetails().getRecords().size(), data.getApprovalLog().size());

    }

    @Test
    public void shouldGetApprovalDetailsByIdWithNullItems() throws ExecutionException, InterruptedException {
        String approvalId = "appId";
        String serviceAgreementId = "saId";
        String userId = "uId";

        PolicyDetailsDto presentationPolicyDto = new PolicyDetailsDto();
        presentationPolicyDto.setItems(null);

        PresentationApprovalDetailDto approvalDetails1 = new PresentationApprovalDetailDto()
            .createdAt(new Date())
            .function("ASSIGN PERMISSIONS")
            .userId(userId)
            .userFullName("fullName")
            .serviceAgreementId(serviceAgreementId)
            .records(singletonList(new RecordDto()
                .createdAt(new Date())
                .status(RecordStatus.APPROVED)
                .userId("approverId1")
                .userFullName("username1")))
            .policy(presentationPolicyDto);
        PresentationGetApprovalDetailResponse detailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(approvalDetails1);

        PresentationPermissionsApprovalDetailsItem detailsItem = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription("sa description")
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("saName")
            .withUserId(userId)
            .withModifiedFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsExtendedPair()
                .withDescription("fg desc 1")
                .withId("id1")
                .withName("fn name 1")
                .withNewDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId1")
                    .withDescription("dg desc 1")
                    .withName("dg name 1")))
                .withRemovedDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId2")
                    .withName("dg name 2")
                    .withDescription("dg desc 2")))
                .withUnmodifiedDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId3")
                    .withName("dg name 3")
                    .withDescription("dg desc 3")))))
            .withNewFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsPair()
                .withId("fgId4")
                .withName("fg name 4")
                .withDescription("fg desc 4")
                .withDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId4")
                    .withName("dg name 4")
                    .withDescription("dg desc 4")))))
            .withRemovedFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsPair()
                .withId("fgId5")
                .withName("fg name 5")
                .withDescription("fg desc 5")
                .withDataGroups(new ArrayList<>())))
            .withUnmodifiedFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsPair()
                .withId("fgId6")
                .withName("fg name 6")
                .withDescription("fg desc 6")
                .withDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId6")
                    .withName("dg name 6")
                    .withDescription("dg desc 6")))));
        when(accessControlApprovalService
            .getPersistenceApprovalPermissions(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(detailsItem);

        GetUser user = new GetUser();
        user.setId(userId);
        user.setFullName("fullName");
        when(userManagementService.getUserByInternalId(eq(userId)))
            .thenReturn(user);

        GetUser userBody = new GetUser();
        userBody.setId(userId);
        userBody.setFullName("fullName");
        userBody.setExternalId("u-ex-id");

        when(mockedFuture.get()).thenReturn(approvalDetails1);
        when(accessControlApprovalService
            .getPresentationApprovalDetailDto(
                eq(approvalId),
                eq(serviceAgreementId),
                eq(userId))).thenReturn(mockedFuture);
        PresentationPermissionsApprovalDetailsItem data = getApprovalDetailsById
            .getPermissionsApprovalById(getVoidInternalRequest(), approvalId, serviceAgreementId, userBody.getId())
            .getData();

        verify(accessControlApprovalService, times(1))
            .getPersistenceApprovalPermissions(eq(approvalId), eq(serviceAgreementId), eq(userId));

        assertEquals(detailsItem.getAction().toString(), data.getAction().toString());
        assertEquals(detailsItem.getCategory().toString(), data.getCategory().toString());
        assertEquals(detailsItem.getUserId(), data.getUserId());
        assertEquals(detailsItem.getServiceAgreementDescription(), data.getServiceAgreementDescription());
        assertEquals(detailsItem.getServiceAgreementId(), data.getServiceAgreementId());
        assertEquals(detailsItem.getServiceAgreementName(), data.getServiceAgreementName());
        assertEquals(detailsItem.getModifiedFunctionGroups().size(), data.getModifiedFunctionGroups().size());
        assertEquals(detailsItem.getNewFunctionGroups().size(), data.getNewFunctionGroups().size());
        assertEquals(detailsItem.getRemovedFunctionGroups().size(), data.getRemovedFunctionGroups().size());
        assertEquals(detailsItem.getUnmodifiedFunctionGroups().size(), data.getUnmodifiedFunctionGroups().size());
        assertEquals(detailResponse.getApprovalDetails().getRecords().size(), data.getApprovalLog().size());

    }

    @Test
    public void shouldThrowForbiddenExceptionWhenServiceAgreementIdsDoNotMatch()
        throws ExecutionException, InterruptedException {

        String approvalId = "appId";
        String serviceAgreementId = "saId";
        String userId = "uId";

        PresentationPermissionsApprovalDetailsItem detailsItem = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription("sa description")
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("saName")
            .withUserId(userId);

        PolicyDetailsDto presentationPolicyDto = new PolicyDetailsDto();
        presentationPolicyDto.setItems(singletonList(new PolicyItemDetailsDto()
            .numberOfApprovals(2)));
        PresentationApprovalDetailDto approvalDetails = new PresentationApprovalDetailDto()
            .createdAt(new Date())
            .function("ASSIGN PERMISSIONS")
            .userId(userId)
            .userFullName("fullName")
            .serviceAgreementId("another_sa_id")
            .records(singletonList(new RecordDto()
                .createdAt(new Date())
                .status(RecordStatus.APPROVED)
                .userId("approverId1")
                .userFullName("username1")))
            .policy(presentationPolicyDto);
        when(mockedFuture.get()).thenReturn(approvalDetails);
        when(accessControlApprovalService
            .getPresentationApprovalDetailDto(
                eq(approvalId),
                eq(serviceAgreementId),
                eq(userId))).thenReturn(mockedFuture);
        when(accessControlApprovalService
            .getPersistenceApprovalPermissions(eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(detailsItem);

        GetUser user = new GetUser();
        user.setId(userId);
        user.setFullName("fullName");

        when(userManagementService.getUserByInternalId(eq(userId))).thenReturn(user);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class, () -> getApprovalDetailsById
            .getPermissionsApprovalById(getVoidInternalRequest(), approvalId, serviceAgreementId, userId));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_090.getErrorMessage(), ERR_AG_090.getErrorCode())));
    }
}