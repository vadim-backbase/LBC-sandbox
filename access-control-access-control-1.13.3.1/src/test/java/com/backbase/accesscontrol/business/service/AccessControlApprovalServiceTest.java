package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_090;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalLevel;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.accesscontrol.service.ServiceAgreementQueryService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PostApprovalRecordRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsExtendedPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlApprovalServiceTest {

    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private FunctionGroupService functionGroupService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private ApprovalService approvalService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Mock
    private UserAccessPermissionCheckService userAccessPermissionCheckService;
    @Mock
    private ServiceAgreementQueryService serviceAgreementService;

    @InjectMocks
    private AccessControlApprovalService accessControlApprovalService;

    @Test
    public void shouldCallGGetUserPermissionsOnGetPersistenceApprovalPermissions() {

        String approvalId = "approvalId";
        String serviceAgreementId = "saId";
        String userId = "uId";

        PresentationPermissionsApprovalDetailsItem data = new PresentationPermissionsApprovalDetailsItem()
            .withAction(PresentationApprovalAction.EDIT)
            .withCategory(PresentationApprovalCategory.ASSIGN_PERMISSIONS)
            .withServiceAgreementDescription("sa desc")
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("name")
            .withUserId(userId)
            .withModifiedFunctionGroups(singletonList(new PresentationFunctionGroupsDataGroupsExtendedPair()
                .withId("fgId")
                .withName("fg name")
                .withDescription("fg desc")
                .withNewDataGroups(singletonList(new PresentationDataGroupApprovalItem()
                    .withId("dgId")
                    .withName("dg name")
                    .withDescription("dg desc")))));

        PresentationGetApprovalDetailResponse approvalById = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId(serviceAgreementId)
                .userId(userId)
                .function("Manage Data Groups"));
        when(approvalService.getApprovalDetailById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(false)))
            .thenReturn(approvalById);
        when(permissionService.getUserPermissionApprovalDetails(eq(approvalId)))
            .thenReturn(data);

        PresentationPermissionsApprovalDetailsItem responseFromPandp = accessControlApprovalService
            .getPersistenceApprovalPermissions(approvalId, serviceAgreementId, userId);

        assertEquals(data.getAction(), responseFromPandp.getAction());
        assertEquals(data.getCategory(), responseFromPandp.getCategory());
        assertEquals(data.getUserId(), responseFromPandp.getUserId());
        assertEquals(data.getModifiedFunctionGroups(), responseFromPandp.getModifiedFunctionGroups());
        assertEquals(data.getServiceAgreementId(), responseFromPandp.getServiceAgreementId());
        assertEquals(data.getServiceAgreementName(), responseFromPandp.getServiceAgreementName());
    }

    @Test
    public void shouldGetApprovalTypeIdFromApprovals() {
        String functionGroupId = "fgId";
        String newApprovalType = "appTypeId";
        mockApprovalLevel(applicationProperties, true);
        when(approvalService.getApprovalTypeAssignment(eq(functionGroupId)))
            .thenReturn(newApprovalType);
        String approvalTypeIdFromApprovals = accessControlApprovalService
            .getApprovalTypeIdFromApprovals(functionGroupId);
        assertEquals(newApprovalType, approvalTypeIdFromApprovals);
    }

    @Test
    public void shouldVerifyThatUpdateApprovalTypeIsCalled() {
        String functionGroupId = "fgId";
        String newApprovalType = "appTypeId";
        mockApprovalLevel(applicationProperties, true);

        accessControlApprovalService.updateApprovalType(functionGroupId, newApprovalType);

        verify(approvalService).putApprovalTypeAssignment(eq(functionGroupId), eq(newApprovalType));
    }

    @Test
    public void shouldVerifyThatDeleteApprovalTypeIsCalled() {
        String functionGroupId = "fgId";
        mockApprovalLevel(applicationProperties, true);

        accessControlApprovalService.deleteApprovalType(functionGroupId);
        verify(approvalService)
            .deleteApprovalTypeAssignment(eq(functionGroupId));
    }

    @Test
    public void shouldVerifyThatCreateApprovalTypeIsCalled() {
        String functionGroupId = "fgId";
        String newApprovalType = "appTypeId";
        mockApprovalLevel(applicationProperties, true);

        accessControlApprovalService.createApprovalType(functionGroupId, newApprovalType);

        verify(approvalService).postBulkAssignApprovalType(eq(functionGroupId), eq(newApprovalType));
    }

    @Test
    public void shouldGetPersistenceApprovalDataGroups() {

        String approvalId = "approvalId";
        String serviceAgreementId = "saId";
        String userId = "userId";

        PresentationDataGroupApprovalDetailsItem data = new PresentationDataGroupApprovalDetailsItem()
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("sa-name")
            .withDataGroupId("dataGroupId")
            .withApprovalId("approvalId")
            .withAddedDataItems(Sets.newHashSet("1", "2"));

        when(dataGroupService.getByApprovalId(eq(approvalId)))
            .thenReturn(data);
        PresentationGetApprovalDetailResponse approvalById = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId(serviceAgreementId)
                .userId(userId)
                .function("Manage Data Groups"));

        when(approvalService.getApprovalDetailById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(false)))
            .thenReturn(approvalById);
        PresentationDataGroupApprovalDetailsItem responseFromPandp = accessControlApprovalService
            .getPersistenceApprovalDataGroups(approvalId, serviceAgreementId, userId);

        assertEquals(data.getServiceAgreementId(), responseFromPandp.getServiceAgreementId());
        assertEquals(data.getServiceAgreementName(), responseFromPandp.getServiceAgreementName());
        assertEquals(data.getDataGroupId(), responseFromPandp.getDataGroupId());
        assertEquals(data.getApprovalId(), responseFromPandp.getApprovalId());
        assertEquals(data.getAddedDataItems(), responseFromPandp.getAddedDataItems());
    }

    @Test
    public void shouldGetPersistenceApprovalFunctionGroups() {

        String approvalId = "approvalId";
        String serviceAgreementId = "saId";
        String userId = "userId";

        PresentationFunctionGroupApprovalDetailsItem data = new PresentationFunctionGroupApprovalDetailsItem()
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("sa-name")
            .withFunctionGroupId("functionGroupId")
            .withApprovalId("approvalId")
            .withAction(PresentationApprovalAction.DELETE);

        when(functionGroupService.getByApprovalId(eq(approvalId)))
            .thenReturn(data);
        PresentationGetApprovalDetailResponse approvalById = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId(serviceAgreementId)
                .userId(userId)
                .function("Manage Function Groups"));

        when(approvalService.getApprovalDetailById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(false)))
            .thenReturn(approvalById);

        PresentationFunctionGroupApprovalDetailsItem responseFromPandp = accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(approvalId, serviceAgreementId, userId);

        assertEquals(data.getServiceAgreementId(), responseFromPandp.getServiceAgreementId());
        assertEquals(data.getServiceAgreementName(), responseFromPandp.getServiceAgreementName());
        assertEquals(data.getFunctionGroupId(), responseFromPandp.getFunctionGroupId());
        assertEquals(data.getApprovalId(), responseFromPandp.getApprovalId());
        assertEquals(data.getAction(), responseFromPandp.getAction());
    }

    @Test
    public void shouldGetPersistenceApprovalFunctionGroupsWithNullOldState() {

        String approvalId = "approvalId";
        String serviceAgreementId = "saId";
        String userId = "userId";

        PresentationFunctionGroupApprovalDetailsItem data = new PresentationFunctionGroupApprovalDetailsItem()
            .withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementName("sa-name")
            .withFunctionGroupId("functionGroupId")
            .withApprovalId("approvalId")
            .withAction(PresentationApprovalAction.DELETE)
            .withOldState(null);

        when(functionGroupService.getByApprovalId(eq(approvalId)))
            .thenReturn(data);
        PresentationGetApprovalDetailResponse approvalById = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId(serviceAgreementId)
                .userId(userId)
                .function("Manage Data Groups"));

        when(approvalService.getApprovalDetailById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(false)))
            .thenReturn(approvalById);

        PresentationFunctionGroupApprovalDetailsItem responseFromPandp = accessControlApprovalService
            .getPersistenceApprovalFunctionGroups(approvalId, serviceAgreementId, userId);

        assertEquals(data.getServiceAgreementId(), responseFromPandp.getServiceAgreementId());
        assertEquals(data.getServiceAgreementName(), responseFromPandp.getServiceAgreementName());
        assertEquals(data.getFunctionGroupId(), responseFromPandp.getFunctionGroupId());
        assertEquals(data.getApprovalId(), responseFromPandp.getApprovalId());
        assertEquals(data.getAction(), responseFromPandp.getAction());
    }

    @Test
    public void testRejectApprovalRequestOnApprovalApiSide() {
        String serviceAgreementId = "saId";
        String userId = "userId";
        String approvalId = "approvalId";

        ApprovalDto approval = new ApprovalDto()
            .status(ApprovalStatus.REJECTED)
            .serviceAgreementId(serviceAgreementId)
            .userId(userId);

        PresentationPostApprovalResponse responseData = new PresentationPostApprovalResponse()
            .approval(approval);
        when(approvalService.postApprovalRecords(eq(approvalId), any(PostApprovalRecordRequest.class)))
            .thenReturn(responseData);

        PresentationGetApprovalDetailResponse approvalById = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId(serviceAgreementId)
                .userId(userId)
                .function("Assign Permissions"));
        when(approvalService.getApprovalDetailById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(false)))
            .thenReturn(approvalById);

        accessControlApprovalService
            .rejectApprovalRequestOnApprovalApiSide(approvalId, serviceAgreementId, userId);

        assertEquals(responseData.getApproval().getStatus(), approval.getStatus());
    }

    @Test
    public void testApproveApprovalRequestOnApprovalApiSide() {

        String serviceAgreementId = "saId";
        String userId = "userId";
        String approvalId = "approvalId";

        ApprovalDto approval = new ApprovalDto()
            .status(ApprovalStatus.APPROVED)
            .serviceAgreementId(serviceAgreementId)
            .userId(userId);

        PresentationPostApprovalResponse responseData = new PresentationPostApprovalResponse()
            .approval(approval);
        when(approvalService.postApprovalRecords(eq(approvalId), any(PostApprovalRecordRequest.class)))
            .thenReturn(responseData);

        PresentationGetApprovalDetailResponse approvalById = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId(serviceAgreementId)
                .userId(userId)
                .function("Assign Permissions"));
        when(approvalService.getApprovalDetailById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(false)))
            .thenReturn(approvalById);

        accessControlApprovalService
            .acceptApprovalRequestOnApprovalApiSide(approvalId, serviceAgreementId, userId);
        verify(approvalService).postApprovalRecords(eq(approvalId), any(PostApprovalRecordRequest.class));
        assertEquals(responseData.getApproval().getStatus(), approval.getStatus());
    }

    @Test
    public void shouldThrowForbiddenWhenServiceAgreementIsMissing() {

        String approvalId = "approvalId";

        PresentationGetApprovalDetailResponse approval = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId("sa1")
                .userId("creator"));

        when(approvalService.getApprovalDetailById(eq(approvalId), isNull(), eq("user1"), eq(false)))
            .thenReturn(approval);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> accessControlApprovalService.acceptApprovalRequestOnApprovalApiSide(
                approvalId, null, "user1"));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_090.getErrorMessage(), ERR_AG_090.getErrorCode())));
    }

    @Test
    public void shouldThrowForbiddenWhenServiceAgreementsAreNotMatching() {

        String approvalId = "approvalId";

        PresentationGetApprovalDetailResponse approval = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId("sa1")
                .userId("creator"));

        when(approvalService.getApprovalDetailById(eq(approvalId), anyString(), eq("user1"), anyBoolean()))
            .thenReturn(approval);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> accessControlApprovalService.acceptApprovalRequestOnApprovalApiSide(
                approvalId, "sa2", "user1"));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_090.getErrorMessage(), ERR_AG_090.getErrorCode())));
    }

    @Test
    public void getPersistenceApprovalServiceAgreement() {

        String approvalId = "approvalId";
        String serviceAgreementId = "saId";
        String userId = "userId";

        PresentationGetApprovalDetailResponse approvalById = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .serviceAgreementId(serviceAgreementId)
                .userId(userId)
                .function("Manage Function Groups"));

        ServiceAgreementApprovalDetailsItem response = new ServiceAgreementApprovalDetailsItem()
            .withApprovalId(approvalId);

        when(approvalService.getApprovalDetailById(eq(approvalId), eq(serviceAgreementId), eq(userId), eq(false)))
            .thenReturn(approvalById);

        when(serviceAgreementService.getByApprovalId(eq(approvalId))).thenReturn(response);

        ServiceAgreementApprovalDetailsItem responseFromPandp = accessControlApprovalService
            .getPersistenceApprovalServiceAgreement(approvalId, serviceAgreementId, userId);

        assertEquals(responseFromPandp, response);
    }

}