package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getUuid;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.mappers.DataGroupItemMapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.DataGroupItemToDataGroupByIdPutRequestBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.DataGroupItemToDataGroupItemMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.DataGroupsGetResponseBodyToDataGroupItemMapper;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.DataGroupService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class DataGroupClientControllerTest extends ValidatorTestSetup {

    private static final String URL = "/client-api/v2/accessgroups/data-groups";
    private static final String GET_ALL_DATA_GROUPS_URL = "/client-api/v2/accessgroups/data-groups?serviceAgreementId=sId&type=";
    private static final String GET_DATA_GROUP_BY_ID_URL = "/client-api/v2/accessgroups/data-groups/id";

    @InjectMocks
    private DataGroupClientController dataGroupClientController;
    @Mock
    private DataGroupService dataGroupService;
    private MockMvc mockMvc;
    @Spy
    private ObjectMapper objectMapper;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private PermissionValidationService permissionValidationService;
    @Mock
    private ValidationConfig validationConfig;
    @Mock
    private DataGroupItemMapper dataGroupItemMapper;
    private ForbiddenException forbiddenException = getForbiddenException(
        AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
        AccessGroupErrorCodes.ERR_AG_032.getErrorCode());

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(DataGroupItemToDataGroupItemMapper.class)),
            spy(Mappers.getMapper(DataGroupsGetResponseBodyToDataGroupItemMapper.class)),
            spy(Mappers.getMapper(DataGroupItemToDataGroupByIdPutRequestBodyConverter.class))
        ));

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(dataGroupClientController)
            .setValidator(getLocalValidatorFactoryBean())
            .setHandlerExceptionResolvers()
            .build();
    }

    @Test
    public void shouldSaveDataGroupWithoutApproval() throws Exception {
        String serviceAgreementId = getUuid();

        DataGroupBase dataAccessGroupsPostRequestBody = new DataGroupBase()
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("CONTACTS")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(asList("000001", "000002", "000003"));
        DataGroupOperationResponse data = new DataGroupOperationResponse()
            .withId("id")
            .withApprovalOn(false);
        when(dataGroupService.addDataGroup(eq(dataAccessGroupsPostRequestBody))).thenReturn(data);
        doNothing().when(validationConfig).validateDataGroupType("CONTACTS");

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        ServiceAgreementItem serviceAgreementGetResponseBody = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity("le-001")
            .withIsMaster(true);

        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(serviceAgreementId)))
            .thenReturn(serviceAgreementGetResponseBody);

        when(
            dataGroupItemMapper
                .convertFromBase(any(com.backbase.accesscontrol.client.rest.spec.model.DataGroupItemBase.class)))
            .thenReturn(dataAccessGroupsPostRequestBody);

        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataAccessGroupsPostRequestBody)))
            .andExpect(status().isCreated());

        verify(dataGroupService, times(1)).addDataGroup(eq(dataAccessGroupsPostRequestBody));
        verifyNoMoreInteractions(dataGroupService);
    }

    @Test
    public void shouldThrowExceptionOnSaveDataGroupWithInvalidType() throws Exception {
        String serviceAgreementId = getUuid();

        DataGroupBase dataAccessGroupsPostRequestBody = new DataGroupBase()
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("INVALID")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("000001", "000002", "000003"));

        doThrow(getBadRequestException(AccessGroupErrorCodes.ERR_AG_001.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_001.getErrorCode()))
            .when(validationConfig).validateDataGroupType("INVALID");
        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataAccessGroupsPostRequestBody)))
            .andExpect(status().isBadRequest())
            .andDo(mvcResult -> {
                BadRequestException exception = (BadRequestException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_001.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_001.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowExceptionOnSaveDataGroupWithNotAllowedType() throws Exception {
        String serviceAgreementId = getUuid();

        DataGroupBase dataAccessGroupsPostRequestBody = new DataGroupBase()
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("CUSTOMERS")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("000001", "000002", "000003"));

        doThrow(getBadRequestException(AccessGroupErrorCodes.ERR_AG_103.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_103.getErrorCode()))
            .when(validationConfig).validateDataGroupType("CUSTOMERS");
        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataAccessGroupsPostRequestBody)))
            .andExpect(status().isBadRequest())
            .andDo(mvcResult -> {
                BadRequestException exception = (BadRequestException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_103.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_103.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowExceptionOnSaveDataGroup() throws Exception {
        String serviceAgreementId = getUuid();

        DataGroupBase dataAccessGroupsPostRequestBody = new DataGroupBase()
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("CONTACTS")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("000001", "000002", "000003"));

        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataAccessGroupsPostRequestBody)))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });

    }

    @Test
    public void shouldGetDataGroupById() throws Exception {
        String dataGroupId = "id";
        String serviceAgreementId = getUuid();

        DataGroupByIdGetResponseBody responseBody = new DataGroupByIdGetResponseBody()
            .withId(dataGroupId)
            .withServiceAgreementId(serviceAgreementId)
            .withDescription("desc")
            .withName("dg")
            .withType("ARRANGEMENTS")
            .withItems(new ArrayList<>());

        when(dataGroupService.getDataGroupById(eq(dataGroupId))).thenReturn(responseBody);

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(GET_DATA_GROUP_BY_ID_URL)
            .contentType(MediaType.APPLICATION_JSON));

        verify(dataGroupService, times(1)).getDataGroupById(eq(dataGroupId));
        verifyNoMoreInteractions(dataGroupService);
    }

    @Test
    public void shouldRetrieveAllDataGroups() throws Exception {
        List<DataGroupsGetResponseBody> dataGroupsGetResponseBodyList = singletonList(
            new DataGroupsGetResponseBody().withId("id"));
        String serviceAgreementId = getUuid();

        when(dataGroupService.getDataGroups(anyString(), anyString(), anyBoolean()))
            .thenReturn(dataGroupsGetResponseBodyList);
        doNothing().when(validationConfig).validateDataGroupTypeWhenProvided("ARRANGEMENTS");

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(GET_ALL_DATA_GROUPS_URL + "ARRANGEMENTS" + "&includeItems=false"))
            .andExpect(status().isOk());

        verify(dataGroupService, times(1)).getDataGroups(anyString(), anyString(), eq(false));
        verifyNoMoreInteractions(dataGroupService);
    }

    @Test
    public void shouldRetrieveAllDataGroupsWhenIncludeItemsIsNotProvided() throws Exception {
        List<DataGroupsGetResponseBody> dataGroupsGetResponseBodyList = singletonList(
            new DataGroupsGetResponseBody().withId("id"));
        String serviceAgreementId = getUuid();

        when(dataGroupService.getDataGroups(anyString(), anyString(), anyBoolean()))
            .thenReturn(dataGroupsGetResponseBodyList);
        doNothing().when(validationConfig).validateDataGroupType("ARRANGEMENTS");

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(GET_ALL_DATA_GROUPS_URL + "ARRANGEMENTS"))
            .andExpect(status().isOk());

        verify(dataGroupService, times(1)).getDataGroups(anyString(), anyString(), eq(true));
        verifyNoMoreInteractions(dataGroupService);
    }

    @Test
    public void shouldThrowExceptionOnRetrieveAllDataGroupsByInvalidType() throws Exception {
        String serviceAgreementId = getUuid();

        doThrow(getBadRequestException(
            AccessGroupErrorCodes.ERR_AG_001.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_001.getErrorCode()))
            .when(validationConfig).validateDataGroupTypeWhenProvided("INVALID");

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(GET_ALL_DATA_GROUPS_URL + "INVALID"))
            .andExpect(status().isBadRequest())
            .andDo(mvcResult -> {
                BadRequestException exception = (BadRequestException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_001.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_001.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowErrorOnGetAllDataGroupsForServiceAgreementId() throws Exception {
        String serviceAgreementId = getUuid();

        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(
            get(URL + "?serviceAgreementId=" + serviceAgreementId + "&type=ARRANGEMENTS"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldUpdateDataGroup() throws Exception {
        String serviceAgreementId = getUuid();

        DataGroupByIdPutRequestBody dataGroupPutRequestBody = new DataGroupByIdPutRequestBody()
            .withId(serviceAgreementId)
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("ARRANGEMENTS")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("000001", "000002", "000003"));

        DataGroupOperationResponse body = new DataGroupOperationResponse()
            .withId(getUuid())
            .withApprovalOn(false);

        ServiceAgreementItem serviceAgreementGetResponseBody = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withCreatorLegalEntity(getUuid())
            .withIsMaster(true);

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        doNothing().when(validationConfig).validateDataGroupType("ARRANGEMENTS");

        when(dataGroupService.updateDataGroup(any(DataGroupByIdPutRequestBody.class), any())).thenReturn(body);

        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(serviceAgreementId)))
            .thenReturn(serviceAgreementGetResponseBody);

        mockMvc.perform(put(URL + "/" + serviceAgreementId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataGroupPutRequestBody)))
            .andExpect(status().isOk());

        verify(dataGroupService, times(1))
            .updateDataGroup(any(DataGroupByIdPutRequestBody.class), eq(serviceAgreementId));
        verifyNoMoreInteractions(dataGroupService);
    }

    @Test
    public void shouldThrowExceptionOnUpdateDataGroupWithInvalidType() throws Exception {
        String serviceAgreementId = getUuid();

        DataGroupByIdPutRequestBody dataGroupPutRequestBody = new DataGroupByIdPutRequestBody()
            .withId(serviceAgreementId)
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("INVALID")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("000001", "000002", "000003"));

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        doThrow(getBadRequestException(
            AccessGroupErrorCodes.ERR_AG_001.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_001.getErrorCode()))
            .when(validationConfig).validateDataGroupType("INVALID");

        mockMvc.perform(put(URL + "/" + serviceAgreementId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataGroupPutRequestBody)))
            .andExpect(status().isBadRequest())
            .andDo(mvcResult -> {
                BadRequestException exception = (BadRequestException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_001.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_001.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowExceptionOnUpdateDataGroupWithNotAllowedType() throws Exception {
        String serviceAgreementId = getUuid();

        DataGroupByIdPutRequestBody dataGroupPutRequestBody = new DataGroupByIdPutRequestBody()
            .withId(serviceAgreementId)
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("CUSTOMERS")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("000001", "000002", "000003"));

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        doThrow(getBadRequestException(
            AccessGroupErrorCodes.ERR_AG_102.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_102.getErrorCode()))
            .when(validationConfig).validateDataGroupType("CUSTOMERS");

        mockMvc.perform(put(URL + "/" + serviceAgreementId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataGroupPutRequestBody)))
            .andExpect(status().isBadRequest())
            .andDo(mvcResult -> {
                BadRequestException exception = (BadRequestException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_102.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_102.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowExceptionOnUpdateDataGroupByIdUnderServiceAgreement() throws Exception {
        String serviceAgreementId = getUuid();
        String dataGroupId = getUuid();

        DataGroupByIdPutRequestBody dataGroupPutRequestBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroupId)
            .withName("Account group example")
            .withDescription("Simple account group")
            .withType("ARRANGEMENTS")
            .withServiceAgreementId(serviceAgreementId)
            .withItems(Arrays.asList("000001", "000002", "000003"));

        doThrow(forbiddenException)
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        mockMvc.perform(put(URL + "/" + dataGroupId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dataGroupPutRequestBody)))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                    exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldDeleteDataGroupByIdWithoutApproval() throws Exception {
        String dataGroupId = getUuid();
        String serviceAgreementId = getUuid();

        DataGroupItemBase dataGroup = new DataGroupItemBase()
            .withId(dataGroupId)
            .withServiceAgreementId(serviceAgreementId);

        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId)
            .withIsMaster(false);

        DataGroupOperationResponse data = new DataGroupOperationResponse()
            .withId(dataGroupId)
            .withApprovalOn(false);

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.ACCOUNT));

        when(dataGroupService.deleteDataGroup(anyString())).thenReturn(data);
        when(permissionValidationService.getDataGroupById(eq(dataGroupId)))
            .thenReturn(dataGroup);
        when(persistenceServiceAgreementService.getServiceAgreementResponseBodyById(eq(serviceAgreementId)))
            .thenReturn(serviceAgreement);

        mockMvc.perform(delete(URL + "/123")
            .contentType(MediaType.APPLICATION_JSON));

        verify(dataGroupService, times(1)).deleteDataGroup(anyString());
    }
}