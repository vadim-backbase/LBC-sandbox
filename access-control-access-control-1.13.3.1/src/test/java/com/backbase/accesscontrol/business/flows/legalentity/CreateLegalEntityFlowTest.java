package com.backbase.accesscontrol.business.flows.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.BAD_REQUEST_MESSAGE;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_020;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.legalentity.CreateLegalEntityWithInternalParentIdHandler;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityFlowTest {

    @Mock
    private CreateLegalEntityWithInternalParentIdHandler createLegalEntityWithInternalParentIdHandler;

    private CreateLegalEntityFlow testy;

    @Before
    public void setUp() {

        testy = new CreateLegalEntityFlow(createLegalEntityWithInternalParentIdHandler);
    }

    @Test
    public void shouldCreateLegalEntity() {
        PresentationCreateLegalEntityItemPostRequestBody legalEntityObject =
            new PresentationCreateLegalEntityItemPostRequestBody()
                .withExternalId("externalId")
                .withActivateSingleServiceAgreement(true)
                .withName("name")
                .withParentInternalId("5643e686d3ae4216b3ff5d66a6ad897d")
                .withType(LegalEntityType.BANK);

        when(createLegalEntityWithInternalParentIdHandler.handleRequest(any(EmptyParameterHolder.class),
            any(PresentationCreateLegalEntityItemPostRequestBody.class)))
            .thenReturn(new LegalEntitiesPostResponseBody().withId("responseId"));

        LegalEntitiesPostResponseBody responseFromExecute = testy.execute(legalEntityObject);

        assertNotNull(responseFromExecute);
        assertEquals("responseId", responseFromExecute.getId());
    }


    @Test
    public void shouldThrowBadRequestFromPersistence() {
        PresentationCreateLegalEntityItemPostRequestBody legalEntityObject = new PresentationCreateLegalEntityItemPostRequestBody()
            .withExternalId("externalId")
            .withActivateSingleServiceAgreement(true)
            .withName("name")
            .withParentInternalId("5643e686d3ae4216b3ff5d66a6ad897d")
            .withType(LegalEntityType.BANK);

        doThrow(new BadRequestException().withMessage(BAD_REQUEST_MESSAGE)
            .withErrors(Collections.singletonList(new Error().withMessage(BAD_REQUEST_MESSAGE).withKey("errorKey"))))
            .when(createLegalEntityWithInternalParentIdHandler).handleRequest(any(EmptyParameterHolder.class),
            any(PresentationCreateLegalEntityItemPostRequestBody.class));

        assertThrows(BadRequestException.class,
            () -> testy.execute(legalEntityObject));
    }


    @Test
    public void shouldThrowBadRequestWhenBankDoesNotProvideExternalId() {
        PresentationCreateLegalEntityItemPostRequestBody legalEntityObject = new PresentationCreateLegalEntityItemPostRequestBody()
            .withActivateSingleServiceAgreement(true)
            .withName("name")
            .withParentInternalId("5643e686d3ae4216b3ff5d66a6ad897d")
            .withType(LegalEntityType.BANK);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.execute(legalEntityObject));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_LE_020.getErrorMessage(), ERR_LE_020.getErrorCode())));
    }
}