package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class ListLegalEntitiesIT extends TestDbWireMock {

    private static final String LEGAL_ENTITIES_ENDPOINT_URL = "/legalentities";
    private LegalEntity companyAUnderBank;
    private LegalEntity companyA1UnderBank;

    @Before
    public void setUp() {

        companyAUnderBank = createLegalEntity(null, "companyAUnderBank", "companyAUnderBank", rootLegalEntity,
            LegalEntityType.CUSTOMER);
        companyA1UnderBank = createLegalEntity(null, "companyA1UnderA", "companyA1UnderA", rootLegalEntity,
            LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(companyAUnderBank);
        legalEntityJpaRepository.save(companyA1UnderBank);
        legalEntityJpaRepository.flush();
    }

    @Test
    public void shouldReturnChildLegalEntitiesUnderQueriedParentEntityId() throws Exception {
        String leId = rootLegalEntity.getId();
        String contentAsString = executeClientRequest(
            new UrlBuilder(LEGAL_ENTITIES_ENDPOINT_URL).addQueryParameter("parentEntityId", leId).build(),
            HttpMethod.GET,
            "admin", MANAGE_LEGAL_ENTITY_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<LegalEntitiesGetResponseBody> legalEntities = readValue(contentAsString,
            new TypeReference<List<LegalEntitiesGetResponseBody>>() {
            });
        LegalEntitiesGetResponseBody legalEntitiesGetResponseBody1 = legalEntities.get(0);
        LegalEntitiesGetResponseBody legalEntitiesGetResponseBody2 = legalEntities.get(1);
        assertEquals(2, legalEntities.size());
        assertEquals(legalEntitiesGetResponseBody1.getName(), companyAUnderBank.getName());
        assertEquals(legalEntitiesGetResponseBody2.getName(), companyA1UnderBank.getName());
    }

    @Test
    public void shouldThrownForbiddenWhenParentLeIsEmpty() {

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> executeClientRequest(
            new UrlBuilder(LEGAL_ENTITIES_ENDPOINT_URL).addQueryParameter("parentEntityId", "").build(),
            HttpMethod.GET,
            contextUserId, MANAGE_LEGAL_ENTITY_FUNCTION_NAME, PRIVILEGE_VIEW));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode()));
    }
}
