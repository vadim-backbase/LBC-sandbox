package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.matchers.MatcherUtil.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.MatcherUtil.containsSuccessfulResponseItem;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupDataProvider.createPresentationFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.routes.functiongroup.AddFunctionGroupRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.DeleteFunctionGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.DeleteFunctionGroupRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.GetFunctionGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.IngestFunctionGroupRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.ListFunctionGroupsRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.UpdateFunctionGroupByIdRouteProxy;
import com.backbase.accesscontrol.routes.functiongroup.UpdateFunctionGroupRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationPermission;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FunctionGroupServiceTest {
    @Mock
    private AddFunctionGroupRouteProxy addFunctionGroupRouteProxy;

    @Mock
    private UpdateFunctionGroupByIdRouteProxy updateFunctionGroupByIdRouteProxy;

    @Mock
    private UpdateFunctionGroupRouteProxy updateFunctionGroupRouteProxy;

    @Mock
    private ListFunctionGroupsRouteProxy listFunctionGroupsRouteProxy;
    @Mock
    private GetFunctionGroupByIdRouteProxy getFunctionGroupRouteProxy;

    @Mock
    private DeleteFunctionGroupByIdRouteProxy deleteFunctionGroupByIdRouteProxy;

    @Mock
    private DeleteFunctionGroupRouteProxy deleteFunctionGroupRouteProxy;
    @Mock
    private IngestFunctionGroupRouteProxy ingestFunctionGroupRouteProxy;

    @Mock
    private InternalRequestContext context;

    @InjectMocks
    private FunctionGroupsService functionGroupsService;

    @Test
    public void shouldSaveFunctionGroupUsingProxy() {

        FunctionGroupsPostResponseBody functionGroupsPostResponseBody = new FunctionGroupsPostResponseBody();

        when(addFunctionGroupRouteProxy.addFunctionGroup(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(functionGroupsPostResponseBody));

        FunctionGroupsPostResponseBody addFunctionAccessGroupResponse =
            functionGroupsService.addFunctionGroup(any(FunctionGroupBase.class));

        verify(addFunctionGroupRouteProxy, times(1)).addFunctionGroup(any(InternalRequest.class));

        assertEquals(functionGroupsPostResponseBody, addFunctionAccessGroupResponse);

    }

    @Test
    public void shouldIngestFunctionGroupUsingProxy() {
        PresentationFunctionGroup functionGroup = new PresentationFunctionGroup()
            .withName("name")
            .withExternalServiceAgreementId("ex id")
            .withDescription("desc")
            .withPermissions(singletonList(new PresentationPermission()
                .withFunctionId("10001")
                .withPrivileges(asList("create", "edit"))));

        PresentationIngestFunctionGroupPostResponseBody responseBody = new PresentationIngestFunctionGroupPostResponseBody()
            .withId("fg-id");

        when(ingestFunctionGroupRouteProxy.ingestFunctionGroup(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(responseBody));

        PresentationIngestFunctionGroupPostResponseBody ingestFunctionGroupResponse =
            functionGroupsService.ingestFunctionGroup(functionGroup);

        verify(ingestFunctionGroupRouteProxy, times(1)).ingestFunctionGroup(any(InternalRequest.class));

        assertEquals(responseBody, ingestFunctionGroupResponse);

    }

    @Test
    public void shouldReturnListOfFunctionGroup() {
        List<FunctionGroupsGetResponseBody> functionGroupsGetResponseBodies = singletonList(
            new FunctionGroupsGetResponseBody().withServiceAgreementId("said"));
        when(listFunctionGroupsRouteProxy.getFunctionGroups(any(InternalRequest.class), eq("001")))
            .thenReturn(getInternalRequest(functionGroupsGetResponseBodies));

        List<FunctionGroupsGetResponseBody> functionGroups =
            functionGroupsService.getAllFunctionGroup("001");

        assertEquals(functionGroupsGetResponseBodies, functionGroups);
    }

    @Test
    public void shouldReturnFunctionGroupById() {

        FunctionGroupByIdGetResponseBody functionGroupByIdGetResponseBody = new FunctionGroupByIdGetResponseBody();
        when(getFunctionGroupRouteProxy.getFunctionGroupById(any(InternalRequest.class), eq("001")))
            .thenReturn(getInternalRequest(functionGroupByIdGetResponseBody));

        FunctionGroupByIdGetResponseBody functionGroups =
            functionGroupsService.getFunctionGroupById("001");

        assertEquals(functionGroupByIdGetResponseBody, functionGroups);
    }

    @Test
    public void shouldReturnVoidInternalRequestWhenDeletingExistingFunctionGroup() {
        InternalRequest<Void> voidInternalRequest = new InternalRequest<>();

        when(deleteFunctionGroupByIdRouteProxy.deleteFunctionGroup(any(InternalRequest.class), eq("1")))
            .thenReturn(voidInternalRequest);

        functionGroupsService.deleteFunctionGroup("1");

        verify(deleteFunctionGroupByIdRouteProxy, times(1))
            .deleteFunctionGroup(
                any(InternalRequest.class),
                eq("1")
            );
    }

    @Test
    public void shouldUpdateFunctionAccessGroupUsingProxy() {
        InternalRequest<Void> voidInternalRequest = new InternalRequest<>();
        String id = "FG-01";
        FunctionGroupByIdPutRequestBody data = new FunctionGroupByIdPutRequestBody()
            .withName("FG-name")
            .withDescription("description")
            .withServiceAgreementId("SA-01")
            .withPermissions(new ArrayList<>());

        when(updateFunctionGroupByIdRouteProxy.updateFunctionGroupById(any(InternalRequest.class), eq(id)))
            .thenReturn(voidInternalRequest);

        functionGroupsService.updateFunctionGroup(id, data);

        verify(updateFunctionGroupByIdRouteProxy, times(1)).updateFunctionGroupById(any(InternalRequest.class), eq(id));
    }

    @Test
    public void shouldUpdateFunctionGroupUsingProxy() {
        String fgId1 = "FG-01";
        String fgId2 = "FG-02";

        PresentationFunctionGroupPutRequestBody functionGroup1 = createPresentationFunctionGroup(fgId1
            , null, null, "Name-1", "description", "FG-Name-1", new ArrayList<>());

        PresentationFunctionGroupPutRequestBody functionGroup2 = createPresentationFunctionGroup(fgId2
            , null, null, "Name-2", "description", "FG-Name-2", new ArrayList<>());

        List<PresentationFunctionGroupPutRequestBody> data = asList(functionGroup1, functionGroup2);

        BatchResponseItemExtended batchResponseItemSuccessful = new BatchResponseItemExtended()
            .withResourceId(fgId1)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        BatchResponseItemExtended batchResponseItemFailed = new BatchResponseItemExtended()
            .withResourceId(fgId2)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND)
            .withErrors(singletonList("error"));
        List<BatchResponseItemExtended> responseData = asList(batchResponseItemSuccessful, batchResponseItemFailed);
        InternalRequest<List<BatchResponseItemExtended>> responseMock = getInternalRequest(responseData);

        when(updateFunctionGroupRouteProxy.updateFunctionGroup(any(InternalRequest.class)))
            .thenReturn(responseMock);

        List<BatchResponseItemExtended> response = functionGroupsService
            .updateFunctionGroupsBatch(data);

        verify(updateFunctionGroupRouteProxy, times(1)).updateFunctionGroup(any(InternalRequest.class));
        assertTrue(containsSuccessfulResponseItem(response, batchResponseItemSuccessful));
        assertTrue(containsFailedResponseItem(response, batchResponseItemFailed));
    }

    @Test
    public void shouldDeleteFunctionGroupUsingProxy() {
        PresentationIdentifier requestData = new PresentationIdentifier().withIdIdentifier("FG-01");
        List<PresentationIdentifier> requestDataList = singletonList(requestData);

        BatchResponseItemExtended successfulBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("FG-01")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        BatchResponseItemExtended failedBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("FG-02")
            .withErrors(singletonList("error"))
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);
        List<BatchResponseItemExtended> mockResponseData = asList(successfulBatchResponseItem, failedBatchResponseItem);
        mockDeleteFGResponse(mockResponseData);

        List<BatchResponseItemExtended> response = functionGroupsService.deleteFunctionGroup(requestDataList);

        verify(deleteFunctionGroupRouteProxy).deleteFunctionGroup(any(InternalRequest.class));
        assertTrue(containsSuccessfulResponseItem(response, successfulBatchResponseItem));
        assertTrue(containsFailedResponseItem(response, failedBatchResponseItem));
    }

    private void mockDeleteFGResponse(List<BatchResponseItemExtended> mockResponseData) {
        InternalRequest<List<BatchResponseItemExtended>> mockResponse = getInternalRequest(mockResponseData);
        when(deleteFunctionGroupRouteProxy.deleteFunctionGroup(any((InternalRequest.class))))
            .thenReturn(mockResponse);
    }
}
