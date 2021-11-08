package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.matchers.PresentationServiceAgreementMatcher.getPresentationServiceAgreementMatcher;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_063;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.ENABLED;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.FunctionsGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationApprovalPermissionsMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationFunctionDataGroupItemsMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.PresentationServiceAgreementConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreementParticipantsGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreementSaveConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreementUsersGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UnexposedUsersGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UsersForServiceAgreementToPresentationUsersForServiceAgreementRequestBodyConverter;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementFlowService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@WebAppConfiguration
public class ServiceAgreementsControllerTest extends ValidatorTestSetup {

    public static final String SERVICE_AGREEMENT_ID = "sa-id";
    private static final String USER_ID = "u-id";
    private static final String serviceAgreementUrl = "/client-api/v2/accessgroups/service-agreements/";

    @Mock
    private ServiceAgreementService serviceAgreementService;
    @Mock
    private PermissionValidationService permissionValidationService;
    @Mock
    private ApprovalOnRequestScope approvalOnRequestScope;
    @Mock
    private ParameterValidationService validationUtil;
    @Captor
    private ArgumentCaptor<ServiceAgreementSave> updateCaptor;
    @Captor
    private ArgumentCaptor<PresentationFunctionDataGroupItems> functionDataGroupItemsArgumentCaptor;

    private MockMvc mockMvc;

    @Spy
    private ObjectMapper objectMapper;
    @Mock
    private UserContextUtil userContextUtil;
    @Spy
    private PayloadConverter payloadConverter = new PayloadConverter(asList(
        spy(Mappers.getMapper(PresentationApprovalPermissionsMapper.class)),
        spy(Mappers.getMapper(FunctionsGetResponseBodyConverter.class)),
        spy(Mappers.getMapper(ServiceAgreementParticipantsGetResponseBodyConverter.class)),
        spy(Mappers.getMapper(ServiceAgreementUsersGetResponseBodyConverter.class)),
        spy(Mappers.getMapper(PresentationServiceAgreementConverter.class)),
        spy(Mappers.getMapper(UnexposedUsersGetResponseBodyConverter.class)),
        spy(Mappers.getMapper(UsersForServiceAgreementToPresentationUsersForServiceAgreementRequestBodyConverter.class)),
        spy(Mappers.getMapper(PresentationFunctionDataGroupItemsMapper.class)),
        spy(Mappers.getMapper(ServiceAgreementSaveConverter.class))
        ));

