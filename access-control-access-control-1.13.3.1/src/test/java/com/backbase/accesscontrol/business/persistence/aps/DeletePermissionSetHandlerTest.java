package com.backbase.accesscontrol.business.persistence.aps;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.IdentifierPair;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeletePermissionSetHandlerTest {

    @Mock
    private PermissionSetService permissionSetService;

    @InjectMocks
    private DeletePermissionSetHandler deletePermissionSetHandler;

    @Test
    public void shouldSuccessfullyInvokeDelete() {
        String identifierType = "id";
        String identifier = "13";

        Long id = 13L;

        IdentifierPair identifierPair = new IdentifierPair(identifierType, identifier);

        when(permissionSetService.delete(identifierType, identifier)).thenReturn(id);

        deletePermissionSetHandler.executeRequest(identifierPair, null);

        verify(permissionSetService, times(1)).delete(eq(identifierType), eq(identifier));

    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        String identifierType = "id";
        String identifier = "13";

        IdentifierPair identifierPair = new IdentifierPair(identifierType, identifier);

        long id = 13L;

        AssignablePermissionSetEvent event = deletePermissionSetHandler
            .createSuccessEvent(identifierPair, null, id);

        assertEquals(new BigDecimal(id), event.getId());
        assertEquals(Action.DELETE, event.getAction());
    }
}
