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
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Business consumer for updating existing Data Groups. This class is a business process component of the access-group
 * presentation service, communicating with the P&P services.
 */
@Service
@RequiredArgsConstructor
public class UpdateDataGroup {

    public static final String ENTITLEMENTS = "Entitlements";
    public static final String MANAGE_DATA_GROUPS = "Manage Data Groups";
    public static final String EDIT = "EDIT";
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroup.class);
    @Value("${backbase.data-group.validation.enabled}")
    private boolean validationEnabled;
    @Produce(value = DIRECT_BUSINESS_GROUP_VALIDATE)
    private ValidateDataGroupRouteProxy validateDataGroupRouteProxy;

    private final DataGroupPAndPService dataGroupPAndPService;
    private final ApprovalsService approvalsService;
    private final UserContextUtil userContextUtil;
    private final ApplicationProperties applicationProperties;


    /**
     * Method that listens on the direct:updateDataGroupRequestedInternal endpoint.
     *
     * @param request Internal Request of {@link DataGroupByIdPutRequestBody} type to be send by the client
     * @param id      data group id
     * @return Business Process Result of {@link DataGroupOperationResponse}
     */
    @Consume(value = EndpointConstants.DIRECT_UPDATE_DATA_GROUP_PERSIST)
    public InternalRequest<DataGroupOperationResponse> updateDataGroup(
        @Body InternalRequest<DataGroupByIdPutRequestBody> request,
        @Header("id") String id) {
        LOGGER.info("Trying to update data group by id {}", id);

        return getInternalRequest(getResult(request.getData(), id), request.getInternalRequestContext());
    }

    private DataGroupOperationResponse getResult(
        DataGroupByIdPutRequestBody request,
        String dataGroupId) {

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            return getStatusBusinessProcessResultApprovalOn(request, dataGroupId);
        }
        return getDataGroupOperationResponseBusinessProcessResult(request, dataGroupId);
    }

    private DataGroupOperationResponse getDataGroupOperationResponseBusinessProcessResult(
        DataGroupByIdPutRequestBody request, String dataGroupId) {

        dataGroupPAndPService.updateDataGroup(request, dataGroupId);

        return new DataGroupOperationResponse()
            .withApprovalOn(false);
    }

    private DataGroupOperationResponse getStatusBusinessProcessResultApprovalOn(
        DataGroupByIdPutRequestBody request, String dataGroupId) {
        LOGGER.info("Updating Data Group request {} for data group with id {} with approval ON",
            request, dataGroupId);

        PresentationPostApprovalResponse approvalResponse = getPresentationPostApprovalResponse();

        LOGGER.info("Response from approval {}", approvalResponse);

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            return getDataGroupOperationResponseBusinessProcessResult(request, dataGroupId);
        }

        String approvalId = approvalResponse.getApproval().getId();
        LOGGER.info("Updating data group with approval id: {}", approvalId);
        try {
            dataGroupPAndPService.updateDataGroupWithApproval(request, approvalId);

        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during updating data group approval");
            approvalsService.cancelApprovalRequest(approvalId);
            throw error;
        }

        return
            new DataGroupOperationResponse()
                .withApprovalOn(true)
                .withId(dataGroupId);
    }

    private PresentationPostApprovalResponse getPresentationPostApprovalResponse() {

        return approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_DATA_GROUPS, EDIT);
    }

    /**
     * Validates data group type.
     *
     * @param request Internal Request of {@link DataGroupByIdPutRequestBody} type to be send by the client
     * @param id      data group id
     */
    @Consume(value = EndpointConstants.DIRECT_UPDATE_DATA_GROUP_VALIDATE)
    public void validateDataGroupType(@Body InternalRequest<DataGroupByIdPutRequestBody> request,
        @Header("id") String id) {
        if (validationEnabled) {
            validateDataGroupRouteProxy.validate(createBody(request));
        }
    }

    private InternalRequest<DataItemsValidatable> createBody(InternalRequest<DataGroupByIdPutRequestBody> request) {
        return getInternalRequest(createDataItem(request.getData()), request.getInternalRequestContext());
    }

    private DataItemsValidatable createDataItem(DataGroupByIdPutRequestBody data) {
        return new DataItemsValidatable(data.getType(),
            data.getItems(),
            data.getServiceAgreementId());
    }
}
