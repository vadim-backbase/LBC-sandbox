package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.domain.enums.LegalEntityType.CUSTOMER;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.LegalEntityController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SearchSubEntitiesParameters;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Test for {@link LegalEntityController#postSubEntities}.
 */
public class ListSubLegalEntitiesWithExcludedIdsIT extends TestDbWireMock {

    private static final String SUB_LEGAL_ENTITIES_ENDPOINT_URL = "/legalentities/sub-entities";
    private static final String PAGINATION_ITEM_COUNT_HEADER = "X-Total-Count";

    private LegalEntity bank;
    private String leId;

    @Before
    public void setUp() throws Exception {

        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);
        addition.put("second", "...");

        bank = rootLegalEntity;

        LegalEntity companyAUnderBank = createLegalEntity(null, "companyAUnderBank", "companyAUnderBank", bank,
            CUSTOMER);
        companyAUnderBank.setAdditions(addition);
        LegalEntity companyBUnderBank = createLegalEntity(null, "companyBUnderBank", "companyBUnderBank", bank,
            CUSTOMER);
        companyBUnderBank.setAdditions(addition);
        LegalEntity companyA1UnderA = createLegalEntity(null, "companyA1UnderA", "companyA1UnderA", companyAUnderBank,
            CUSTOMER);
        companyA1UnderA.setAdditions(addition);

        legalEntityJpaRepository.save(companyAUnderBank);
        legalEntityJpaRepository.save(companyBUnderBank);
        leId = legalEntityJpaRepository.save(companyA1UnderA).getId();
    }

    @Test
    public void shouldGetSubEntities() throws Exception {
        String key = "leExternalId";
        String value = "123456789";

        SearchSubEntitiesParameters entityIds = new SearchSubEntitiesParameters().withParentEntityId(bank.getId());

        ResponseEntity<String> response = executeClientRequestEntity(SUB_LEGAL_ENTITIES_ENDPOINT_URL, HttpMethod.POST,
            getStringFromObject(entityIds),
            "admin", MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<SubEntitiesPostResponseBody> data = objectMapper.readValue(response.getBody(),
            new TypeReference<List<SubEntitiesPostResponseBody>>() {
            });

        assertEquals(String.valueOf(4), response.getHeaders().get(PAGINATION_ITEM_COUNT_HEADER).get(0));
        assertEquals(4, data.size());
        SubEntitiesPostResponseBody subEntity = data.stream()
            .filter(le -> !le.getId().equals(rootLegalEntity.getId()))
            .findFirst().get();
        assertThat(subEntity.getAdditions().size(), is(2));
        assertTrue(subEntity.getAdditions().containsKey(key));
        assertTrue(subEntity.getAdditions().containsValue(value));
    }

    @Test
    public void shouldGetSubEntitiesWithSearchAndPagination() throws Exception {

        SearchSubEntitiesParameters entityIds = new SearchSubEntitiesParameters()
            .withFrom(0)
            .withSize(3)
            .withParentEntityId(bank.getId());

        ResponseEntity<String> response = executeClientRequestEntity(SUB_LEGAL_ENTITIES_ENDPOINT_URL, HttpMethod.POST,
            getStringFromObject(entityIds),
            "admin", MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<SubEntitiesPostResponseBody> data = objectMapper.readValue(response.getBody(),
            new TypeReference<List<SubEntitiesPostResponseBody>>() {
            });

        assertEquals(String.valueOf(4), response.getHeaders().get(PAGINATION_ITEM_COUNT_HEADER).get(0));
        assertEquals(3, data.size());
    }

    @Test
    public void shouldSearchWithSpecialCharacters() throws Exception {
        LegalEntity legalEntity = createLegalEntity(null, "company-RsnpY!@#$%^&*()_",
            "newCompany", bank, LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.saveAndFlush(legalEntity);

        SearchSubEntitiesParameters entityIds = new SearchSubEntitiesParameters()
            .withFrom(0)
            .withSize(1)
            .withQuery("RsnpY!@#$%^&*()_")
            .withParentEntityId(bank.getId());

        ResponseEntity<String> response = executeClientRequestEntity(SUB_LEGAL_ENTITIES_ENDPOINT_URL, HttpMethod.POST,
            getStringFromObject(entityIds),
            "admin", MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<SubEntitiesPostResponseBody> data = objectMapper.readValue(response.getBody(),
            new TypeReference<List<SubEntitiesPostResponseBody>>() {
            });

        assertEquals(String.valueOf(1), response.getHeaders().get(PAGINATION_ITEM_COUNT_HEADER).get(0));
        assertEquals(1, data.size());
    }

    @Test
    public void shouldGetSubEntitiesWithExcludedLegalEntitiesBank() throws Exception {

        SearchSubEntitiesParameters entityIds = new SearchSubEntitiesParameters()
            .withExcludeIds(Sets.newHashSet(bank.getId()))
            .withParentEntityId(bank.getId());

        ResponseEntity<String> response = executeClientRequestEntity(SUB_LEGAL_ENTITIES_ENDPOINT_URL, HttpMethod.POST,
            getStringFromObject(entityIds),
            "admin", MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<SubEntitiesPostResponseBody> data = objectMapper.readValue(response.getBody(),
            new TypeReference<List<SubEntitiesPostResponseBody>>() {
            });

        assertEquals(String.valueOf(3), response.getHeaders().get(PAGINATION_ITEM_COUNT_HEADER).get(0));
        assertEquals(3, data.size());
    }

    @Test
    public void shouldGetSubEntitiesWithExcludedLegalEntities() throws Exception {

        SearchSubEntitiesParameters entityIds = new SearchSubEntitiesParameters()
            .withExcludeIds(Sets.newHashSet(leId))
            .withParentEntityId(bank.getId());

        ResponseEntity<String> response = executeClientRequestEntity(SUB_LEGAL_ENTITIES_ENDPOINT_URL, HttpMethod.POST,
            getStringFromObject(entityIds),
            "admin", MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<SubEntitiesPostResponseBody> data = objectMapper.readValue(response.getBody(),
            new TypeReference<List<SubEntitiesPostResponseBody>>() {
            });

        assertEquals(String.valueOf(3), response.getHeaders().get(PAGINATION_ITEM_COUNT_HEADER).get(0));
        assertEquals(3, data.size());
    }
}
