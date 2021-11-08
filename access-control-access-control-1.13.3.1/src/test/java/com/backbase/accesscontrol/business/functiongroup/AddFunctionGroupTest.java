package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.helpers.ApplicationPropertiesUtils.mockApprovalValidation;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddFunctionGroupTest {

    @Mock
    private FunctionGroupPAndPService functionGroupPAndPService;
    @Mock
    private UserContextUtil userContextUtil;
    @Spy
    private FunctionGroupMapper functionGroupMapper = Mappers.getMapper(FunctionGroupMapper.class);
    @Mock
    private DateTimeService dateTimeService;
    @Mock
    private ApprovalsService approvalsService;
    @Mock
    private ApprovalOnRequestScope approvalOnRequestScope;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ApplicationProperties applicationProperties;
    @Mock
    private AccessControlApprovalService accessControlApprovalService;
    @InjectMocks
    private AddFunctionGroup addFunctionGroup;

    @Test
    public void shouldSaveFunctionGroupWhenAuthenticatedUserAndOperatesUnderMSA() {
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String creatorLe = "le-creator";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockApprovalValidation(applicationProperties, false);
        mockUserContext(serviceAgreementId, "user", creatorLe);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveFunctionGroupWhenAuthenticatedUserAndOperatesUnderCSA() {
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String legalEntityOfLoggedUser = "le-creator";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);
        mockApprovalValidation(applicationProperties, false);

        mockUserContext(serviceAgreementId, "user", legalEntityOfLoggedUser);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveFunctionGroupWhenAuthenticatedUserIsNotPresentAndOperatesUnderMSA() {
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);
        mockApprovalValidation(applicationProperties, false);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveFunctionGroupWhenAuthenticatedUserIsNotPresentAndOperatesUnderCSA() {
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockApprovalValidation(applicationProperties, false);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveZeroPolicyFunctionGroupWhenApprovalOnAndAuthenticatedUserAndOperatesUnderCSA() {

        mockApprovalValidation(applicationProperties, true);
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String legalEntityOfLoggedUser = "le-creator";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockUserContext(serviceAgreementId, "user", legalEntityOfLoggedUser);
        ApprovalDto approvalDto = mockApprovalService(ApprovalStatus.APPROVED);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        verify(functionGroupPAndPService, times(0))
            .createFunctionGroupWithApproval(refEq(postData), eq(approvalDto.getId()));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveZeroPolicyFunctionGroupWhenApprovalOnAndAuthenticatedUserIsNotPresentAndOperatesUnderMSA() {

        mockApprovalValidation(applicationProperties, true);
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String creatorLegalEntity = "le-creator";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockUserContext(serviceAgreementId, "user", creatorLegalEntity);
        ApprovalDto approvalDto = mockApprovalService(ApprovalStatus.APPROVED);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        verify(functionGroupPAndPService, times(0))
            .createFunctionGroupWithApproval(refEq(postData), eq(approvalDto.getId()));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveZeroPolicyFunctionGroupWhenApprovalOnAuthenticatedUserIsNotPresentAndOperatesUnderCSA() {

        mockApprovalValidation(applicationProperties, true);
        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String rootLegalEntityId = "root-le";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockUserContext(serviceAgreementId, "user", rootLegalEntityId);
        ApprovalDto approvalDto = mockApprovalService(ApprovalStatus.APPROVED);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        verify(functionGroupPAndPService, times(0))
            .createFunctionGroupWithApproval(refEq(postData), eq(approvalDto.getId()));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveZeroPolicyWithApprovalTypeId() {

        mockApprovalValidation(applicationProperties, true);

        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String creatorLe = "le-creator";
        String newApprovalTypeId = "944c27c0-2808-457b-aa13-71ff07c5b536";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockUserContext(serviceAgreementId, "user", creatorLe);
        mockApprovalService(ApprovalStatus.APPROVED);
        mockCreateFunctionGroup(data);
        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)
            .withApprovalTypeId(newApprovalTypeId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);
        doNothing().when(accessControlApprovalService).createApprovalType(eq(createdFagId), eq(newApprovalTypeId));

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(accessControlApprovalService, times(1))
            .createApprovalType(eq(createdFagId), eq(newApprovalTypeId));
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSaveZeroPolicyFunctionGroupWhenApprovalOnAndAuthenticatedUserAndOperatesUnderMSA() {

        mockApprovalValidation(applicationProperties, true);

        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String creatorLe = "le-creator";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockUserContext(serviceAgreementId, "user", creatorLe);
        ApprovalDto approvalDto = mockApprovalService(ApprovalStatus.APPROVED);

        FunctionGroupsPostResponseBody postResponse = mockCreateFunctionGroup(data);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroup(eq(new
            FunctionGroupBase()
            .withName(name)
            .withServiceAgreementId(serviceAgreementId)));
        verify(functionGroupPAndPService, times(0))
            .createFunctionGroupWithApproval(refEq(postData), eq(approvalDto.getId()));
        assertEquals(postResponse, responseResult.getData());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSavePendingFunctionGroupWhenApprovalOnAndAuthenticatedUserAndOperatesUnderMSA() {

        mockApprovalValidation(applicationProperties, true);

        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String creatorLe = "le-creator";
        String approvalId = "approvalId";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        ApprovalDto approvalDto = mockApprovalService(ApprovalStatus.PENDING);
        mockUserContext(serviceAgreementId, "user", creatorLe);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        FunctionGroupsPostResponseBody postResponse = mockCreatePendingFunctionGroup(data, postData,
            approvalDto.getId());
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroupWithApproval(
            eq(postData), eq(approvalId));
        verify(functionGroupPAndPService, times(0)).createFunctionGroup(eq(postData));
        assertEquals(postResponse.getId(), responseResult.getData().getId());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSavePendingFunctionGroupWhenApprovalOnAndAuthenticatedUserAndOperatesUnderCSA() {

        mockApprovalValidation(applicationProperties, true);

        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String legalEntityOfLoggedUser = "le-creator";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        ApprovalDto approvalDto = mockApprovalService(ApprovalStatus.PENDING);
        mockUserContext(serviceAgreementId, "user", legalEntityOfLoggedUser);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        mockCreatePendingFunctionGroup(data, postData, approvalDto.getId());

        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);
        FunctionGroupsPostResponseBody response = new FunctionGroupsPostResponseBody();
        response.setId(data.getId());

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroupWithApproval(
            eq(new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)),
            eq(approvalDto.getId()));
        verify(functionGroupPAndPService, times(0))
            .createFunctionGroup(eq(postData));
        assertEquals(data.getId(), responseResult.getData().getId());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldSavePendingFunctionGroupWhenApprovalOnAuthenticatedUserIsNotPresentAndOperatesUnderMSA() {

        mockApprovalValidation(applicationProperties, true);

        String serviceAgreementId = "SA-01";
        String name = "FAG-1";
        String creatorLegalEntity = "le-creator";
        String fgID = "fgID";
        String approvalId = "approvalId";
        Map<String, String> additions = new HashMap<>();
        additions.put("add", "test");

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(fgID);
        data.setAdditions(additions);

        mockUserContext(serviceAgreementId, "user", creatorLegalEntity);
        ApprovalDto approvalDto = mockApprovalService(ApprovalStatus.PENDING);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        FunctionGroupsPostResponseBody postResponse = mockCreatePendingFunctionGroup(data, postData,
            approvalDto.getId());
        FunctionGroupsPostResponseBody response = new FunctionGroupsPostResponseBody();
        response.setId(data.getId());
        response.setAdditions(additions);

        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);
        verify(functionGroupPAndPService, times(1)).createFunctionGroupWithApproval(
            refEq(postData),
            eq(approvalId));
        verify(functionGroupPAndPService, times(0)).createFunctionGroup(eq(postData));
        assertEquals(postResponse.getId(), responseResult.getData().getId());
        assertEquals(postResponse.getAdditions(), responseResult.getData().getAdditions());
        assertEquals(fgID, responseResult.getData().getId());
    }

    @Test
    public void shouldSavePendingFunctionGroupWhenApprovalOnAuthenticatedUserIsNotPresentAndOperatesUnderCSA() {

        mockApprovalValidation(applicationProperties, true);

        String serviceAgreementId = "SA-01";
        String createdFagId = "000001";
        String name = "FAG-1";
        String rootLegalEntityId = "root-le";
        String approvalId = "approvalId";

        FunctionGroupsPostResponseBody data = new FunctionGroupsPostResponseBody()
            .withId(createdFagId);

        mockUserContext(serviceAgreementId, "user", rootLegalEntityId);
        mockApprovalService(ApprovalStatus.PENDING);

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId);
        FunctionGroupsPostResponseBody postResponse = mockCreatePendingFunctionGroup(data, postData,
            approvalId);

        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);
        FunctionGroupsPostResponseBody response = new FunctionGroupsPostResponseBody();
        response.setId(data.getId());

        InternalRequest<FunctionGroupsPostResponseBody> responseResult = addFunctionGroup.addGroup(postRequest);

        verify(functionGroupPAndPService, times(1)).createFunctionGroupWithApproval(
            eq(new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)),
            eq(approvalId));
        verify(functionGroupPAndPService, times(0)).createFunctionGroup(any());
        assertEquals(postResponse.getId(), responseResult.getData().getId());
        assertEquals(createdFagId, responseResult.getData().getId());
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenAuthenticatedUserAndOperatesUnderMSAAndFromDateNullAndFromTimeNotNull() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";
        String creatorLe = "le-creator";

        mockUserContext(serviceAgreementId, "user", creatorLe);

        String fromDate = null;
        String fromTime = "07:48:23";
        String untilDate = null;
        String untilTime = null;
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> addFunctionGroup.addGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0))
            .createFunctionGroup(any(FunctionGroupBase.class));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenAuthenticatedUserAndOperatesUnderMSAAndUntilDateNullAndUntilTimeNotNull() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";
        String creatorLe = "le-creator";

        mockUserContext(serviceAgreementId, "user", creatorLe);

        String fromDate = null;
        String fromTime = null;
        String untilDate = null;
        String untilTime = "07:48:23";
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> addFunctionGroup.addGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0)).createFunctionGroup(any(
            FunctionGroupBase.class));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenAuthenticatedUserAndOperatesUnderMSAAndInvalidFormatFromDateTime() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";
        String creatorLe = "le-creator";

        mockUserContext(serviceAgreementId, "user", creatorLe);

        String fromDate = "2017-21-43";
        String fromTime = "07:48:23";
        String untilDate = null;
        String untilTime = null;
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> addFunctionGroup.addGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0))
            .createFunctionGroup(any(FunctionGroupBase.class));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenAuthenticatedUserAndOperatesUnderMSAAndInvalidFormatUntilDateTime() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";
        String creatorLe = "le-creator";

        mockUserContext(serviceAgreementId, "user", creatorLe);

        String fromDate = null;
        String fromTime = null;
        String untilDate = "2017-14-44";
        String untilTime = "07:48:23";
        doThrow(getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> addFunctionGroup.addGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(functionGroupPAndPService, times(0))
            .createFunctionGroup(any(FunctionGroupBase.class));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenAuthenticatedUserAndOperatesUnderMSAAndInvalidPeriodOfDateTime() {
        String serviceAgreementId = "SA-01";
        String name = "FAG-1";
        String creatorLe = "le-creator";

        mockUserContext(serviceAgreementId, "user", creatorLe);

        String fromDate = "2018-03-31";
        String fromTime = "07:48:23";
        String untilDate = "2017-01-31";
        String untilTime = "07:48:23";

        doThrow(getBadRequestException(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())).when(dateTimeService)
            .validateTimebound(eq(fromDate), eq(fromTime), eq(untilDate), eq(untilTime));

        FunctionGroupBase postData = new FunctionGroupBase().withName(name).withServiceAgreementId(serviceAgreementId)
            .withValidFromDate(fromDate).withValidFromTime(fromTime).withValidUntilDate(untilDate)
            .withValidUntilTime(untilTime);
        InternalRequest<FunctionGroupBase> postRequest = getInternalRequest(postData);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> addFunctionGroup.addGroup(postRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())));

        verify(functionGroupPAndPService, times(0))
            .createFunctionGroup(any(FunctionGroupBase.class));
    }

    private void mockUserContext(String serviceAgreementId, String userId, String leId) {
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, leId));
    }

    private FunctionGroupsPostResponseBody mockCreateFunctionGroup(FunctionGroupsPostResponseBody data) {
        when(functionGroupPAndPService.createFunctionGroup(any(FunctionGroupBase.class)))
            .thenReturn(data);
        return data;
    }

    private FunctionGroupsPostResponseBody mockCreatePendingFunctionGroup(
        FunctionGroupsPostResponseBody data, FunctionGroupBase functionGroupBase, String approvalId) {
        when(functionGroupPAndPService.createFunctionGroupWithApproval(refEq(functionGroupBase),
            eq(approvalId))).thenReturn(data);
        return data;
    }

    private ApprovalDto mockApprovalService(ApprovalStatus status) {
        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .userId("user")
            .serviceAgreementId("SA-01")
            .itemId("someItemId")
            .resource("Entitlements")
            .function("Manage Data Groups")
            .action("CREATE")
            .status(status);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse()
            .approval(approval);
        when(approvalsService.getApprovalResponse(any(), any(), any(), any(), any()))
            .thenReturn(approvalResponse);
        return approval;
    }
}
