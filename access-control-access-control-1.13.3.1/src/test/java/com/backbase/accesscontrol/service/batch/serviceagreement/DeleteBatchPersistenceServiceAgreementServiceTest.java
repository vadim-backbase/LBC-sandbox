package com.backbase.accesscontrol.service.batch.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_054;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import com.backbase.accesscontrol.util.validation.AccessToken;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteBatchPersistenceServiceAgreementServiceTest {

    private static final String SA_NAME = "saName";
    private static final String SA_ID_IDENTIFIER = "saIdIdentifier";
    private static final String ERROR_MESSAGE = "error-message";

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private AccessToken accessToken;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @Mock
    private Validator validator;
    @Mock
    private EventBus eventBus;
    @InjectMocks
    private DeleteBatchServiceAgreementService deleteBatchServiceAgreementService;

    private final String externalId = "externalServiceAgreementId";

    @Test
    public void successfullyDeleteBatchServiceAgreementByExternalId() {

        PresentationServiceAgreementIdentifier externalIdIdentifier = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(externalId);
        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(singletonList(externalIdIdentifier))
            .withAccessToken("test");

        ResponseItem expectedBean = new ResponseItem()
            .withErrors(new ArrayList<>())
            .withStatus(ItemStatusCode.HTTP_STATUS_OK)
            .withResourceId(externalId);

        List<ResponseItem> ResponseItemsReturned = deleteBatchServiceAgreementService
            .deleteBatchServiceAgreement(batchDelete);
        verify(persistenceServiceAgreementService)
            .deleteServiceAgreementByIdentifier(externalIdIdentifier);
        verify(accessToken, times(1)).validateAccessToken(eq("test"), any());
        verify(serviceAgreementBusinessRulesService).isServiceAgreementInPendingStateByExternalId(eq(externalId));

        assertThat(ResponseItemsReturned, hasSize(1));
        assertThat(ResponseItemsReturned, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void successfullyDeleteBatchServiceAgreementById() {

        PresentationServiceAgreementIdentifier idIdentifier = new PresentationServiceAgreementIdentifier()
            .withIdIdentifier(SA_ID_IDENTIFIER);

        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(singletonList(idIdentifier))
            .withAccessToken("test");

        ResponseItem expectedBean = new ResponseItem()
            .withErrors(new ArrayList<>())
            .withStatus(ItemStatusCode.HTTP_STATUS_OK)
            .withResourceId(SA_ID_IDENTIFIER);

        List<ResponseItem> ResponseItemsReturned = deleteBatchServiceAgreementService
            .deleteBatchServiceAgreement(batchDelete);
        verify(persistenceServiceAgreementService)
            .deleteServiceAgreementByIdentifier(idIdentifier);
        verify(accessToken, times(1)).validateAccessToken(eq("test"), any());
        verify(serviceAgreementBusinessRulesService).isServiceAgreementInPendingState(eq(SA_ID_IDENTIFIER));

        assertThat(ResponseItemsReturned, hasSize(1));
        assertThat(ResponseItemsReturned, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void successfullyDeleteBatchServiceAgreementByName() {

        PresentationServiceAgreementIdentifier nameIdentifier = new PresentationServiceAgreementIdentifier()
            .withNameIdentifier(SA_NAME);
        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(singletonList(nameIdentifier))
            .withAccessToken("test");

        ResponseItem expectedBean = new ResponseItem()
            .withErrors(new ArrayList<>())
            .withStatus(ItemStatusCode.HTTP_STATUS_OK)
            .withResourceId(SA_NAME);

        List<ResponseItem> ResponseItemsReturned = deleteBatchServiceAgreementService
            .deleteBatchServiceAgreement(batchDelete);
        verify(persistenceServiceAgreementService)
            .deleteServiceAgreementByIdentifier(nameIdentifier);
        verify(accessToken, times(1)).validateAccessToken(eq("test"), any());

        assertThat(ResponseItemsReturned, hasSize(1));
        assertThat(ResponseItemsReturned, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void failDeleteBatchServiceAgreementBecauseOfNotValidToken() {

        PresentationServiceAgreementIdentifier PresentationServiceAgreementIdentifier = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(externalId);
        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(singletonList(PresentationServiceAgreementIdentifier))
            .withAccessToken("test");

        doThrow(getBadRequestException(
            QueryErrorCodes.ERR_ACQ_054.getErrorMessage(),
            QueryErrorCodes.ERR_ACQ_054.getErrorCode())).when(accessToken).validateAccessToken(anyString(), any());

        new ResponseItem()
            .withStatus(ItemStatusCode.HTTP_STATUS_OK)
            .withResourceId(externalId);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> deleteBatchServiceAgreementService.deleteBatchServiceAgreement(batchDelete));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_054.getErrorMessage(), ERR_ACQ_054.getErrorCode()));
    }

    @Test
    public void deleteBatchServiceAgreement() {

        PresentationServiceAgreementIdentifier PresentationServiceAgreementIdentifier = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(externalId);
        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(singletonList(PresentationServiceAgreementIdentifier));

        ResponseItem expectedBean = new ResponseItem()
            .withErrors(new ArrayList<>())
            .withStatus(ItemStatusCode.HTTP_STATUS_OK)
            .withResourceId(externalId);

        List<ResponseItem> ResponseItemsReturned = deleteBatchServiceAgreementService
            .processBatchItems(new ArrayList<>(batchDelete.getServiceAgreementIdentifiers()));
        verify(persistenceServiceAgreementService)
            .deleteServiceAgreementByIdentifier(PresentationServiceAgreementIdentifier);
        assertThat(ResponseItemsReturned, hasSize(1));
        assertThat(ResponseItemsReturned, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnBadRequestWhenDeleteFailed() {

        PresentationServiceAgreementIdentifier PresentationServiceAgreementIdentifier = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(externalId);
        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(singletonList(PresentationServiceAgreementIdentifier));

        ResponseItem expectedBean = new ResponseItem()
            .withStatus(ItemStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList(ERROR_MESSAGE))
            .withResourceId(externalId);

        doThrow(getBadRequestException(ERROR_MESSAGE, null))
            .when(persistenceServiceAgreementService)
            .deleteServiceAgreementByIdentifier(PresentationServiceAgreementIdentifier);

        List<ResponseItem> ResponseItems = deleteBatchServiceAgreementService
            .processBatchItems(new ArrayList<>(batchDelete.getServiceAgreementIdentifiers()));

        assertThat(ResponseItems, hasSize(1));
        assertThat(ResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnNotFoundWhenDeleteFailed() {

        PresentationServiceAgreementIdentifier PresentationServiceAgreementIdentifier = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(externalId);
        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(singletonList(PresentationServiceAgreementIdentifier));
        ResponseItem expectedBean = new ResponseItem()
            .withStatus(ItemStatusCode.HTTP_STATUS_NOT_FOUND)
            .withErrors(singletonList(ERROR_MESSAGE))
            .withResourceId(externalId);

        doThrow(getNotFoundException(ERROR_MESSAGE, null))
            .when(persistenceServiceAgreementService)
            .deleteServiceAgreementByIdentifier(PresentationServiceAgreementIdentifier);

        List<ResponseItem> ResponseItems = deleteBatchServiceAgreementService
            .processBatchItems(new ArrayList<>(batchDelete.getServiceAgreementIdentifiers()));

        assertThat(ResponseItems, hasSize(1));
        assertThat(ResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnInvalidIdentifierWhenNoIdentifiersArePresent() {

        PresentationServiceAgreementIdentifier requestBody = new PresentationServiceAgreementIdentifier();
        List<String> error = deleteBatchServiceAgreementService.customValidateConstraintsForRequestBody(requestBody);
        assertEquals("Identifier is not valid.", error.get(0));
    }

    @Test
    public void shouldReturnInvalidIdentifierWhenMoreThenOneIdentifierIsPresent() {

        PresentationServiceAgreementIdentifier requestBody = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier("exId")
            .withNameIdentifier("NameSa");
        List<String> error = deleteBatchServiceAgreementService.customValidateConstraintsForRequestBody(requestBody);
        assertEquals("Identifier is not valid.", error.get(0));
    }

    @Test
    public void shouldNotReturnErrorForInvalidIdentifier() {

        PresentationServiceAgreementIdentifier requestBody = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier("exId");
        List<String> error = deleteBatchServiceAgreementService.customValidateConstraintsForRequestBody(requestBody);
        assertEquals(0, error.size());
    }

}