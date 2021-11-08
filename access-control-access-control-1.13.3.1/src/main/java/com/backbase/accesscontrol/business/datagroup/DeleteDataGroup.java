package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
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
 * Business consumer for deleting a Data Group This class is the business process component of the access-group
 * presentation service, communicating with the persistence service.
 */
@Service
@AllArgsConstructor
public class DeleteDataGroup {

    private static final String ENTITLEMENTS = "Entitlements";
    private static final String MANAGE_DATA_GROUPS = "Manage Data Groups";
    private static final String ACTION = "DELETE";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataGroup.class);

    private DataGroupPAndPService dataGroupPAndPService;
    private ApprovalsService approvalsService;
    private UserContextUtil userContextUtil;
    private ApplicationProperties applicationProperties;

    /**
     * Method that listens on the direct:getDataGroupByIdRequestedInternal endpoint and uses the DataGroup client to
     * forward the delete request to the p&p service.
     *
     * @param request     void internal request
     * @param dataGroupId data group id
     * @return {@link InternalRequest} of {@link DataGroupOperationResponse}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_DELETE_DATA_GROUP)
    public InternalRequest<DataGroupOperationResponse> deleteDataGroup(@Body InternalRequest<Void> request,
        @Header("id") String dataGroupId) {
        LOGGER.info("Trying to delete data group with id {}", dataGroupId);
        return getInternalRequest(getResult(dataGroupId), request.getInternalRequestContext());
    }

    private DataGroupOperationResponse getResult(String dataGroupId) {

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            return getStatusBusinessProcessResultApprovalOn(dataGroupId);
        }

        return getDataGroupOperationResponseBusinessProcessResult(dataGroupId);
    }

    private DataGroupOperationResponse getDataGroupOperationResponseBusinessProcessResult(
        String dataGroupId) {
        dataGroupPAndPService.deleteDataGroup(dataGroupId);
        return new DataGroupOperationResponse()
            .withApprovalOn(false);
    }

    private DataGroupOperationResponse getStatusBusinessProcessResultApprovalOn(
        String dataGroupId) {
        LOGGER.info("Deleting Data Group with approval ON");
        PresentationPostApprovalResponse approvalResponse = getPresentationPostApprovalResponse();

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            return getDataGroupOperationResponseBusinessProcessResult(dataGroupId);
        }

        DataGroupOperationResponse dataGroup;
        String approvalId = approvalResponse.getApproval().getId();
        try {
            dataGroupPAndPService.deleteDataGroupWithApproval(dataGroupId, approvalId);

            dataGroup = new DataGroupOperationResponse()
                .withId(approvalId)
                .withApprovalOn(true);

        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during deleting data group approval");
            approvalsService.cancelApprovalRequest(approvalId);
            throw error;
        }

        return dataGroup;

    }

    private PresentationPostApprovalResponse getPresentationPostApprovalResponse() {
        return approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_DATA_GROUPS, ACTION
            );
    }
}
