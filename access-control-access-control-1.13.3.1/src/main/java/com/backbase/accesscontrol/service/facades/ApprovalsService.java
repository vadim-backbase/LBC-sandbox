package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.business.service.ApprovalService;
import com.backbase.accesscontrol.dto.ApprovalsListDto;
import com.backbase.accesscontrol.dto.parameterholder.ApprovalsParametersHolder;
import com.backbase.accesscontrol.routes.approval.AcceptApprovalRouteProxy;
import com.backbase.accesscontrol.routes.approval.GetPermissionsApprovalDetailsByIdRouteProxy;
import com.backbase.accesscontrol.routes.approval.ListPendingApprovalsRouteProxy;
import com.backbase.accesscontrol.routes.approval.RejectApprovalRequestRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.PutUpdateStatusRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import java.util.UUID;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of service request/reply interface that is transport agnostic. Forwards on to relevant component
 * depending on service type.
 */
@Service
public class ApprovalsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalsService.class);

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_PENDING_APPROVALS)
    private ListPendingApprovalsRouteProxy listPendingApprovalsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_PERMISSIONS_APPROVAL_BY_ID)
    private GetPermissionsApprovalDetailsByIdRouteProxy getPermissionsApprovalDetailsByIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_REJECT_APPROVAL_REQUEST)
    private RejectApprovalRequestRouteProxy rejectApprovalRequestRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_ACCEPT_APPROVAL_REQUEST)
    private AcceptApprovalRouteProxy acceptApprovalRouteProxy;

    @Autowired
    private ApprovalService approvalService;
    @Autowired
    private InternalRequestContext internalRequestContext;

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_LIST_PENDING_APPROVALS endpoint.
     *
     * @param parametersHolder - request data {@link ApprovalsParametersHolder}.
     * @return {@link ApprovalsParametersHolder} for retrieving pending approvals.
     */
    public ApprovalsListDto listPendingApprovals(
        ApprovalsParametersHolder parametersHolder) {
        LOGGER.info("Trying to list pending approvals for user {} under service agreement {}",
            parametersHolder.getUserId(), parametersHolder.getServiceAgreementId());
        return listPendingApprovalsRouteProxy
            .listApprovals(getInternalRequest(parametersHolder, internalRequestContext)).getData();
    }

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_GET_APPROVAL_DETAILS_BY_ID endpoint.
     */
    /**
     * Produces an Exchange to the DIRECT_BUSINESS_GET_APPROVAL_BY_ID endpoint.
     *
     * @param approvalId         - id of the approval to be returned
     * @param serviceAgreementId - service agreement id from context
     * @param userId             - user id of the logged in user
     * @return - InternalRequest with {@link PresentationPermissionsApprovalDetailsItem}
     */
    public PresentationPermissionsApprovalDetailsItem getPermissionsApprovalDetailsById(String approvalId,
        String serviceAgreementId, String userId) {
        LOGGER.info("Trying to get approval with id {}", approvalId);
        return getPermissionsApprovalDetailsByIdRouteProxy
            .getPermissionsApprovalById(getVoidInternalRequest(internalRequestContext), approvalId,
                serviceAgreementId, userId).getData();
    }

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_REJECT_APPROVAL_REQUEST endpoint.
     *
     * @param approvalId         approval id
     * @param serviceAgreementId service agreement id
     * @param userId             user id
     * @return {@link PresentationApprovalStatus}
     */
    public PresentationApprovalStatus rejectApprovalRequest(String approvalId,
        String serviceAgreementId, String userId) {
        return rejectApprovalRequestRouteProxy
            .rejectApprovalRequest(approvalId, getVoidInternalRequest(internalRequestContext), serviceAgreementId,
                userId).getData();
    }

    /**
     * Produces an Exchange to the DIRECT_BUSINESS_ACCEPT_APPROVAL_REQUEST endpoint.
     *
     * @param approvalId         - id of the approval to be accepted
     * @param serviceAgreementId service agreement id
     * @param userId             user id
     * @return {@link PresentationApprovalStatus}
     */
    public PresentationApprovalStatus acceptApprovalRequest(String approvalId,
        String serviceAgreementId, String userId) {
        LOGGER.info("Trying to accept approval with id {}", approvalId);
        return acceptApprovalRouteProxy
            .acceptApprovalRequest(getVoidInternalRequest(internalRequestContext), approvalId, serviceAgreementId,
                userId).getData();
    }

    /**
     * Makes a call to approval service in order to get approval. It creates the request.
     *
     * @param serviceAgreementId service agreement
     * @param function           function name
     * @param action             action type (create, update..)
     * @return {@link PresentationPostApprovalResponse}
     */
    public PresentationPostApprovalResponse getApprovalResponse(String userId,
        String serviceAgreementId, String resource, String function, String action) {

        PresentationPostApprovalRequest approvalRequest = new PresentationPostApprovalRequest();
        approvalRequest.setUserId(userId);
        approvalRequest.setServiceAgreementId(serviceAgreementId);
        approvalRequest.setResource(resource);
        approvalRequest.setItemId(UUID.randomUUID().toString());
        approvalRequest.setFunction(function);
        approvalRequest.setAction(action);

        return approvalService.postApprovals(approvalRequest);
    }

    /**
     * Cancels approval request.
     *
     * @param approvalId approval id
     */
    public void cancelApprovalRequest(String approvalId) {
        PutUpdateStatusRequest putUpdateStatusRequest = new PutUpdateStatusRequest()
            .status(ApprovalStatus.CANCELLED);

        approvalService.putSetStatusById(approvalId, putUpdateStatusRequest);
    }

}
