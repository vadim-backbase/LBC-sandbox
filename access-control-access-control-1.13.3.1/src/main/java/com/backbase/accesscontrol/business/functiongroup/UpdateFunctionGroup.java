package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static java.util.Objects.isNull;

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
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for updating an existing Function Group. This class is a business process component of the access
 * group presentation service, communicating with the persistence services.
 */
@Service
@AllArgsConstructor
public class UpdateFunctionGroup {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFunctionGroup.class);
    private static final String ENTITLEMENTS = "Entitlements";
    private static final String MANAGE_FUNCTION_GROUPS = "Manage Function Groups";
    private static final String EDIT = "EDIT";

    private FunctionGroupPAndPService functionGroupPAndPService;
    private DateTimeService dateTimeService;
    private ApprovalsService approvalsService;
    private AccessControlApprovalService accessControlApprovalService;
    private ApprovalOnRequestScope approvalOnRequestScope;
    private UserContextUtil userContextUtil;
    private ApplicationProperties applicationProperties;

    /**
     * Method that listens on the direct:direct:updateFunctionGroupByIdRequestedInternal endpoint and  forwards the put
     * request to the persistence service.
     *
     * @param request Internal Request of FunctionGroupByIdPutRequestBody type to be send by the client
     * @param functionGroupId      function group id
     * @return InternalRequest of {@link Void}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_UPDATE_FUNCTION_GROUP_BY_ID)
    public InternalRequest<Void> updateFunctionGroupById(@Body InternalRequest<FunctionGroupByIdPutRequestBody> request,
        @Header("id") String functionGroupId) {

        FunctionGroupByIdPutRequestBody putRequestBody = request.getData();

        dateTimeService.validateTimebound(putRequestBody.getValidFromDate(), putRequestBody.getValidFromTime(),
            putRequestBody.getValidUntilDate(), putRequestBody.getValidUntilTime());

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            updateFunctionGroupWithApprovalOn(putRequestBody, functionGroupId);
        } else {
            updateFunctionGroupWithApprovalOff(putRequestBody, functionGroupId);
        }

        return getVoidInternalRequest(request.getInternalRequestContext());
    }

    private void updateFunctionGroupWithApprovalOff(
        FunctionGroupByIdPutRequestBody request, String functionGroupId) {

        LOGGER.info("Updating function group with functionGroupId id: {}", functionGroupId);
        if (functionGroupPAndPService.getFunctionGroupById(functionGroupId).getType() == Type.DEFAULT) {
            functionGroupPAndPService.updateFunctionGroup(request, functionGroupId);
        }
        String oldApprovalType = accessControlApprovalService.getApprovalTypeIdFromApprovals(functionGroupId);
        String newApprovalType = request.getApprovalTypeId();

        if (!Objects.equals(oldApprovalType, newApprovalType)) {
            if (isNull(oldApprovalType)) {
                accessControlApprovalService.createApprovalType(functionGroupId, newApprovalType);
            } else if (isNull(newApprovalType)) {
                accessControlApprovalService.deleteApprovalType(functionGroupId);
            } else {
                accessControlApprovalService.updateApprovalType(functionGroupId, newApprovalType);
            }
        }
    }

    private void updateFunctionGroupWithApprovalOn(
        FunctionGroupByIdPutRequestBody request, String functionGroupId) {
        LOGGER.info("Updating Function Group with approval ON");

        PresentationPostApprovalResponse approvalResponse = approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_FUNCTION_GROUPS, EDIT);

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            LOGGER.info("Adding Function Group with approval ON with zero policy approval");
            updateFunctionGroupWithApprovalOff(request, functionGroupId);
            return;
        }

        String approvalId = approvalResponse.getApproval().getId();
        LOGGER.info("Updating function group with approval id: {}", approvalId);

        try {
            functionGroupPAndPService.updateFunctionGroupWithApproval(request, functionGroupId, approvalId);
            approvalOnRequestScope.setApproval(true);
        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during updating function group with approval");
            approvalsService.cancelApprovalRequest(approvalId);
            throw error;
        }
    }
}
