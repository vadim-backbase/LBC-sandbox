package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getUuid;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.FunctionGroupByIdGetResponseBodyToFunctionGroupItemConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.FunctionGroupItemBaseToFunctionGroupBaseMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.FunctionGroupItemPutToFunctionGroupByIdPutRequestBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.FunctionGroupsGetResponseBodyToFunctionGroupItemConverter;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.FunctionGroupsService;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@WebAppConfiguration
public class FunctionGroupsControllerTest extends ValidatorTestSetup {

    private static final String functionGroupUrl = "/client-api/v2/accessgroups/function-groups";

    @Mock
    private PermissionValidationService permissionValidationService;
    @Mock
    private FunctionGroupsService functionGroupsService;
    @Mock
    private ApprovalOnRequestScope approvalOnRequestScope;
    @InjectMocks
    private FunctionGroupsController functionGroupsController;

    private ForbiddenException forbiddenException = getForbiddenException(
        AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
        AccessGroupErrorCodes.ERR_AG_032.getErrorCode());

    private MockMvc mockMvc;

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(FunctionGroupByIdGetResponseBodyToFunctionGroupItemConverter.class)),
            spy(Mappers.getMapper(FunctionGroupsGetResponseBodyToFunctionGroupItemConverter.class)),
            spy(Mappers.getMapper(FunctionGroupItemBaseToFunctionGroupBaseMapper.class)),
            spy(Mappers.getMapper(FunctionGroupItemPutToFunctionGroupByIdPutRequestBodyConverter.class))
        ));

    @Spy
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(functionGroupsController)
            .setValidator(getLocalValidatorFactoryBean())
            .build();
    }

    @Test
    public void shouldSaveFunctionGroupUnderMasterServiceAgreement() throws Exception {
        String serviceAgreementId = getUuid();
        String legalEntityId = getUuid();

        InternalRequest<ServiceAgreementItem> internalRequest = new InternalRequest<>();
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withIsMaster(true)
            .withCreatorLegalEntity(legalEntityId);
        internalRequest.setData(serviceAgreement);

        when(functionGroupsService.addFunctionGroup(any(FunctionGroupBase.class)))
            .thenReturn(new FunctionGroupsPostResponseBody());

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        FunctionGroupBase functionGroupsPostRequestBody = new FunctionGroupBase()
            .withName("Verifier")
            .withDescription("Verifies payments")
            .withServiceAgreementId(serviceAgreementId);

        mockMvc.perform(post(functionGroupUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(functionGroupsPostRequestBody)));

        verify(functionGroupsService, times(1)).addFunctionGroup(any(FunctionGroupBase.class));
        verifyNoMoreInteractions(functionGroupsService);
    }

    @Test
    public void shouldThrowExceptionOnSaveFunctionGroupUnderMasterServiceAgreement() throws Exception {
        String serviceAgreementId = getUuid();

        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        FunctionGroupBase functionGroupsPostRequestBody = new FunctionGroupBase()
            .withName("Verifier")
            .withDescription("Verifies payments")
            .withServiceAgreementId(serviceAgreementId);

        mockMvc.perform(post(functionGroupUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(functionGroupsPostRequestBody)))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldGetAllFunctionGroupsForServiceAgreementId() throws Exception {
        String serviceAgreementId = getUuid();

        when(functionGroupsService.getAllFunctionGroup(anyString())).thenReturn(singletonList(
            new FunctionGroupsGetResponseBody().withServiceAgreementId(serviceAgreementId)));

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(functionGroupUrl + "?serviceAgreementId=001"))
            .andExpect(status()
                .isOk());

        verify(functionGroupsService, times(1)).getAllFunctionGroup(anyString());
        verifyNoMoreInteractions(functionGroupsService);
    }

    @Test
    public void shouldThrowErrorOnGetAllFunctionGroupsForServiceAgreementId() throws Exception {
        String serviceAgreementId = getUuid();

        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(functionGroupUrl + "?serviceAgreementId=" + serviceAgreementId))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldGetFunctionAccessGroupById() throws Exception {
        String serviceAgreementId = getUuid();

        FunctionGroupByIdGetResponseBody functionGroup = new FunctionGroupByIdGetResponseBody()
            .withServiceAgreementId(serviceAgreementId);

        when(functionGroupsService.getFunctionGroupById(anyString()))
            .thenReturn(functionGroup);
        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(functionGroupUrl + "/001"))
            .andExpect(status()
                .isOk());

        verify(functionGroupsService, times(1)).getFunctionGroupById(anyString());
        verifyNoMoreInteractions(functionGroupsService);
    }

    @Test
    public void shouldThrowExceptionOnGetFunctionGroupById() throws Exception {
        String serviceAgreementId = getUuid();

        FunctionGroupByIdGetResponseBody functionGroup = new FunctionGroupByIdGetResponseBody()
            .withServiceAgreementId(serviceAgreementId);

        when(functionGroupsService.getFunctionGroupById(anyString()))
            .thenReturn(functionGroup);
        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(functionGroupUrl + "/001"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }
    
    @Test
    public void shouldGetTemplateFunctionGroupByIdWithoutAccessCheck() throws Exception {
        String serviceAgreementId = getUuid();
        FunctionGroupByIdGetResponseBody functionGroup = new FunctionGroupByIdGetResponseBody()
            .withServiceAgreementId(serviceAgreementId).withType(Type.TEMPLATE);
        when(functionGroupsService.getFunctionGroupById(anyString()))
            .thenReturn(functionGroup);
        mockMvc.perform(get(functionGroupUrl + "/001"))
            .andExpect(status()
                .isOk());
        verify(permissionValidationService, times(0)).validateAccessToServiceAgreementResource(eq(serviceAgreementId),
            eq(AccessResourceType.USER_OR_ACCOUNT));
        verify(functionGroupsService, times(1)).getFunctionGroupById(anyString());
        verifyNoMoreInteractions(functionGroupsService);
    }

    @Test
    public void shouldDeleteFunctionGroupById() throws Exception {
        String functionGroupId = "123";
        String serviceAgreementId = getUuid();

        InternalRequest<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody> internalRequest
            = new InternalRequest<>();
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody functionGroup =
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody()
                .withId(functionGroupId)
                .withServiceAgreementId(serviceAgreementId);
        internalRequest.setData(functionGroup);

        InternalRequest<ServiceAgreementItem> internalRequestSA = new InternalRequest<>();
        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withIsMaster(false);
        internalRequestSA.setData(serviceAgreement);

        doNothing().when(functionGroupsService).deleteFunctionGroup(anyString());
        when(permissionValidationService.getFunctionGroupById(eq(functionGroupId)))
            .thenReturn(functionGroup);
        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        mockMvc.perform(
            delete(functionGroupUrl + "/" + functionGroupId)
                .contentType(MediaType.APPLICATION_JSON));

        verify(functionGroupsService, times(1)).deleteFunctionGroup(anyString());
        verifyNoMoreInteractions(functionGroupsService);
    }

    @Test
    public void shouldThrowExceptionOnDeleteFunctionGroupById() throws Exception {
        String functionGroupId = "123";
        String serviceAgreementId = getUuid();

        InternalRequest<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody> internalRequest
            = new InternalRequest<>();
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody functionGroup =
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody()
                .withId(functionGroupId)
                .withServiceAgreementId(serviceAgreementId);
        internalRequest.setData(functionGroup);

        doNothing().when(functionGroupsService).deleteFunctionGroup(anyString());

        when(permissionValidationService.getFunctionGroupById(eq(functionGroupId)))
            .thenReturn(functionGroup);
        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        mockMvc.perform(delete(functionGroupUrl + "/123")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldUpdateFunctionGroupByIdUnderServiceAgreement() throws Exception {
        String id = getUuid();
        String serviceAgreementId = getUuid();

        FunctionGroupByIdPutRequestBody putBody = new FunctionGroupByIdPutRequestBody()
            .withName("FG-name")
            .withDescription("description")
            .withServiceAgreementId(serviceAgreementId)
            .withPermissions(new ArrayList<>());

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        when(functionGroupsService.addFunctionGroup(any(FunctionGroupBase.class)))
            .thenReturn(new FunctionGroupsPostResponseBody());

        mockMvc.perform(put(functionGroupUrl + "/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(putBody)));

        verify(functionGroupsService, times(1)).updateFunctionGroup(eq(id), any(FunctionGroupByIdPutRequestBody.class));
        verifyNoMoreInteractions(functionGroupsService);
    }

    @Test
    public void shouldThrowExceptionOnUpdateFunctionGroupByIdUnderServiceAgreement() throws Exception {
        String id = getUuid();
        String serviceAgreementId = getUuid();

        FunctionGroupByIdPutRequestBody putBody = new FunctionGroupByIdPutRequestBody()
            .withName("FG-name")
            .withDescription("description")
            .withServiceAgreementId(serviceAgreementId)
            .withPermissions(new ArrayList<>());

        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        when(functionGroupsService.addFunctionGroup(any(FunctionGroupBase.class)))
            .thenReturn(new FunctionGroupsPostResponseBody());

        mockMvc.perform(put(functionGroupUrl + "/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(putBody)))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }
}
