package com.backbase.accesscontrol.business.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.mappers.ExternalSearchLegalEntityMapper;
import com.backbase.dbs.accesscontrol.api.client.v2.LegalEntitiesApi;
import com.backbase.dbs.accesscontrol.api.client.v2.model.LegalEntityItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntitiesServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LegalEntitiesApi legalEntitiesApi;

    @Spy
    private ExternalSearchLegalEntityMapper mapper = Mappers.getMapper(ExternalSearchLegalEntityMapper.class);

    @InjectMocks
    private LegalEntitiesService legalEntitiesService;

    @Test
    public void testGetLegalEntities() {
        String field = "field";
        String term = "term";
        int from = 0;
        String cursor = "cursor";
        int size = 10;
        Long totalCount = 100L;

        List<LegalEntityExternalData> expectedExternalLegalEntities = Lists.newArrayList(
            new LegalEntityExternalData().withExternalId("e1"),
            new LegalEntityExternalData().withExternalId("e2"));

        List<LegalEntityItem> externalLegalEntities = Lists.newArrayList(
            new LegalEntityItem().externalId("e1"),
            new LegalEntityItem().externalId("e2"));

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Total-Count", String.valueOf(totalCount));

        when(legalEntitiesApi.getLegalEntitiesWithHttpInfo(eq(field), eq(term), eq(from), eq(cursor), eq(size)))
            .thenReturn(new ResponseEntity<>(externalLegalEntities, headers, HttpStatus.OK));

        ListElementsWrapper<LegalEntityExternalData> response = legalEntitiesService
            .getLegalEntities(field, term, from, cursor, size);

        assertEquals(expectedExternalLegalEntities, response.getRecords());
        assertEquals(totalCount, response.getTotalNumberOfRecords());
    }
}
