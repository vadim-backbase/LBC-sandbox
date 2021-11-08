package com.backbase.accesscontrol.pandp.it.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_005;
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
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link LegalEntityQueryController#getLegalEntity}
 */
public class GetLegalEntityByIdIT extends TestConfig {

    private static final String LEGAL_ENTITY_BY_ID_URL = "/service-api/v2/accesscontrol/legalentities/";
    private LegalEntity bank;

    @Before
    public void setUp() throws Exception {
        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);

        bank = createLegalEntity(null, "bank", "ex-01", null, LegalEntityType.BANK);
        bank.setAdditions(addition);
        legalEntityJpaRepository.save(bank);
    }

    @Test
    public void shouldGetLegalEntityById() throws Exception {
        String key = "leExternalId";
        String value = "123456789";

        String contentAsString = mockMvc.perform(get(LEGAL_ENTITY_BY_ID_URL + bank.getId())
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        LegalEntityGetResponseBody legalEntityGetResponseBody = objectMapper
            .readValue(contentAsString, LegalEntityGetResponseBody.class);

        assertEquals(bank.getId(), legalEntityGetResponseBody.getId());

        assertThat(legalEntityGetResponseBody.getAdditions().size(), is(1));
        assertTrue(legalEntityGetResponseBody.getAdditions().containsKey(key));
        assertTrue(legalEntityGetResponseBody.getAdditions().containsValue(value));
    }

    @Test
    public void shouldThrowNotFoundException() throws Exception {

        mockMvc.perform(get(LEGAL_ENTITY_BY_ID_URL + UUID.randomUUID().toString())
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isNotFound())
            .andDo(mvcResult -> {
                NotFoundException exception = (NotFoundException) mvcResult.getResolvedException();
                assertEquals(ERR_ACQ_005.getErrorCode(), Objects.requireNonNull(exception).getErrors().get(0).getKey());
                assertEquals(ERR_ACQ_005.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }

}
