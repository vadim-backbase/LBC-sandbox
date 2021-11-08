package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getVoidInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.approvals.RejectApprovalHandler;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RejectApprovalRequestTest {

    @InjectMocks
    private RejectApprovalRequest rejectApprovalRequest;

    @Mock
    private AccessControlApprovalService accessControlApprovalService;

    @Mock
    private RejectApprovalHandler rejectApprovalHandler;

    @Test
    public void rejectApprovalRequest() {
        String approvalId = "appId";
        String serviceAgreementId = "saId";
        String userId = "uId";

        PresentationApprovalStatus response = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.REJECTED);
        String approvalStatus = String.valueOf(ApprovalStatus.REJECTED);
        when(accessControlApprovalService.rejectApprovalRequestOnApprovalApiSide(approvalId, serviceAgreementId, userId))
            .thenReturn(
                new ApprovalDto().status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED));
        doNothing().when(rejectApprovalHandler).handleRequest(any(SingleParameterHolder.class), isNull());
        rejectApprovalRequest.rejectApprovalRequest(approvalId, getVoidInternalRequest(), serviceAgreementId, userId);

        ArgumentCaptor<SingleParameterHolder<String>> captor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        verify(rejectApprovalHandler).handleRequest(captor.capture(), isNull());
        assertEquals(approvalId, captor.getValue().getParameter());
        assertEquals(approvalStatus, response.getApprovalStatus().toString());
    }
}