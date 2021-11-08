package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.persistence.approvals.RejectApprovalHandler;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RejectApprovalRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RejectApprovalRequest.class);
    private AccessControlApprovalService accessControlApprovalService;
    private RejectApprovalHandler rejectApprovalHandler;

    /**
     * Reject an approval request.
     *
     * @param approvalId         approval id
     * @param internalRequest    void internal request
     * @param serviceAgreementId service agreement id
     * @param userId             user id
     * @return status of the approval request.
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_REJECT_APPROVAL_REQUEST)
    public InternalRequest<PresentationApprovalStatus> rejectApprovalRequest(
        @Header("approvalId") String approvalId,
        @Body InternalRequest<Void> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("userId") String userId) {

        ApprovalDto approvalDto = accessControlApprovalService
            .rejectApprovalRequestOnApprovalApiSide(approvalId, serviceAgreementId, userId);

        rejectApprovalHandler.handleRequest(new SingleParameterHolder<>(approvalId), null);

        PresentationApprovalStatus responseToReturn = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.valueOf(approvalDto.getStatus().toString()));
        LOGGER
            .info(
                "Invoking access control approval client to reject approval request with the following id: {}",
                approvalId);

        return getInternalRequest(responseToReturn, internalRequest.getInternalRequestContext());
    }
}
