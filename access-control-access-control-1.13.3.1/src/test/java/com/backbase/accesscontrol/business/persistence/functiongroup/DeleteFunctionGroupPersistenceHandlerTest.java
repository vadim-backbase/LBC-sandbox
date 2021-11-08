package com.backbase.accesscontrol.business.persistence.functiongroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteFunctionGroupPersistenceHandlerTest {

    @Mock
    private FunctionGroupService functionGroupService;

    @InjectMocks
    private DeleteFunctionGroupHandler deleteFunctionGroupHandler;

    @Test
    public void shouldSuccessfullyInvokeDeleteFunctionGroup() {
        String functionGroupId = "FG-001";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(functionGroupId);

        deleteFunctionGroupHandler.executeRequest(parameterHolder, null);

        verify(functionGroupService).deleteFunctionGroup(functionGroupId);
    }

    @Test
    public void testCreateSuccessEvent() {

        FunctionGroupEvent successEvent = deleteFunctionGroupHandler
            .createSuccessEvent(new SingleParameterHolder("id"),null,
                null);

        assertNotNull(successEvent);
        assertEquals(Action.DELETE, successEvent.getAction());
        assertEquals("id", successEvent.getId());

    }
}