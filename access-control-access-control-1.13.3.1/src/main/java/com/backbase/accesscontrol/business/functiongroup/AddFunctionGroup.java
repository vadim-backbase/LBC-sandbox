package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for adding new Function Group. This class is a business process component of the access-group
 * presentation service, communicating with the P&P services.
 */
@Service
@AllArgsConstructor
public class AddFunctionGroup {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddFunctionGroup.class);
    private static final String ENTITLEMENTS = "Entitlements";
    public static final String MANAGE_FUNCTION_GROUPS = "Manage Function Groups";
    public static final String CREATE = "CREATE";

    private FunctionGroupPAndPService functionGroupPAndPService;
    private UserContextUtil userContextUtil;
    private DateTimeService dateTimeService;

    private ApprovalsService approvalsService;
    private AccessControlApprovalService accessControlApprovalService;
    private ApprovalOnRequestScope approvalOnRequestScope;
    private ApplicationProperties applicationProperties;


    /**
     * Method that listens on the direct:addFunctionGroupRequestedInternal endpoint
     *
     * @param request Internal Request of {@link FunctionGroupBase} type to be send by the client
     * @return Internal Request of {@link FunctionGroupsPostResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_ADD_FUNCTION_GROUP)
    public InternalRequest<FunctionGroupsPostResponseBody> addGroup(InternalRequest<FunctionGroupBase> request) {
        LOGGER.info("Trying to add function group");

        return getInternalRequest(addFunctionGroup(request.getData()), request.getInternalRequestContext());
    }

    private FunctionGroupsPostResponseBody addFunctionGroup(FunctionGroupBase request) {
        validate(request);
        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            return createFunctionGroupApprovalOn(request);
        }
        return createFunctionGroup(request);

    }

    private void validate(FunctionGroupBase request) {
        dateTimeService.validateTimebound(request.getValidFromDate(), request.getValidFromTime(),
            request.getValidUntilDate(), request.getValidUntilTime());
    }

    private FunctionGroupsPostResponseBody createFunctionGroupApprovalOn(FunctionGroupBase request) {
        LOGGER.info("Adding Function Group with approval ON");
        PresentationPostApprovalResponse approvalResponse = approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_FUNCTION_GROUPS, CREATE);

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            LOGGER.info("Adding Function Group with approval ON with zero policy approval");
            return createFunctionGroup(request);
        }
        FunctionGroupsPostResponseBody result;
        try {
            result = functionGroupPAndPService
                .createFunctionGroupWithApproval(request, approvalResponse.getApproval().getId());
            approvalOnRequestScope.setApproval(true);
        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during creating function group approval");
            approvalsService.cancelApprovalRequest(approvalResponse.getApproval().getId());
            throw error;
        }
        return result;
    }

    private FunctionGroupsPostResponseBody createFunctionGroup(FunctionGroupBase request) {

        FunctionGroupsPostResponseBody functionGroupsPostResponseBody = functionGroupPAndPService
            .createFunctionGroup(request);
        if (nonNull(request.getApprovalTypeId())) {
            accessControlApprovalService.createApprovalType(functionGroupsPostResponseBody.getId(),
                request.getApprovalTypeId());
        }
        return functionGroupsPostResponseBody;
    }

}
