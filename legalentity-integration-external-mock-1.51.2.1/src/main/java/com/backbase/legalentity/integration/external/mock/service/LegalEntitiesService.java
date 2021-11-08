package com.backbase.legalentity.integration.external.mock.service;


import static java.util.Arrays.asList;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.integration.legalentity.external.outbound.rest.spec.v2.legalentities.LegalEntityItem;
import com.backbase.legalentity.integration.external.mock.util.PaginationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LegalEntitiesService {

    private ObjectMapper objectMapper;
    private List<LegalEntityItem> legalEntityItems;

    public LegalEntitiesService(ObjectMapper objectMapper) throws IOException {
        this.objectMapper = objectMapper;
        legalEntityItems = asList(
            this.objectMapper.readValue(this.getClass().getResourceAsStream("/all-legal-entities.json"),
                LegalEntityItem[].class));
    }

    public PaginationDto<LegalEntityItem> getLegalEntities(String field, String term, Integer from, String cursor,
        Integer size) {

        List<LegalEntityItem> filtered;
        if (Strings.isNullOrEmpty(field) && Strings.isNullOrEmpty(term)) {
            filtered = legalEntityItems;
        } else if (Strings.isNullOrEmpty(field)) {
            throw new BadRequestException()
                .withMessage("Invalid query parameters")
                .withErrors(asList(new Error()
                    .withMessage("Invalid query parameters")));
        } else if (Strings.isNullOrEmpty(term) && (field.equals("externalId") || field.equals("name"))) {
            filtered = legalEntityItems;
        } else {
            switch (field) {
                case "externalId": {
                    filtered = legalEntityItems.stream().filter(le -> le.getExternalId().contains(term))
                        .collect(Collectors.toList());
                    break;
                }
                case "name": {
                    filtered = legalEntityItems.stream().filter(le -> le.getName().contains(term))
                        .collect(Collectors.toList());
                    break;
                }
                default: {
                    filtered = new ArrayList<>();
                }
            }
        }
        if (from * size > filtered.size()) {
            return new PaginationDto<>(filtered.size(), Collections.emptyList());
        }

        List<LegalEntityItem> paginated = filtered.subList(from * size, Math.min(from * size + size, filtered.size()));

        return new PaginationDto<>(filtered.size(), paginated);

    }

}
