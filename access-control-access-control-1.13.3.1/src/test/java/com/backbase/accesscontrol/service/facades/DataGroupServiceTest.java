package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.matchers.MatcherUtil.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.MatcherUtil.containsSuccessfulResponseItem;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.routes.datagroup.AddDataGroupRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.DeleteDataGroupRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.DeleteDataGroupsByIdentifiersRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.GetDataGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.ListDataGroupsRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.UpdateDataGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.datagroup.UpdateDataGroupItemsByIdentifierRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataGroupServiceTest {

    @Mock
    private AddDataGroupRouteProxy addDataGroupRouteProxy;
    @Mock
    private ListDataGroupsRouteProxy listDataGroupsRouteProxy;
    @Mock
    private GetDataGroupByIdRouteProxy getDataGroupByIdRouteProxy;
    @Mock
    private UpdateDataGroupByIdRouteProxy updateDataGroupByIdRouteProxy;
    @Mock
    private DeleteDataGroupRouteProxy deleteDataGroupRouteProxy;
    @Mock
    private DeleteDataGroupsByIdentifiersRouteProxy deleteDataGroupsByIdentifiersRouteProxy;
    @Mock
    private UpdateDataGroupItemsByIdentifierRouteProxy updateDataGroupItemsByIdentifierRouteProxy;

    @InjectMocks
    private DataGroupService dataGroupService;

    @Test
    public void shouldReturnDataGroupIdWhenAddingNewDataGroup() {
        DataGroupBase dataGroupBase = new DataGroupBase().withName("name").withDescription("description")
            .withType("ARRANGEMENTS");
        DataGroupOperationResponse dataGroupOperationResponse = new DataGroupOperationResponse()
            .withId(UUID.randomUUID().toString());

        when((addDataGroupRouteProxy.addDataGroup(any(InternalRequest.class))))
            .thenReturn(getInternalRequest(dataGroupOperationResponse));

        DataGroupOperationResponse postResponse = dataGroupService.addDataGroup(dataGroupBase);

        assertEquals(dataGroupOperationResponse.getId(), postResponse.getId());
    }

    @Test
    public void shouldReturnDataGroupWhenGetDataGroupByIdIsInvoked() {
        String dataGroupId = "id";
        DataGroupByIdGetResponseBody responseBody = new DataGroupByIdGetResponseBody()
            .withId(dataGroupId)
            .withServiceAgreementId("sa-001");
        when(getDataGroupByIdRouteProxy.getDataGroupById(any(InternalRequest.class), eq(dataGroupId)))
            .thenReturn(getInternalRequest(responseBody));

        DataGroupByIdGetResponseBody postResponse = dataGroupService.getDataGroupById(dataGroupId);

        assertEquals(responseBody.getId(), postResponse.getId());
    }

    @Test
    public void shouldReturnDataGroupsWhenListingByServiceAgreement() {
        String serviceAgreementId = "sa-01";
        String type = "ARRANGEMENTS";
        InternalRequest<List<DataGroupsGetResponseBody>> proxyResponse = new InternalRequest<>();
        proxyResponse.setData(singletonList(
            new DataGroupsGetResponseBody()
                .withId("001")
        ));
        when(listDataGroupsRouteProxy.getDataGroups(any(InternalRequest.class), eq(serviceAgreementId), eq(type),
            eq(true)))
            .thenReturn(proxyResponse);

        List<DataGroupsGetResponseBody> dataGroups = dataGroupService.getDataGroups(serviceAgreementId, type, true);

        assertEquals(proxyResponse.getData().size(), dataGroups.size());
        assertEquals(proxyResponse.getData().get(0).getId(), dataGroups.get(0).getId());
    }

    @Test
    public void shouldUpdateDataGroup() {
        String serviceAgreementId = "sa-01";
        DataGroupByIdPutRequestBody dataGroupByIdPutRequestBody = new DataGroupByIdPutRequestBody()
            .withId(serviceAgreementId);
        DataGroupOperationResponse dataGroupOperationResponseMock = new DataGroupOperationResponse().withId("001");

        ArgumentCaptor<InternalRequest> requestCaptor = ArgumentCaptor.forClass(InternalRequest.class);

        when(updateDataGroupByIdRouteProxy.updateDataGroupById(any(InternalRequest.class), eq(serviceAgreementId)))
            .thenReturn(getInternalRequest(dataGroupOperationResponseMock));

        DataGroupOperationResponse dataGroupOperationResponse = dataGroupService
            .updateDataGroup(dataGroupByIdPutRequestBody, serviceAgreementId);

        verify(updateDataGroupByIdRouteProxy, times(1))
            .updateDataGroupById(requestCaptor.capture(), eq(serviceAgreementId));

        DataGroupByIdPutRequestBody data = (DataGroupByIdPutRequestBody) requestCaptor.getValue().getData();

        assertEquals(dataGroupByIdPutRequestBody, data);
        assertEquals(dataGroupOperationResponseMock, dataGroupOperationResponse);
    }

    @Test
    public void shouldUpdateDataGroupItemsBatchByIdentifier() {

        List<BatchResponseItemExtended> batchResponseItems = new ArrayList<>();
        BatchResponseItemExtended item = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("dgId")
            .withAction(PresentationAction.ADD);
        batchResponseItems.add(item);

        PresentationItemIdentifier dataItem = new PresentationItemIdentifier()
            .withInternalIdIdentifier("dgId");
        PresentationDataGroupItemPutRequestBody body = new PresentationDataGroupItemPutRequestBody();
        body.withAction(PresentationAction.ADD);
        body.withType("ARRANGEMENTS");
        body.withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"));
        body.withDataItems(singletonList(dataItem));

        List<PresentationDataGroupItemPutRequestBody> dataGroupItemPutRequestBodies = singletonList(body);
        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest = getInternalRequest(
            dataGroupItemPutRequestBodies);

        when(updateDataGroupItemsByIdentifierRouteProxy
            .updateDataGroupItemsByIdentifier(any(InternalRequest.class), anyList(), anyMap(), anyMap()))
            .thenReturn(getInternalRequest(batchResponseItems));

        List<BatchResponseItemExtended> response = dataGroupService
            .updateDataGroupItemsBatchByIdentifier(dataGroupItemPutRequestBodies, new ArrayList<>(), new HashMap<>(),
                new HashMap<>());

        assertNotNull(response);
        assertEquals(body.getDataItems().get(0).getInternalIdIdentifier(), response.get(0).getResourceId());
        assertEquals(body.getAction(), response.get(0).getAction());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_OK, response.get(0).getStatus());

    }

    @Test
    public void shouldReturnVoidInternalRequestWhenDeletingExistingDataGroup() {
        DataGroupOperationResponse dataGroupOperationResponseMock = new DataGroupOperationResponse().withId("1");
        when(deleteDataGroupRouteProxy.deleteDataGroup(any(InternalRequest.class), eq("1")))
            .thenReturn(getInternalRequest(dataGroupOperationResponseMock));

        DataGroupOperationResponse dataGroupOperationResponse = dataGroupService.deleteDataGroup("1");

        verify(deleteDataGroupRouteProxy, times(1)).deleteDataGroup(any(InternalRequest.class), eq("1"));
        assertEquals(dataGroupOperationResponseMock, dataGroupOperationResponse);
    }

    @Test
    public void shouldInvokeDeleteDataGroupsByIdentifiersRoute() {
        PresentationIdentifier requestData = new PresentationIdentifier().withIdIdentifier("DG-01");

        List<PresentationIdentifier> presentationIdentifiers = singletonList(requestData);

        BatchResponseItemExtended successfulBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("DG-01")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        BatchResponseItemExtended failedBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("DG-02")
            .withErrors(singletonList("error"))
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);
        List<BatchResponseItemExtended> mockResponseData = asList(successfulBatchResponseItem, failedBatchResponseItem);

        when(deleteDataGroupsByIdentifiersRouteProxy.deleteDataGroupsByIdentifiers(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(mockResponseData));

        List<BatchResponseItemExtended> response = dataGroupService
            .deleteDataGroupsByIdentifiers(presentationIdentifiers);

        verify(deleteDataGroupsByIdentifiersRouteProxy, times(1))
            .deleteDataGroupsByIdentifiers(any(InternalRequest.class));
        assertTrue(containsSuccessfulResponseItem(response, successfulBatchResponseItem));
        assertTrue(containsFailedResponseItem(response, failedBatchResponseItem));
    }
}