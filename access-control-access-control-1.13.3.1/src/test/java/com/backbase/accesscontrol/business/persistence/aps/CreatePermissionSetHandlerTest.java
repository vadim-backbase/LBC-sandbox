package com.backbase.accesscontrol.business.persistence.aps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreatePermissionSetHandlerTest {

    @Mock
    private PermissionSetService permissionSetService;

    @InjectMocks
    private CreatePermissionSetHandler createPermissionSetHandler;

    @Test
    public void shouldSuccessfullyInvokeSave() {
        BigDecimal expectedInternalIdResponse = new BigDecimal(1234);

        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");

        when(permissionSetService.save(eq(permissionSet))).thenReturn(expectedInternalIdResponse);

        BigDecimal persistenceInternalIdResponse = createPermissionSetHandler
            .executeRequest(any(EmptyParameterHolder.class), permissionSet);

        verify(permissionSetService, times(1)).save(eq(permissionSet));
        assertEquals(expectedInternalIdResponse, persistenceInternalIdResponse);
    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        BigDecimal internalIdResponse = new BigDecimal(1234);

        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");

        AssignablePermissionSetEvent successEvent = createPermissionSetHandler
            .createSuccessEvent(null, permissionSet, internalIdResponse);

        assertEquals(internalIdResponse, successEvent.getId());
        assertEquals(Action.ADD, successEvent.getAction());
    }

    @Test
    public void shouldDoNothingOnFailure() {
        Event failureEvent = createPermissionSetHandler
            .createFailureEvent(null, new PresentationPermissionSet().withName("apsName"), new BadRequestException());

        assertNull(failureEvent);
    }
}
