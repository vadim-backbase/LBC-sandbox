package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.business.service.ServiceAgreementApprovalService;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for adding new Service agreement. This class is a business process component of the access-group
 * presentation service, communicating with the P&P services.
 */
@Service
@AllArgsConstructor
public class AddServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddServiceAgreement.class);
    private static final String ENTITLEMENTS = "Entitlements";
    public static final String MANAGE_SERVICE_AGREEMENT = "Manage Service Agreements";
    public static final String CREATE = "CREATE";

    private UserContextUtil userContextUtil;
    private ServiceAgreementApprovalService serviceAgreementApprovalService;

    private ApprovalsService approvalsService;
    private ApprovalOnRequestScope approvalOnRequestScope;
    private ApplicationProperties applicationProperties;

    /**
     * Method that listens on the {@value EndpointConstants#DIRECT_DEFAULT_ADD_SERVICE_AGREEMENT} endpoint.
     *
     * @param request Internal Request of {@link ServiceAgreementPostRequestBody} type to be send by the client
     * @return Business Process Result of {@link ServiceAgreementPostResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_ADD_SERVICE_AGREEMENT)
    public InternalRequest<ServiceAgreementPostResponseBody> addServiceAgreement(
        InternalRequest<ServiceAgreementPostRequestBody> request) {
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody = request.getData();
        LOGGER.info("Trying to add service agreement {}", serviceAgreementPostRequestBody);

        String legalEntityId = userContextUtil.getUserContextDetails().getLegalEntityId();

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            return getInternalRequest(
                getCreatedServiceAgreementApprovalOn(serviceAgreementPostRequestBody, legalEntityId),
                request.getInternalRequestContext());
        }

        return getInternalRequest(getCreatedServiceAgreement(serviceAgreementPostRequestBody, legalEntityId),
            request.getInternalRequestContext());
    }

    private ServiceAgreementPostResponseBody getCreatedServiceAgreementApprovalOn(
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody, String legalEntityId) {
        LOGGER.info("Adding Service agreement with approval ON");
        PresentationPostApprovalResponse approvalResponse = approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_SERVICE_AGREEMENT, CREATE);

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            LOGGER.info("Adding Service agreement with approval ON with zero policy approval");
            return getCreatedServiceAgreement(serviceAgreementPostRequestBody, legalEntityId);
        }
        ServiceAgreementPostResponseBody result;
        try {
            result = serviceAgreementApprovalService
                .createServiceAgreementWithApproval(serviceAgreementPostRequestBody, legalEntityId,
                    approvalResponse.getApproval().getId());
            approvalOnRequestScope.setApproval(true);
        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during creating service agreement approval");
            approvalsService.cancelApprovalRequest(approvalResponse.getApproval().getId());
            throw error;
        }
        return result;
    }


    /**
     * Method for creating service agreement. This method simply creates service agreement.
     *
     * @param serviceAgreementPostRequestBody presentation payload {@link ServiceAgreementPostResponseBody}
     * @param legalEntityId                   creators legal entity id
     * @return id of the created service agreement
     */
    public ServiceAgreementPostResponseBody getCreatedServiceAgreement(
        ServiceAgreementPostRequestBody serviceAgreementPostRequestBody, String legalEntityId) {
        return serviceAgreementApprovalService.createServiceAgreement(serviceAgreementPostRequestBody, legalEntityId);
    }
}

