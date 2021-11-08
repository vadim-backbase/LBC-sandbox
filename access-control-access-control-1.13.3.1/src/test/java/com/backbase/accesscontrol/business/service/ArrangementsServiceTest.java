package com.backbase.accesscontrol.business.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.dbs.arrangement.api.client.v2.ArrangementsApi;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItem;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItems;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsFilter;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountInternalIdGetResponseBody;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArrangementsServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ArrangementsApi arrangementsApi;

    @InjectMocks
    private ArrangementsService arrangementsService;

    @Test
    public void testGetInternalId() {
        String externalId = "externalId";
        String internalId = "internalId";

        when(arrangementsApi.getInternalId(eq(externalId)))
            .thenReturn(new AccountInternalIdGetResponseBody().internalId(internalId));

        String responseInternalId = arrangementsService.getInternalId(externalId);
        assertEquals(internalId, responseInternalId);
    }

    @Test
    public void testPostFilter() {
        List<String> externalIds = asList("ext1", "ext2");

        AccountArrangementsFilter accountArrangementsFilter = new AccountArrangementsFilter()
            .externalArrangementIds(externalIds)
            .size(externalIds.size());

        AccountArrangementItem accountArrangementItem = new AccountArrangementItem();
        accountArrangementItem.setId("id");
        accountArrangementItem.setExternalArrangementId("extId");

        AccountArrangementItems accountArrangementItems = new AccountArrangementItems()
            .arrangementElements(singletonList(accountArrangementItem));

        when(arrangementsApi.postFilter(eq(accountArrangementsFilter)))
            .thenReturn(accountArrangementItems);

        AccountArrangementItems response = arrangementsService.postFilter(accountArrangementsFilter);

        assertEquals(accountArrangementItems, response);
    }

    @Test
    public void testGetArrangementsLegalEntities() {
        List<String> legalEntityIds = asList("le1", "le2");
        List<String> arrangementIds = asList("arr1", "arr2");

        AccountArrangementsLegalEntities accountArrangementsLegalEntities = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(asList(
                new AccountPresentationArrangementLegalEntityIds()
                    .legalEntityIds(legalEntityIds)
                    .arrangementId("arr1"),
                new AccountPresentationArrangementLegalEntityIds()
                    .legalEntityIds(legalEntityIds)
                    .arrangementId("arr2")
            ));

        when(arrangementsApi.getArrangementsLegalEntities(eq(arrangementIds), eq(legalEntityIds)))
            .thenReturn(accountArrangementsLegalEntities);

        AccountArrangementsLegalEntities response = arrangementsService
            .getArrangementsLegalEntities(arrangementIds, legalEntityIds);

        assertEquals(accountArrangementsLegalEntities, response);
    }
}
