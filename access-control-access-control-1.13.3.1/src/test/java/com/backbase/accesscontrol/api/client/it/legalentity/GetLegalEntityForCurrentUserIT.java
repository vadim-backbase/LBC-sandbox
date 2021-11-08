package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetLegalEntityForCurrentUserIT extends TestDbWireMock {

    private static final String GET_LEGAL_ENTITY_FOR_CURRENT_USER = "/legalentities/me";

    private static final String EXTERNAL_ID = "BANK001";
    private static final String LEGAL_ENTITY_NAME = "BANK";

    @Test
    public void shouldReturnLegalEntity() throws Exception {
        String legalEntityId = rootLegalEntity.getId();

        String contentAsString = executeClientRequest(GET_LEGAL_ENTITY_FOR_CURRENT_USER, HttpMethod.GET, "admin",
            MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW);

        LegalEntityForUserGetResponseBody legalEntityForUserGetResponseBody = readValue(contentAsString,
            LegalEntityForUserGetResponseBody.class);

        assertEquals(legalEntityId, legalEntityForUserGetResponseBody.getId());
        assertEquals(EXTERNAL_ID, legalEntityForUserGetResponseBody.getExternalId());
        assertEquals(LEGAL_ENTITY_NAME, legalEntityForUserGetResponseBody.getName());
    }
}
