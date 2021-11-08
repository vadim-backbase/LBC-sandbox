package com.backbase.accesscontrol.service.facades;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.service.DataGroupService;
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
public class DataGroupServiceFacadeTest {

    @Mock
    private DataGroupService dataGroupService;

    @Spy
    private DataGroupMapper dataGroupMapper = Mappers.getMapper(DataGroupMapper.class);

    @InjectMocks
    private DataGroupServiceFacade dataGroupServiceFacade;

    @Test
    public void shouldReturnValidObjectWhenQueriedById() {
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

        DataGroup dataGroup = getDataGroup(id, description, name, dataItemType, saId, itemId);
        when(dataGroupService.getByIdWithExtendedData(eq(id))).thenReturn(dataGroup);

        DataGroupItemBase response = dataGroupServiceFacade.getDataGroupById(id, true);

        verify(dataGroupService).getByIdWithExtendedData(eq(id));
        assertEquals(dataGroupItemBase, response);
    }

    @Test
    public void shouldReturnValidObjectWhenQueriedByIdAndIncludeItemsSetToFalse() {
        String id = "id";
        String description = "description";
        String name = "dg.name";
        String dataItemType = "CONTACTS";
        String saId = "saId";
        String itemId = null;

        DataGroupItemBase dataGroupItemBase = new DataGroupItemBase()
            .withId(id)
            .withName(name)
            .withDescription(description)
            .withType(dataItemType)
            .withServiceAgreementId(saId);

        DataGroup dataGroup = getDataGroup(id, description, name, dataItemType, saId, itemId);

        when(dataGroupService.getById(eq(id))).thenReturn(dataGroup);

        DataGroupItemBase response = dataGroupServiceFacade.getDataGroupById(id, false);

        verify(dataGroupService).getById(eq(id));
        assertEquals(dataGroupItemBase, response);
    }

    protected DataGroup getDataGroup(String id, String desc, String name, String dataItemType, String
        serviceAgreementId, String... itemIds) {
        DataGroup value = new DataGroup();
        value.setId(id);
        value.setDescription(desc);
        value.setDataItemType(dataItemType);
        value.setName(name);
        value.setServiceAgreementId(serviceAgreementId);
        value.setDataItemIds(Sets.newHashSet(itemIds));
        return value;
    }

    @Test
    public void testGetDataGroups() {
        String serviceAgreementId = "serviceAgreementId";
        String type = "type";
        boolean includeItems = true;

        when(
            dataGroupService.getByServiceAgreementIdAndDataItemType(eq(serviceAgreementId), eq(type), eq(includeItems)))
            .thenReturn(Collections.singletonList(new DataGroupItemBase().withServiceAgreementId(serviceAgreementId)));
        List<DataGroupItemBase> result = dataGroupServiceFacade.getDataGroups(serviceAgreementId, type, includeItems);
        verify(dataGroupService)
            .getByServiceAgreementIdAndDataItemType(eq(serviceAgreementId), eq(type), eq(includeItems));
        assertEquals(serviceAgreementId, result.get(0).getServiceAgreementId());
    }

    @Test
    public void testGetBulkDataGroups() {
        BulkSearchDataGroupsPostRequestBody requestData = new BulkSearchDataGroupsPostRequestBody()
            .withIds(Sets.newHashSet("1", "2"));
        DataGroup d1 = new DataGroup();
        d1.setId("1");
        DataGroup d2 = new DataGroup();
        d2.setId("2");
        List<DataGroup> response = new ArrayList<>(asList(d1, d2));
        when(dataGroupService.getBulkDataGroups(requestData.getIds())).thenReturn(response);
        List<DataGroupItemBase> bulkDataGroups = dataGroupServiceFacade.getBulkDataGroups(requestData);
        assertEquals(2, bulkDataGroups.size());
    }
}