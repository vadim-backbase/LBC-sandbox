package com.backbase.accesscontrol.service.batch.legalentity;

import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_BAD_REQUEST;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_NOT_FOUND;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_OK;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_054;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import com.backbase.accesscontrol.util.validation.AccessToken;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityBatchDeleteServiceTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Mock
    private Validator validator;

    @Mock
    EventBus eventBus;

    @Mock
    private AccessToken accessToken;

    @InjectMocks
    private LegalEntityBatchDeleteService legalEntityBatchDeleteService;

    private String externalId = "540949a03a7846abb69e7c667bc73688";

    @Test
    public void successDeleteBatchLegalEntitiesValidToken() {

        PresentationBatchDeleteLegalEntities batchDelete = new PresentationBatchDeleteLegalEntities()
            .withExternalIds(new LinkedHashSet<>(singletonList(externalId)))
            .withAccessToken("test");

        ResponseItem expectedBean = new ResponseItem(externalId, HTTP_STATUS_OK, new ArrayList<>());

        when(persistenceLegalEntityService.deleteLegalEntityByExternalId(anyString())).thenReturn("id");
        List<ResponseItem> batchResponseItemsReturned = legalEntityBatchDeleteService
            .deleteBatchLegalEntities(batchDelete);
        verify(persistenceLegalEntityService)
            .deleteLegalEntityByExternalId(externalId);
        verify(accessToken, times(1)).validateAccessToken(eq("test"), any());
        verify(eventBus, times(1)).emitEvent(any(EnvelopedEvent.class));
        assertThat(batchResponseItemsReturned, hasSize(1));
        assertThat(batchResponseItemsReturned, hasItem(samePropertyValuesAs(expectedBean)));

    }

    @Test
    public void failDeleteBatchLegalEntitiesNotValidToken() {

        PresentationBatchDeleteLegalEntities batchDelete = new PresentationBatchDeleteLegalEntities()
            .withExternalIds(new LinkedHashSet<>(singletonList(externalId)))
            .withAccessToken("");

        doThrow(getBadRequestException(
            QueryErrorCodes.ERR_ACQ_054.getErrorMessage(),
            QueryErrorCodes.ERR_ACQ_054.getErrorCode())).when(accessToken).validateAccessToken(anyString(), any());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> legalEntityBatchDeleteService.deleteBatchLegalEntities(batchDelete));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_054.getErrorMessage(), ERR_ACQ_054.getErrorCode()));
    }

    @Test
    public void shouldReturnBadRequestWhenDeleteFailed() {

        PresentationBatchDeleteLegalEntities batchDelete = new PresentationBatchDeleteLegalEntities()
            .withExternalIds(new LinkedHashSet<>(singletonList(externalId)));

        ResponseItem expectedBean = new ResponseItem(externalId,
            HTTP_STATUS_BAD_REQUEST, singletonList("error-message"));

        doThrow(getBadRequestException("error-message", null))

            .when(persistenceLegalEntityService).deleteLegalEntityByExternalId(externalId);

        List<ResponseItem> batchResponseItems = legalEntityBatchDeleteService
            .processBatchItems(new ArrayList<>(batchDelete.getExternalIds()));

        assertThat(batchResponseItems, hasSize(1));
        assertThat(batchResponseItems, hasItem(samePropertyValuesAs(expectedBean)));

    }

    @Test
    public void shouldReturnNotFoundWhenDeleteFailed() {

        PresentationBatchDeleteLegalEntities batchDelete = new PresentationBatchDeleteLegalEntities()
            .withExternalIds(new LinkedHashSet<>(singletonList(externalId)));

        ResponseItem expectedBean = new ResponseItem(externalId, HTTP_STATUS_NOT_FOUND, singletonList("error-message"));

        doThrow(getNotFoundException("error-message", null))
            .when(persistenceLegalEntityService).deleteLegalEntityByExternalId(externalId);

        List<ResponseItem> batchResponseItems = legalEntityBatchDeleteService
            .processBatchItems(new ArrayList<>(batchDelete.getExternalIds()));

        assertThat(batchResponseItems, hasSize(1));
        assertThat(batchResponseItems, hasItem(samePropertyValuesAs(expectedBean)));

    }
}