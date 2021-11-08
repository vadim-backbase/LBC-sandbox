package com.backbase.accesscontrol.business.persistence.legalentity;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityAsParticipantHandlerTest {
    
    private static final String LE_EXTERNAL_ID_VALUE = "123456789";

    private static final String LE_EXTERNAL_ID_KEY = "leExternalId";

    private static final String MESSAGE = "message";

    private static final String PARENT_ID = "parentId";

    private static final String NAME = "name";

    private static final String EX_ID = "exId";

    private static final String SERVICE_AGREEMENT_ID = "serviceAgreementId";

    private static final String LEGAL_ENTITY_ID = "legalEntityId";

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    
    @InjectMocks
    private CreateLegalEntityAsParticipantHandler handler;
    
    @Mock
    private EventBus eventBus;
    
    @Test
    public void shouldSuccessfullyInvokePersistenceService() {
        
        Map<String, String> additions = new HashMap<>();
        additions.put(LE_EXTERNAL_ID_KEY, LE_EXTERNAL_ID_VALUE);
        
        LegalEntityAsParticipantPostRequestBody request = new LegalEntityAsParticipantPostRequestBody();
        request.setAdditions(additions);
        
        LegalEntityAsParticipantPostResponseBody response = createLegalEntityAsParticipantPostResponseBody();
        response.setAdditions(additions);
        
        doReturn(response).when(persistenceLegalEntityService)
                        .createLegalEntityAsParticipant(any(LegalEntityAsParticipantPostRequestBody.class), anyString());
        
        String legalEntityId = "legalEntityId";
        
        LegalEntityAsParticipantPostResponseBody responseBody =
                        handler.handleRequest(new SingleParameterHolder<String>(legalEntityId), request);
        
        verify(eventBus, times(2)).emitEvent(any(EnvelopedEvent.class));
        verify(persistenceLegalEntityService).createLegalEntityAsParticipant(any(LegalEntityAsParticipantPostRequestBody.class), eq(legalEntityId));
        assertNotNull(responseBody);
        assertThat(responseBody.getAdditions().size(), is(1));
        assertTrue(responseBody.getAdditions().containsKey(LE_EXTERNAL_ID_KEY));
        assertTrue(responseBody.getAdditions().containsValue(LE_EXTERNAL_ID_VALUE));
    }

    @Test
    public void shouldCreateSuccessfulEvents() { 
        //Test new LE + SA
        LegalEntityAsParticipantPostRequestBody request = createLegalEntityAsParticipantPostRequestBody();
        LegalEntityAsParticipantPostResponseBody response = createLegalEntityAsParticipantPostResponseBody();
              
        List<Event> successEvents = handler
            .createSuccessEvents(new SingleParameterHolder<String>("legalEntityId"), request, response);
        assertEquals(2, successEvents.size());
        LegalEntityEvent eventLE = (LegalEntityEvent) successEvents.get(0);
        assertEquals(LEGAL_ENTITY_ID, eventLE.getId());
        assertEquals(Action.ADD, eventLE.getAction());
        ServiceAgreementEvent eventSA = (ServiceAgreementEvent) successEvents.get(1);
        assertEquals(SERVICE_AGREEMENT_ID, eventSA.getId());
        assertEquals(Action.ADD, eventSA.getAction());
        
        //Test new LE + update existing SA
        response.withServiceAgreementId(null);
        
        successEvents = handler
                        .createSuccessEvents(new SingleParameterHolder<String>("legalEntityId"), request, response);
        assertEquals(2, successEvents.size());
        eventLE = (LegalEntityEvent) successEvents.get(0);
        assertEquals(LEGAL_ENTITY_ID, eventLE.getId());
        assertEquals(Action.ADD, eventLE.getAction());
        eventSA = (ServiceAgreementEvent) successEvents.get(1);
        assertNull(eventSA.getId());
        assertEquals(Action.UPDATE, eventSA.getAction());
    }
    
    @Test
    public void shouldCreateSuccessfulEvent() {
        //Test new LE + SA
        LegalEntityAsParticipantPostRequestBody request = createLegalEntityAsParticipantPostRequestBody();
        LegalEntityAsParticipantPostResponseBody response = createLegalEntityAsParticipantPostResponseBody();
              
        Event successEvent = handler
            .createSuccessEvent(new SingleParameterHolder<String>("legalEntityId"), request, response);
        assertNull(successEvent);
        
        //Test new LE + update existing SA
        response.withServiceAgreementId(null);
        
        successEvent = handler
                        .createSuccessEvent(new SingleParameterHolder<String>("legalEntityId"), request, response);
        assertNull(successEvent);
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = MESSAGE;
        LegalEntityAsParticipantPostRequestBody request = createLegalEntityAsParticipantPostRequestBody();
        Event failureEvent = handler
            .createFailureEvent(new SingleParameterHolder<String>("legalEntityId"), request, new RuntimeException(errorMessage));
        assertNull(failureEvent);
    }
    
    private LegalEntityAsParticipantPostRequestBody createLegalEntityAsParticipantPostRequestBody() {
        LegalEntityAsParticipantPostRequestBody request = new LegalEntityAsParticipantPostRequestBody()
            .withLegalEntityExternalId(EX_ID)
            .withLegalEntityName(NAME)
            .withLegalEntityParentId(PARENT_ID);
        return request;
    }

    private LegalEntityAsParticipantPostResponseBody createLegalEntityAsParticipantPostResponseBody() {
        LegalEntityAsParticipantPostResponseBody response = new LegalEntityAsParticipantPostResponseBody()
                        .withLegalEntityId(LEGAL_ENTITY_ID)
                        .withServiceAgreementId(SERVICE_AGREEMENT_ID);
        return response;
    }
}
