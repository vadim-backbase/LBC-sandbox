package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GROUP_VALIDATE;

import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.accesscontrol.dto.DataItemsValidatable;
import com.backbase.accesscontrol.routes.datagroup.ValidateDataGroupRouteProxy;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Business consumer for adding new Data Groups. This class is a business process component of the access-group
 * presentation service, communicating with the P&P services.
 */
@Service
@RequiredArgsConstructor
public class AddDataGroup {

    public static final String ENTITLEMENTS = "Entitlements";
    public static final String MANAGE_DATA_GROUPS = "Manage Data Groups";
    public static final String CREATE = "CREATE";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddDataGroup.class);
    @Value("${backbase.data-group.validation.enabled}")
    private boolean validationEnabled;

    @Produce(value = DIRECT_BUSINESS_GROUP_VALIDATE)
    private ValidateDataGroupRouteProxy validateDataGroupRouteProxy;

    private final DataGroupPAndPService dataGroupPandPService;
    private final UserContextUtil userContextUtil;
    private final ApprovalsService approvalsService;
    private final ApplicationProperties applicationProperties;

    /**
     * Method that listens on the direct:addDataGroupRequestedInternal endpoint.
     *
     * @param request Internal Request of {@link DataGroupBase} type to be send by the client
     * @return Business Process Result of DataGroupsPostResponseBody
     */
    @Consume(value = EndpointConstants.DIRECT_ADD_DATA_GROUP_PERSIST)
    public InternalRequest<DataGroupOperationResponse> addDataGroup(InternalRequest<DataGroupBase> request) {
        LOGGER.info("Trying to add data group");

        return getInternalRequest(getOperationResponse(request.getData()), request.getInternalRequestContext());
    }

    private DataGroupOperationResponse getOperationResponse(DataGroupBase request) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            return getOperationResponseApprovalOn(request);
        }
        return getOperationResponseApprovalOff(request);
    }

    private DataGroupOperationResponse getOperationResponseApprovalOn(DataGroupBase request) {
        LOGGER.info("Adding Data Group with approval ON");
        PresentationPostApprovalResponse approvalResponse = getPresentationPostApprovalResponse();

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            return getOperationResponseZeroPolicyApproval(request);
        }

        DataGroupOperationResponse dataGroup;
        try {
            DataGroupsPostResponseBody dataGroupFromDba = dataGroupPandPService
                .createDataGroupWithApproval(request, approvalResponse.getApproval().getId());

            dataGroup = new DataGroupOperationResponse()
                .withId(dataGroupFromDba.getId())
                .withApprovalOn(true);

        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during creating data group approval");
            approvalsService.cancelApprovalRequest(approvalResponse.getApproval().getId());
            throw error;
        }
        return dataGroup;

    }

    private PresentationPostApprovalResponse getPresentationPostApprovalResponse() {
        return approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_DATA_GROUPS, CREATE);
    }

    private DataGroupOperationResponse getOperationResponseZeroPolicyApproval(DataGroupBase request) {
        LOGGER.info("Adding Data Group with approval ON with zero policy approval");
        return getOperationResponseApprovalOff(request);
    }

    private DataGroupOperationResponse getOperationResponseApprovalOff(DataGroupBase request) {
        DataGroupsPostResponseBody dataGroupFromDba = dataGroupPandPService.createDataGroupWithAudit(request);
        return new DataGroupOperationResponse()
            .withId(dataGroupFromDba.getId())
            .withApprovalOn(false);
    }


    /**
     * Validates type of data group.
     *
     * @param request Internal Request of {@link DataGroupBase} type to be validated
     */
    @Consume(value = EndpointConstants.DIRECT_ADD_DATA_GROUP_VALIDATE)
    public void validateDataGroupType(InternalRequest<DataGroupBase> request) {
        if (validationEnabled) {
            InternalRequest<DataItemsValidatable> body = createBody(request);
            validateDataGroupRouteProxy.validate(body);
        }
    }

    private InternalRequest<DataItemsValidatable> createBody(InternalRequest<DataGroupBase> request) {
        return getInternalRequest(createDataItem(request.getData()), request.getInternalRequestContext());
    }

    private DataItemsValidatable createDataItem(DataGroupBase data) {
        return new DataItemsValidatable(data.getType(),
            data.getItems(),
            data.getServiceAgreementId());
    }
}
