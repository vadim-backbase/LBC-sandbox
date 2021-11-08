package com.backbase.accesscontrol.business.persistence.functiongroup;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.persistence.model.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteFunctionGroupPersistenceApprovalHandlerTest {

    @InjectMocks
    private DeleteFunctionGroupApprovalHandler deleteFunctionGroupApprovalHandler;

    @Mock
    private FunctionGroupService functionGroupService;

    @Test
    public void testExecuteRequest() {
        ApprovalDto requestData = new ApprovalDto("approval id", null);
        deleteFunctionGroupApprovalHandler.executeRequest(new SingleParameterHolder<>("fgId"), requestData);
        verify(functionGroupService).deleteApprovalFunctionGroup("fgId", requestData);
    }

    @Test
    public void testCreateSuccessEvent() {
        ApprovalDto requestData = new ApprovalDto("approval id", null);
        Event functionGroupApprovalDeletedEvent =
            deleteFunctionGroupApprovalHandler.createSuccessEvent(new SingleParameterHolder<>("fgId"), requestData, null);
        assertNull(functionGroupApprovalDeletedEvent);
    }

    @Test
    public void shouldCreateFailedEvent() {
        ApprovalDto requestData = new ApprovalDto("approval id", null);
        Event failureEvent = deleteFunctionGroupApprovalHandler
            .createFailureEvent(new SingleParameterHolder<>("fgId"), requestData, new RuntimeException("message"));
        assertNull(failureEvent);
    }
}
