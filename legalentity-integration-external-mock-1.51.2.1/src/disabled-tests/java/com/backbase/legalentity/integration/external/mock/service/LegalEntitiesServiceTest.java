package com.backbase.legalentity.integration.external.mock.service;

import static junit.framework.TestCase.assertEquals;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.integration.legalentity.external.outbound.rest.spec.v2.legalentities.LegalEntityItem;
import com.backbase.legalentity.integration.external.mock.util.PaginationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class LegalEntitiesServiceTest {

    @InjectMocks
    private LegalEntitiesService legalEntitiesService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Spy
    private ObjectMapper objectMapper;

    @Test
    public void shouldLoadAllLegalEntitiesFromFileIfFieldAndTermAreEmpty() {

        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService.getLegalEntities("", "", 0, "", 10);
        assertEquals(23, legalEntities.getTotalNumberOfRecords().intValue());
    }

    @Test
    public void shouldThrowBadRequestIfFieldIsEmptyAndTermIsNot() {

        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Invalid query parameters");
        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService
            .getLegalEntities("", "something", 0, "", 10);
    }

    @Test
    public void shouldReturnAllLegalEntitiesIfFieldIsExternalIdAndTermIsEmpty() {
        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService
            .getLegalEntities("externalId", "", 0, "", 10);
        assertEquals(23, legalEntities.getTotalNumberOfRecords().intValue());
    }

    @Test
    public void shouldReturnAllLegalEntitiesIfFieldIsNameAndTermIsEmpty() {
        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService
            .getLegalEntities("name", "", 0, "", 10);
        assertEquals(23, legalEntities.getTotalNumberOfRecords().intValue());
    }

    @Test
    public void shouldReturnEmptyListIfFieldIsNotExternalIdOrName() {
        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService
            .getLegalEntities("something", "", 0, "", 10);
        assertEquals(0, legalEntities.getTotalNumberOfRecords().intValue());
    }

    @Test
    public void shouldReturnFilteredLegalEntitiesByExternalIdAndTerm() {
        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService
            .getLegalEntities("externalId", "CORPCUST", 0, "", 10);
        assertEquals(2, legalEntities.getTotalNumberOfRecords().intValue());
    }

    @Test
    public void shouldReturnEmptyListWhenFromIsBiggerThenTotalSize() {
        PaginationDto<LegalEntityItem> legalEntities = legalEntitiesService
            .getLegalEntities("externalId", "CORPCUST", 4, "", 10);
        assertEquals(2, legalEntities.getTotalNumberOfRecords().intValue());
    }

}