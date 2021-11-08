package com.backbase.accesscontrol.api.service.it.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_004;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_007;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.LegalEntityServiceApiController;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link LegalEntityServiceApiController#postLegalEntities}.
 */
public class PostLegalEntityIT extends TestDbWireMock {

    private static String LEGAL_ENTITY_URL = "/legalentities";

    @Test
    public void shouldSuccessfullySaveRootLegalEntity() throws Exception {
        tearDown();

        LegalEntitiesPostRequestBody postData = new LegalEntitiesPostRequestBody()
            .withName("BANK")
            .withType(LegalEntityType.BANK)
            .withExternalId("BANK001");

        String contentAsString = executeServiceRequest(
            new UrlBuilder(LEGAL_ENTITY_URL)
                .build(), postData, "USER", rootMsa.getId(), HttpMethod.POST);

        String result = objectMapper.readValue(contentAsString, LegalEntitiesPostResponseBody.class).getId();

        assertNotNull(result);

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(ADD)
            .withId(result)));
    }

    @Test
    public void shouldSuccessfullySaveLegalEntityWithParent() throws Exception {

        LegalEntitiesPostRequestBody postData = new LegalEntitiesPostRequestBody()
            .withExternalId("childExId")
            .withName("Child")
            .withActivateSingleServiceAgreement(true)
            .withType(
                LegalEntityType.CUSTOMER)
            .withParentExternalId(rootLegalEntity.getExternalId());

        String contentAsString = executeServiceRequest(
            new UrlBuilder(LEGAL_ENTITY_URL)
                .build(), postData, "USER", rootMsa.getId(), HttpMethod.POST);

        String result = objectMapper.readValue(contentAsString, LegalEntitiesPostResponseBody.class).getId();

        assertNotNull(result);

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(ADD)
            .withId(result)));
    }

    @Test
    public void shouldThrowBadRequestWithInvalidParentId() {

        LegalEntitiesPostRequestBody postData = new LegalEntitiesPostRequestBody()
            .withExternalId("childExId")
            .withName("Child")
            .withType(
                LegalEntityType.CUSTOMER)
            .withParentExternalId(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeServiceRequest(
            new UrlBuilder(LEGAL_ENTITY_URL)
                .build(), postData, "USER", rootMsa.getId(), HttpMethod.POST));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_007.getErrorMessage(), ERR_ACC_007.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenLegalEntityAlreadyExists() {

        LegalEntitiesPostRequestBody postData = new LegalEntitiesPostRequestBody()
            .withExternalId("BANK001")
            .withName("BANK")
            .withType(LegalEntityType.BANK)
            .withParentExternalId(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeServiceRequest(
            new UrlBuilder(LEGAL_ENTITY_URL)
                .build(), postData, "USER", rootMsa.getId(), HttpMethod.POST));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_004.getErrorMessage(), ERR_ACC_004.getErrorCode()));
    }
}
