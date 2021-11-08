package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_103;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.persistence.datagroup.AddDataGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.AddDataGroupHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.DeleteDataGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.DeleteDataGroupHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.UpdateDataGroupApprovalHandler;
import com.backbase.accesscontrol.business.persistence.datagroup.UpdateDataGroupHandler;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataGroupPAndPServiceTest {

    @Mock
    private PermissionValidationService permissionValidationService;
    @Mock
    private ValidationConfig validationConfig;
    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private UpdateDataGroupHandler updateDataGroupHandler;
    @Mock
    private UpdateDataGroupApprovalHandler updateDataGroupApprovalHandler;
    @Mock
    private DeleteDataGroupHandler deleteDataGroupHandler;
    @Mock
    private DeleteDataGroupApprovalHandler deleteDataGroupApprovalHandler;

    @InjectMocks
    private DataGroupPAndPService dataGroupPAndPService;

    @Mock
    private AddDataGroupHandler addDataGroupHandler;
    @Mock
    private AddDataGroupApprovalHandler addDataGroupApprovalHandler;

    @Test
    public void createDataGroup() {
        String id = "id";
        DataGroupBase body = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withItems(asList("1", "2"))
            .withServiceAgreementId("sa-id")
            .withType("ARRANGEMENTS");

        DataGroupBase pandpRequest = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withItems(asList("1", "2"))
            .withServiceAgreementId("sa-id")
            .withType("ARRANGEMENTS");

        DataGroupsPostResponseBody response = new DataGroupsPostResponseBody()
            .withId(id);

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(response);

        dataGroupPAndPService.createDataGroupWithAudit(body);

        verify(addDataGroupHandler).handleRequest(any(EmptyParameterHolder.class), eq(pandpRequest));
    }

    @Test
    public void createDataGroupBaseData() {
        String id = "id";
        DataGroupBase body = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withItems(Arrays.asList("1", "2"))
            .withServiceAgreementId("sa-id")
            .withType("ARRANGEMENTS");

        DataGroupBase pandpRequest = new DataGroupBase()
            .withName("name")
            .withDescription("description")
            .withItems(Arrays.asList("1", "2"))
            .withServiceAgreementId("sa-id")
            .withType("ARRANGEMENTS");

        DataGroupsPostResponseBody response = new DataGroupsPostResponseBody()
            .withId(id);

        when(addDataGroupHandler.handleRequest(any(EmptyParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(response);

        DataGroupsPostResponseBody result = dataGroupPAndPService.createDataGroup(body);

        verify(addDataGroupHandler).handleRequest(any(EmptyParameterHolder.class), eq(pandpRequest));
        assertEquals(id, result.getId());
    }

    @Test
    public void updateDataGroup() {
        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withId("dg-01")
            .withName("name")
            .withDescription("description")
            .withItems(asList("1", "2"))
            .withServiceAgreementId("sa-id")
            .withType("ARRANGEMENTS");

        doNothing().when(updateDataGroupHandler)
            .handleRequest(any(SingleParameterHolder.class), any(DataGroupByIdPutRequestBody.class));

        dataGroupPAndPService.updateDataGroup(body, body.getId());

        ArgumentCaptor<SingleParameterHolder> requestCaptor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        ArgumentCaptor<DataGroupByIdPutRequestBody> requestCaptorForData = ArgumentCaptor
            .forClass(DataGroupByIdPutRequestBody.class);
        verify(updateDataGroupHandler).handleRequest(requestCaptor.capture(), requestCaptorForData.capture());

        DataGroupByIdPutRequestBody data = requestCaptorForData.getValue();

        assertEquals(body.getId(), data.getId());
        assertEquals(body.getName(), data.getName());
        assertEquals(body.getDescription(), data.getDescription());
        assertEquals(body.getItems(), data.getItems());
        assertEquals(body.getServiceAgreementId(), data.getServiceAgreementId());
        assertEquals(body.getType(), data.getType());
        assertEquals(body.getId(), requestCaptor.getValue().getParameter());
    }

    @Test
    public void updateDataGroupWithApprovalOn() {
        String approvalId = "approvalId";
        DataGroupByIdPutRequestBody body = new DataGroupByIdPutRequestBody()
            .withId("dg-01")
            .withName("name")
            .withDescription("description")
            .withItems(asList("1", "2"))
            .withServiceAgreementId("sa-id")
            .withType("ARRANGEMENTS");

        doNothing().when(updateDataGroupApprovalHandler)
            .handleRequest(any(SingleParameterHolder.class), any(DataGroupByIdPutRequestBody.class));

        dataGroupPAndPService.updateDataGroupWithApproval(body, approvalId);

        ArgumentCaptor<DataGroupByIdPutRequestBody> requestCaptor = ArgumentCaptor
            .forClass(DataGroupByIdPutRequestBody.class);
        verify(updateDataGroupApprovalHandler).handleRequest(any(SingleParameterHolder.class), requestCaptor.capture());

        DataGroupByIdPutRequestBody data = requestCaptor.getValue();

        assertEquals(body.getId(), data.getId());
        assertEquals(body.getName(), data.getName());
        assertEquals(body.getDescription(), data.getDescription());
        assertEquals(body.getItems(), data.getItems());
        assertEquals(body.getServiceAgreementId(), data.getServiceAgreementId());
        assertEquals(body.getType(), data.getType());
    }

    @Test
    public void testShouldThrowBadRequestExceptionWhenUpdateDataGroup() {
        when(updateDataGroupHandler
            .handleRequest(any(SingleParameterHolder.class), any(DataGroupByIdPutRequestBody.class)))
            .thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class,
            () -> dataGroupPAndPService.updateDataGroup(new DataGroupByIdPutRequestBody(), "1"));
    }

    @Test
    public void testShouldThrowNotFoundExceptionWhenUpdateDataGroup() {
        when(updateDataGroupHandler
            .handleRequest(any(SingleParameterHolder.class), any(DataGroupByIdPutRequestBody.class)))
            .thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class,
            () -> dataGroupPAndPService.updateDataGroup(new DataGroupByIdPutRequestBody(), "1"));
    }

    @Test
    public void testShouldThrowInternalServerErrorExceptionWhenUpdateDataGroup() {
        when(updateDataGroupHandler
            .handleRequest(any(SingleParameterHolder.class), any(DataGroupByIdPutRequestBody.class)))
            .thenThrow(InternalServerErrorException.class);

        assertThrows(InternalServerErrorException.class,
            () -> dataGroupPAndPService.updateDataGroup(new DataGroupByIdPutRequestBody(), "1"));
    }

    @Test
    public void deleteDataGroup() {
        String dgId = "dgId";
        when(permissionValidationService.getDataGroupById(eq(dgId)))
            .thenReturn(new DataGroupItemBase()
                .withId(dgId)
                .withServiceAgreementId("sa-id")
                .withName("dg name"));
        doNothing().when(permissionValidationService)
            .validateAccessToServiceAgreementResource("sa-id", AccessResourceType.USER_AND_ACCOUNT);

        mockGetDataGroupWithoutItems(dgId, "CONTACTS");
        dataGroupPAndPService.deleteDataGroup(dgId);

        verify(deleteDataGroupHandler, times(1)).handleRequest(any(SingleParameterHolder.class), eq(null));
    }

    @Test
    public void deleteDataGroupShouldThrowNotFoundException() {
        String dgId = "dgId";
        when(permissionValidationService.getDataGroupById(eq(dgId)))
            .thenThrow(getNotFoundException("Data group does not exist", "error code"));
        mockGetDataGroupWithoutItems(dgId, "CONTACTS");

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
            () -> dataGroupPAndPService.deleteDataGroup(dgId));

        assertThat(notFoundException,
            is(new NotFoundErrorMatcher("Data group does not exist", "error code")));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenDataGroupTypeIsNotAllowed() {
        String dgId = "dgId";

        mockGetDataGroupWithoutItems(dgId, "CUSTOMERS");
        doThrow(getBadRequestException(AccessGroupErrorCodes.ERR_AG_103.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_103.getErrorCode()))
            .when(validationConfig).validateIfDataGroupTypeIsAllowed("CUSTOMERS");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> dataGroupPAndPService.deleteDataGroup(dgId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_103.getErrorMessage(), ERR_AG_103.getErrorCode())));
    }

    @Test
    public void deleteDataGroupShouldThrowForbiddenException() {
        String dgId = "dgId";
        when(permissionValidationService.getDataGroupById(eq(dgId)))
            .thenReturn(new DataGroupItemBase()
                .withId(dgId)
                .withServiceAgreementId("sa-id")
                .withName("dg name"));

        doThrow(getForbiddenException(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode()))
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource("sa-id", AccessResourceType.USER_AND_ACCOUNT);
        mockGetDataGroupWithoutItems(dgId, "CONTACTS");

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> dataGroupPAndPService.deleteDataGroup(dgId));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode())));
    }

    @Test
    public void deleteDataGroupWithApprovalOn() {
        String approvalId = "approvalId";
        String dgId = "dgId";
        when(permissionValidationService.getDataGroupById(eq(dgId)))
            .thenReturn(new DataGroupItemBase()
                .withId(dgId)
                .withServiceAgreementId("sa-id")
                .withName("dg name"));
        doNothing().when(permissionValidationService)
            .validateAccessToServiceAgreementResource("sa-id", AccessResourceType.USER_AND_ACCOUNT);
        mockGetDataGroupWithoutItems(dgId, "CONTACTS");
        dataGroupPAndPService.deleteDataGroupWithApproval(dgId, approvalId);

        verify(deleteDataGroupApprovalHandler, times(1))
            .handleRequest(any(SingleParameterHolder.class), eq(new ApprovalDto(approvalId, null)));
    }

    @Test
    public void shouldThrowBadRequestExceptionDeleteDataGroupTypeIsNotAllowedWithApprovalOn() {
        String approvalId = "approvalId";
        String dgId = "dgId";

        mockGetDataGroupWithoutItems(dgId, "CUSTOMERS");
        doThrow(getBadRequestException(AccessGroupErrorCodes.ERR_AG_103.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_103.getErrorCode()))
            .when(validationConfig).validateIfDataGroupTypeIsAllowed("CUSTOMERS");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> dataGroupPAndPService.deleteDataGroupWithApproval(dgId, approvalId));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_103.getErrorMessage(), ERR_AG_103.getErrorCode())));
    }

    @Test
    public void deleteDataGroupWithApprovalOnShouldThrowNotFoundException() {
        String dgId = "dgId";
        String approvalId = "approvalId";
        when(permissionValidationService.getDataGroupById(eq(dgId)))
            .thenThrow(getNotFoundException("Data group does not exist", "error code"));
        mockGetDataGroupWithoutItems(dgId, "CONTACTS");

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
            () -> dataGroupPAndPService.deleteDataGroupWithApproval(dgId, approvalId));

        assertThat(notFoundException,
            is(new NotFoundErrorMatcher("Data group does not exist", "error code")));
    }

    @Test
    public void deleteDataGroupWithApprovalOnShouldThrowForbiddenException() {
        String dgId = "dgId";
        String approvalId = "approvalId";
        when(permissionValidationService.getDataGroupById(eq(dgId)))
            .thenReturn(new DataGroupItemBase()
                .withId(dgId)
                .withServiceAgreementId("sa-id")
                .withName("dg name"));

        doThrow(getForbiddenException(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode()))
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource("sa-id", AccessResourceType.USER_AND_ACCOUNT);
        mockGetDataGroupWithoutItems(dgId, "CONTACTS");

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> dataGroupPAndPService.deleteDataGroupWithApproval(dgId, approvalId));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode())));
    }

    @Test
    public void shouldCreateDataGroupWithApproval() {
        String id = "id";
        DataGroupBase body = new DataGroupBase()
            .withName("someName")
            .withDescription("some Desc")
            .withType("type")
            .withServiceAgreementId("saId");
        String approvalId = "approvalId";

        DataGroupsPostResponseBody dataGroupPostResponseBody =
            new DataGroupsPostResponseBody()
                .withId(id);

        when(addDataGroupApprovalHandler.handleRequest(any(SingleParameterHolder.class), any(DataGroupBase.class)))
            .thenReturn(dataGroupPostResponseBody);

        dataGroupPAndPService
            .createDataGroupWithApproval(body, approvalId);

        verify(addDataGroupApprovalHandler).handleRequest(any(SingleParameterHolder.class), any(DataGroupBase.class));
    }

    private void mockGetDataGroupWithoutItems(String dgId, String dgType) {
        DataGroup dataGroup = new DataGroup()
            .withDataItemType(dgType);
        when(dataGroupService.getById(eq(dgId))).thenReturn(dataGroup);
    }
}