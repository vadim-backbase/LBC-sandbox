package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

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
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Business consumer for updating Service agreement. This class is a business process component of the access-group
 * presentation service, communicating with the P&P services.
 */
@Service
@AllArgsConstructor
public class EditServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditServiceAgreement.class);
    private static final String ENTITLEMENTS = "Entitlements";
    public static final String MANAGE_SERVICE_AGREEMENT = "Manage Service Agreements";
    public static final String EDIT = "EDIT";

    private ApprovalOnRequestScope approvalOnRequestScope;
    private ApplicationProperties applicationProperties;
    private UserContextUtil userContextUtil;
    private ServiceAgreementApprovalService serviceAgreementApprovalService;
    private ApprovalsService approvalsService;

    /**
     * Method that listens on the direct:editServiceAgreementRequestedInternal endpoint.
     *
     * @param request            Internal Request of {@link ServiceAgreementSave} type to be send by the client
     * @param serviceAgreementId id of the Service Agreement to be updated
     * @return void internal request
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_EDIT_SERVICE_AGREEMENT)
    public InternalRequest<Void> editServiceAgreement(InternalRequest<ServiceAgreementSave> request,
        @Header("id") String serviceAgreementId) {
        ServiceAgreementSave serviceAgreementSave = request.getData();
        LOGGER.info("Trying to update service agreement {}", serviceAgreementId);

        if (Objects.isNull(serviceAgreementSave.getIsMaster())) {
            serviceAgreementSave.withIsMaster(false);
        }

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            updateServiceAgreementWithApprovalOn(serviceAgreementSave, serviceAgreementId);
        } else {
            updateServiceAgreement(serviceAgreementSave, serviceAgreementId);
        }

        return getVoidInternalRequest(request.getInternalRequestContext());
    }

    private void updateServiceAgreement(ServiceAgreementSave serviceAgreementSave, String serviceAgreementId) {
        serviceAgreementApprovalService.updateServiceAgreement(serviceAgreementSave, serviceAgreementId);
    }

    private void updateServiceAgreementWithApprovalOn(ServiceAgreementSave serviceAgreementSave,
        String serviceAgreementId) {
        LOGGER.info("Update Service agreement with approval ON");
        PresentationPostApprovalResponse approvalResponse = approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(),
                ENTITLEMENTS, MANAGE_SERVICE_AGREEMENT, EDIT);

        if (approvalResponse.getApproval().getStatus() == ApprovalStatus.APPROVED) {
            LOGGER.info("Adding Service agreement with approval ON with zero policy approval");
            updateServiceAgreement(serviceAgreementSave, serviceAgreementId);
            return;
        }

        try {
            serviceAgreementApprovalService.updateServiceAgreementWithApproval(serviceAgreementSave, serviceAgreementId,
                approvalResponse.getApproval().getId());
            approvalOnRequestScope.setApproval(true);
        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during creating service agreement approval");
            approvalsService.cancelApprovalRequest(approvalResponse.getApproval().getId());
            throw error;
        }
    }

}
