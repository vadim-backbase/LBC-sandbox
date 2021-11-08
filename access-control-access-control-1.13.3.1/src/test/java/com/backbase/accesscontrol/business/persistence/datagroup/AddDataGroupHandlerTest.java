package com.backbase.accesscontrol.business.persistence.datagroup;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddDataGroupHandlerTest {

    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @InjectMocks
    private AddDataGroupHandler addDataGroupHandler;
    @Mock
    private EventBus eventBus;

    @Test
    public void shouldSuccessfullyInvokeSave() {
        DataGroupBase dataGroupBase = new DataGroupBase();
        dataGroupBase.setServiceAgreementId("serviceAgreementId");

        String id = UUID.randomUUID().toString();
        when(dataGroupService.save(eq(dataGroupBase))).thenReturn(id);

        DataGroupsPostResponseBody data = addDataGroupHandler
            .handleRequest(new EmptyParameterHolder(), dataGroupBase);

        verify(dataGroupService).save(eq(dataGroupBase));
        verify(eventBus, times(1)).emitEvent(any(EnvelopedEvent.class));
        assertEquals(id, data.getId());
    }

    @Test
    public void shouldSuccessfullyInvokeSaveWhenDataGroupHasExternalSa() {
        DataGroupBase dataGroupBase = new DataGroupBase();
        dataGroupBase.setExternalServiceAgreementId("externalServiceAgreementId");

        String id = UUID.randomUUID().toString();
        when(dataGroupService.save(eq(dataGroupBase))).thenReturn(id);

        when(persistenceServiceAgreementService.getServiceAgreementByExternalId(anyString()))
            .thenReturn(new ServiceAgreement().withId("serviceAgreementId"));

        DataGroupsPostResponseBody data = addDataGroupHandler
            .handleRequest(new EmptyParameterHolder(), dataGroupBase);

        verify(dataGroupService).save(eq(dataGroupBase));
        assertEquals(id, data.getId());
    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        DataGroupBase body = new DataGroupBase()
            .withName("name");
        DataGroupsPostResponseBody responseBody = new DataGroupsPostResponseBody();
        responseBody.setId("dg-id");
        DataGroupEvent successEvent = addDataGroupHandler
            .createSuccessEvent(new EmptyParameterHolder(), body, responseBody);
        assertEquals(responseBody.getId(), successEvent.getId());
        assertEquals(Action.ADD, successEvent.getAction());
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";
        DataGroupBase dataGroupBase = new DataGroupBase();
        Event failureEvent =
            addDataGroupHandler.createFailureEvent(null, dataGroupBase, new RuntimeException(errorMessage));

        assertNull(failureEvent);
    }

    @Test
    public void shouldThrowBadRequestWhenThereIsNoSaForExternalSaId() {
        DataGroupBase dataGroupBase = new DataGroupBase();
        dataGroupBase.setExternalServiceAgreementId("INVALID_SA_ID");

        when(persistenceServiceAgreementService.getServiceAgreementByExternalId(anyString()))
            .thenThrow(new BadRequestException());

        assertThrows(BadRequestException.class,
            () -> addDataGroupHandler.handleRequest(null, dataGroupBase));
    }
}
