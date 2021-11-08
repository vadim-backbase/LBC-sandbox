package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_090;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.accesscontrol.service.ServiceAgreementQueryService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.constants.FunctionToResourceMap;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.PostApprovalRecordRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.RecordStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A service class that communicates with the P&P services via the clients and returns responses.
 */
@Service
@AllArgsConstructor
public class AccessControlApprovalService {

    private static final String INCORRECT_SA_FROM_USER_CONTEXT = "Service agreement from user context is {}, "
        + "but the approval is created in service agreement {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlApprovalService.class);
    private static final String APPROVE = "approve";
    private ApprovalService approvalService;
    private ApplicationProperties applicationProperties;
    private UserAccessPermissionCheckService userAccessPermissionCheckService;
    private FunctionGroupService functionGroupService;
    private DataGroupService dataGroupService;
    private PermissionService permissionService;
    private ServiceAgreementQueryService serviceAgreementService;


    /**
     * Get user permissions from query client.
     *
     * @param approvalId         id of the approval
     * @param serviceAgreementId service agreement id from context
     * @param userId             user id of the logged in user
     * @return {@link PresentationPermissionsApprovalDetailsItem} status of the request
     */
    public PresentationPermissionsApprovalDetailsItem getPersistenceApprovalPermissions(String approvalId,
        String serviceAgreementId, String userId) {
        LOGGER.info("Calling access control pandp for get approval permission for approval with id {}", approvalId);
        checkPermissionsForApproval(approvalId, serviceAgreementId, userId);

        return permissionService.getUserPermissionApprovalDetails(approvalId);
    }

    /**
     * Get data group approval from query client.
     *
     * @param approvalId         id of the approval
     * @param serviceAgreementId service agreement id from context
     * @param userId             user id of the logged in user
     * @return {@link PresentationDataGroupApprovalDetailsItem} status of the request
     */
    public PresentationDataGroupApprovalDetailsItem getPersistenceApprovalDataGroups(String approvalId,
        String serviceAgreementId, String userId) {
        LOGGER.info("Calling access control pandp for get approval data groups for approval with id {}", approvalId);
        checkPermissionsForApproval(approvalId, serviceAgreementId, userId);

        return dataGroupService.getByApprovalId(approvalId);
    }

    /**
     * Get function group approval from query client.
     *
     * @param approvalId         id of the approval
     * @param serviceAgreementId service agreement id from context
     * @param userId             user id of the logged in user
     * @return {@link PresentationFunctionGroupApprovalDetailsItem} status of the request
     */
    public PresentationFunctionGroupApprovalDetailsItem getPersistenceApprovalFunctionGroups(String approvalId,
        String serviceAgreementId, String userId) {
        LOGGER.info("Calling access control pandp for get approval function groups details for approval with id {}",
            approvalId);
        checkPermissionsForApproval(approvalId, serviceAgreementId, userId);
        PresentationFunctionGroupApprovalDetailsItem persistenceFunctionGroupApprovalDetailsItem =
            functionGroupService.getByApprovalId(approvalId);
        if (nonNull(persistenceFunctionGroupApprovalDetailsItem)
            && nonNull(persistenceFunctionGroupApprovalDetailsItem.getOldState())) {
            persistenceFunctionGroupApprovalDetailsItem.getOldState().setApprovalTypeId(
                getApprovalTypeIdFromApprovals(persistenceFunctionGroupApprovalDetailsItem.getFunctionGroupId()));
        }
        return persistenceFunctionGroupApprovalDetailsItem;
    }

    /**
     * Get service agreement approval from query.
     *
     * @param approvalId         id of the approval
     * @param serviceAgreementId service agreement id from context
     * @param userId             user id of the logged in user
     * @return {@link PresentationFunctionGroupApprovalDetailsItem} status of the request
     */
    public ServiceAgreementApprovalDetailsItem getPersistenceApprovalServiceAgreement(String approvalId,
        String serviceAgreementId, String userId) {
        LOGGER.info("Calling persistence layer for get approval service agreement details for approval with id {}",
            approvalId);
        checkPermissionsForApproval(approvalId, serviceAgreementId, userId);
        return serviceAgreementService.getByApprovalId(approvalId);
    }

