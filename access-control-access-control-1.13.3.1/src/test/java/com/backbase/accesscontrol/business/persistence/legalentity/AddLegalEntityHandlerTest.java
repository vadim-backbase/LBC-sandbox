package com.backbase.accesscontrol.business.persistence.legalentity;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddLegalEntityHandlerTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @InjectMocks
    private AddLegalEntityHandler addLegalEntityHandler;

    @Mock
    private EventBus eventBus;

    @Test
    public void shouldSuccessfullyInvokeSaveAndPublish() {

        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = new LegalEntitiesPostRequestBody();
        legalEntityPostRequestBody.setAdditions(addition);

        LegalEntity legalEntity = createLegalEntity("le-name", "exId", null);
        legalEntity.setAdditions(addition);

        doReturn(legalEntity).when(persistenceLegalEntityService)
            .addLegalEntity(any(LegalEntitiesPostRequestBody.class));
        LegalEntitiesPostResponseBody data = addLegalEntityHandler
            .handleRequest(new EmptyParameterHolder(), legalEntityPostRequestBody);

        verify(eventBus, times(1)).emitEvent(any(EnvelopedEvent.class));

        verify(persistenceLegalEntityService).addLegalEntity(any(LegalEntitiesPostRequestBody.class));
        assertThat(data.getAdditions().size(), is(1));
        assertTrue(data.getAdditions().containsKey(key));
        assertTrue(data.getAdditions().containsValue(value));
    }

    @Test
    public void shouldCreateSuccessfulEvent() {

        LegalEntitiesPostRequestBody request = new LegalEntitiesPostRequestBody();
        request.withExternalId("exId");
        request.withName("name");
        request.withParentExternalId("parentEx");
        LegalEntitiesPostResponseBody response = new LegalEntitiesPostResponseBody();
        response.withId("id");
        LegalEntityEvent successEvent = addLegalEntityHandler
            .createSuccessEvent(new EmptyParameterHolder(), request, response);
        assertEquals("id", successEvent.getId());
        assertEquals(Action.ADD, successEvent.getAction());
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";
        LegalEntitiesPostRequestBody request = new LegalEntitiesPostRequestBody();
        request.withExternalId("exId");
        request.withName("name");
        request.withParentExternalId("parentEx");
        Event failureEvent = addLegalEntityHandler
            .createFailureEvent(new EmptyParameterHolder(), request, new RuntimeException(errorMessage));
        assertNull(failureEvent);
    }

}
