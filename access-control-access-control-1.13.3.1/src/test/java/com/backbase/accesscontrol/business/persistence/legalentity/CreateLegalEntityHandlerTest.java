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
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityHandlerTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @InjectMocks
    private CreateLegalEntityHandler createLegalEntityHandler;

    @Mock
    private EventBus eventBus;

    @Test
    public void shouldSuccessfullyInvokeSaveAndPublish() {

        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);

        CreateLegalEntitiesPostRequestBody legalEntityPostRequestBody = new CreateLegalEntitiesPostRequestBody();
        legalEntityPostRequestBody.setAdditions(addition);

        LegalEntity legalEntity = createLegalEntity("le-name", "exId", null);
        legalEntity.setAdditions(addition);

        doReturn(legalEntity).when(persistenceLegalEntityService)
            .createLegalEntity(any(CreateLegalEntitiesPostRequestBody.class));
        CreateLegalEntitiesPostResponseBody data = createLegalEntityHandler
            .handleRequest(any(EmptyParameterHolder.class), legalEntityPostRequestBody);

        verify(eventBus, times(1)).emitEvent(any(EnvelopedEvent.class));
        verify(persistenceLegalEntityService).createLegalEntity(any(CreateLegalEntitiesPostRequestBody.class));
        assertThat(data.getAdditions().size(), is(1));
        assertTrue(data.getAdditions().containsKey(key));
        assertTrue(data.getAdditions().containsValue(value));
    }

    @Test
    public void shouldCreateSuccessfulEvent() {

        CreateLegalEntitiesPostRequestBody request = new CreateLegalEntitiesPostRequestBody()
            .withExternalId("exId")
            .withName("name")
            .withParentExternalId("parentId");
        CreateLegalEntitiesPostResponseBody response = new CreateLegalEntitiesPostResponseBody()
            .withId("id");
        LegalEntityEvent successEvent = createLegalEntityHandler
            .createSuccessEvent(new EmptyParameterHolder(), request, response);
        assertEquals("id", successEvent.getId());
        assertEquals(Action.ADD, successEvent.getAction());
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";
        CreateLegalEntitiesPostRequestBody request = new CreateLegalEntitiesPostRequestBody()
            .withExternalId("exId")
            .withName("name")
            .withParentExternalId("parentId");
        Event failureEvent = createLegalEntityHandler
            .createFailureEvent(new EmptyParameterHolder(), request, new RuntimeException(errorMessage));
        assertNull(failureEvent);
    }

}
