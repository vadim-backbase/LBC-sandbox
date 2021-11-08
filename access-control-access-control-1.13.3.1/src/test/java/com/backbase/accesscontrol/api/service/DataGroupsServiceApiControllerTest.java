package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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

import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.PresentationIdentifierMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.BatchResponseItemExtendedToBatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups.DataGroupItemSystemBaseToDataGroupBaseMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups.DataGroupPostResponseBodyToIdItemMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups.PresentationDataGroupItemPutRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups.PresentationDataGroupUpdateToPresentationSingleDataGroupPutRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups.PresentationSearchDataGroupRequestMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.datagroups.PresentationServiceAgreementWithDataGroupItemsMapper;
import com.backbase.accesscontrol.service.facades.DataGroupFlowService;
import com.backbase.accesscontrol.service.facades.DataGroupService;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementWithDataGroupsItem;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupDetails;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataGroupsServiceApiControllerTest {

    @InjectMocks
    private DataGroupsServiceApiController dataGroupsServiceApiController;
    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private ValidationConfig validationConfig;
    @Mock
    private DataGroupFlowService dataGroupFlowService;
    @Captor
    private ArgumentCaptor<List<PresentationDataGroupItemPutRequestBody>> captorDataItems;

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(DataGroupItemSystemBaseToDataGroupBaseMapper.class)),
            spy(Mappers.getMapper(DataGroupPostResponseBodyToIdItemMapper.class)),
            spy(Mappers.getMapper(PresentationDataGroupItemPutRequestBodyMapper.class)),
            spy(Mappers.getMapper(PresentationDataGroupUpdateToPresentationSingleDataGroupPutRequestBodyMapper.class)),
            spy(Mappers.getMapper(PresentationSearchDataGroupRequestMapper.class)),
            spy(Mappers.getMapper(PresentationServiceAgreementWithDataGroupItemsMapper.class)),
            spy(Mappers.getMapper(PresentationIdentifierMapper.class)),
            spy(Mappers.getMapper(BatchResponseItemExtendedToBatchResponseItemExtendedMapper.class))
        ));

    @Test
    public void testPostDataGroups() {
        String id = UUID.randomUUID().toString();

        com.backbase.accesscontrol.service.rest.spec.model.DataGroupItemSystemBase request = new com.backbase.accesscontrol.service.rest.spec.model.DataGroupItemSystemBase()
            .name("name.legalentity")
            .type("CONTACTS")
            .description("desc.legalentity")
            .serviceAgreementId("sa-001");

        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody().withId(id);
        when(dataGroupFlowService.createDataGroup(any(DataGroupBase.class))).thenReturn(dataGroupsPostResponseBody);

        IdItem responseBody = dataGroupsServiceApiController.postDataGroups(request).getBody();

        assertNotNull(responseBody);
        assertEquals(id, responseBody.getId());
    }

    @Test
    public void testPutDataGroups() {
        com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupUpdate request = new com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupUpdate()
            .name("name")
            .description("desc")
            .dataGroupIdentifier(new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier()
                .idIdentifier("dg id"))
            .type("ARRANGEMENTS")
            .dataItems(new ArrayList<>());

        PresentationSingleDataGroupPutRequestBody data = new PresentationSingleDataGroupPutRequestBody()
            .withName("name")
            .withDescription("desc")
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier("dg id"))
            .withType("ARRANGEMENTS")
            .withDataItems(new ArrayList<>());

        doNothing().when(validationConfig).validateDataGroupType("ARRANGEMENTS");

        dataGroupsServiceApiController.putDataGroups(request);

        verify(dataGroupFlowService, times(1)).updateDataGroup(eq(data));
    }

    @Test
    public void testDataGroupItemsUpdate() {
        PresentationIdentifier dataGroupIdentifier = new PresentationIdentifier()
            .withIdIdentifier("dgId");
        List<PresentationItemIdentifier> dataItems = new ArrayList<>();
        PresentationItemIdentifier item = new PresentationItemIdentifier()
            .withExternalIdIdentifier("exId");
        dataItems.add(item);
        PresentationDataGroupItemPutRequestBody data = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(dataGroupIdentifier)
            .withDataItems(dataItems);

        com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupItemPutRequestBody requestItem = new com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupItemPutRequestBody()
            .action(com.backbase.accesscontrol.service.rest.spec.model.PresentationAction.ADD)
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(
                new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier().idIdentifier("dgId"))
            .dataItems(singletonList(new com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier()
                .externalIdIdentifier("exId")));

        List<BatchResponseItemExtended> responseItems = new ArrayList<>();
        BatchResponseItemExtended responseItem = new BatchResponseItemExtended()
            .withAction(PresentationAction.ADD)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId("exId");
        responseItems.add(responseItem);

        when(dataGroupService
            .updateDataGroupItemsBatchByIdentifier(captorDataItems.capture(), eq(new ArrayList<>()), any(Map.class),
                any(Map.class))).thenReturn(responseItems);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> batchResponseItemExtendedList = dataGroupsServiceApiController
            .putDataGroupItemsUpdate(singletonList(requestItem)).getBody();

        List<PresentationDataGroupItemPutRequestBody> captorPayload = captorDataItems.getValue();

        assertNotNull(batchResponseItemExtendedList);
        assertEquals(data, captorPayload.get(0));
    }

    @Test
    public void testDeleteDataGroup() {
        String dataGroupId = "dg-001";
        when(dataGroupService.deleteDataGroup(eq(dataGroupId)))
            .thenReturn(new DataGroupOperationResponse().withId(dataGroupId));

        dataGroupsServiceApiController.deleteDataGroupById(dataGroupId);

        verify(dataGroupService, times(1)).deleteDataGroup(eq(dataGroupId));
        verifyNoMoreInteractions(dataGroupService);
    }

    @Test
    public void testDeleteDataGroupsByIdentifiers() {
        PresentationIdentifier presentationIdentifier01 = new PresentationIdentifier().withIdIdentifier("DG-01");
        PresentationIdentifier presentationIdentifier02 = new PresentationIdentifier().withIdIdentifier("DG-02");
        List<PresentationIdentifier> presentationIdentifiers = asList(presentationIdentifier01,
            presentationIdentifier02);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> expectedResponse = asList(
            new com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended()
                .additions(new HashMap<>())
                .resourceId("DG-01")
                .status(StatusEnum.HTTP_STATUS_OK)
                .errors(emptyList()),
            new com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended()
                .additions(new HashMap<>())
                .resourceId("DG-02")
                .errors(singletonList("error"))
                .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
        );

        BatchResponseItemExtended successfulBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("DG-01")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        BatchResponseItemExtended failedBatchResponseItem = new BatchResponseItemExtended()
            .withResourceId("DG-02")
            .withErrors(Collections.singletonList("error"))
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);
        List<BatchResponseItemExtended> mockResponse = asList(successfulBatchResponseItem, failedBatchResponseItem);

        when(dataGroupService.deleteDataGroupsByIdentifiers(eq(presentationIdentifiers))).thenReturn(mockResponse);

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended> response = dataGroupsServiceApiController
            .postDataGroupsDelete(asList(
                new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier().idIdentifier("DG-01"),
                new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier().idIdentifier("DG-02")
            )).getBody();

        assertNotNull(response);
        assertEquals(expectedResponse.get(0).getResourceId(), response.get(0).getResourceId());
        assertEquals(expectedResponse.get(0).getStatus(), response.get(0).getStatus());
        assertEquals(expectedResponse.get(1).getResourceId(), response.get(1).getResourceId());
        assertEquals(expectedResponse.get(1).getStatus(), response.get(1).getStatus());
        verify(dataGroupService, times(1)).deleteDataGroupsByIdentifiers(eq(presentationIdentifiers));
    }

    @Test
    public void testPostSearchPresentationGetDataGroupsRequest() {
        String type = "ARRANGEMENTS";

        com.backbase.accesscontrol.service.rest.spec.model.PresentationSearchDataGroupsRequest request = new com.backbase.accesscontrol.service.rest.spec.model.PresentationSearchDataGroupsRequest()
            .dataItemIdentifier(new com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier()
                .internalIdIdentifier("internalId"))
            .serviceAgreementIdentifier(
                new com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementIdentifier()
                    .idIdentifier("saId"));

        PresentationGetDataGroupsRequest presentationGetDataGroupsRequest = new PresentationGetDataGroupsRequest()
            .withDataItemIdentifier(new PresentationItemIdentifier().withInternalIdIdentifier("internalId"))
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier().withIdIdentifier("saId"));

        List<PresentationServiceAgreementWithDataGroupsItem> responseExpected = singletonList(
            new PresentationServiceAgreementWithDataGroupsItem()
                .additions(new HashMap<>())
                .serviceAgreement(
                    new com.backbase.accesscontrol.service.rest.spec.model.PresentationServiceAgreementIds().id("said"))
                .dataGroups(
                    singletonList(new com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupDetails()
                        .id("internalId").name("name")
                        .description("description"))));

        List<PresentationServiceAgreementWithDataGroups> serviceAgreementWithDataGroupsMock = singletonList(
            new PresentationServiceAgreementWithDataGroups()
                .withServiceAgreement(new PresentationServiceAgreementIds().withId("said")).withDataGroups(
                singletonList(new PresentationDataGroupDetails().withId("internalId").withName("name")
                    .withDescription("description"))));
        when(dataGroupService.searchDataGroups(eq(presentationGetDataGroupsRequest), eq(type)))
            .thenReturn(serviceAgreementWithDataGroupsMock);

        List<PresentationServiceAgreementWithDataGroupsItem> serviceAgreementWithDataGroups = dataGroupsServiceApiController
            .postSearch(type, request).getBody();

        verify(dataGroupService, times(1)).searchDataGroups(eq(presentationGetDataGroupsRequest), eq(type));
        assertEquals(responseExpected.get(0).getDataGroups(), serviceAgreementWithDataGroups.get(0).getDataGroups());
    }
}
