package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_093;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalItem;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalStatus;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationDataGroupApprovalDetailsItem;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationPermissionsApprovalDetailsItem;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreeementApprovalDetailsItem;
import com.backbase.accesscontrol.dto.ApprovalsListDto;
import com.backbase.accesscontrol.dto.GetDataGroupApprovalDetailsParametersFlow;
import com.backbase.accesscontrol.dto.GetFunctionGroupApprovalDetailsParametersFlow;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.parameterholder.ApprovalsParametersHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.ApprovalFlowService;
import com.backbase.accesscontrol.service.facades.ApprovalsService;
import com.backbase.accesscontrol.util.ApplicationProperties;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for approvals.
 */
@RestController
@RequiredArgsConstructor
public class ApprovalsController implements com.backbase.accesscontrol.client.rest.spec.api.ApprovalsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalsController.class);
    private static final String APPROVAL_OFF_EXCEPTION = "Approval is off, throwing exception";
    private static final String GET_APPROVAL_DETAILS = "Getting approval details for approval with id {}";

    @NonNull
    private UserContextUtil userContextUtil;
    @NonNull
    private ApprovalsService approvalsService;
    @NonNull
    private ApprovalFlowService approvalFlowService;
    @NonNull
    private PermissionValidationService permissionValidationService;
    @NonNull
    private ApplicationProperties applicationProperties;
    @NonNull
    private PayloadConverter payloadConverter;


    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public ResponseEntity<List<PresentationApprovalItem>> getApprovals(Integer from, String cursor, Integer size) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        String userId = userContextDetails.getInternalUserId();
        String legalEntityId = userContextDetails.getLegalEntityId();
        ApprovalsParametersHolder parametersHolder = new ApprovalsParametersHolder()
            .withUserId(userId)
            .withLegalEntityId(legalEntityId)
            .withServiceAgreementId(serviceAgreementId)
            .withFrom(from)
            .withSize(size)
            .withCursor(cursor);

        LOGGER.info("Listing pending approvals for user {} under service agreement {}",
            userId, serviceAgreementId);

        permissionValidationService
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);
        ApprovalsListDto data = approvalsService.listPendingApprovals(parametersHolder);
        HttpHeaders headers = new HttpHeaders();
        if (!StringUtils.isBlank(data.getCursor())) {
            headers.add("X-Cursor", data.getCursor());
        }

        return new ResponseEntity<>(payloadConverter.convertListPayload(
            data.getPresentationApprovalItems(), PresentationApprovalItem.class), headers, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE + "'})")
    public ResponseEntity<PresentationDataGroupApprovalDetailsItem> getPresentationDataGroupApprovalDetailsItem(
        String approvalId) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {

            LOGGER.info(GET_APPROVAL_DETAILS, approvalId);

            String serviceAgreementId = userContextUtil.getServiceAgreementId();
            String loggedUserId = userContextUtil.getUserContextDetails().getInternalUserId();

            GetDataGroupApprovalDetailsParametersFlow parameters = new GetDataGroupApprovalDetailsParametersFlow()
                .withUserId(loggedUserId)
                .withServiceAgreementId(serviceAgreementId)
                .withApprovalId(approvalId);

            return new ResponseEntity<>(
                payloadConverter.convert(approvalFlowService.getDataGroupApprovalDetailsById(parameters),
                    PresentationDataGroupApprovalDetailsItem.class),
                HttpStatus.OK);
        } else {
            LOGGER.warn(APPROVAL_OFF_EXCEPTION);
            throw getForbiddenException(ERR_AG_093.getErrorMessage(), ERR_AG_093.getErrorCode());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE + "'})")
    public ResponseEntity<PresentationFunctionGroupApprovalDetailsItem> getPresentationFunctionGroupApprovalDetailsItem(
        String approvalId) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {

            LOGGER.info(GET_APPROVAL_DETAILS, approvalId);

            String serviceAgreementId = userContextUtil.getServiceAgreementId();
            String loggedUserId = userContextUtil.getUserContextDetails().getInternalUserId();

            GetFunctionGroupApprovalDetailsParametersFlow parameters =
                new GetFunctionGroupApprovalDetailsParametersFlow(approvalId, serviceAgreementId, loggedUserId);

            return new ResponseEntity<>(
                payloadConverter.convert(approvalFlowService.getFunctionGroupApprovalDetailsById(parameters),
                    PresentationFunctionGroupApprovalDetailsItem.class),
                HttpStatus.OK);
        } else {
            LOGGER.warn(APPROVAL_OFF_EXCEPTION);
            throw getForbiddenException(ERR_AG_093.getErrorMessage(), ERR_AG_093.getErrorCode());
        }
    }

    @Override
    @AuditEvent(eventAction = EventAction.REJECT, objectType = AuditObjectType.APPROVAL)
    public ResponseEntity<PresentationApprovalStatus> postRejectApprovalRequest(String approvalId) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            LOGGER.info("Rejecting approval with id {}", approvalId);
            String serviceAgreementId = userContextUtil.getServiceAgreementId();
            String loggedUserId = userContextUtil.getUserContextDetails().getInternalUserId();
            return new ResponseEntity<>(payloadConverter.convert(
                approvalsService.rejectApprovalRequest(approvalId, serviceAgreementId, loggedUserId),
                PresentationApprovalStatus.class), HttpStatus.OK);
        } else {
            LOGGER.warn(APPROVAL_OFF_EXCEPTION);
            throw getForbiddenException(ERR_AG_093.getErrorMessage(), ERR_AG_093.getErrorCode());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.APPROVE, objectType = AuditObjectType.APPROVAL)
    public ResponseEntity<PresentationApprovalStatus> postApproveApprovalRequest(String approvalId) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            String serviceAgreementId = userContextUtil.getServiceAgreementId();
            String userId = userContextUtil.getUserContextDetails().getInternalUserId();
            LOGGER.info("Accepting approval with id {}", approvalId);
            return new ResponseEntity<>(payloadConverter.convert(
                approvalsService
                    .acceptApprovalRequest(approvalId, serviceAgreementId, userId),
                PresentationApprovalStatus.class), HttpStatus.OK);
        } else {
            LOGGER.warn(APPROVAL_OFF_EXCEPTION);
            throw getForbiddenException(AccessGroupErrorCodes.ERR_AG_093.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_093.getErrorCode());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE + "'})")
    public ResponseEntity<PresentationPermissionsApprovalDetailsItem> getPresentationPermissionsApprovalDetailsItem(
        String approvalId) {
        if (applicationProperties.getApproval().getValidation().isEnabled()) {
            LOGGER.info(GET_APPROVAL_DETAILS, approvalId);
            String serviceAgreementId = userContextUtil.getServiceAgreementId();
            String loggedUserId = userContextUtil.getUserContextDetails().getInternalUserId();
            return new ResponseEntity<>(
                payloadConverter.convert(
                    approvalsService.getPermissionsApprovalDetailsById(approvalId, serviceAgreementId, loggedUserId),
                    PresentationPermissionsApprovalDetailsItem.class),
                HttpStatus.OK);
        } else {
            LOGGER.warn(APPROVAL_OFF_EXCEPTION);
            throw getForbiddenException(ERR_AG_093.getErrorMessage(), ERR_AG_093.getErrorCode());
        }
    }

    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE + "'})")
    public ResponseEntity<ServiceAgreeementApprovalDetailsItem> getServiceAgreementApprovalDetailsItem(
        String approvalId) {

        if (applicationProperties.getApproval().getValidation().isEnabled()) {

            LOGGER.info(GET_APPROVAL_DETAILS, approvalId);

            String serviceAgreementId = userContextUtil.getServiceAgreementId();
            String loggedUserId = userContextUtil.getUserContextDetails().getInternalUserId();

            return ResponseEntity.ok(
                payloadConverter.convert(
                    approvalFlowService.getServiceAgreementDetailsById(approvalId, serviceAgreementId, loggedUserId),
                    ServiceAgreeementApprovalDetailsItem.class));

        } else {
            LOGGER.warn(APPROVAL_OFF_EXCEPTION);
            throw getForbiddenException(ERR_AG_093.getErrorMessage(), ERR_AG_093.getErrorCode());
        }
    }

}
