package com.backbase.accesscontrol.business.persistence.functiongroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddFunctionGroupApprovalHandlerTest {

    @InjectMocks
    private AddFunctionGroupApprovalHandler addFunctionGroupApprovalHandler;

    @Mock
    private FunctionGroupService functionGroupService;

    @Test
    public void executeRequest() {
        FunctionGroupApprovalBase requestData = new FunctionGroupApprovalBase().withApprovalId("approvalId");
        String functionGroupId = "new FG approval";
        when(functionGroupService.addFunctionGroupApproval(requestData)).thenReturn(functionGroupId);
        FunctionGroupsPostResponseBody functionGroupApprovalCreatePostResponseBody =
            addFunctionGroupApprovalHandler.executeRequest(null, requestData);
        verify(functionGroupService, times(1))
            .addFunctionGroupApproval(requestData);
        assertEquals(functionGroupId, functionGroupApprovalCreatePostResponseBody.getId());
    }

    @Test
    public void createSuccessEvent() {
        FunctionGroupApprovalBase requestData = new FunctionGroupApprovalBase()
            .withDescription("desc")
            .withName("name")
            .withPermissions(new ArrayList<>())
            .withServiceAgreementId("sa id")
            .withApprovalId("approval id");

        FunctionGroupsPostResponseBody responseData = new FunctionGroupsPostResponseBody();
        Event functionGroupAddedEvent = addFunctionGroupApprovalHandler
            .createSuccessEvent(null, requestData, responseData);

        assertNull(functionGroupAddedEvent);
    }

}
