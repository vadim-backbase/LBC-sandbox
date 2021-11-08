package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.approvers.ApproverFactory;
import com.backbase.accesscontrol.business.service.approvers.ApproverKey;
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

/**
 * Business consumer for accepting approval by id. This class is the business process component of the access-group
 * presentation service, communicating with the p&p service and accepting approval by ID.
 */
@Service
@AllArgsConstructor
public class AcceptApprovalRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptApprovalRequest.class);

    private AccessControlApprovalService accessControlApprovalService;
    private ApproverFactory approverFactory;

    /**
     * Approve an approval request.
     *
     * @param approvalId         approval id
     * @param internalRequest    void internal request
     * @param serviceAgreementId service agreement id
     * @param userId             user id
     * @return status of the approval request.
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_ACCEPT_APPROVAL_REQUEST)
    public InternalRequest<PresentationApprovalStatus> acceptApprovalRequest(
        @Header("approvalId") String approvalId,
        @Body InternalRequest<Void> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("userId") String userId) {

        ApprovalDto approvalDto = accessControlApprovalService
            .acceptApprovalRequestOnApprovalApiSide(approvalId, serviceAgreementId, userId);

        if (approvalDto.getStatus().equals(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED)) {
            approverFactory
                .getApprover(new ApproverKey(approvalDto.getFunction(), approvalDto.getAction()))
                .manageApprove(approvalId, serviceAgreementId, userId);
        }

        PresentationApprovalStatus responseToReturn = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.valueOf(approvalDto.getStatus().toString()));
        LOGGER
            .info(
                "Invoking access control approval client to approve approval request with the following id: {}",
                approvalId);
        return getInternalRequest(responseToReturn, internalRequest.getInternalRequestContext());
    }
}