    /**
     * Get function group approval from query client.
     *
     * @param functionGroupId Function Group Id
     * @return ApprovalTypeId for given functionGroupId from approvals
     */
    public String getApprovalTypeIdFromApprovals(String functionGroupId) {
        if (applicationProperties.getApproval().getLevel().isEnabled()) {
            try {
                return approvalService.getApprovalTypeAssignment(functionGroupId);
            } catch (NotFoundException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Reject Approval Request on Approval Api.
     *
     * @param approvalId         id of the approval
     * @param serviceAgreementId service agreement id from context
     * @param userId             user id of the logged in user
     * @return {@link ApprovalDto} status of the request
     */
    public ApprovalDto rejectApprovalRequestOnApprovalApiSide(String approvalId, String serviceAgreementId,
        String userId) {
        LOGGER.info("Making a request to approval client to reject the approval request with id: {}.", approvalId);

        checkPermissionsForApproval(approvalId, serviceAgreementId, userId);

        PostApprovalRecordRequest approvalData = new PostApprovalRecordRequest()
            .serviceAgreementId(serviceAgreementId)
            .status(RecordStatus.REJECTED)
            .userId(userId);

        PresentationPostApprovalResponse approvalResponse = approvalService
            .postApprovalRecords(approvalId, approvalData);

        return approvalResponse.getApproval();
    }

    /**
     * Approve Approval Request on Approval Api.
     *
     * @param approvalId         id of the approval that needs to be approved
     * @param serviceAgreementId service agreement id from context
     * @param userId             user id of the logged in user
     * @return {@link ApprovalDto} status of the request
     */
    public ApprovalDto acceptApprovalRequestOnApprovalApiSide(String approvalId,
        String serviceAgreementId, String userId) {

        PostApprovalRecordRequest approvalData = new PostApprovalRecordRequest()
            .serviceAgreementId(serviceAgreementId)
            .status(RecordStatus.APPROVED)
            .userId(userId);

        checkPermissionsForApproval(approvalId, serviceAgreementId, userId);

        PresentationPostApprovalResponse response = approvalService
            .postApprovalRecords(approvalId, approvalData);

        return response.getApproval();
    }

    private void checkPermissionsForApproval(String approvalId, String serviceAgreementId, String userId) {

        PresentationGetApprovalDetailResponse approvalDetails = approvalService
            .getApprovalDetailById(approvalId, serviceAgreementId, userId, false);

        if (Objects.isNull(serviceAgreementId)
            || !serviceAgreementId.equals(approvalDetails.getApprovalDetails().getServiceAgreementId())) {

            LOGGER.warn(INCORRECT_SA_FROM_USER_CONTEXT,
                serviceAgreementId,
                approvalDetails.getApprovalDetails().getServiceAgreementId());
            throw getForbiddenException(ERR_AG_090.getErrorMessage(), ERR_AG_090.getErrorCode());
        }

        String approvalBusinessFunction = approvalDetails.getApprovalDetails().getFunction();

        String resourceName = FunctionToResourceMap.getResourceName(approvalBusinessFunction);
        if (nonNull(resourceName)) {

            userAccessPermissionCheckService
                .checkUserPermission(userId, serviceAgreementId, approvalBusinessFunction, resourceName, APPROVE);
        } else {
            throw getForbiddenException(ERR_AG_090.getErrorMessage(), ERR_AG_090.getErrorCode());
        }
    }

    /**
     * Gets approval details by id.
     *
     * @param approvalId         id of the approval
     * @param serviceAgreementId service agreement id from context
     * @param userId             user id of the logged in user
     * @return {@link FutureTask} of {@link PresentationApprovalDetailDto}
     */
    public FutureTask<PresentationApprovalDetailDto> getPresentationApprovalDetailDto(String approvalId,
        String serviceAgreementId, String userId) {
        LOGGER.info("Calling approval client for get approval with id {}, service agreement id {} and user Id {}",
            approvalId,
            serviceAgreementId, userId);

        Callable<PresentationApprovalDetailDto> approvalDetails = () -> approvalService
            .getApprovalDetailById(approvalId, serviceAgreementId, userId, true)
            .getApprovalDetails();

        FutureTask<PresentationApprovalDetailDto> approvalTask = new FutureTask<>(approvalDetails);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(approvalTask);

        return approvalTask;
    }

    /**
     * Updates Approval Type in approvals.
     *
     * @param functionGroupId function Group Id
     * @param newApprovalType Approval Type Id value to be replaced in approvals
     */
    public void updateApprovalType(String functionGroupId, String newApprovalType) {
        if (applicationProperties.getApproval().getLevel().isEnabled()) {
            approvalService.putApprovalTypeAssignment(functionGroupId, newApprovalType);
        }
    }

    /**
     * Removes Approval Type in approvals.
     *
     * @param functionGroupId function Group Id
     */
    public void deleteApprovalType(String functionGroupId) {
        if (applicationProperties.getApproval().getLevel().isEnabled()) {
            approvalService.deleteApprovalTypeAssignment(functionGroupId);
        }
    }

    /**
     * Creates Approval Type in approvals.
     *
     * @param functionGroupId function Group Id
     * @param newApprovalType Approval Type Id value to be stored in approvals
     */
    public void createApprovalType(String functionGroupId, String newApprovalType) {
        if (applicationProperties.getApproval().getLevel().isEnabled()) {
            approvalService.postBulkAssignApprovalType(functionGroupId, newApprovalType);
        }
    }
}
