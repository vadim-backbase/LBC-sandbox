package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getParticipant;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getParticipants;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.routes.serviceagreement.AddServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.AddUsersInServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.DeleteBatchServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.EditServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementByExternalIdRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementParticipantsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.GetUnexposedUsersRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.IngestServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListAdminsForServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListServiceAgreementsHierarchyRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListServiceAgreementsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.ListUsersForServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.RemoveUsersFromServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateAssignUsersPermissionsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateBatchAdminsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateServiceAgreementAdminsRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateServiceAgreementRouteProxy;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateUsersInServiceAgreementRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementServiceTest {

    @Mock
    private AddServiceAgreementRouteProxy addServiceAgreementRouteProxy;
    @Mock
    private UpdateServiceAgreementRouteProxy updateServiceAgreementRouteProxy;
    @Mock
    private ListUsersForServiceAgreementRouteProxy listUsersForServiceAgreementRouteProxy;
    @Mock
    private ListServiceAgreementsRouteProxy listServiceAgreementsRouteProxy;
    @Mock
    private ListServiceAgreementsHierarchyRouteProxy listServiceAgreementsHierarchyRouteProxy;
    @Mock
    private UpdateServiceAgreementAdminsRouteProxy updateServiceAgreementAdminsRouteProxy;
    @Mock
    private AddUsersInServiceAgreementRouteProxy addUsersInServiceAgreementRouteProxy;
    @Mock
    private RemoveUsersFromServiceAgreementRouteProxy removeUserInServiceAgreementRouteProxy;
    @Mock
    private GetServiceAgreementRouteProxy getServiceAgreementRouteProxy;
    @Mock
    private GetServiceAgreementByExternalIdRouteProxy getServiceAgreementByExternalIdRouteProxy;
    @Mock
    private IngestServiceAgreementRouteProxy ingestServiceAgreementRouteProxy;
    @Mock
    private GetServiceAgreementParticipantsRouteProxy getServiceAgreementParticipantsRouteProxy;
    @Mock
    private ListAdminsForServiceAgreementRouteProxy listAdminsForServiceAgreementRouteProxy;
    @Mock
    private GetUnexposedUsersRouteProxy getUnexposedUsersRouteProxy;
    @Mock
    private EditServiceAgreementRouteProxy editServiceAgreementRouteProxy;
    @Mock
    private UpdateUsersInServiceAgreementRouteProxy updateUsersInServiceAgreementRouteProxy;
    @Mock
    private UpdateBatchAdminsRouteProxy updateBatchAdminsRouteProxy;
    @Mock
    private UpdateAssignUsersPermissionsRouteProxy updateAssignUsersPermissionsRouteProxy;
    @Mock
    private DeleteBatchServiceAgreementRouteProxy deleteBatchServiceAgreementRouteProxy;
    @InjectMocks
    private ServiceAgreementService serviceAgreementService;

    @Test
    public void shouldReturnInternalRequestWithAListOfTheRetrievedServiceAgreements() {
        ServiceAgreementGetResponseBody serviceAgreementGetResponseBody = mock(ServiceAgreementGetResponseBody.class);
        PaginationDto<ServiceAgreementGetResponseBody> paginationDto = new PaginationDto<>((long) 1,
            singletonList(serviceAgreementGetResponseBody));

        String query = "";
        String cursor = "";
        String creatorId = "008";
        when(listServiceAgreementsRouteProxy
            .getServiceAgreements(any(InternalRequest.class), eq(creatorId), eq(query), eq(0), eq(10), eq(cursor)))
            .thenReturn(getInternalRequest(paginationDto));

        PaginationDto<ServiceAgreementGetResponseBody> serviceAgreements = serviceAgreementService
            .getServiceAgreements(creatorId, query, 0, 10, cursor);

        assertEquals(paginationDto, serviceAgreements);
    }

    @Test
    public void shouldSaveServiceAgreement() {
        ServiceAgreementPostResponseBody serviceAgreementPostResponseBody = mock(
            ServiceAgreementPostResponseBody.class);

        when(addServiceAgreementRouteProxy.addServiceAgreement(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(serviceAgreementPostResponseBody));

        ServiceAgreementPostResponseBody responseBody = serviceAgreementService
            .addServiceAgreement(new ServiceAgreementPostRequestBody());

        verify(addServiceAgreementRouteProxy, times(1)).addServiceAgreement(any(InternalRequest.class));
        assertEquals(serviceAgreementPostResponseBody, responseBody);
    }

    @Test
    public void shouldReturnVoidInternalRequestWhenUpdatingAdmins() {
        String serviceAgreementId = "SA-001";

        AdminsPutRequestBody data = new AdminsPutRequestBody();

        when((updateServiceAgreementAdminsRouteProxy.updateAdmins(any(InternalRequest.class), eq(serviceAgreementId))))
            .thenReturn(new InternalRequest<>());

        serviceAgreementService.updateAdmins(data, serviceAgreementId);

        verify(updateServiceAgreementAdminsRouteProxy, times(1))
            .updateAdmins(any(InternalRequest.class), eq(serviceAgreementId));
    }

    @Test
    public void shouldReturnInternalRequestWithListOfUsersForServiceAgreement() {
        String serviceAgreementId = "SA-001";
        Integer from = 1;
        Integer size = 2;
        Long totalNumberOfRecords = 100L;

        List<ServiceAgreementUsersGetResponseBody> serviceAgreementUsers = new ArrayList<>();
        ServiceAgreementUsersGetResponseBody serviceAgreementUser = new ServiceAgreementUsersGetResponseBody();
        serviceAgreementUser.setId("userid-01");
        serviceAgreementUser.setExternalId("user-ex-id-01");
        serviceAgreementUser.setFullName("User First Name");
        serviceAgreementUser.setLegalEntityId("user-le-id");
        serviceAgreementUsers.add(serviceAgreementUser);
        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> users = new ListElementsWrapper<>(serviceAgreementUsers, totalNumberOfRecords);

        when(listUsersForServiceAgreementRouteProxy
            .getUsersForServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId), eq(null), eq(from), eq(size), eq(null)))
            .thenReturn(getInternalRequest(users));

        ListElementsWrapper<ServiceAgreementUsersGetResponseBody> usersForServiceAgreement = serviceAgreementService
            .getUsersForServiceAgreement(serviceAgreementId, null, from, size, null);

        verify(listUsersForServiceAgreementRouteProxy, times(1))
            .getUsersForServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId), eq(null), eq(from),
                eq(size), eq(null));

        assertEquals(users, usersForServiceAgreement);
        assertEquals(totalNumberOfRecords, usersForServiceAgreement.getTotalNumberOfRecords());

    }

    @Test
    public void shouldReturnVoidInternalRequestWhenAddUserInServiceAgreement() {
        String serviceAgreementId = "SA-001";
        String userId = "U-001";

        PresentationUsersForServiceAgreementRequestBody data = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(singletonList(userId));

        when((addUsersInServiceAgreementRouteProxy
            .addUsersInServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId))))
            .thenReturn(new InternalRequest<>());

        serviceAgreementService.addUsersInServiceAgreement(data, serviceAgreementId);
        verify(addUsersInServiceAgreementRouteProxy, times(1))
            .addUsersInServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId));
    }

    @Test
    public void shouldReturnVoidInternalRequestWhenRemoveUserInServiceAgreement() {
        String serviceAgreementId = "SA-001";
        String userId = "U-001";

        PresentationUsersForServiceAgreementRequestBody data = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(asList(userId));

        when((removeUserInServiceAgreementRouteProxy
            .removeUsersFromServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId))))
            .thenReturn(new InternalRequest<>());

        serviceAgreementService.removeUsersFromServiceAgreement(data, serviceAgreementId);

        verify(removeUserInServiceAgreementRouteProxy, times(1))
            .removeUsersFromServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId));
    }

    @Test
    public void shouldReturnInternalRequestWithServiceAgreement() {
        String serviceAgreementId = "005";

        ServiceAgreementItemGetResponseBody data = new ServiceAgreementItemGetResponseBody()
            .withExternalId("external")
            .withStatus(Status.DISABLED)
            .withId(serviceAgreementId);

        when(getServiceAgreementRouteProxy.getServiceAgreementById(any(InternalRequest.class), eq(serviceAgreementId)))
            .thenReturn(getInternalRequest(data));

        ServiceAgreementItemGetResponseBody serviceAgreement = serviceAgreementService
            .getServiceAgreementById(serviceAgreementId);

        verify(getServiceAgreementRouteProxy, times(1))
            .getServiceAgreementById(any(InternalRequest.class), eq(serviceAgreementId));

        assertEquals(data, serviceAgreement);
    }


    @Test
    public void shouldIngestServiceAgreement() {
        ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody = new ServiceAgreementIngestPostRequestBody()
            .withExternalId("externalId")
            .withDescription("desc")
            .withName("name")
            .withStatus(CreateStatus.DISABLED)
            .withParticipantsToIngest(getParticipants(
                getParticipant("11", asList("a1_1", "a1_2"), true, true, new ArrayList<>()),
                getParticipant("12", asList("a2_1", "a2_2"), true, true, new ArrayList<>()),
                getParticipant("13", asList("a3_1", "a3_2"), true, false, new ArrayList<>()),
                getParticipant("14", asList("u3_1", "u3_2"), false, true, new ArrayList<>())

            ));

        ServiceAgreementIngestPostResponseBody id = new ServiceAgreementIngestPostResponseBody().withId("id");

        when(ingestServiceAgreementRouteProxy.ingestServiceAgreement(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(id));

        ServiceAgreementIngestPostResponseBody serviceAgreementIngestPostResponseBody = serviceAgreementService
            .ingestServiceAgreement(serviceAgreementIngestPostRequestBody);

        assertEquals(id, serviceAgreementIngestPostResponseBody);
    }

    @Test
    public void shouldUpdateServiceAgreement() {
        String serviceAgreementId = "SA-01";
        ServiceAgreementPutRequestBody body = new ServiceAgreementPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withStatus(Status.DISABLED);

        when(updateServiceAgreementRouteProxy.updateServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId)))
            .thenReturn(getInternalRequest(null));

        serviceAgreementService.updateServiceAgreement(body, serviceAgreementId);

        verify(updateServiceAgreementRouteProxy, times(1))
            .updateServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId));
    }


    @Test
    public void shouldReturnInternalRequestWithListOfAdminsOfServiceAgreement() {
        String serviceAgreementId = "SA-001";

        List<ServiceAgreementUsersGetResponseBody> serviceAgreementAdmins = new ArrayList<>();
        ServiceAgreementUsersGetResponseBody serviceAgreementAdmin = new ServiceAgreementUsersGetResponseBody()
            .withId("userid-01")
            .withExternalId("user-ex-id-01")
            .withFullName("User First Name")
            .withLegalEntityId("user-le-id");
        serviceAgreementAdmins.add(serviceAgreementAdmin);

        when(listAdminsForServiceAgreementRouteProxy
            .getAdminsForServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId)))
            .thenReturn(getInternalRequest(
                serviceAgreementAdmins));

        List<ServiceAgreementUsersGetResponseBody> adminsServiceAgreementsResponse = serviceAgreementService
            .getServiceAgreementAdmins(serviceAgreementId);

        verify(listAdminsForServiceAgreementRouteProxy, times(1))
            .getAdminsForServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId));

        assertEquals(serviceAgreementAdmins, adminsServiceAgreementsResponse);

    }

    @Test
    public void shouldReturnInternalRequestWithServiceAgreementParticipants() {
        String serviceAgreementId = "005";

        List<ServiceAgreementParticipantsGetResponseBody> data = Arrays.asList(
            new ServiceAgreementParticipantsGetResponseBody()
                .withId("LE-1")
                .withName("Consumer1"),
            new ServiceAgreementParticipantsGetResponseBody()
                .withId("LE-2")
                .withName("Provider2"));

        when(getServiceAgreementParticipantsRouteProxy
            .getServiceAgreementParticipants(any(InternalRequest.class), eq(serviceAgreementId)))
            .thenReturn(getInternalRequest(data));

        List<ServiceAgreementParticipantsGetResponseBody> serviceAgreementParticipants = serviceAgreementService
            .getServiceAgreementParticipants(serviceAgreementId);

        verify(getServiceAgreementParticipantsRouteProxy, times(1))
            .getServiceAgreementParticipants(any(InternalRequest.class), eq(serviceAgreementId));

        assertEquals(data, serviceAgreementParticipants);
    }

    @Test
    public void shouldReturnServiceAgreementByExternalId() {
        String serviceAgreementId = "005";
        String externalId = "external-sa-id";

        ServiceAgreementExternalIdGetResponseBody data = new ServiceAgreementExternalIdGetResponseBody()
            .withExternalId(externalId)
            .withStatus(Status.DISABLED)
            .withId(serviceAgreementId);

        when(getServiceAgreementByExternalIdRouteProxy
            .getServiceAgreementByExternalId(any(InternalRequest.class), eq(externalId)))
            .thenReturn(getInternalRequest(data));

        ServiceAgreementExternalIdGetResponseBody serviceAgreement = serviceAgreementService
            .getServiceAgreementByExternalId(externalId);

        verify(getServiceAgreementByExternalIdRouteProxy, times(1))
            .getServiceAgreementByExternalId(any(InternalRequest.class), eq(externalId));

        assertEquals(data, serviceAgreement);
    }

    @Test
    public void shouldReturnInternalRequestWithListOfUnexposedUsersForServiceAgreement() {
        String serviceAgreementId = "SA-001";
        Integer from = 1;
        Integer size = 2;
        Long totalNumberOfRecords = 100L;

        List<UnexposedUsersGetResponseBody> unexposedUsersGetResponseBodies = new ArrayList<>();
        UnexposedUsersGetResponseBody unexposedUsersGetResponseBody = new UnexposedUsersGetResponseBody()
            .withExternalId("user-ex-id-01")
            .withId("userid-01")
            .withLegalEntityName("le-name")
            .withFullName("User First Name")
            .withLegalEntityId("user-le-id");
        unexposedUsersGetResponseBodies.add(unexposedUsersGetResponseBody);

        PaginationDto<UnexposedUsersGetResponseBody> paginationDto = new PaginationDto<>(totalNumberOfRecords,
            unexposedUsersGetResponseBodies);

        when(getUnexposedUsersRouteProxy.getUnexposedUsers(serviceAgreementId, from, size, null, null))
            .thenReturn(getInternalRequest(paginationDto));

        PaginationDto<UnexposedUsersGetResponseBody> unexposedUsers = serviceAgreementService
            .getUnexposedUsers(serviceAgreementId, from, size, null, null);
        verify(getUnexposedUsersRouteProxy, times(1))
            .getUnexposedUsers(eq(serviceAgreementId), eq(from), eq(size), eq(null), eq(null));

        assertEquals(paginationDto, unexposedUsers);
        assertEquals(totalNumberOfRecords, unexposedUsers.getTotalNumberOfRecords());

    }

    @Test
    public void shouldEditServiceAgreement() {
        String serviceAgreementId = "SA-01";
        ServiceAgreementSave body = new ServiceAgreementSave()
            .withName("name")
            .withExternalId("external-id")
            .withDescription("description")
            .withStatus(Status.DISABLED)
            .withParticipants(new HashSet<>());

        when(editServiceAgreementRouteProxy.editServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId)))
            .thenReturn(getInternalRequest(null));

        serviceAgreementService.editServiceAgreement(body, serviceAgreementId);

        verify(editServiceAgreementRouteProxy, times(1))
            .editServiceAgreement(any(InternalRequest.class), eq(serviceAgreementId));
    }

    @Test
    public void shouldReturnInternalRequestWithAListOfTheRetrievedServiceAgreementsInHierarchy() {
        String creatorId = "creatorLe";
        String userId = "";
        String query = "";
        String cursor = "";
        PaginationDto<PresentationServiceAgreement> persistenceServiceAgreements = new PaginationDto<>(
            1L, singletonList(new PresentationServiceAgreement()
            .withId("saId")
            .withIsMaster(false)
            .withName("saName")
            .withCreatorLegalEntity(creatorId)
            .withCreatorLegalEntityName("creatorName")));

        when(listServiceAgreementsHierarchyRouteProxy
            .listServiceAgreements(any(InternalRequest.class), eq(creatorId), eq(userId), eq(query), eq(0), eq(10),
                eq(cursor))).thenReturn(getInternalRequest(persistenceServiceAgreements));

        PaginationDto<PresentationServiceAgreement> serviceAgreements = serviceAgreementService
            .listServiceAgreements(creatorId, userId, query, 0, 10, cursor);

        assertEquals(persistenceServiceAgreements, serviceAgreements);
    }

    @Test
    public void shouldUpdateBatchAdminsOfServiceAgreement() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD).withUsers(singletonList(
                new PresentationServiceAgreementUserPair().withExternalServiceAgreementId("extSaid")
                    .withExternalUserId("extId")));

        List<BatchResponseItemExtended> mockResponse = singletonList(
            new BatchResponseItemExtended().withResourceId("extId").withExternalServiceAgreementId("extSaid"));

        when(updateBatchAdminsRouteProxy.updateBatchAdmins(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(mockResponse));

        List<BatchResponseItemExtended> response = serviceAgreementService.updateServiceAgreementAdminsBatch(request);

        assertEquals(mockResponse, response);
    }


    @Test
    public void testUpdateUsersInServiceAgreement() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD).withUsers(singletonList(
                new PresentationServiceAgreementUserPair().withExternalServiceAgreementId("extSaid")
                    .withExternalUserId("extId")));

        List<BatchResponseItemExtended> mockResponse = singletonList(
            new BatchResponseItemExtended().withResourceId("extId").withExternalServiceAgreementId("extSaid"));

        when(updateUsersInServiceAgreementRouteProxy.updateUsersInServiceAgreement(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(mockResponse));

        List<BatchResponseItemExtended> returnedResponse = serviceAgreementService
            .updateUsersInServiceAgreement(request);

        assertEquals(mockResponse, returnedResponse);
    }

    @Test
    public void shouldInvokeProxyMethod() {
        String userId = "userId";
        String id = "id";
        PresentationFunctionDataGroupItems presentationFunctionDataGroupItems = new PresentationFunctionDataGroupItems()
            .withItems(singletonList(new PresentationFunctionDataGroup().withFunctionGroupId("fgId")
                .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId("dgId")))));

        PresentationApprovalStatus presentationApprovalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.APPROVED);

        when(updateAssignUsersPermissionsRouteProxy.putAssignUsersPermissions(any(InternalRequest.class), eq(id), eq(userId)))
            .thenReturn(getInternalRequest(presentationApprovalStatus));

        PresentationApprovalStatus response = serviceAgreementService
            .putAssignUsersPermissions(presentationFunctionDataGroupItems, id, userId);

        assertEquals(presentationApprovalStatus, response);
    }

    @Test
    public void shouldBatchDeleteServiceAgreement() {
        PresentationServiceAgreementIdentifier identifier = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier("externalId");
        List<PresentationServiceAgreementIdentifier> identifiers = singletonList(identifier);

        PresentationDeleteServiceAgreements presentationDeleteServiceAgreements =
            new PresentationDeleteServiceAgreements()
                .withServiceAgreementIdentifiers(identifiers)
                .withAccessToken("123");

        BatchResponseItem batchResponseItem = new BatchResponseItem()
            .withResourceId("externalId")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        List<BatchResponseItem> responseExpected = singletonList(batchResponseItem);

        when(deleteBatchServiceAgreementRouteProxy.deleteBatchServiceAgreement(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(responseExpected));

        List<BatchResponseItem> responseActual = serviceAgreementService
            .batchDeleteServiceAgreement(presentationDeleteServiceAgreements);

        assertEquals(responseExpected, responseActual);
    }
}
