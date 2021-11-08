package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.helpers.RequestUtils;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFunctionGroupTest {

    @Mock
    private FunctionGroupPAndPService functionGroupPAndPService;
    @Mock
    private DateTimeService dateTimeService;
    @Mock
    private ApprovalOnRequestScope approvalOnRequestScope;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private ApprovalsService approvalsService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Mock
    private AccessControlApprovalService accessControlApprovalService;

    @InjectMocks
    private UpdateFunctionGroup updateFunctionGroup;

    @Test
    public void shouldUpdateFunctionGroupWhenApprovalIsOff() {
        String id = "id";
        String saId = "saId";
        String name = "name";
        String description = "description";

        mockApprovalValidation(applicationProperties, false);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.DEFAULT));
        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(saId).withName(name).withDescription(description);

        updateFunctionGroup.updateFunctionGroupById(getInternalRequest(functionGroupByIdPutRequestBody), id);

        verify(functionGroupPAndPService).updateFunctionGroup(eq(functionGroupByIdPutRequestBody), eq(id));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenFromDateNullFromTimeNotNull() {
        String id = "id";
        String saId = "saId";
        String name = "name";
        String description = "description";
        String fromDate = null;
        String fromTime = "11:15:00";
        String untilDate = null;
        String untilTime = null;

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(saId).withName(name).withDescription(description)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<FunctionGroupByIdPutRequestBody> request = getInternalRequest(functionGroupByIdPutRequestBody);

        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateFunctionGroup.updateFunctionGroupById(request, id));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenUntilDateNullAndUntilTimeNotNull() {
        String id = "id";
        String saId = "saId";
        String name = "name";
        String description = "description";
        String fromDate = null;
        String fromTime = null;
        String untilDate = null;
        String untilTime = "07:48:23";

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(saId).withName(name).withDescription(description)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<FunctionGroupByIdPutRequestBody> request = getInternalRequest(functionGroupByIdPutRequestBody);

        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateFunctionGroup.updateFunctionGroupById(request, id));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidFormatFromDateTime() {
        String id = "id";
        String saId = "saId";
        String name = "name";
        String description = "description";
        String fromDate = "2017-21-43";
        String fromTime = "07:48:23";
        String untilDate = null;
        String untilTime = null;

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(saId).withName(name).withDescription(description)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<FunctionGroupByIdPutRequestBody> request = getInternalRequest(functionGroupByIdPutRequestBody);

        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateFunctionGroup.updateFunctionGroupById(request, id));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidFormatUntilDateTime() {
        String id = "id";
        String saId = "saId";
        String name = "name";
        String description = "description";
        String fromDate = null;
        String fromTime = null;
        String untilDate = "2017-14-44";
        String untilTime = "07:48:23";

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(saId).withName(name).withDescription(description)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<FunctionGroupByIdPutRequestBody> request = getInternalRequest(functionGroupByIdPutRequestBody);

        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateFunctionGroup.updateFunctionGroupById(request, id));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenInvalidPeriodOfDateTime() {
        String id = "id";
        String saId = "saId";
        String name = "name";
        String description = "description";
        String fromDate = "2018-03-31";
        String fromTime = "07:48:23";
        String untilDate = "2017-01-31";
        String untilTime = "07:48:23";

        FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
            .withServiceAgreementId(saId).withName(name).withDescription(description)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);

        InternalRequest<FunctionGroupByIdPutRequestBody> request = getInternalRequest(functionGroupByIdPutRequestBody);

        doThrow(getBadRequestException(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateFunctionGroup.updateFunctionGroupById(request, id));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())));

        verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
    }

    @Test
    public void shouldUpdateFunctionGroupWhenApprovalIsOn() {
        mockApprovalValidation(applicationProperties, true);
        String approvalId = "approvalId";

        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId("approvalTypeId")
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id(approvalId)
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);

        InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);
        updateFunctionGroup.updateFunctionGroupById(request, "fgId").getData();

        verify(functionGroupPAndPService).updateFunctionGroupWithApproval(eq(putData), eq("fgId"), eq(approvalId));
    }

    @Test
    public void shouldCallFunctionGroupPandPServiceWhenApprovalOnAndGetBadRequestToCancelApproval() {
        mockApprovalValidation(applicationProperties, true);

        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId("approvalTypeId")
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.PENDING)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        doThrow(getBadRequestException("error", "error")).when(functionGroupPAndPService)
            .updateFunctionGroupWithApproval(any(FunctionGroupByIdPutRequestBody.class), anyString(), anyString());

        doNothing().when(approvalsService).cancelApprovalRequest(eq("approvalId"));

        InternalRequest<FunctionGroupByIdPutRequestBody> request = getInternalRequest(putData);
        assertThrows(BadRequestException.class,
            () -> updateFunctionGroup.updateFunctionGroupById(request, "fgId").getData());

        verify(functionGroupPAndPService).updateFunctionGroupWithApproval(eq(putData), eq("fgId"), eq("approvalId"));
        verify(approvalsService).cancelApprovalRequest(eq("approvalId"));
    }

    @Test
    public void shouldCallFunctionGroupPAndPServiceWithZeroApproval() {
        mockApprovalValidation(applicationProperties, true);

        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId("approvalTypeId")
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.DEFAULT));

        doNothing().when(functionGroupPAndPService)
            .updateFunctionGroup(any(FunctionGroupByIdPutRequestBody.class), anyString());

        InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);
        updateFunctionGroup.updateFunctionGroupById(request, "fgId").getData();

        verify(functionGroupPAndPService).updateFunctionGroup(eq(putData), eq("fgId"));
    }

    @Test
    public void shouldCallFunctionGroupPAndPServiceWithZeroApprovalWithUpdateApprovalTypeId() {
        mockApprovalValidation(applicationProperties, true);
        String fgId = "fgId";
        String oldApprovalTypeId = "type id";
        String newApprovalTypeId = "944c27c0-2808-457b-aa13-71ff07c5b536";

        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId(newApprovalTypeId)
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);

        when(accessControlApprovalService.getApprovalTypeIdFromApprovals(fgId))
            .thenReturn(oldApprovalTypeId);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.DEFAULT));
        updateFunctionGroup.updateFunctionGroupById(request, fgId);

        verify(functionGroupPAndPService).updateFunctionGroup(eq(putData), eq(fgId));
        verify(accessControlApprovalService).updateApprovalType(eq(fgId), eq(newApprovalTypeId));
    }

    @Test
    public void shouldCallFunctionGroupPAndPServiceWithZeroApprovalWithCreateApprovalTypeId() {
        mockApprovalValidation(applicationProperties, true);
        String fgId = "fgId";
        String oldApprovalTypeId = null;
        String newApprovalTypeId = "944c27c0-2808-457b-aa13-71ff07c5b536";

        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId(newApprovalTypeId)
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
        InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);

        when(accessControlApprovalService.getApprovalTypeIdFromApprovals(fgId))
            .thenReturn(oldApprovalTypeId);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.DEFAULT));

        updateFunctionGroup.updateFunctionGroupById(request, fgId);

        verify(functionGroupPAndPService).updateFunctionGroup(eq(putData), eq(fgId));
        verify(accessControlApprovalService).createApprovalType(eq(fgId), eq(newApprovalTypeId));
    }

    @Test
    public void shouldCallFunctionGroupPAndPServiceWithZeroApprovalWithDeleteApprovalTypeId() {
        mockApprovalValidation(applicationProperties, true);
        String fgId = "fgId";
        String oldApprovalTypeId = "type id";
        String newApprovalTypeId = null;

        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId(newApprovalTypeId)
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);

        when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
        when(approvalsService
            .getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
                eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
            .thenReturn(approvalResponse);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.DEFAULT));
        InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);

        when(accessControlApprovalService.getApprovalTypeIdFromApprovals(fgId))
            .thenReturn(oldApprovalTypeId);
        updateFunctionGroup.updateFunctionGroupById(request, fgId).getData();

        verify(functionGroupPAndPService).updateFunctionGroup(eq(putData), eq(fgId));
        verify(accessControlApprovalService).deleteApprovalType(eq(fgId));
    }
    
	@Test
	public void shouldUpdateApprovalTypeOnlyWhenApprovalIsOff() {
		String id = "idOfSystemJobRole";
		String saId = "saId";
		String name = "name";
		String description = "description";
		String oldApprovalTypeId = "type id";
		String newApprovalTypeId = "new type id";
		mockApprovalValidation(applicationProperties, false);
		when(functionGroupPAndPService.getFunctionGroupById(id))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.SYSTEM));
		when(accessControlApprovalService.getApprovalTypeIdFromApprovals(id)).thenReturn(oldApprovalTypeId);
		FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
				.withServiceAgreementId(saId).withName(name).withDescription(description)
				.withApprovalTypeId(newApprovalTypeId);
		updateFunctionGroup.updateFunctionGroupById(getInternalRequest(functionGroupByIdPutRequestBody), id);
		verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
		verify(accessControlApprovalService).updateApprovalType(eq(id), eq(newApprovalTypeId));
	}
	
	@Test
	public void shouldCreateApprovalTypeOnlyWhenApprovalIsOff() {
		String id = "idOfSystemJobRole";
		String saId = "saId";
		String name = "name";
		String description = "description";
		String oldApprovalTypeId = null;
		String newApprovalTypeId = "new type id";
		mockApprovalValidation(applicationProperties, false);
		when(functionGroupPAndPService.getFunctionGroupById(id))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.SYSTEM));
		when(accessControlApprovalService.getApprovalTypeIdFromApprovals(id)).thenReturn(oldApprovalTypeId);
		FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
				.withServiceAgreementId(saId).withName(name).withDescription(description)
				.withApprovalTypeId(newApprovalTypeId);
		updateFunctionGroup.updateFunctionGroupById(getInternalRequest(functionGroupByIdPutRequestBody), id);
		verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
		verify(accessControlApprovalService).createApprovalType(eq(id), eq(newApprovalTypeId));
	}
	
	@Test
	public void shouldDeleteApprovalTypeOnlyWhenApprovalIsOff() {
		String id = "idOfSystemJobRole";
		String saId = "saId";
		String name = "name";
		String description = "description";
		String oldApprovalTypeId = "old type id";
		String newApprovalTypeId = null;
		mockApprovalValidation(applicationProperties, false);
		when(functionGroupPAndPService.getFunctionGroupById(id))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.SYSTEM));
		when(accessControlApprovalService.getApprovalTypeIdFromApprovals(id)).thenReturn(oldApprovalTypeId);
		FunctionGroupByIdPutRequestBody functionGroupByIdPutRequestBody = new FunctionGroupByIdPutRequestBody()
				.withServiceAgreementId(saId).withName(name).withDescription(description)
				.withApprovalTypeId(newApprovalTypeId);
		updateFunctionGroup.updateFunctionGroupById(getInternalRequest(functionGroupByIdPutRequestBody), id);
		verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
		verify(accessControlApprovalService).deleteApprovalType(eq(id));
	}
	
	@Test
	public void shouldUpdateApprovalTypeOnlyWithZeroApproval() {
		mockApprovalValidation(applicationProperties, true);
        String id = "idOfSystemJobRole";
        String oldApprovalTypeId = "type id";
		String newApprovalTypeId = "new type id";
        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId(newApprovalTypeId)
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
		when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
		when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
		when(approvalsService.getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
				eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
						.thenReturn(approvalResponse);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.SYSTEM));
		when(accessControlApprovalService.getApprovalTypeIdFromApprovals(id)).thenReturn(oldApprovalTypeId);
		InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);
		updateFunctionGroup.updateFunctionGroupById(request, id).getData();
		verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
		verify(functionGroupPAndPService, times(0)).updateFunctionGroupWithApproval(any(), any(), any());
		verify(accessControlApprovalService).updateApprovalType(eq(id), eq(newApprovalTypeId));
	}
	
	@Test
	public void shouldCreateApprovalTypeOnlyWithZeroApproval() {
		mockApprovalValidation(applicationProperties, true);
        String id = "idOfSystemJobRole";
        String oldApprovalTypeId = null;
		String newApprovalTypeId = "new type id";
        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId(newApprovalTypeId)
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
		when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
		when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
		when(approvalsService.getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
				eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
						.thenReturn(approvalResponse);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.SYSTEM));
		when(accessControlApprovalService.getApprovalTypeIdFromApprovals(id)).thenReturn(oldApprovalTypeId);
		InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);
		updateFunctionGroup.updateFunctionGroupById(request, id).getData();
		verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
		verify(functionGroupPAndPService, times(0)).updateFunctionGroupWithApproval(any(), any(), any());
		verify(accessControlApprovalService).createApprovalType(eq(id), eq(newApprovalTypeId));
	}
	
	@Test
	public void shouldDeleteApprovalTypeOnlyWithZeroApproval() {
		mockApprovalValidation(applicationProperties, true);
        String id = "idOfSystemJobRole";
        String oldApprovalTypeId = "type id";
		String newApprovalTypeId = null;
        FunctionGroupByIdPutRequestBody putData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withApprovalTypeId(newApprovalTypeId)
            .withServiceAgreementId("service-agreement-id");
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("saId")
            .status(ApprovalStatus.APPROVED)
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Function Groups")
            .action("EDIT");
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
		when(userContextUtil.getServiceAgreementId()).thenReturn("saId");
		when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("user", "le"));
		when(approvalsService.getApprovalResponse(eq(approval.getUserId()), eq(approval.getServiceAgreementId()),
				eq(approval.getResource()), eq(approval.getFunction()), eq(approval.getAction())))
						.thenReturn(approvalResponse);
		when(functionGroupPAndPService.getFunctionGroupById(anyString()))
				.thenReturn(new FunctionGroupByIdGetResponseBody().withType(Type.SYSTEM));
		when(accessControlApprovalService.getApprovalTypeIdFromApprovals(id)).thenReturn(oldApprovalTypeId);
		InternalRequest<FunctionGroupByIdPutRequestBody> request = RequestUtils.getInternalRequest(putData);
		updateFunctionGroup.updateFunctionGroupById(request, id).getData();
		verify(functionGroupPAndPService, times(0)).updateFunctionGroup(any(), any());
		verify(functionGroupPAndPService, times(0)).updateFunctionGroupWithApproval(any(), any(), any());
		verify(accessControlApprovalService).deleteApprovalType(eq(id));
	}
    
}
