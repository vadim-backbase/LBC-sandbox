package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.business.datagroup.AddDataGroup.CREATE;
import static com.backbase.accesscontrol.business.datagroup.AddDataGroup.ENTITLEMENTS;
import static com.backbase.accesscontrol.business.datagroup.AddDataGroup.MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.matchers.MatcherUtil.hasPayload;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import com.backbase.accesscontrol.business.service.ApprovalService;
import com.backbase.accesscontrol.dto.ApprovalsListDto;
import com.backbase.accesscontrol.dto.parameterholder.ApprovalsParametersHolder;
import com.backbase.accesscontrol.routes.approval.AcceptApprovalRouteProxy;
import com.backbase.accesscontrol.routes.approval.GetPermissionsApprovalDetailsByIdRouteProxy;
import com.backbase.accesscontrol.routes.approval.ListPendingApprovalsRouteProxy;
import com.backbase.accesscontrol.routes.approval.RejectApprovalRequestRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.PutUpdateStatusRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsExtendedPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApprovalsServiceTest {

    @Mock
    private ListPendingApprovalsRouteProxy listPendingApprovalsRouteProxy;
    @Mock
    private GetPermissionsApprovalDetailsByIdRouteProxy getPermissionsApprovalDetailsByIdRouteProxy;
    @Mock
    private RejectApprovalRequestRouteProxy rejectApprovalRequestRouteProxy;
    @Mock
    private AcceptApprovalRouteProxy acceptApprovalRouteProxy;
    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private ApprovalsService approvalsService;

    @Test
    public void shouldReturnPendingApprovals() {
        String userId = "U-01";
        String legalEntityId = "LE-01";
        String serviceAgreementId = "SA-01";
        String cursor = "";
        ApprovalsParametersHolder parametersHolder = new ApprovalsParametersHolder()
            .withUserId(userId)
            .withLegalEntityId(legalEntityId)
            .withCursor(cursor)
            .withServiceAgreementId(serviceAgreementId);
        ApprovalsListDto mockResponseData = new ApprovalsListDto()
            .withPresentationApprovalItems(Lists.newArrayList(
                new PresentationApprovalItem()
                    .withApprovalId("AP-01"),
                new PresentationApprovalItem()
                    .withApprovalId("AP-02")))
            .withCursor(cursor);

        when(listPendingApprovalsRouteProxy.listApprovals(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(mockResponseData, new DefaultInternalRequestContext()));

        ApprovalsListDto response = approvalsService.listPendingApprovals(parametersHolder);

        verify(listPendingApprovalsRouteProxy).listApprovals(argThat(hasPayload(parametersHolder)));
        assertEquals(mockResponseData, response);
    }

    @Test
    public void shouldGetPermissionsApprovalById() {
        String approvalId = "app-id";
        String serviceAgreementId = "sa-id";

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("u-id");
        user1.setFullName("fullName");

        PresentationPermissionsApprovalDetailsItem mockResponse = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withCreatedAt(new Date())
            .withCreatorUserFullName("creator full name")
            .withCreatorUserId("creatorId")
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("sa-name")
            .withServiceAgreementDescription("sa-desc")
            .withUserFullName(user1.getFullName())
            .withUserId(user1.getId())
            .withModifiedFunctionGroups(Collections.singletonList(new PresentationFunctionGroupsDataGroupsExtendedPair()
                .withDescription("fg desc")
                .withId("fg-id")
                .withName("fg name")
                .withNewDataGroups(Collections.singletonList(new PresentationDataGroupApprovalItem()
                    .withDescription("dg desc")
                    .withId("dg id")
                    .withName("dg name")))));
        when(getPermissionsApprovalDetailsByIdRouteProxy
            .getPermissionsApprovalById(any(InternalRequest.class), eq(approvalId), eq(serviceAgreementId),
                eq(user1.getId())))
            .thenReturn(getInternalRequest(mockResponse));

        PresentationPermissionsApprovalDetailsItem approvalById = approvalsService
            .getPermissionsApprovalDetailsById(approvalId, serviceAgreementId, user1.getId());

        verify(getPermissionsApprovalDetailsByIdRouteProxy, times(1))
            .getPermissionsApprovalById(any(InternalRequest.class), eq(approvalId), eq(serviceAgreementId),
                eq(user1.getId()));

        assertEquals(mockResponse, approvalById);

    }

    @Test
    public void testRejectApprovalRequest() {
        PresentationApprovalStatus approvalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.REJECTED);
        InternalRequest<PresentationApprovalStatus> expectedResponse = getInternalRequest(approvalStatus);
        String approvalId = "appId";
        String serviceAgreementId = "saId";
        String userId = "userId";
        when(rejectApprovalRequestRouteProxy
            .rejectApprovalRequest(eq(approvalId), any(InternalRequest.class), eq(serviceAgreementId), eq(userId)))
            .thenReturn(expectedResponse);
        PresentationApprovalStatus actualResponse = approvalsService
            .rejectApprovalRequest(approvalId, serviceAgreementId, userId);
        assertEquals(expectedResponse.getData(), actualResponse);
    }

    @Test
    public void testApproveApprovalRequest() {
        PresentationApprovalStatus approvalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.APPROVED);
        InternalRequest<PresentationApprovalStatus> expectedResponse = getInternalRequest(approvalStatus);
        String approvalId = "appId";
        String serviceAgreementId = "saId";
        String userId = "userId";
        when(acceptApprovalRouteProxy
            .acceptApprovalRequest(any(InternalRequest.class), eq(approvalId), eq(serviceAgreementId), eq(userId)))
            .thenReturn(expectedResponse);
        PresentationApprovalStatus actualResponse = approvalsService
            .acceptApprovalRequest(approvalId, serviceAgreementId, userId);
        assertEquals(expectedResponse.getData(), actualResponse);
    }

    @Test
    public void testGetApprovalResponse() {
        ApprovalDto approved = new ApprovalDto()
            .serviceAgreementId("saId")
            .id("approvalId");
        PresentationPostApprovalResponse response = new PresentationPostApprovalResponse()
            .approval(approved);

        when(approvalService.postApprovals(any(PresentationPostApprovalRequest.class))).thenReturn(response);

        PresentationPostApprovalResponse approvalResponse = approvalsService
            .getApprovalResponse("id", "saId", ENTITLEMENTS, MANAGE_DATA_GROUPS, CREATE);

        assertEquals("approvalId", requireNonNull(approvalResponse).getApproval().getId());

    }

    @Test
    public void testCancelApprovalRequest() {
        doNothing().when(approvalService).putSetStatusById(eq("approvalId"), any(PutUpdateStatusRequest.class));

        approvalsService.cancelApprovalRequest("approvalId");

        ArgumentCaptor<PutUpdateStatusRequest> captor = ArgumentCaptor.forClass(PutUpdateStatusRequest.class);

        verify(approvalService).putSetStatusById(eq("approvalId"), captor.capture());

        assertEquals(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.CANCELLED,
            captor.getValue().getStatus());
    }
}
