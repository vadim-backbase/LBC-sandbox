package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_063;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_064;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_065;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.accesscontrol.api.client.v2.model.LegalEntityItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class ExternalSearchLegalEntityIT extends TestDbWireMock {

    private static final String EXTERNAL_SEARCH_LEGAL_ENTITY_URL = "/legalentities/external-data?field=f&term=t&from=0&size=10";
    private static final String LEGAL_ENTITY_SEARCH_INTEGRATION_URL = "/legal-entities?field=f&term=t&from=0&size=10";

    @Test
    public void shouldGetLegalEntitiesWithAdditionalPropertiesAndPersistenceValidation()
        throws IOException, JSONException {

        List<LegalEntityItem> externalLegalEntities = Lists.newArrayList(
            new LegalEntityItem()
                .externalId("e1")
                .name("name1")
                .additions(new HashMap<>() {{
                    put("address", "address1");
                }}),
            new LegalEntityItem()
                .externalId("e2")
                .name("name2")
                .additions(new HashMap<>() {{
                    put("phone", "12345");
                }}));
        HashMap<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("X-Total-Count", String.valueOf(externalLegalEntities.size()));
        addStubGet(baseServiceUrl + LEGAL_ENTITY_SEARCH_INTEGRATION_URL,
            objectMapper.writeValueAsString(externalLegalEntities),
            HttpStatus.OK.value(),
            new HashMap<>(),
            responseHeaders);

        String response = executeClientRequest(EXTERNAL_SEARCH_LEGAL_ENTITY_URL, HttpMethod.GET, null,
            MANAGE_LEGAL_ENTITY_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<LegalEntityExternalData> result = objectMapper
            .readValue(response, new TypeReference<List<LegalEntityExternalData>>() {
            });

        List<LegalEntityExternalData> expected = Lists.newArrayList(
            (LegalEntityExternalData) new LegalEntityExternalData().withExternalId("e1")
                .withName("name1")
                .withAddition("address", "address1"),
            (LegalEntityExternalData) new LegalEntityExternalData()
                .withExternalId("e2")
                .withName("name2")
                .withAddition("phone", "12345"));

        assertEquals(expected, result);
    }

    @Test
    public void shouldFailNegativeFromValue() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            "/legalentities/external-data?csrf=0d22a906-a963-491d-bf20-eeee464caaeb&field=name&term=Bank&size=3&from=-1&query=",
            HttpMethod.GET, "user", MANAGE_LEGAL_ENTITY_FUNCTION_NAME, PRIVILEGE_VIEW));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_063.getErrorMessage(), ERR_AG_063.getErrorCode()));
    }

    @Test
    public void shouldFail1FromNotDefined() {

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            "/legalentities/external-data?csrf=0d22a906-a963-491d-bf20-eeee464caaeb&field=name&term=Bank&size=3&query=",
            HttpMethod.GET, "user", MANAGE_LEGAL_ENTITY_FUNCTION_NAME, PRIVILEGE_VIEW));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_065.getErrorMessage(), ERR_AG_065.getErrorCode()));
    }

    @Test
    public void shouldFailNegativeSize() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            "/legalentities/external-data?csrf=0d22a906-a963-491d-bf20-eeee464caaeb&field=name&term=Bank&size=-3&query=",
            HttpMethod.GET, "user", MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_064.getErrorMessage(), ERR_AG_064.getErrorCode()));
    }

}
