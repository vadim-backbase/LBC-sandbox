package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
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
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreementItemGetResponseBodyToServiceAgreementItemConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreementPostToPostRequestBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreementStatePutToServiceAgreementStatePutRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.ServiceAgreementUsersGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UnexposedUsersGetResponseBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UpdateAdminsToAdminsPutRequestBodyConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UsersForServiceAgreementToPresentationUsersForServiceAgreementRequestBodyConverter;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementItemToServiceAgreementItemMapper;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.LegalEntityAdmins;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementStatePutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
public class ServiceAgreementControllerTest extends ValidatorTestSetup {

    private static final String serviceAgreementUrl = "/client-api/v2/accessgroups/serviceagreements";
    @Mock
    private ServiceAgreementService serviceAgreementService;
    @Mock
    private ParameterValidationService validationService;
    @Mock
    private PermissionValidationService permissionValidationService;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private ApprovalOnRequestScope approvalOnRequestScope;
    @InjectMocks
    private ServiceAgreementController serviceAgreementController;
    @Captor
    private ArgumentCaptor<ServiceAgreementPostRequestBody> captor;
    @Captor
    private ArgumentCaptor<ServiceAgreementStatePutRequestBody> updateStateCaptor;
    @Spy
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    private String newEntry;
    private String newEntryAdmins;
    private Participant validParticipant1;
    private Participant validParticipant2;

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(UpdateAdminsToAdminsPutRequestBodyConverter.class)),
            spy(Mappers.getMapper(ServiceAgreementPostToPostRequestBodyConverter.class)),
            spy(Mappers.getMapper(ServiceAgreementItemGetResponseBodyToServiceAgreementItemConverter.class)),
            spy(Mappers.getMapper(ServiceAgreementItemToServiceAgreementItemMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementUsersGetResponseBodyConverter.class)),
            spy(Mappers.getMapper(ServiceAgreementStatePutToServiceAgreementStatePutRequestBodyMapper.class)),
            spy(Mappers.getMapper(UnexposedUsersGetResponseBodyConverter.class)),
            spy(Mappers.getMapper(
                UsersForServiceAgreementToPresentationUsersForServiceAgreementRequestBodyConverter.class))
        ));

    @Before
    public void setUp() throws Exception {
        validParticipant1 = new Participant().withId("f9400fb7a3b3ca4cafb8136b06baccfe").withSharingAccounts(true)
            .withSharingUsers(false);
        validParticipant2 = new Participant().withId("f9400fb7a3b3ca4cafb8136b06baccfa").withSharingAccounts(false)
            .withSharingUsers(true);

        mockMvc = MockMvcBuilders
            .standaloneSetup(serviceAgreementController)
            .setValidator(getLocalValidatorFactoryBean())
            .build();
        newEntry = objectMapper.writeValueAsString(new ServiceAgreementPostRequestBody()
            .withName("SA name")
            .withDescription("SA desc")
            .withParticipants(asList(validParticipant1, validParticipant2)));
        List<LegalEntityAdmins> participants = new ArrayList<>(
            asList(new LegalEntityAdmins().withId(validParticipant1.getId()),
                new LegalEntityAdmins().withId(validParticipant2.getId())));

        newEntryAdmins = objectMapper.writeValueAsString(new AdminsPutRequestBody()
            .withParticipants(participants));
    }

    @Test
    public void postServiceAgreement() throws Exception {
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = objectMapper
            .readValue(newEntry, ServiceAgreementPostRequestBody.class);
        when(serviceAgreementService.addServiceAgreement(eq(serviceAgreementPostRequestBody)))
            .thenReturn(new ServiceAgreementPostResponseBody());
        when(approvalOnRequestScope.isApproval()).thenReturn(false);

        mockMvc.perform(post(serviceAgreementUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(serviceAgreementPostRequestBody)));

        verify(serviceAgreementService, times(1)).addServiceAgreement(captor.capture());
        verifyNoMoreInteractions(serviceAgreementService);
        assertEquals(serviceAgreementPostRequestBody, captor.getValue());
    }

    @Test
    public void missingNameParameter() throws Exception {

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = new ServiceAgreementPostRequestBody()
            .withDescription("desc")
            .withParticipants(asList(validParticipant1, validParticipant2));

        assertBadRequest(objectMapper.writeValueAsString(serviceAgreementPostRequestBody));
    }

    @Test
    public void missingDescriptionParameter() throws Exception {

        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = new ServiceAgreementPostRequestBody()
            .withName("name")
            .withParticipants(asList(validParticipant1, validParticipant2));

        assertBadRequest(objectMapper.writeValueAsString(serviceAgreementPostRequestBody));
    }

    @Test
    public void emptyDescriptionParameter() throws Exception {
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = new ServiceAgreementPostRequestBody()
            .withName("name")
            .withParticipants(asList(validParticipant1, validParticipant2))
            .withDescription("");

        assertBadRequest(objectMapper.writeValueAsString(serviceAgreementPostRequestBody));
    }

    @Test
    public void emptyNameParameter() throws Exception {
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = new ServiceAgreementPostRequestBody()
            .withName("")
            .withParticipants(asList(validParticipant1, validParticipant2))
            .withDescription("desc");

        assertBadRequest(objectMapper.writeValueAsString(serviceAgreementPostRequestBody));
    }

    private void assertBadRequest(String stringRequestBody) throws Exception {
        mockMvc.perform(post(serviceAgreementUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .content(stringRequestBody))
            .andExpect(status().isBadRequest());

        verify(serviceAgreementService, times(0)).addServiceAgreement(captor.capture());
    }


    @Test
    public void getAllServiceAgreements() throws Exception {
        String creatorId = "002";
        PaginationDto<ServiceAgreementGetResponseBody> responseBody = new PaginationDto<>(0L, new ArrayList<>());

        when(serviceAgreementService
            .getServiceAgreements(eq(creatorId), eq("SA"), eq(0), eq(10), eq("")))
            .thenReturn(responseBody);

        String query = "SA";
        when(validationService.validateQueryParameter(eq(query))).thenReturn(query);

        String contentAsString = mockMvc.perform(
            get(serviceAgreementUrl)
                .param("creatorId", creatorId)
                .param("query", query)
                .param("from", "0")
                .param("size", "10")
                .param("cursor", ""))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<ServiceAgreementGetResponseBody> actualResponse = objectMapper.readValue(
            contentAsString,
            new TypeReference<>() {
            });

        verify(serviceAgreementService)
            .getServiceAgreements(eq(creatorId), eq("SA"), eq(0), eq(10), eq(""));
        verifyNoMoreInteractions(serviceAgreementService);
        assertEquals(responseBody.getRecords().size(), actualResponse.size());
    }

    @Test
    public void shouldFailWhenInsufficientPermissions() throws Exception {
        String creatorId = "002";

        doThrow(getForbiddenException(AccessGroupErrorCodes.ERR_AG_043.getErrorMessage(), null))
            .when(permissionValidationService)
            .validateAccessToLegalEntityResource(eq(creatorId), eq(AccessResourceType.NONE));
        mockMvc.perform(
            get(serviceAgreementUrl)
                .param("creatorId", creatorId))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException resolvedException = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(AccessGroupErrorCodes.ERR_AG_043.getErrorMessage(),
                    resolvedException.getErrors().get(0).getMessage());
            });
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldUpdateAdmins() throws Exception {
        String said = "001";
        AdminsPutRequestBody adminsPutRequestBody = objectMapper.readValue(newEntryAdmins, AdminsPutRequestBody.class);

        doNothing().when(serviceAgreementService).updateAdmins(eq(adminsPutRequestBody), eq(said));

        mockMvc.perform(put(serviceAgreementUrl + "/" + said + "/admins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adminsPutRequestBody)));

        verify(serviceAgreementService, times(1)).updateAdmins(eq(adminsPutRequestBody), eq(said));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldGetAllUsersForServiceAgreement() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        Long totalElements = 100L;
        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersOfServiceAgreementDto = new ListElementsWrapper<>(
            new ArrayList<>(), totalElements);

        when(serviceAgreementService
            .getUsersForServiceAgreement(eq(serviceAgreementId), eq(null), eq(null),
                eq(null), eq(null)))
            .thenReturn(usersOfServiceAgreementDto);

        mockMvc.perform(get(serviceAgreementUrl + "/context/users"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1))
            .getUsersForServiceAgreement(eq(serviceAgreementId), eq(null), eq(null), eq(null), eq(null));

        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldAddServiceAgreementUser() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";
        String userId = "U001";

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        PresentationUsersForServiceAgreementRequestBody usersAddBody = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(Collections.singletonList(userId));

        doNothing().when(serviceAgreementService).addUsersInServiceAgreement(eq(usersAddBody), eq(serviceAgreementId));
        mockMvc.perform(post(serviceAgreementUrl + "/context/users/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usersAddBody)))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1)).addUsersInServiceAgreement(eq(usersAddBody), eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldRemoveServiceAgreementUser() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";
        String userId = "U001";

        PresentationUsersForServiceAgreementRequestBody usersRemoveBody = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(Collections.singletonList(userId));

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        doNothing().when(serviceAgreementService)
            .removeUsersFromServiceAgreement(eq(usersRemoveBody), eq(serviceAgreementId));

        mockMvc.perform(post(serviceAgreementUrl + "/context/users/remove")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(usersRemoveBody)))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1))
            .removeUsersFromServiceAgreement(eq(usersRemoveBody), eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void getServiceAgreementById() throws Exception {
        String serviceAgreementId = "003";

        when(serviceAgreementService.getServiceAgreementById(eq(serviceAgreementId)))
            .thenReturn(new ServiceAgreementItemGetResponseBody());

        mockMvc.perform(
            get(serviceAgreementUrl + "/003"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1)).getServiceAgreementById(eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void getServiceAgreementByContext() throws Exception {
        String serviceAgreementId = "003";

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);

        when(serviceAgreementService.getServiceAgreementById(eq(serviceAgreementId)))
            .thenReturn(new ServiceAgreementItemGetResponseBody());

        mockMvc.perform(
            get(serviceAgreementUrl + "/context"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1)).getServiceAgreementById(eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void putServiceAgreementState() throws Exception {
        String serviceAgreementId = "SA-01";
        ServiceAgreementStatePutRequestBody serviceAgreementStatePutRequestBody = new ServiceAgreementStatePutRequestBody()
            .withState(Status.DISABLED);
        doNothing().when(serviceAgreementService)
            .updateServiceAgreementState(eq(serviceAgreementStatePutRequestBody), eq(serviceAgreementId));

        mockMvc.perform(put(serviceAgreementUrl + "/" + serviceAgreementId + "/state")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(serviceAgreementStatePutRequestBody)));

        verify(serviceAgreementService, times(1))
            .updateServiceAgreementState(updateStateCaptor.capture(), eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
        assertEquals(serviceAgreementStatePutRequestBody, updateStateCaptor.getValue());
    }

    @Test
    public void shouldGetAdminsForServiceAgreement() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";

        List<ServiceAgreementUsersGetResponseBody> listOfAdmins = new ArrayList<>();
        ServiceAgreementUsersGetResponseBody admin = new ServiceAgreementUsersGetResponseBody();
        listOfAdmins.add(admin);

        when(serviceAgreementService.getServiceAgreementAdmins(eq(serviceAgreementId))).thenReturn(listOfAdmins);

        mockMvc.perform(get(serviceAgreementUrl + "/" + serviceAgreementId + "/admins"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1)).getServiceAgreementAdmins(eq(serviceAgreementId));
        verifyNoMoreInteractions(serviceAgreementService);
    }

    @Test
    public void shouldFailWhenUserHasNoAccessToServiceAgreement() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";

        doThrow(new ForbiddenException().withMessage(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage()))
            .when(permissionValidationService)
            .validateAccessToServiceAgreementResource(eq(serviceAgreementId), eq(AccessResourceType.USER_OR_ACCOUNT));

        mockMvc.perform(get(serviceAgreementUrl + "/" + serviceAgreementId + "/admins"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> assertEquals(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                mvcResult.getResolvedException().getMessage()));
    }

    @Test
    public void shouldThrowForbiddenExceptionIfNoContext() throws Exception {
        when(userContextUtil.getServiceAgreementId()).thenThrow(new ForbiddenException());

        mockMvc.perform(
            get(serviceAgreementUrl + "/context/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldGetAllUnexposedUsersForServiceAgreement() throws Exception {
        String serviceAgreementId = "62eca1f1-809d-4976-9e84-91c5a2f87931";
        Long totalElements = 100L;

        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);
        when(serviceAgreementService.getUnexposedUsers(eq(serviceAgreementId), eq(0), eq(10), eq(null), eq("")))
            .thenReturn(new PaginationDto<>(totalElements, new ArrayList<>()));

        mockMvc.perform(get(serviceAgreementUrl + "/context/users/unexposed"))
            .andExpect(status().isOk());

        verify(serviceAgreementService, times(1))
            .getUnexposedUsers(eq(serviceAgreementId), eq(0), eq(10), eq(null), eq(""));
        verifyNoMoreInteractions(serviceAgreementService);
    }
}
