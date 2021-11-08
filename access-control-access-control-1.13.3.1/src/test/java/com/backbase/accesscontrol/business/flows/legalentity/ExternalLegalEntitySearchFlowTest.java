package com.backbase.accesscontrol.business.flows.legalentity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.LegalEntitiesService;
import com.backbase.accesscontrol.dto.ExternalLegalEntitySearchParameters;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExternalLegalEntitySearchFlowTest {

    private static final String PAGINATION_ITEM_COUNT_HEADER = "X-Total-Count";

    private ExternalLegalEntitySearchFlow testy;
    @Mock
    private LegalEntitiesService legalEntitiesService;
    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Test
    public void shouldExecuteFlowWithEnabledPersistenceValidation() {
        String field = "field";
        String term = "term";
        int from = 0;
        String cursor = "cursor";
        int size = 10;
        Long totalCount = 100L;

        testy = new ExternalLegalEntitySearchFlow(true, legalEntitiesService, persistenceLegalEntityService);

        List<LegalEntityExternalData> externalLegalEntities = Lists.newArrayList(
            new LegalEntityExternalData().withExternalId("e1"),
            new LegalEntityExternalData().withExternalId("e2"));

        when(legalEntitiesService.getLegalEntities(eq(field), eq(term), eq(from), eq(cursor), eq(size)))
            .thenReturn(new ListElementsWrapper<>(externalLegalEntities, totalCount));

        Map<String, String> persistenceResponse = new HashMap<>();
        persistenceResponse.put("e1", "id1");
        persistenceResponse.put("e2", "id2");

        when(persistenceLegalEntityService.findInternalByExternalIdsForLegalEntity(eq(Sets.newHashSet("e1", "e2"))))
            .thenReturn(persistenceResponse);

        RecordsDto<LegalEntityExternalData> res = testy
            .start(new ExternalLegalEntitySearchParameters(field, term, from, cursor, size));

        verify(legalEntitiesService).getLegalEntities(eq(field), eq(term), eq(from), eq(cursor), eq(size));

        List<Entry<String, String>> entries = new ArrayList<>(persistenceResponse.entrySet());

        assertThat(res.getRecords(), containsInAnyOrder(
            allOf(
                hasProperty("externalId", is(entries.get(0).getKey())),
                hasProperty("id", is(entries.get(0).getValue()))
            ),
            allOf(
                hasProperty("externalId", is(entries.get(1).getKey())),
                hasProperty("id", is(entries.get(1).getValue()))
            )
        ));
        assertEquals(totalCount, res.getTotalNumberOfRecords());
    }

    @Test
    public void shouldExecuteFlowWithDisabledPersistenceValidation() {
        String field = "field";
        String term = "term";
        int from = 0;
        String cursor = "cursor";
        int size = 10;
        Long totalCount = 100L;

        testy = new ExternalLegalEntitySearchFlow(false, legalEntitiesService, persistenceLegalEntityService);

        List<LegalEntityExternalData> externalLegalEntities = Lists.newArrayList(
            new LegalEntityExternalData().withExternalId("e1"),
            new LegalEntityExternalData().withExternalId("e2"));

        when(legalEntitiesService.getLegalEntities(eq(field), eq(term), eq(from), eq(cursor), eq(size)))
            .thenReturn(new ListElementsWrapper<>(externalLegalEntities, totalCount));

        List<LegalEntityExternalData> presentationResponse = Lists.newArrayList(
            new LegalEntityExternalData().withExternalId("e1"),
            new LegalEntityExternalData().withExternalId("e2"));

        RecordsDto<LegalEntityExternalData> res = testy
            .start(new ExternalLegalEntitySearchParameters(field, term, from, cursor, size));

        verify(legalEntitiesService).getLegalEntities(eq(field), eq(term), eq(from), eq(cursor), eq(size));
        assertEquals(presentationResponse, res.getRecords());
        assertEquals(totalCount, res.getTotalNumberOfRecords());
    }
}
