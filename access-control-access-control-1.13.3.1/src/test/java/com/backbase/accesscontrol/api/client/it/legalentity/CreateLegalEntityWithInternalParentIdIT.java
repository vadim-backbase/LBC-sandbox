package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_020;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.LegalEntityController;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.google.common.collect.Sets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link LegalEntityController#postLegalEntities(PresentationCreateLegalEntityItemPostRequestBody,
 * HttpServletRequest, HttpServletResponse)}.
 */
public class CreateLegalEntityWithInternalParentIdIT extends TestDbWireMock {

    private static final String CREATE_LEGAL_ENTITY_WITH_INTERNAL_PARENT_URL = "/legalentities";

    private static final String USER_EXT_ID_ADMIN = "admin";

    @Test
    public void shouldCreateLegalEntityByInternalParentId() throws Exception {

        String parentLegalEntityId = rootLegalEntity.getId();
        PresentationCreateLegalEntityItemPostRequestBody legalEntitiesPostRequestBody =
            new PresentationCreateLegalEntityItemPostRequestBody()
                .withName("name")
                .withActivateSingleServiceAgreement(true)
                .withExternalId("external")
                .withParentInternalId(parentLegalEntityId)
                .withType(LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setAdditions(null);

        String contentAsString = executeClientRequest(CREATE_LEGAL_ENTITY_WITH_INTERNAL_PARENT_URL,
            HttpMethod.POST, legalEntitiesPostRequestBody, USER_EXT_ID_ADMIN,
            MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_CREATE);

        LegalEntitiesPostResponseBody responseBody = readValue(contentAsString,
            LegalEntitiesPostResponseBody.class);
        assertNotNull(responseBody);
        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(ADD)
            .withId(responseBody.getId())));
    }

    @Test
    public void shouldThrowBadRequestForExternalId() {
        PresentationCreateLegalEntityItemPostRequestBody request = new PresentationCreateLegalEntityItemPostRequestBody()
            .withActivateSingleServiceAgreement(true)
            .withName("Backbase")
            .withParentInternalId(rootLegalEntity.getId())
            .withType(LegalEntityType.CUSTOMER);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(CREATE_LEGAL_ENTITY_WITH_INTERNAL_PARENT_URL, HttpMethod.POST, request, "user",
                MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_CREATE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_LE_020.getErrorMessage(), ERR_LE_020.getErrorCode()));
    }
}
