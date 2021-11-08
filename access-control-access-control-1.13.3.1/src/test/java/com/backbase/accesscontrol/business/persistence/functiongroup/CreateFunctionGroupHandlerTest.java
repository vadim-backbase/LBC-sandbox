package com.backbase.accesscontrol.business.persistence.functiongroup;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateFunctionGroupHandlerTest {

    @InjectMocks
    private CreateFunctionGroupHandler createFunctionGroupHandler;

    @Mock
    private FunctionGroupService functionGroupService;

    @Test
    public void testExecuteRequest() {
        FunctionGroupBase requestData = new FunctionGroupBase();
        String functionGroupId = "new FG";
        when(functionGroupService.addFunctionGroup(requestData)).thenReturn(functionGroupId);
        FunctionGroupsPostResponseBody functionGroupsPostResponseBody = createFunctionGroupHandler
            .executeRequest(new EmptyParameterHolder(), requestData);

        assertEquals(functionGroupId, functionGroupsPostResponseBody.getId());
    }

    @Test
    public void testCreateSuccessEvent() {

        FunctionGroupEvent successEvent = createFunctionGroupHandler
            .createSuccessEvent(new EmptyParameterHolder(), new FunctionGroupBase(),
                new FunctionGroupsPostResponseBody().withId("id"));

        assertNotNull(successEvent);
        assertEquals(Action.ADD, successEvent.getAction());
        assertEquals("id", successEvent.getId());

    }

    @Test
    public void testCreateFailureEvent() {
        String errorMessage = "message";

        Event failureEvent = createFunctionGroupHandler
            .createFailureEvent(new EmptyParameterHolder(), new FunctionGroupBase(),
                new Exception(errorMessage));

        assertNull(failureEvent);
    }
}