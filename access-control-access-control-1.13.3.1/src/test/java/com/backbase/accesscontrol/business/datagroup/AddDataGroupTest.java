package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PostApprovalRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddDataGroupTest {

    @Mock
    private DataGroupPAndPService dataGroupPAndPService;
    @Mock
    private ApprovalsService approvalsService;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;

    private AddDataGroup addDataGroup;

    @Before
    public void setUp() {

        addDataGroup = new AddDataGroup(dataGroupPAndPService, userContextUtil, approvalsService,
            applicationProperties);
    }

    @Test
    public void shouldCallDataGroupPAndPServiceWhenApprovalOff() {
        mockApprovalValidation(applicationProperties, false);

        DataGroupBase postData = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));
        InternalRequest<DataGroupBase> request = getInternalRequest(postData);

        DataGroupsPostResponseBody responseData = new DataGroupsPostResponseBody().withId("id");

        when(dataGroupPAndPService.createDataGroupWithAudit(eq(postData)))
            .thenReturn(responseData);

        DataGroupOperationResponse data = addDataGroup.addDataGroup(request).getData();

        assertEquals(responseData.getId(), data.getId());
    }

    @Test
    public void shouldCallDataGroupPandPServiceWhenApprovalOn() {
        mockApprovalValidation(applicationProperties, true);

        DataGroupBase postData = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));
        InternalRequest<DataGroupBase> request = getInternalRequest(postData);

        DataGroupsPostResponseBody responseData = new DataGroupsPostResponseBody().withId("id");

        PostApprovalRequest approvalRequest = new PostApprovalRequest();
        approvalRequest.setUserId("user");
        approvalRequest.setServiceAgreementId("saId");
        approvalRequest.setResource("Entitlements");
        approvalRequest.setItemId("someItemId");
        approvalRequest.setFunction("Manage Data Groups");
        approvalRequest.setAction("CREATE");

        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        when(dataGroupPAndPService.createDataGroupWithApproval(any(DataGroupBase.class), eq("approvalId")))
            .thenReturn(responseData);

        DataGroupOperationResponse data = addDataGroup.addDataGroup(request).getData();

        assertEquals(responseData.getId(), data.getId());
    }

    @Test
    public void shouldCallDataGroupPandPServiceWhenApprovalOnAndGetBadRequestToCancelApproval() {
        mockApprovalValidation(applicationProperties, true);

        DataGroupBase postData = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));
        InternalRequest<DataGroupBase> request = getInternalRequest(postData);

        PostApprovalRequest approvalRequest = new PostApprovalRequest();
        approvalRequest.setUserId("user");
        approvalRequest.setServiceAgreementId("saId");
        approvalRequest.setResource("Entitlements");
        approvalRequest.setItemId("someItemId");
        approvalRequest.setFunction("Manage Data Groups");
        approvalRequest.setAction("CREATE");

        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        doThrow(getBadRequestException("error", "error")).when(dataGroupPAndPService)
            .createDataGroupWithApproval(any(DataGroupBase.class), eq("approvalId"));
        doNothing().when(approvalsService).cancelApprovalRequest(eq("approvalId"));

        assertThrows(BadRequestException.class,
            () -> addDataGroup.addDataGroup(request).getData());

        verify(approvalsService, times(1)).cancelApprovalRequest(eq("approvalId"));
    }

    @Test
    public void shouldCallDataGroupPAndPServiceWithZeroApproval() {
        mockApprovalValidation(applicationProperties, true);

        DataGroupBase postData = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));
        InternalRequest<DataGroupBase> request = getInternalRequest(postData);

        DataGroupsPostResponseBody responseData = new DataGroupsPostResponseBody().withId("id");

        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        when(dataGroupPAndPService.createDataGroupWithAudit(eq(postData)))
            .thenReturn(responseData);

        DataGroupOperationResponse data = addDataGroup.addDataGroup(request).getData();

        assertEquals(responseData.getId(), data.getId());
    }
}