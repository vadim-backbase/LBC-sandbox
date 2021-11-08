package com.backbase.accesscontrol.business.persistence.legalentity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateLegalEntityHandlerTest {

    @InjectMocks
    private UpdateLegalEntityHandler updateLegalEntityHandler;

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Test
    public void shouldCreateEmptySuccessFullEvent() {
        LegalEntityEvent successEvent = updateLegalEntityHandler
            .createSuccessEvent(new SingleParameterHolder<>("id"), null, "id");
        assertEquals("id", successEvent.getId());
        assertEquals(Action.UPDATE, successEvent.getAction());
    }

    @Test
    public void shouldCreateFailedEvent() {
        LegalEntityByExternalIdPutRequestBody request = new LegalEntityByExternalIdPutRequestBody().withType(
            LegalEntityType.CUSTOMER);
        Event failureEvent = updateLegalEntityHandler
            .createFailureEvent(new SingleParameterHolder<>("id"), request, null);
        assertNull(failureEvent);
    }

    @Test
    public void shouldInvokeUpdateFromTheService() {
        String externalId = "ex-id";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(externalId);
        when(persistenceLegalEntityService.updateLegalEntity(eq(externalId), isNull()))
            .thenReturn("id");
        updateLegalEntityHandler.executeRequest(parameterHolder, null);
        verify(persistenceLegalEntityService).updateLegalEntity(eq(externalId), isNull());
    }

}