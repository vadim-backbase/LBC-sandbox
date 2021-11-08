package com.backbase.accesscontrol.business.persistence.functiongroup;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.FunctionGroupIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateFunctionGroupApprovalHandlerTest {

    @InjectMocks
    private UpdateFunctionGroupApprovalHandler updateFunctionGroupApprovalHandler;

    @Mock
    private FunctionGroupService functionGroupService;

    @Test
    public void executeRequest() {
        String approvalId = "approvalId";
        String fgId = "fgId";

        FunctionGroupByIdPutRequestBody requestData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("saId");

        FunctionGroupIdApprovalIdParameterHolder holder = new FunctionGroupIdApprovalIdParameterHolder()
            .withFunctionGroupId(fgId)
            .withApprovalId(approvalId);
        updateFunctionGroupApprovalHandler.executeRequest(holder, requestData);
        verify(functionGroupService).updateFunctionGroupApproval(eq(requestData), eq(fgId), eq(approvalId));
    }

    @Test
    public void createSuccessEvent() {
        String approvalId = "approvalId";
        String fgId = "fgId";

        FunctionGroupByIdPutRequestBody requestData = new FunctionGroupByIdPutRequestBody()
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("saId");

        FunctionGroupIdApprovalIdParameterHolder holder = new FunctionGroupIdApprovalIdParameterHolder()
            .withFunctionGroupId(fgId)
            .withApprovalId(approvalId);

        Event event = updateFunctionGroupApprovalHandler
            .createSuccessEvent(holder, requestData, null);

        assertNull(event);
    }
}