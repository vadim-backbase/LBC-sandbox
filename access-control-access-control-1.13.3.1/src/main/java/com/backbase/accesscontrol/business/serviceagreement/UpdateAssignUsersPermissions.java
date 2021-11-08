package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_072;
import static java.util.Objects.requireNonNull;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateAssignUsersPermissionsRouteProxy;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.service.facades.PermissionsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.ApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateAssignUsersPermissions implements UpdateAssignUsersPermissionsRouteProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAssignUsersPermissions.class);
    private static final String SERVICE_AGREEMENT = "Service Agreement";
    private static final String ASSIGN_PERMISSIONS = "Assign Permissions";
    private static final String ASSIGN_PERMISSIONS_ACTION = "EDIT";

    private UserManagementService userManagementService;
    private PermissionsService permissionsService;
    private UserContextUtil userContextUtil;
    private ApprovalsService approvalsService;
    private ApplicationProperties applicationProperties;
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    private ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;

    /**
     * Update user permissions.
     *
     * @param internalRequest    internal request of {@link PresentationFunctionDataGroupItems}
     * @param serviceAgreementId service agreement id
     * @param userId             user id
     * @return internal request of {@link PresentationApprovalStatus}
     */
    @Override
    @Consume(value = DIRECT_DEFAULT_DIRECT_BUSINESS_ASSIGN_USERS_PERMISSIONS)
    public InternalRequest<PresentationApprovalStatus> putAssignUsersPermissions(
        @Body InternalRequest<PresentationFunctionDataGroupItems> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("userId") String userId) {
        LOGGER.info("Updating permissions for user id {}, under service agreement id {}",
            userId, serviceAgreementId);

        GetUser userByInternalId = userManagementService
            .getUserByInternalId(userId);

        LOGGER.info("Retrieved user with user id {} ", userId);

        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            checkIfSomeUserFromLegalEntityHasPendingPermissions(serviceAgreementId,
                userByInternalId.getLegalEntityId());

            return getStatusApprovalOn(internalRequest, serviceAgreementId, userId, userByInternalId);
        }
        return getStatusApprovalOff(internalRequest, serviceAgreementId, userId, userByInternalId);

    }

    private void checkIfSomeUserFromLegalEntityHasPendingPermissions(String serviceAgreementId, String legalEntityId) {
        if (approvalServiceAgreementJpaRepository.existsByServiceAgreementId(serviceAgreementId)) {
            long count = approvalUserContextJpaRepository.countByServiceAgreementIdAndLegalEntityId(
                serviceAgreementId, legalEntityId);
            if (count == 0) {
                LOGGER.warn(
                    "You are not able to update the permissions, "
                        + "while there is a pending change to the service agreement");
                throw getBadRequestException(CommandErrorCodes.ERR_ACC_106.getErrorMessage(),
                    CommandErrorCodes.ERR_ACC_106.getErrorCode());
            }
        }
    }

    private InternalRequest<PresentationApprovalStatus> getStatusZeroPolicyApproval(
        InternalRequest<PresentationFunctionDataGroupItems> request, String serviceAgreementId, String userId,
        com.backbase.dbs.user.api.client.v2.model.GetUser userByInternalId) {

        permissionsService
            .savePermissions(request.getData(), userByInternalId, serviceAgreementId, userId);

        PresentationApprovalStatus presentationApprovalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(ApprovalStatus.APPROVED);
        return getInternalRequest(presentationApprovalStatus, request.getInternalRequestContext());
    }

    private InternalRequest<PresentationApprovalStatus> getStatusApprovalOff(
        InternalRequest<PresentationFunctionDataGroupItems> request, String serviceAgreementId, String userId,
        com.backbase.dbs.user.api.client.v2.model.GetUser userByInternalId) {

        permissionsService
            .savePermissions(request.getData(), userByInternalId, serviceAgreementId,
                userId);
        return getInternalRequest(new PresentationApprovalStatus(), request.getInternalRequestContext());
    }

    @SuppressWarnings("squid:S2139")
    private InternalRequest<PresentationApprovalStatus> getStatusApprovalOn(
        InternalRequest<PresentationFunctionDataGroupItems> request,
        String serviceAgreementId, String userId,
        com.backbase.dbs.user.api.client.v2.model.GetUser userByInternalId) {

        PresentationPostApprovalResponse approvalResponse = approvalsService
            .getApprovalResponse(userContextUtil.getUserContextDetails().getInternalUserId(),
                userContextUtil.getServiceAgreementId(), SERVICE_AGREEMENT, ASSIGN_PERMISSIONS,
                ASSIGN_PERMISSIONS_ACTION);
        if (requireNonNull(approvalResponse).getApproval().getStatus()
            == com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.APPROVED) {
            return getStatusZeroPolicyApproval(request, serviceAgreementId, userId, userByInternalId);
        }
        try {
            permissionsService
                .savePermissionsToApproval(request.getData(), serviceAgreementId,
                    userId, userByInternalId.getLegalEntityId(), approvalResponse.getApproval().getId());
        } catch (BadRequestException error) {
            LOGGER.warn("Bad request exception during creating user assign approval {}: {}",
                approvalResponse.getApproval().getId(),
                error.getErrors().get(0).getMessage());
            try {
                approvalsService.cancelApprovalRequest(approvalResponse.getApproval().getId());
            } catch (Exception e) {
                error.getErrors()
                    .add(new Error().withKey(ERR_ACQ_072.getErrorCode()).withMessage(ERR_ACQ_072.getErrorMessage()));
            }
            throw error;
        }

        ApprovalStatus statusFromApproval = ApprovalStatus
            .fromValue(approvalResponse.getApproval().getStatus().toString());
        PresentationApprovalStatus presentationApprovalStatus = new PresentationApprovalStatus()
            .withApprovalStatus(statusFromApproval);
        return getInternalRequest(presentationApprovalStatus, request.getInternalRequestContext());
    }

}
