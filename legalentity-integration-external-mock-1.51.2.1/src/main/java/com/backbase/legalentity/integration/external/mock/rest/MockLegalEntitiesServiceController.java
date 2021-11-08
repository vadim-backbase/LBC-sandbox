package com.backbase.legalentity.integration.external.mock.rest;

import com.backbase.integration.legalentity.external.outbound.rest.spec.serviceapi.v2.legalentities.LegalEntitiesApi;
import com.backbase.integration.legalentity.external.outbound.rest.spec.v2.legalentities.LegalEntityItem;
import com.backbase.legalentity.integration.external.mock.service.LegalEntitiesService;
import com.backbase.legalentity.integration.external.mock.util.PaginationDto;
import com.backbase.legalentity.integration.external.mock.util.ParameterValidationUtil;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockLegalEntitiesServiceController implements LegalEntitiesApi {

    private static final String PAGINATION_ITEM_COUNT_HEADER = "X-Total-Count";

    private LegalEntitiesService legalEntitiesService;
    private ParameterValidationUtil validationUtil;

    public MockLegalEntitiesServiceController(
        LegalEntitiesService legalEntitiesService,
        ParameterValidationUtil validationUtil) {
        this.legalEntitiesService = legalEntitiesService;
        this.validationUtil = validationUtil;
    }

    @Override
    public ResponseEntity<List<LegalEntityItem>> getLegalEntities(@Valid String field, @Valid String term,
        @Valid Integer from, @Valid String cursor, @Valid Integer size) {

        validationUtil.validateFromAndSizeParameter(from, size);
        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService
            .getLegalEntities(field, term, from, cursor, size);

        return ResponseEntity.ok()
            .header(PAGINATION_ITEM_COUNT_HEADER, String.valueOf(legalEntities.getTotalNumberOfRecords()))
            .body(legalEntities.getRecords());
    }
}
