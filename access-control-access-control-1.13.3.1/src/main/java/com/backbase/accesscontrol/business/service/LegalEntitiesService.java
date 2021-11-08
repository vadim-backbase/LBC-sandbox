package com.backbase.accesscontrol.business.service;

import static java.util.Objects.requireNonNull;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.mappers.ExternalSearchLegalEntityMapper;
import com.backbase.dbs.accesscontrol.api.client.v2.LegalEntitiesApi;
import com.backbase.dbs.accesscontrol.api.client.v2.model.LegalEntityItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LegalEntitiesService {

    private static final String PAGINATION_ITEM_COUNT_HEADER = "X-Total-Count";

    private LegalEntitiesApi legalEntitiesApi;
    private ExternalSearchLegalEntityMapper mapper;

    public ListElementsWrapper<LegalEntityExternalData> getLegalEntities(String field, String term, Integer from,
        String cursor, Integer size) {
        ResponseEntity<List<LegalEntityItem>> integrationResponse = legalEntitiesApi
            .getLegalEntitiesWithHttpInfo(field, term, from, cursor, size);

        List<LegalEntityItem> legalEntities = integrationResponse.getBody();

        Long numberOfRecords = Long
            .parseLong(requireNonNull(integrationResponse.getHeaders().get(PAGINATION_ITEM_COUNT_HEADER)).get(0));

        return new ListElementsWrapper<>(mapper.toPresentation(legalEntities), numberOfRecords);
    }
}