    @InjectMocks
    private ServiceAgreementsController serviceAgreementsController;
    @Mock
    private ServiceAgreementFlowService serviceAgreementFlowService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders
            .standaloneSetup(serviceAgreementsController)
            .setValidator(getLocalValidatorFactoryBean())
            .build();
    }

    @Test
    public void getAllServiceAgreementParticipantsContext() throws Exception {
        String serviceAgreementId = "SA-01";
        when(
            serviceAgreementService.getServiceAgreementParticipants(eq(serviceAgreementId)))
            .thenReturn(singletonList(new ServiceAgreementParticipantsGetResponseBody()));

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        mockMvc.perform(
            get(serviceAgreementUrl + "usercontext/participants"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1))
            .getServiceAgreementParticipants(eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldGetBusinessFunctionForServiceAgreement() throws Exception {
        when(serviceAgreementFlowService.getBusinessFunctionsForServiceAgreement(eq("saId")))
            .thenReturn(new ArrayList<>());

        mockMvc.perform(get(serviceAgreementUrl + "saId/business-functions"))
            .andExpect(status()
                .isOk());

        verify(serviceAgreementFlowService, times(1)).getBusinessFunctionsForServiceAgreement(eq("saId"));
        verifyNoMoreInteractions(serviceAgreementFlowService);
    }

    @Test
    public void getAllServiceAgreementParticipants() throws Exception {
        String id = "SA-01";
        when(serviceAgreementService.getServiceAgreementParticipants(eq(id)))
            .thenReturn(singletonList(new ServiceAgreementParticipantsGetResponseBody()));

        mockMvc.perform(
            get(serviceAgreementUrl + id + "/participants"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1)).getServiceAgreementParticipants(eq(id));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldForwardRequestForUpdatingServiceAgreement() throws Exception {
        String serviceAgreementId = "SA-01";
        ServiceAgreementSave serviceAgreementSave = new ServiceAgreementSave()
            .withName("new name")
            .withExternalId("external-id")
            .withStatus(ENABLED)
            .withDescription("new Description")
            .withParticipants(new HashSet<>(singletonList(new Participant()
                .withId("f9400fb7a3b3ca4cafb8136b06baccfe")
                .withSharingUsers(true)
                .withSharingAccounts(true))));
        doNothing().when(serviceAgreementService)
            .editServiceAgreement(eq(serviceAgreementSave), eq(serviceAgreementId));
        when(approvalOnRequestScope.isApproval()).thenReturn(false);

        mockMvc.perform(put(serviceAgreementUrl + serviceAgreementId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(serviceAgreementSave)));

        verify(serviceAgreementService, times(1)).editServiceAgreement(updateCaptor.capture(), eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
        assertEquals(serviceAgreementSave, updateCaptor.getValue());
    }

    @Test
    public void shouldFailWhenUserDoesNotHaveAccessToServiceAgreementWhenUpdatingServiceAgreement() throws Exception {
        String serviceAgreementId = "SA-01";

        doThrow(new ForbiddenException().withMessage(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage()))
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.NONE));

        ServiceAgreementSave serviceAgreementSave = new ServiceAgreementSave()
            .withName("new name")
            .withExternalId("external-id")
            .withStatus(ENABLED)
            .withDescription("new Description")
            .withParticipants(new HashSet<>(singletonList(new Participant()
                .withId("f9400fb7a3b3ca4cafb8136b06baccfe")
                .withSharingUsers(true)
                .withSharingAccounts(true))));
        doNothing().when(serviceAgreementService)
            .editServiceAgreement(eq(serviceAgreementSave), eq(serviceAgreementId));

        mockMvc.perform(put(serviceAgreementUrl + serviceAgreementId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(serviceAgreementSave)))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                mvcResult.getResolvedException().getMessage()));

        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldGetAllUsersForServiceAgreement() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";
        Long totalElements = 1L;
        String query = null;

        ServiceAgreementUsersGetResponseBody mockResponse = new ServiceAgreementUsersGetResponseBody();
        List<ServiceAgreementUsersGetResponseBody> listOfUsers = singletonList(mockResponse);
        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = new ListElementsWrapper(
            listOfUsers, totalElements);

        mockValidateQueryParameter(query);
        when(serviceAgreementService.getUsersForServiceAgreement(eq(serviceAgreementId),
            eq(null), eq(0), eq(10), eq("")))
            .thenReturn(usersOfServiceAgreementDto);

        String contentAsString = mockMvc
            .perform(get(serviceAgreementUrl + serviceAgreementId + "/users"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<ServiceAgreementUsersGetResponseBody> actualResponse = objectMapper.readValue(
            contentAsString,
            new TypeReference<List<ServiceAgreementUsersGetResponseBody>>() {
            });

        verify(serviceAgreementService, times(1))
            .getUsersForServiceAgreement(eq(serviceAgreementId), eq(null), eq(0), eq(10), eq(""));
        verifyNoMoreInteractions(serviceAgreementService);
        assertEquals(listOfUsers.size(), actualResponse.size());
    }

    @Test
    public void shouldRemoveServiceAgreementUsers() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";
        String userId = "U001";

        PresentationUsersForServiceAgreementRequestBody usersAddBody = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(singletonList(userId));

        doNothing().when(serviceAgreementService)
            .removeUsersFromServiceAgreement(eq(usersAddBody), eq(serviceAgreementId));

        mockMvc.perform(post(serviceAgreementUrl + serviceAgreementId + "/users/remove")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usersAddBody)))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1))
            .removeUsersFromServiceAgreement(eq(usersAddBody), eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldGetAllUnexposedUsersForServiceAgreement() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";
        Long totalElements = 100L;

        when(serviceAgreementService.getUnexposedUsers(eq(serviceAgreementId), eq(0), eq(10), eq(null), eq("")))
            .thenReturn(new PaginationDto<>(totalElements, new ArrayList<>()));

        mockMvc.perform(get(serviceAgreementUrl + serviceAgreementId + "/users/unexposed"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1))
            .getUnexposedUsers(eq(serviceAgreementId), eq(0), eq(10), eq(null), eq(""));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldAddServiceAgreementUser() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";
        String userId = "U001";

        PresentationUsersForServiceAgreementRequestBody usersAddBody = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(singletonList(userId));

        doNothing().when(serviceAgreementService).addUsersInServiceAgreement(eq(usersAddBody), eq(serviceAgreementId));

        mockMvc.perform(post(serviceAgreementUrl + serviceAgreementId + "/users/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usersAddBody)))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1))
            .addUsersInServiceAgreement(eq(usersAddBody), eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldGetServiceAgreementsWhenNoCreatorIdProvided() throws Exception {
        String creatorId = "003";
        String userId = "userId";
        String legalEntityId = "legalEntityId";
        String query = "";
        int from = 0;
        String cursor = "";
        int size = 10;

        mockValidateQueryParameter(query);
        mockGetUserContextDetails(userId, legalEntityId);
        mockValidateFromAndSize(from, size);
        mockValidateAccessToResource(creatorId);

        PresentationServiceAgreement presentationServiceAgreement = new PresentationServiceAgreement()
            .withId("SA-01")
            .withStatus(ENABLED)
            .withCreatorLegalEntity(creatorId)
            .withCreatorLegalEntityName("LE name")
            .withIsMaster(true)
            .withDescription("desc")
            .withExternalId("ex-id")
            .withNumberOfParticipants(BigDecimal.ONE);
        PaginationDto<PresentationServiceAgreement> data = new PaginationDto<>(3L,
            singletonList(presentationServiceAgreement));

   GetUser userGetResponseBody = new GetUser();
        userGetResponseBody.setId(userId);
        userGetResponseBody.setLegalEntityId(legalEntityId);

        when(serviceAgreementService
            .listServiceAgreements(eq(legalEntityId), eq(userId), eq(query), eq(from), eq(size), eq(cursor)))
            .thenReturn(data);

        MockHttpServletResponse httpServletResponse = mockMvc
            .perform(get(serviceAgreementUrl + "hierarchy")
                .param("userId", userId)
                .param("query", query)
                .param("from", "0")
                .param("cursor", cursor)
                .param("size", "10"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        List<PresentationServiceAgreement> returnedServiceAgreements = objectMapper
            .readValue(httpServletResponse.getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(String.valueOf(data.getTotalNumberOfRecords()), httpServletResponse.getHeader("X-Total-Count"));
        assertThat(returnedServiceAgreements, contains(
            getPresentationServiceAgreementMatcher(presentationServiceAgreement)
        ));
    }

    @Test
    public void shouldThrowBadRequestOnGetServiceAgreementsWhenNoCreatorIdProvidedAndUserIsLoggedUnderCustomServiceAgreement()
        throws Exception {
        String loggedUser = "U-01";
        String creatorId = "003";
        String query = "";
        String cursor = "";

        mockValidateQueryParameter(query);
        mockGetUserContextDetails(loggedUser, creatorId);
        doThrow(getForbiddenException(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
            AccessGroupErrorCodes.ERR_AG_032.getErrorCode()))
            .when(permissionValidationService).validateAccessToLegalEntityResource(creatorId, AccessResourceType.NONE);

        ResultActions resultActions = mockMvc
            .perform(get(serviceAgreementUrl + "hierarchy")
                .param("creatorId", creatorId)
                .param("query", query)
                .param("from", "0")
                .param("cursor", cursor)
                .param("size", "10"))
            .andExpect(status().isForbidden());

        resultActions.andDo((mvcResult -> {
            ForbiddenException resolvedException = (ForbiddenException) mvcResult.getResolvedException();
            assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                resolvedException.getErrors().get(0).getMessage());
            assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorCode(),
                resolvedException.getErrors().get(0).getKey());
        }));
    }

    @Test
    public void shouldGetServiceAgreementsWhenCreatorIdProvided() throws Exception {
        String creatorId = "003";
        String query = "";
        int from = 0;
        String cursor = "";
        int size = 10;
        String userId = "userId";
        String legalEntityId = "legalEntityId";

        mockValidateQueryParameter(query);
        mockValidateFromAndSize(from, size);
        mockValidateAccessToResource(creatorId);
        mockValidateAccessToResource(creatorId);

        PresentationServiceAgreement persistenceServiceAgreement = new PresentationServiceAgreement()
            .withId("SA-01")
            .withStatus(ENABLED)
            .withCreatorLegalEntity(creatorId)
            .withCreatorLegalEntityName("LE name")
            .withIsMaster(true)
            .withDescription("desc")
            .withExternalId("ex-id")
            .withNumberOfParticipants(BigDecimal.ONE);
        PaginationDto<PresentationServiceAgreement> data = new PaginationDto<>(3L,
            singletonList(persistenceServiceAgreement));

        when(serviceAgreementService
            .listServiceAgreements(eq(creatorId), eq(userId), eq(query), eq(from), eq(size), eq(cursor)))
            .thenReturn(data);

        GetUser userGetResponseBody = new GetUser();
        userGetResponseBody.setId(userId);
        userGetResponseBody.setLegalEntityId(legalEntityId);

        MockHttpServletResponse httpServletResponse = mockMvc
            .perform(get(serviceAgreementUrl + "hierarchy")
                .param("creatorId", creatorId)
                .param("userId", userId)
                .param("query", query)
                .param("from", "0")
                .param("cursor", cursor)
                .param("size", "10"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        List<PresentationServiceAgreement> returnedServiceAgreements = objectMapper
            .readValue(httpServletResponse.getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(String.valueOf(data.getTotalNumberOfRecords()), httpServletResponse.getHeader("X-Total-Count"));
        assertThat(returnedServiceAgreements, contains(
            getPresentationServiceAgreementMatcher(persistenceServiceAgreement)
        ));
    }

    @Test
    public void shouldThrowBatRequestOnGetServiceAgreementsWhenQueryNotValid() throws Exception {
        String creatorId = "003";
        String query = "";
        int from = 0;
        String cursor = "";
        int size = 10;

        mockValidateAccessToResource(creatorId);
        mockValidateQueryParameter(query);
        doThrow(getBadRequestException(ERR_AG_063.getErrorMessage(),
            ERR_AG_063.getErrorCode()))
            .when(validationUtil).validateFromAndSizeParameter(from, size);

        ResultActions resultActions = mockMvc
            .perform(get(serviceAgreementUrl + "hierarchy")
                .param("creatorId", creatorId)
                .param("query", query)
                .param("from", "0")
                .param("cursor", cursor)
                .param("size", "10"))
            .andExpect(status().isBadRequest());

        resultActions.andDo((mvcResult -> {
            BadRequestException resolvedException = (BadRequestException) mvcResult.getResolvedException();
            assertEquals(AccessGroupErrorCodes.ERR_AG_063.getErrorMessage(),
                resolvedException.getErrors().get(0).getMessage());
            assertEquals(AccessGroupErrorCodes.ERR_AG_063.getErrorCode(),
                resolvedException.getErrors().get(0).getKey());
        }));
    }

    @Test
    public void shouldThrowBatRequestOnGetServiceAgreementsWhenFromAndSizeNotValid() throws Exception {
        String creatorId = "003";
        String query = "";
        String cursor = "";

        mockValidateAccessToResource(creatorId);
        when(validationUtil.validateQueryParameter(eq(query)))
            .thenThrow(getBadRequestException("error",
                AccessGroupErrorCodes.ERR_AG_062.getErrorCode()));

        ResultActions resultActions = mockMvc
            .perform(get(serviceAgreementUrl + "hierarchy")
                .param("creatorId", creatorId)
                .param("query", query)
                .param("from", "0")
                .param("cursor", cursor)
                .param("size", "10"))
            .andExpect(status().isBadRequest());

        resultActions.andDo((mvcResult -> {
            BadRequestException resolvedException = (BadRequestException) mvcResult.getResolvedException();
            assertEquals("error", resolvedException.getErrors().get(0).getMessage());
            assertEquals(AccessGroupErrorCodes.ERR_AG_062.getErrorCode(),
                resolvedException.getErrors().get(0).getKey());
        }));
    }

    @Test
    public void shouldThrowBadRequestWhenUnableToAccessResource() throws Exception {
        doThrow(new BadRequestException()
            .withMessage("error"))
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource("sa-id", AccessResourceType.USER_AND_ACCOUNT);

        ResultActions resultActions = mockMvc
            .perform(put(serviceAgreementUrl + "{id}/users/{userId}/permissions", "sa-id", "u-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PresentationFunctionDataGroupItems()))
            )
            .andExpect(status().isBadRequest());

        resultActions
            .andDo((mvcResult -> {
                BadRequestException resolvedException = (BadRequestException) mvcResult.getResolvedException();
                assertEquals("error", resolvedException.getMessage());
            }));
    }

    @Test
    public void shouldInvokeService() throws Exception {
        String saId = "sa-id";
        String uId = "u-id";
        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(saId, AccessResourceType.USER_AND_ACCOUNT);

        PresentationFunctionDataGroupItems body = new PresentationFunctionDataGroupItems();

        when(serviceAgreementService.putAssignUsersPermissions(body, saId, uId))
            .thenReturn(new PresentationApprovalStatus().withApprovalStatus(ApprovalStatus.PENDING));

        mockMvc.perform(put(serviceAgreementUrl + "{id}/users/{userId}/permissions", saId, uId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));

        verify(permissionValidationService).validateAccessToServiceAgreementResource(saId, AccessResourceType.USER_AND_ACCOUNT);
        verify(serviceAgreementService)
            .putAssignUsersPermissions(functionDataGroupItemsArgumentCaptor.capture(), eq(saId), eq(uId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void getAssignUsersPermissions() throws Exception {

        doNothing()
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(SERVICE_AGREEMENT_ID, AccessResourceType.USER_AND_ACCOUNT);

        when(serviceAgreementService.getAssignedUsersPermissions(SERVICE_AGREEMENT_ID, USER_ID))
            .thenReturn(new PresentationApprovalPermissions().withApprovalId("5234523452454"));

        mockMvc
            .perform(get(serviceAgreementUrl + "{id}/users/{userId}/permissions", SERVICE_AGREEMENT_ID,
                USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        verify(permissionValidationService)
            .validateAccessToServiceAgreementResource(SERVICE_AGREEMENT_ID, AccessResourceType.USER_AND_ACCOUNT);
        verify(serviceAgreementService, times(1)).getAssignedUsersPermissions(SERVICE_AGREEMENT_ID, USER_ID);
        verifyNoMoreInteractions(serviceAgreementService);
    }

    private void mockValidateAccessToResource(String creatorId) {
        doNothing()
            .when(permissionValidationService).validateAccessToLegalEntityResource(creatorId, AccessResourceType.NONE);
    }

    private void mockValidateFromAndSize(int from, int size) {
        doNothing()
            .when(validationUtil).validateFromAndSizeParameter(from, size);
    }

    private void mockValidateQueryParameter(String query) {
        when(validationUtil.validateQueryParameter(eq(query))).thenReturn(query);
    }

    private void mockGetUserContextDetails(String userId, String leId) {
        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto(userId, leId));
    }

}