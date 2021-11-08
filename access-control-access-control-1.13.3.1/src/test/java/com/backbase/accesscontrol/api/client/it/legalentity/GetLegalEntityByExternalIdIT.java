package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetLegalEntityByExternalIdIT extends TestDbWireMock {

    private static final String GET_LEGAL_ENTITY_BY_EXTERNAL_ID = "/legalentities/external/{externalId}";

    @Test
    public void shouldReturnLegalEntityByExternalId() throws Exception {
        String externalId = "ex-11";
        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);

        LegalEntity le = createLegalEntity(null, "bank", externalId, rootLegalEntity, LegalEntityType.BANK);
        le.setAdditions(addition);
        legalEntityJpaRepository.save(le);

        String contentAsString = executeClientRequest(
            new UrlBuilder(GET_LEGAL_ENTITY_BY_EXTERNAL_ID).addPathParameter(externalId).build(), HttpMethod.GET,
            "USER",
            MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        LegalEntityByExternalIdGetResponseBody responseBody =
            readValue(contentAsString, LegalEntityByExternalIdGetResponseBody.class);

        assertEquals(le.getId(), responseBody.getId());
        assertEquals(le.getExternalId(), responseBody.getExternalId());
        assertEquals(le.getName(), responseBody.getName());

        assertEquals(1, responseBody.getAdditions().size());
        assertTrue(responseBody.getAdditions().containsKey(key));
        assertTrue(responseBody.getAdditions().containsValue(value));
    }
}
