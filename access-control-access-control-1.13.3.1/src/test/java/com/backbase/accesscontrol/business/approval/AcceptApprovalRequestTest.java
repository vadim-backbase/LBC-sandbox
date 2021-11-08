package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getVoidInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.approvers.Approver;
import com.backbase.accesscontrol.business.service.approvers.ApproverFactory;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AcceptApprovalRequestTest {

    @InjectMocks
    private AcceptApprovalRequest acceptApprovalRequest;
    @Mock
    private ApproverFactory approverFactory;
    @Mock
    private AccessControlApprovalService accessControlApprovalService;
    @Mock
    private Approver approver;

    @Test
    public void acceptApprovalRequest() {
        InternalRequest<Void> request = getVoidInternalRequest();
        String approvalId = "appId";
        String serviceAgreementId = "saId";
        String userId = "uId";

        PresentationApprovalStatus response = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.APPROVED);
        String approvalStatus = String.valueOf(ApprovalStatus.APPROVED);
        when(accessControlApprovalService.acceptApprovalRequestOnApprovalApiSide(approvalId, serviceAgreementId, userId))
            .thenReturn(
                new ApprovalDto().status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED));
        when(approverFactory.getApprover(any())).thenReturn(approver);

        acceptApprovalRequest.acceptApprovalRequest(approvalId, request, serviceAgreementId, userId);
        verify(approver, times(1))
            .manageApprove(approvalId, serviceAgreementId, userId);
        assertEquals(approvalStatus, response.getApprovalStatus().toString());
    }

}