package com.backbase.accesscontrol.service.batch.legalentity;

import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_BAD_REQUEST;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_INTERNAL_SERVER_ERROR;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_NOT_FOUND;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_OK;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityBatchServiceTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Mock
    private Validator validator;
    @Mock
    private EventBus eventBus;

    @InjectMocks
    private LegalEntityBatchService legalEntityBatchService;

    @Test
    public void updateBatchLegalEntities() {
        LegalEntityPut legalEntity = new LegalEntityPut()
            .withExternalId("ex-id");
        List<LegalEntityPut> legalEntities = asList(legalEntity);

        ResponseItem expectedBean = new ResponseItem(legalEntity.getExternalId(), HTTP_STATUS_OK, new ArrayList<>());

        List<ResponseItem> ResponseItemsReturned = legalEntityBatchService.processBatchItems(legalEntities);
        verify(persistenceLegalEntityService)
            .updateLegalEntityFields(eq(legalEntity.getExternalId()), eq(legalEntity.getLegalEntity()));
        verify(eventBus, times(1)).emitEvent(any(EnvelopedEvent.class));
        assertThat(ResponseItemsReturned, hasSize(1));
        assertThat(ResponseItemsReturned, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnBadRequestWhenUpdateFailed() {
        LegalEntityPut legalEntity = new LegalEntityPut()
            .withExternalId("ex-id");
        List<LegalEntityPut> legalEntities = asList(legalEntity);

        ResponseItem expectedBean = new ResponseItem(legalEntity.getExternalId(), HTTP_STATUS_BAD_REQUEST,
            asList("error-message"));

        doThrow(getBadRequestException("error-message", null))
            .when(persistenceLegalEntityService)
            .updateLegalEntityFields(eq(legalEntity.getExternalId()), eq(legalEntity.getLegalEntity()));

        List<ResponseItem> ResponseItems = legalEntityBatchService.processBatchItems(legalEntities);

        verify(eventBus, times(0)).emitEvent(any(EnvelopedEvent.class));
        assertThat(ResponseItems, hasSize(1));
        assertThat(ResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnNotFoundRequestWhenUpdateFailed() {
        LegalEntityPut legalEntity = new LegalEntityPut()
            .withExternalId("ex-id");
        List<LegalEntityPut> legalEntities = asList(legalEntity);

        ResponseItem expectedBean = new ResponseItem(legalEntity.getExternalId(), HTTP_STATUS_NOT_FOUND,
            asList("error-message"));

        doThrow(getNotFoundException("error-message", null))
            .when(persistenceLegalEntityService)
            .updateLegalEntityFields(eq(legalEntity.getExternalId()), eq(legalEntity.getLegalEntity()));

        List<ResponseItem> ResponseItems = legalEntityBatchService.processBatchItems(legalEntities);
        verify(eventBus, times(0)).emitEvent(any(EnvelopedEvent.class));
        assertThat(ResponseItems, hasSize(1));
        assertThat(ResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnInvalidRequestWhenUpdateFailed() {
        LegalEntityPut legalEntity = new LegalEntityPut()
            .withExternalId("ex-id");
        List<LegalEntityPut> legalEntities = asList(legalEntity);

        ResponseItem expectedBean = new ResponseItem(legalEntity.getExternalId(), HTTP_STATUS_INTERNAL_SERVER_ERROR,
            asList("error-message"));

        doThrow(new RuntimeException("error-message"))
            .when(persistenceLegalEntityService)
            .updateLegalEntityFields(eq(legalEntity.getExternalId()), eq(legalEntity.getLegalEntity()));

        List<ResponseItem> ResponseItems = legalEntityBatchService.processBatchItems(legalEntities);
        verify(eventBus, times(0)).emitEvent(any(EnvelopedEvent.class));
        assertThat(ResponseItems, hasSize(1));
        assertThat(ResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }
}