package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for deleting a Function Group This class is the business process component of the access-group
 * presentation service, communicating with the P&P service.
 */
@Service
@AllArgsConstructor
public class DeleteFunctionGroup {

    private static final String ENTITLEMENTS = "Entitlements";
    private static final String MANAGE_FUNCTION_GROUPS = "Manage Function Groups";
    private static final String ACTION = "DELETE";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFunctionGroup.class);

    private FunctionGroupPAndPService functionGroupPAndPService;

    private ApprovalsService approvalsService;
    private AccessControlApprovalService accessControlApprovalService;
    private ApprovalOnRequestScope approvalOnRequestScope;
    private UserContextUtil userContextUtil;
    private ApplicationProperties applicationProperties;


    /**
     * Method that listens on the direct:deleteFunctionGroupRequestedInternal endpoint
     *
     * @param request Internal Request of {@link Void} type to be sent by the client
     * @param id      id of the Function Group to be deleted
     * @return Internal Request of {@link Void}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_DELETE_FUNCTION_GROUP_BY_ID)
    public InternalRequest<Void> deleteFunctionGroup(@Body InternalRequest<Void> request, @Header("id") String id) {
        LOGGER.info("Trying to delete Function Group by given ID {}", id);
        deleteFunctionGroup(id);
        return getVoidInternalRequest(request.getInternalRequestContext());
    }

    private void deleteFunctionGroup(String id) {

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            getStatusBusinessProcessResultApprovalOn(id);
        } else {
            deleteFunctionGroupZeroPolicyOrApprovalOff(id);
        }
    }

    private void getStatusBusinessProcessResultApprovalOn(String id) {

        LOGGER.info("Deleting Function Group with approval ON");
        PresentationPostApprovalResponse approvalResponse = approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_FUNCTION_GROUPS, ACTION);

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            LOGGER.info("Deleting Function Group with approval ON with zero policy approval");
            deleteFunctionGroupZeroPolicyOrApprovalOff(id);
        } else {
            try {
                functionGroupPAndPService.deleteFunctionGroup(id, approvalResponse.getApproval().getId());
                approvalOnRequestScope.setApproval(true);
            } catch (BadRequestException | NotFoundException error) {
                LOGGER.warn("Bad request | Not found exception during deletion of function group approval");
                approvalsService.cancelApprovalRequest(approvalResponse.getApproval().getId());
                throw error;
            }
        }
    }

    private void deleteFunctionGroupZeroPolicyOrApprovalOff(String id) {
        functionGroupPAndPService.
            deleteFunctionGroup(id);
        if (nonNull(accessControlApprovalService.getApprovalTypeIdFromApprovals(id))) {
            accessControlApprovalService.deleteApprovalType(id);
        }
    }

}
