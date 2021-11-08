package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_090;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.business.service.AccessControlApprovalService;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.approval.api.client.v2.model.PolicyDetailsDto;
import com.backbase.dbs.approval.api.client.v2.model.PolicyItemDetailsDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalLogItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer retrieving approval by id. This class is the business process component of the access-group
 * presentation service, communicating with the p&p service and retrieving approval by ID.
 */
@Service
@AllArgsConstructor
public class GetPermissionsApprovalDetailsById {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPermissionsApprovalDetailsById.class);

    private UserManagementService userManagementService;
    private AccessControlApprovalService accessControlApprovalService;

    /**
     * Sends request to pandp service for retrieving pending approvals.
     *
     * @param internalRequest the internal request
     * @param approvalId approval id
     * @param serviceAgreementId - service agreement id from context
     * @param userId - logged in user
     * @return Business Process Result of List{@link PresentationUserDataItemPermission}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_PERMISSIONS_APPROVAL_BY_ID)
    public InternalRequest<PresentationPermissionsApprovalDetailsItem> getPermissionsApprovalById(
        @Body InternalRequest<Void> internalRequest,
        @Header("approvalId") String approvalId, @Header("serviceAgreementId") String serviceAgreementId,
        @Header("userId") String userId) throws ExecutionException, InterruptedException {

        LOGGER.info("Trying to get approval by id {}, sa id {}, user id {}", approvalId, serviceAgreementId,
            userId);

        FutureTask<PresentationApprovalDetailDto> approvalDetailById = accessControlApprovalService
            .getPresentationApprovalDetailDto(
                approvalId,
                serviceAgreementId, userId);

        PresentationPermissionsApprovalDetailsItem persistenceApprovalPermissions = accessControlApprovalService
            .getPersistenceApprovalPermissions(approvalId, serviceAgreementId, userId);

        String userFullName = userManagementService.getUserByInternalId(
            persistenceApprovalPermissions.getUserId()).getFullName();

        PresentationApprovalDetailDto approval = approvalDetailById.get();

        if (!approval.getServiceAgreementId().equals(serviceAgreementId)) {
            LOGGER.warn("Service agreement from approval with id {} does not match id sent in request",
                approval.getServiceAgreementId());
            throw getForbiddenException(ERR_AG_090.getErrorMessage(), ERR_AG_090.getErrorCode());
        }

        PresentationPermissionsApprovalDetailsItem responseToReturn = buildResponseForApprovalById(
            approvalId, userFullName, approval, persistenceApprovalPermissions);

        return getInternalRequest(responseToReturn, internalRequest.getInternalRequestContext());
    }


    private PresentationPermissionsApprovalDetailsItem buildResponseForApprovalById(String approvalId,
        String userFullName,
        PresentationApprovalDetailDto approvalDetailById,
        PresentationPermissionsApprovalDetailsItem persistenceApprovalPermissions) {

        PolicyDetailsDto policy = approvalDetailById.getPolicy();
        if (policy == null) {
            LOGGER.warn("Missing policy assignment for service agreement and approval with id {}", approvalId);
            throw getInternalServerErrorException("Missing policy assignment for service agreement");
        }
        int totalRequired = 0;
        if (policy.getItems() != null) {
            totalRequired = policy.getItems().stream()
                .filter(i -> nonNull(i) && nonNull(i.getNumberOfApprovals()))
                .mapToInt(PolicyItemDetailsDto::getNumberOfApprovals)
                .sum();
        }

        persistenceApprovalPermissions.setApprovalId(approvalId);
        persistenceApprovalPermissions.setCreatedAt(approvalDetailById.getCreatedAt());
        persistenceApprovalPermissions.setCompletedApproves(approvalDetailById.getRecords().size());
        persistenceApprovalPermissions.setRequiredApproves(totalRequired);

        List<PresentationApprovalLogItem> approvalLogItems = approvalDetailById.getRecords().stream()
            .map(record -> new PresentationApprovalLogItem()
                .withApprovedAt(record.getCreatedAt())
                .withApproverFullName(record.getUserFullName())
                .withApproverId(record.getUserId()))
            .collect(Collectors.toList());

        persistenceApprovalPermissions.setApprovalLog(approvalLogItems);
        persistenceApprovalPermissions.setCreatorUserFullName(approvalDetailById.getUserFullName());
        persistenceApprovalPermissions.setCreatorUserId(approvalDetailById.getUserId());
        persistenceApprovalPermissions.setUserId(persistenceApprovalPermissions.getUserId());
        persistenceApprovalPermissions.setUserFullName(userFullName);
        return persistenceApprovalPermissions;
    }
}
