package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.UserAssignedFunctionGroupDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.BatchResponseItemExtendedToBatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.PresentationParticipantBatchUpdateToPresentationParticipantsPutMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.PresentationServiceAgreementUsersBatchUpdateToPresentationServiceAgreementUsersUpdateMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ServiceAgreementExternalIdGetResponseBodyToServiceAgreementItemMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ServiceAgreementIngestPostResponseBodyToIdItemMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ServiceAgreementPutToServiceAgreementPutRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.ServicesAgreementIngestToServiceAgreementIngestPostRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementItemToServiceAgreementItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserAssignedFunctionGroupToUserAssignedFunctionGroupMapper;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementFlowService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.accesscontrol.service.rest.spec.model.ServicesAgreementIngest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UserAssignedFunctionGroupResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementServiceApiControllerTest {

    @Mock
    private ServiceAgreementService serviceAgreementService;
    @Mock
    private ParameterValidationService parameterValidationService;
    @Mock
    private ServiceAgreementFlowService serviceAgreementFlowService;
    @InjectMocks
    private ServiceAgreementServiceApiController serviceAgreementServiceApiController;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(ServicesAgreementIngestToServiceAgreementIngestPostRequestBodyMapper.class)),
            spy(Mappers.getMapper(BatchResponseItemExtendedToBatchResponseItemExtendedMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementItemToServiceAgreementItemMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementPutToServiceAgreementPutRequestBodyMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementExternalIdGetResponseBodyToServiceAgreementItemMapper.class)),
            spy(Mappers.getMapper(PresentationParticipantBatchUpdateToPresentationParticipantsPutMapper.class)),
            spy(Mappers.getMapper(
                PresentationServiceAgreementUsersBatchUpdateToPresentationServiceAgreementUsersUpdateMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementIngestPostResponseBodyToIdItemMapper.class)),
            spy(Mappers.getMapper(UserAssignedFunctionGroupToUserAssignedFunctionGroupMapper.class))
        ));

    @Test
    public void shouldInvokeServiceAndConvertPayload() {
        String id = "id";
        ServiceAgreementIngestPostResponseBody serviceAgreementIngestPostResponseBody = new ServiceAgreementIngestPostResponseBody()
            .withId(id);
        ServicesAgreementIngest serviceAgreementIngestPostRequestBody
            = new ServicesAgreementIngest()
            .externalId("extId").name("name").description("description");

        when(serviceAgreementService.ingestServiceAgreement(any()))
            .thenReturn(serviceAgreementIngestPostResponseBody);

        IdItem postResponseBody = serviceAgreementServiceApiController
            .postServiceAgreementIngest(serviceAgreementIngestPostRequestBody).getBody();

        assertEquals(id, postResponseBody.getId());
    }

    @Test
    public void shouldGetServiceAgreementByExternalId() {
        String serviceAgreementId = "id";
        String externalId = "exid";

        ServiceAgreementExternalIdGetResponseBody putBody = new ServiceAgreementExternalIdGetResponseBody()
            .withId(serviceAgreementId)
            .withExternalId(externalId)
            .withDescription("description")
            .withName("SA-name")
            .withStatus(Status.DISABLED);

        when(serviceAgreementService.getServiceAgreementByExternalId(eq(externalId))).thenReturn(putBody);

        com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItem serviceAgreementResponse =
            serviceAgreementServiceApiController
                .getServiceAgreementExternalId(externalId).getBody();

        assertEquals(putBody.getExternalId(), serviceAgreementResponse.getExternalId());
    }

    @Test
    public void shouldInvokeParticipants() {

        com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantBatchUpdate
            presentationParticipantsPut =
            new com.backbase.accesscontrol.service.rest.spec.model.PresentationParticipantBatchUpdate();
        List<BatchResponseItemExtended> batchResponseItemExtendedList = singletonList(
            new BatchResponseItemExtended().withExternalServiceAgreementId("extid").withResourceId("id"));

        when(serviceAgreementService.updateParticipants(any())).thenReturn(batchResponseItemExtendedList);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended>
            responseItemExtendedList = serviceAgreementServiceApiController
            .putPresentationIngestServiceAgreementParticipants(presentationParticipantsPut).getBody();

        assertNotNull(responseItemExtendedList);
        assertEquals(batchResponseItemExtendedList.size(), responseItemExtendedList.size());
        assertEquals(batchResponseItemExtendedList.get(0).getExternalServiceAgreementId(),
            responseItemExtendedList.get(0).getExternalServiceAgreementId());
    }

    @Test
    public void shouldInvokeUpdateBatchAdminsInServiceAgreementService() {
        String externalServiceAgreementId = "SA-01";
        String externalUserId = "U-01";
        com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUsersBatchUpdate data =
            new com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUsersBatchUpdate()
                .users(singletonList(new
                    com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUserPair()
                    .externalServiceAgreementId(externalServiceAgreementId)
                    .externalUserId(externalUserId)));

        List<BatchResponseItemExtended> responseData = singletonList(new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResourceId(externalUserId));

        when(serviceAgreementService.updateServiceAgreementAdminsBatch(any())).thenReturn(responseData);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> response =
            serviceAgreementServiceApiController
                .putPresentationServiceAgreementAdminsBatchUpdate(data).getBody();

        verify(serviceAgreementService, times(1)).updateServiceAgreementAdminsBatch(any());
        assertNotNull(response);
        assertEquals(responseData.size(), response.size());
        assertEquals(responseData.get(0).getExternalServiceAgreementId(),
            response.get(0).getExternalServiceAgreementId());
    }

    @Test
    public void shouldPutPresentationServiceAgreementUsersBatchUpdate() {
        String externalServiceAgreementId = "SA-01";
        String externalUserId = "U-01";
        com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUsersBatchUpdate data =
            new com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUsersBatchUpdate()
                .users(singletonList(
                    new com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementUserPair()
                        .externalServiceAgreementId(externalServiceAgreementId)
                        .externalUserId(externalUserId)));

        List<BatchResponseItemExtended> responseData = singletonList(new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResourceId(externalUserId));

        when(serviceAgreementService.updateUsersInServiceAgreement(any())).thenReturn(responseData);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> response =
            serviceAgreementServiceApiController
                .putPresentationServiceAgreementUsersBatchUpdate(
                    data).getBody();

        verify(serviceAgreementService, times(1)).updateUsersInServiceAgreement(any());
        assertEquals(responseData.get(0).getExternalServiceAgreementId(),
            response.get(0).getExternalServiceAgreementId());
    }

    @Test
    public void shouldUpdateService() {
        String serviceAgreementId = "id";

        com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementPut putBody =
            new com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementPut()
                .externalId("exid")
                .description("description")
                .name("SA-name")
                .status(com.backbase.accesscontrol.service.rest.spec.model.Status.DISABLED);

        doNothing().when(serviceAgreementService).updateServiceAgreement(any(), eq(serviceAgreementId));

        serviceAgreementServiceApiController
            .putServiceAgreementItem(serviceAgreementId, putBody);

        verify(serviceAgreementService, times(1))
            .updateServiceAgreement(any(), eq(serviceAgreementId));
    }

    @Test
    public void shouldGetUsersByServiceAgreementIdAndFunctionGroupId() {
        String serviceAgreementId = "sa_id";
        String functionGroupId = "fg_id";
        String userId = "user_id";
        Integer from = 0;
        Integer size = 20;

        doNothing().when(parameterValidationService).validateFromAndSizeParameter(from, size);
        doReturn(createRecords(1L, userId))
            .when(serviceAgreementFlowService)
            .getUsersWithAssignedFunctionGroup(
                new UserAssignedFunctionGroupDto(serviceAgreementId, functionGroupId, from, size));

        ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.UserAssignedFunctionGroupResponse>> response = serviceAgreementServiceApiController
            .getUsers(serviceAgreementId, functionGroupId, from, size);

        assertEquals("1", response.getHeaders().get("X-Total-Count").get(0));

        assertEquals(1, response.getBody().size());
        assertEquals(userId, response.getBody().get(0).getId());
    }

    private RecordsDto<UserAssignedFunctionGroupResponse> createRecords(Long numberOfRecords, String userId) {
        UserAssignedFunctionGroupResponse body = new UserAssignedFunctionGroupResponse()
            .withId(userId);

        return new RecordsDto<>(numberOfRecords, Lists.newArrayList(body));
    }
}
