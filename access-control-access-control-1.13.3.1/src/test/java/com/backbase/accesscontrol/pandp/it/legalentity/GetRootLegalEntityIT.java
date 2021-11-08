package com.backbase.accesscontrol.pandp.it.legalentity;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.LegalEntityQueryController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.RootLegalEntityGetResponseBody;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link LegalEntityQueryController#getRootLegalEntity}
 */
public class GetRootLegalEntityIT extends TestConfig {

    private static final String GET_ROOT_LEGAL_ENTITY_URL = "/service-api/v2/accesscontrol/legalentities/root";

    private LegalEntity legalEntity;

    @Before
    public void setUp() throws Exception {
        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);

        legalEntity = createLegalEntity(null, "legalEntity", "ex-01", null, LegalEntityType.BANK);
        legalEntity.setAdditions(addition);
        legalEntityJpaRepository.save(legalEntity);
    }

    @Test
    public void shouldGetRootLegalEntity() throws Exception {
        String key = "leExternalId";
        String value = "123456789";

        String contentAsString = mockMvc.perform(get(GET_ROOT_LEGAL_ENTITY_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        RootLegalEntityGetResponseBody legalEntityGetResponseBody = objectMapper
            .readValue(contentAsString, RootLegalEntityGetResponseBody.class);

        assertEquals(legalEntity.getId(), legalEntityGetResponseBody.getId());

        assertThat(legalEntityGetResponseBody.getAdditions().size(), is(1));
        assertTrue(legalEntityGetResponseBody.getAdditions().containsKey(key));
        assertTrue(legalEntityGetResponseBody.getAdditions().containsValue(value));
    }

}
