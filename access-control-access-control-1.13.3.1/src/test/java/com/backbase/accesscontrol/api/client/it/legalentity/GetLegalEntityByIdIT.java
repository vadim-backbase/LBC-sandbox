package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetLegalEntityByIdIT extends TestDbWireMock {

    private static final String GET_LEGAL_ENTITY_BY_ID = "/legalentities/{legalEntityId}";

    @Test
    public void shouldReturnLegalEntity() throws Exception {
        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);

        LegalEntity le = createLegalEntity(null, "bank", "ex-11", rootLegalEntity, LegalEntityType.BANK);
        le.setAdditions(addition);
        legalEntityJpaRepository.save(le);

        String contentAsString = executeClientRequest(
            new UrlBuilder(GET_LEGAL_ENTITY_BY_ID).addPathParameter(le.getId()).build(), HttpMethod.GET, "admin",
            MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody legalEntityGetResponseBody = readValue(
            contentAsString,
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody.class);

        assertEquals(le.getId(), legalEntityGetResponseBody.getId());
        assertEquals(le.getExternalId(), legalEntityGetResponseBody.getExternalId());
        assertEquals(le.getName(), legalEntityGetResponseBody.getName());
        assertNotNull(legalEntityGetResponseBody.getParentId());

        assertEquals(1, legalEntityGetResponseBody.getAdditions().size());
        assertTrue(legalEntityGetResponseBody.getAdditions().containsKey(key));
        assertTrue(legalEntityGetResponseBody.getAdditions().containsValue(value));
    }
}
