package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
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
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupTest {

    @Mock
    private DataGroupPAndPService dataGroupPAndPService;
    @Captor
    private ArgumentCaptor<DataGroupByIdPutRequestBody> captor;
    @Captor
    private ArgumentCaptor<DataGroupByIdPutRequestBody> captorApproval;

    private UpdateDataGroup updateDataGroup;
    @Mock
    private ApprovalsService approvalsService;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() {
        updateDataGroup = new UpdateDataGroup(dataGroupPAndPService,
            approvalsService, userContextUtil, applicationProperties);
    }

    @Test
    public void shouldCallDataGroupPAndPService() {
        mockApprovalValidation(applicationProperties, false);

        DataGroupByIdPutRequestBody postData = new DataGroupByIdPutRequestBody()
            .withId("dg-01")
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));
        InternalRequest<DataGroupByIdPutRequestBody> request = getInternalRequest(postData);

        doNothing().when(dataGroupPAndPService).updateDataGroup(eq(postData), eq(postData.getId()));

        updateDataGroup.updateDataGroup(request, postData.getId()).getData();

        verify(dataGroupPAndPService, times(1)).updateDataGroup(captor.capture(), eq(postData.getId()));

        DataGroupByIdPutRequestBody data = captor.getValue();

        assertEquals(postData, data);
    }

    @Test
    public void shouldCallDataGroupPAndPServiceWithApprovalOn() {

        mockApprovalValidation(applicationProperties, true);

        String approvalId = "approvalId";

        DataGroupByIdPutRequestBody postData = new DataGroupByIdPutRequestBody()
            .withId("dg-01")
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
        ResponseEntity<Void> response = getResponseEntity(null, HttpStatus.OK);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        when(dataGroupPAndPService.updateDataGroupWithApproval(eq(postData), eq(approvalId)))
            .thenReturn(response);
        InternalRequest<DataGroupByIdPutRequestBody> request = getInternalRequest(postData);
        updateDataGroup.updateDataGroup(request, postData.getId()).getData();

        verify(dataGroupPAndPService, times(1)).updateDataGroupWithApproval(captorApproval.capture(), eq(approvalId));

        DataGroupByIdPutRequestBody data = captorApproval.getValue();

        assertEquals(postData, data);
    }

    @Test
    public void shouldCallDataGroupPandPServiceWhenApprovalOnAndGetBadRequestToCancelApproval() {
        mockApprovalValidation(applicationProperties, true);

        DataGroupByIdPutRequestBody postData = new DataGroupByIdPutRequestBody()
            .withId("dg-01")
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        doThrow(getBadRequestException("error", "error")).when(dataGroupPAndPService)
            .updateDataGroupWithApproval(any(DataGroupByIdPutRequestBody.class), eq("approvalId"));
        doNothing().when(approvalsService).cancelApprovalRequest(eq("approvalId"));

        InternalRequest<DataGroupByIdPutRequestBody> request = getInternalRequest(postData);
        assertThrows(BadRequestException.class,
            () -> updateDataGroup.updateDataGroup(request, postData.getId()).getData());

        verify(approvalsService, times(1)).cancelApprovalRequest(eq("approvalId"));
    }

    @Test
    public void shouldCallDataGroupPAndPServiceWithZeroApproval() {
        mockApprovalValidation(applicationProperties, true);

        DataGroupByIdPutRequestBody postData = new DataGroupByIdPutRequestBody()
            .withId("dg-01")
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1", "2"));

        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        InternalRequest<DataGroupByIdPutRequestBody> request = getInternalRequest(postData);

        updateDataGroup.updateDataGroup(request, postData.getId()).getData();
        verify(dataGroupPAndPService, times(1)).updateDataGroup(captor.capture(), eq(postData.getId()));

        DataGroupByIdPutRequestBody data = captor.getValue();

        assertEquals(postData, data);
    }

}