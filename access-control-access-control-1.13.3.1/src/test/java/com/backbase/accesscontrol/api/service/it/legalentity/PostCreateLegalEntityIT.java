package com.backbase.accesscontrol.api.service.it.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_004;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_007;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.LegalEntityServiceApiController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link LegalEntityServiceApiController#postCreateLegalEntities}.
 */
public class PostCreateLegalEntityIT extends TestDbWireMock {

    private static String LEGAL_ENTITY_CREATE_URL = "/legalentities/create";

    @Test
    public void shouldSuccessfullySaveRootLegalEntity() throws Exception {

        tearDown();

        CreateLegalEntitiesPostRequestBody legalEntityPostRequestBody = new CreateLegalEntitiesPostRequestBody()
            .withName("BANK1")
            .withExternalId("BANK1")
            .withType(LegalEntityType.BANK);

        String contentAsString = executeServiceRequest(
            new UrlBuilder(LEGAL_ENTITY_CREATE_URL)
                .build(), legalEntityPostRequestBody, "USER", rootMsa.getId(), HttpMethod.POST);

        String result = objectMapper.readValue(contentAsString, CreateLegalEntitiesPostResponseBody.class).getId();

        assertNotNull(result);

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(ADD)
            .withId(result)));
    }

    @Test
    public void shouldSuccessfullySaveLegalEntityWithParent() throws Exception {
        CreateLegalEntitiesPostRequestBody legalEntityPostRequestBody = new CreateLegalEntitiesPostRequestBody()
            .withName("leId2")
            .withExternalId("external-le-id2")
            .withParentExternalId(rootLegalEntity.getExternalId())
            .withActivateSingleServiceAgreement(true)
            .withType(LegalEntityType.CUSTOMER);

        String contentAsString = executeServiceRequest(
            new UrlBuilder(LEGAL_ENTITY_CREATE_URL)
                .build(), legalEntityPostRequestBody, "USER", rootMsa.getId(), HttpMethod.POST);

        String result = objectMapper.readValue(contentAsString, CreateLegalEntitiesPostResponseBody.class).getId();

        assertNotNull(result);

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(ADD)
            .withId(result)));
    }

    @Test
    public void shouldThrowBadRequestWithInvalidParentId() {
        CreateLegalEntitiesPostRequestBody legalEntityPostRequestBody = new CreateLegalEntitiesPostRequestBody()
            .withName("leId3")
            .withExternalId("external-le-id3")
            .withParentExternalId("invalidParentLeId")
            .withActivateSingleServiceAgreement(true)
            .withType(LegalEntityType.CUSTOMER);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeServiceRequest(
                new UrlBuilder(LEGAL_ENTITY_CREATE_URL)
                    .build(), legalEntityPostRequestBody, "USER", rootMsa.getId(), HttpMethod.POST));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_007.getErrorMessage(), ERR_ACC_007.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenLegalEntityAlreadyExists() {

        legalEntityJpaRepository.save(new LegalEntity()
            .withExternalId("BANK1")
            .withName("BANK1")
            .withType(com.backbase.accesscontrol.domain.enums.LegalEntityType.BANK));

        CreateLegalEntitiesPostRequestBody legalEntityPostRequestBody = new CreateLegalEntitiesPostRequestBody()
            .withName("BANK1")
            .withExternalId("BANK1")
            .withParentExternalId(rootLegalEntity.getExternalId())
            .withType(LegalEntityType.BANK);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeServiceRequest(
                new UrlBuilder(LEGAL_ENTITY_CREATE_URL)
                    .build(), legalEntityPostRequestBody, "USER", rootMsa.getId(), HttpMethod.POST));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_004.getErrorMessage(), ERR_ACC_004.getErrorCode()));
    }
}
