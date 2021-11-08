package com.backbase.accesscontrol.business.persistence.datagroup;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.ApprovalDto;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDataGroupApprovalHandlerTest {

    @Mock
    private DataGroupService dataGroupService;

    @InjectMocks
    private DeleteDataGroupApprovalHandler deleteDataGroupApprovalHandler;

    @Test
    public void shouldSuccessfullyInvokeSave() {
        String id = "id";
        ApprovalDto approval = new ApprovalDto();
        approval.setApprovalId("approvalId");
        doNothing().when(dataGroupService).deleteDataGroupApproval(eq(id), eq("approvalId"));
        deleteDataGroupApprovalHandler
            .executeRequest(new SingleParameterHolder<>(id), approval);

        verify(dataGroupService).deleteDataGroupApproval(eq(id), eq("approvalId"));
    }
}