package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.DataGroupItemBaseToDataGroupItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.DataGroupsIdsToBulkSearchDataGroupsPostRequestBodyMapper;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupItem;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupsIds;
import com.backbase.accesscontrol.service.facades.DataGroupServiceFacade;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.BulkSearchDataGroupsPostRequestBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataGroupQueryControllerTest {

    @InjectMocks
    private DataGroupQueryController dataGroupQueryController;
    @Mock
    private DataGroupServiceFacade dataGroupServiceFacade;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(DataGroupItemBaseToDataGroupItemMapper.class)),
            spy(Mappers.getMapper(DataGroupsIdsToBulkSearchDataGroupsPostRequestBodyMapper.class))
        ));

    @Test
    public void shouldGetDataGroupsByServiceAgreementIdAndItemType() {
        String serviceAgreementId = "id.sa";
        String type = "id.type";

        DataGroupItemBase dataGroupItemBase = new DataGroupItemBase().withId("id.dgitembase");

        when(dataGroupServiceFacade.getDataGroups(eq(serviceAgreementId), eq(type), eq(true)))
            .thenReturn(Collections.singletonList(dataGroupItemBase));

        List<DataGroupItem> queryListenerResponse = dataGroupQueryController
            .getDataGroups(serviceAgreementId, type, true).getBody();

        verify(dataGroupServiceFacade, times(1))
            .getDataGroups(eq(serviceAgreementId), eq(type), eq(true));
        assertEquals(1, queryListenerResponse.size());
        assertEquals("id.dgitembase", queryListenerResponse.get(0).getId());

    }


    @Test
    public void shouldReturnValidObjectWhenQueriedByIdAndIncludeItemsSetToTrue() {
        String id = "id";
        String description = "description";
        String name = "dg.name";
        String dataItemType = "CONTACTS";
        String saId = "saId";
        String itemId = "itemId";

        DataGroupItemBase dataGroupItemBase = new DataGroupItemBase()
            .withId(id)
            .withName(name)
            .withDescription(description)
            .withType(dataItemType)
            .withServiceAgreementId(saId)
            .withItems(Collections.singletonList(itemId));

        when(dataGroupServiceFacade.getDataGroupById(eq(id), eq(true))).thenReturn(dataGroupItemBase);

        DataGroupItem response = dataGroupQueryController
            .getDataGroupById(id, true).getBody();

        verify(dataGroupServiceFacade).getDataGroupById(eq(id), eq(true));
        assertEquals(dataGroupItemBase.getServiceAgreementId(), response.getServiceAgreementId());
        assertEquals(dataGroupItemBase.getId(), response.getId());
    }

    @Test
    public void shouldCallHandlerForSearchBulkDataGroups() {
        BulkSearchDataGroupsPostRequestBody bulkDataGroupsGetRequestBody = new BulkSearchDataGroupsPostRequestBody()
            .withIds(Sets.newHashSet("id1", "id2", "id3"));
        DataGroupsIds dataGroupsIds = new DataGroupsIds();
        dataGroupsIds.setIds(asList("id1", "id2", "id3"));

        when(dataGroupServiceFacade
            .getBulkDataGroups(any(BulkSearchDataGroupsPostRequestBody.class)))
            .thenReturn(new ArrayList());

         dataGroupQueryController.postBulkSearchDataGroups(dataGroupsIds);

        verify(dataGroupServiceFacade)
            .getBulkDataGroups(refEq(bulkDataGroupsGetRequestBody));
    }

}
