package com.backbase.accesscontrol.api.service.it.legalentity;

import static com.backbase.accesscontrol.domain.enums.LegalEntityType.CUSTOMER;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.LegalEntityServiceApiController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link LegalEntityServiceApiController#getSubEntities}.
 */
public class ListSubLegalEntitiesIT extends TestDbWireMock {

    private static final String SUB_LEGAL_ENTITIES_ENDPOINT_URL = "/legalentities/sub-entities";

    private LegalEntity bank;

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
        legalEntityJpaRepository.save(companyA1UnderA);
    }

    @Test
    public void shouldGetSubEntities() throws Exception {
        String key = "leExternalId";
        String value = "123456789";

        String response = executeServiceRequest(new UrlBuilder(SUB_LEGAL_ENTITIES_ENDPOINT_URL)
                .addQueryParameter("parentEntityId", bank.getId()).build(), null, "admin",
            rootMsa.getId(), contextUserId, rootLegalEntity.getId(), HttpMethod.GET, new HashMap<>());

        List<SubEntitiesGetResponseBody> data = objectMapper.readValue(response,
            new TypeReference<List<SubEntitiesGetResponseBody>>() {
            });

        assertEquals(4, data.size());
        SubEntitiesGetResponseBody subEntity = data.stream()
            .filter(le -> !le.getId().equals(rootLegalEntity.getId()))
            .findFirst().get();
        assertThat(subEntity.getAdditions().size(), is(2));
        assertTrue(subEntity.getAdditions().containsKey(key));
        assertTrue(subEntity.getAdditions().containsValue(value));
    }
}
