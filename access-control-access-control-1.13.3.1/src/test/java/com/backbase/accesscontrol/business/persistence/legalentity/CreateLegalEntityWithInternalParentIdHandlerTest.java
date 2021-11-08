package com.backbase.accesscontrol.business.persistence.legalentity;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityWithInternalParentIdHandlerTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private CreateLegalEntityWithInternalParentIdHandler createLegalEntityWithInternalParentIdHandler;

    @Test
    public void shouldSuccessfullyInvokeSaveAndPublish() {

        String key = "leExternalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);

        PresentationCreateLegalEntityItemPostRequestBody legalEntityPostRequestBody =
            new PresentationCreateLegalEntityItemPostRequestBody();
        legalEntityPostRequestBody.setAdditions(addition);

        LegalEntity legalEntity = createLegalEntity("le-name", "exId", null);
        legalEntity.setAdditions(addition);

        doReturn(legalEntity).when(persistenceLegalEntityService)
            .createLegalEntityWithInternalParentId(any(PresentationCreateLegalEntityItemPostRequestBody.class));
        LegalEntitiesPostResponseBody data = createLegalEntityWithInternalParentIdHandler
            .handleRequest(new EmptyParameterHolder(), legalEntityPostRequestBody);

        verify(eventBus, times(1)).emitEvent(any(EnvelopedEvent.class));
        verify(persistenceLegalEntityService)
            .createLegalEntityWithInternalParentId(any(PresentationCreateLegalEntityItemPostRequestBody.class));
        assertThat(data.getAdditions().size(), is(1));
        assertTrue(data.getAdditions().containsKey(key));
        assertTrue(data.getAdditions().containsValue(value));
    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        PresentationCreateLegalEntityItemPostRequestBody request = new PresentationCreateLegalEntityItemPostRequestBody()
            .withExternalId("exId")
            .withName("name")
            .withParentInternalId("parentId");
        LegalEntitiesPostResponseBody response = new LegalEntitiesPostResponseBody()
            .withId("id");
        LegalEntityEvent successEvent = createLegalEntityWithInternalParentIdHandler
            .createSuccessEvent(new EmptyParameterHolder(), request, response);
        assertEquals("id", successEvent.getId());
        assertEquals(Action.ADD, successEvent.getAction());
    }

    @Test
    public void shouldCreateFailureEvent() {
        String errorMessage = "message";
        PresentationCreateLegalEntityItemPostRequestBody request = new PresentationCreateLegalEntityItemPostRequestBody()
            .withExternalId("exId")
            .withName("name")
            .withParentInternalId("parentId");
        Event failureEvent = createLegalEntityWithInternalParentIdHandler
            .createFailureEvent(new EmptyParameterHolder(), request, new RuntimeException(errorMessage));
        assertNull(failureEvent);
    }
}