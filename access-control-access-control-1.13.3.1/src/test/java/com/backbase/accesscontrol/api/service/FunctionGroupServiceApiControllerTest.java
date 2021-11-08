package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupDataProvider.createPresentationFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupDataProvider.createPresentationFunctionGroupModel;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.PresentationIdentifierMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.BatchResponseItemExtendedToBatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.aps.PresentationInternalIdResponseToPresentationIdMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.functiongroups.PresentationFunctionGroupPutRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.functiongroups.PresentationIngestFunctionGroupPostResponseBodyToIdItemMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.functiongroups.PresentationIngestFunctionGroupToPresentationFunctionGroupMapper;
import com.backbase.accesscontrol.service.facades.FunctionGroupsService;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FunctionGroupServiceApiControllerTest {

    @InjectMocks
    private FunctionGroupServiceApiController functionGroupServiceApiController;

    @Mock
    private FunctionGroupsService functionGroupsService;

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(PresentationInternalIdResponseToPresentationIdMapper.class)),
            spy(Mappers.getMapper(PresentationFunctionGroupPutRequestBodyMapper.class)),
            spy(Mappers.getMapper(PresentationIngestFunctionGroupToPresentationFunctionGroupMapper.class)),
            spy(Mappers.getMapper(PresentationIngestFunctionGroupPostResponseBodyToIdItemMapper.class)),
            spy(Mappers.getMapper(PresentationIdentifierMapper.class)),
            spy(Mappers.getMapper(BatchResponseItemExtendedToBatchResponseItemExtendedMapper.class))
        ));

    @Test
    public void testIngestFunctionGroup() {
        String functionGroupId = "fg-001";

        com.backbase.accesscontrol.service.rest.spec.model.PresentationIngestFunctionGroup request = new com.backbase.accesscontrol.service.rest.spec.model.PresentationIngestFunctionGroup()
            .externalServiceAgreementId("SA-01")
            .name("FAG-NAME");

        PresentationIngestFunctionGroupPostResponseBody data1 = new PresentationIngestFunctionGroupPostResponseBody()
            .withId(functionGroupId);

        when(functionGroupsService.ingestFunctionGroup(any()))
            .thenReturn(data1);

        IdItem response = functionGroupServiceApiController
            .postPresentationIngestFunctionGroup(request).getBody();

        assertNotNull(response);
        assertEquals(functionGroupId, response.getId());
    }

    @Test
    public void testDeleteFunctionGroups() {
        String functionGroupId = "fg-001";
        doNothing().when(functionGroupsService).deleteFunctionGroup(eq(functionGroupId));

        functionGroupServiceApiController.deleteFunctionGroupById(functionGroupId);

        verify(functionGroupsService, times(1)).deleteFunctionGroup(eq(functionGroupId));
        verifyNoMoreInteractions(functionGroupsService);
    }

    @Test
    public void testPutFunctionGroupsUpdate() {
        final String functionGroupId1 = "fg-001";
        final String functionGroupId2 = "fg-002";
        final String functionGroupName = "FAG-NAME";

        PresentationFunctionGroupPutRequestBody functionGroup1 = createPresentationFunctionGroup(functionGroupId1,
            null, null, functionGroupName, null, null, Lists.emptyList());

        PresentationFunctionGroupPutRequestBody functionGroup2 = createPresentationFunctionGroup(functionGroupId2,
            null, null, functionGroupName, null, null, Lists.emptyList());

        List<PresentationFunctionGroupPutRequestBody> list = asList(functionGroup1, functionGroup2);

        List<com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody> request = asList(
            createPresentationFunctionGroupModel(functionGroupId1, null, null, functionGroupName, null, null, Lists.emptyList()),
            createPresentationFunctionGroupModel(functionGroupId2, null, null, functionGroupName, null, null, Lists.emptyList()));

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> responseExpected = asList(
            new com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended()
                .additions(new HashMap<>())
                .status(StatusEnum.HTTP_STATUS_OK)
                .errors(Collections.emptyList()),
            new com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended()
                .additions(new HashMap<>())
                .status(StatusEnum.HTTP_STATUS_OK)
                .errors(Collections.emptyList()));

        List<BatchResponseItemExtended> responseMock = asList(new BatchResponseItemExtended()
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK),
            new BatchResponseItemExtended()
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK));

        when(functionGroupsService.updateFunctionGroupsBatch(list)).thenReturn(responseMock);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> batchResponseItemExtendedList = functionGroupServiceApiController
            .putFunctionGroupsUpdate(request).getBody();

        assertNotNull(batchResponseItemExtendedList);
        assertEquals(responseExpected.get(0).getResourceId(), batchResponseItemExtendedList.get(0).getResourceId());
        assertEquals(responseExpected.get(0).getStatus(), batchResponseItemExtendedList.get(0).getStatus());
        assertEquals(responseExpected.get(1).getResourceId(), batchResponseItemExtendedList.get(1).getResourceId());
        assertEquals(responseExpected.get(1).getStatus(), batchResponseItemExtendedList.get(1).getStatus());
        verify(functionGroupsService, times(1)).updateFunctionGroupsBatch(list);
    }

    @Test
    public void testDeleteFunctionGroup() {
        List<PresentationIdentifier> list = asList(new PresentationIdentifier().withIdIdentifier("FG-01"),
            new PresentationIdentifier().withIdIdentifier("FG-02"));

        com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier firstItem = new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier()
            .idIdentifier("FG-01");
        com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier secondItem = new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier()
            .idIdentifier("FG-02");

        BatchResponseItemExtended successfulBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("FG-01")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended successResponseItem = new com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended()
            .additions(new HashMap<>())
            .resourceId("FG-01")
            .status(StatusEnum.HTTP_STATUS_OK)
            .errors(Collections.emptyList());
        BatchResponseItemExtended failedBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("FG-02")
            .withErrors(Collections.singletonList("error"))
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);
        com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended failedResponseItem = new com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended()
            .additions(new HashMap<>())
            .resourceId("FG-02")
            .errors(Collections.singletonList("error"))
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        List<BatchResponseItemExtended> mockResponse = asList(successfulBatchResponseItem, failedBatchResponseItem);

        when(functionGroupsService.deleteFunctionGroup(list)).thenReturn(mockResponse);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> response = functionGroupServiceApiController
            .postFunctionGroupsDelete(asList(firstItem, secondItem)).getBody();

        assertNotNull(response);
        assertEquals(successResponseItem.getResourceId(), response.get(0).getResourceId());
        assertEquals(failedBatchResponseItem.getResourceId(), response.get(1).getResourceId());
        verify(functionGroupsService, times(1)).deleteFunctionGroup(list);
    }
}