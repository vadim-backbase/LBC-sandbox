package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.ApprovalService;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ApprovalsListDto;
import com.backbase.accesscontrol.dto.parameterholder.ApprovalsParametersHolder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsResponse;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer retrieving a List of Pending Approvals. This class is the business process component of the
 * access-group presentation service, communicating with the p&p service and retrieving all approvals by user and
 * service agreement.
 */
@Service
@AllArgsConstructor
public class ListPendingApprovals {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPendingApprovals.class);
    private static final String ASSIGN_PERMISSIONS_ACTION = "EDIT";

    private UserManagementService userManagementService;
    private ApprovalService approvalService;

    /**
     * Sends request to pandp service for retrieving pending approvals.
     *
     * @param internalRequest the internal request
     * @return Business Process Result of List{@link PresentationUserDataItemPermission}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_PENDING_APPROVALS)
    public InternalRequest<ApprovalsListDto> listApprovals(
        @Body InternalRequest<ApprovalsParametersHolder> internalRequest) {
        ApprovalsParametersHolder parametersHolder = internalRequest.getData();

        InternalRequest<ApprovalsListDto> response = getInternalRequest(
            new ApprovalsListDto(null, new ArrayList<>()),
            internalRequest.getInternalRequestContext());
        LOGGER.info(
            "Trying to list pending approvals for user with id {}, under service Agreement Id {}",
            parametersHolder.getUserId(), parametersHolder.getServiceAgreementId());

        PresentationPostFilterApprovalsRequest approvalRequest = getPresentationPostFilterApprovalsRequest(
            parametersHolder);

        LOGGER.info("Trying to get pending approvals from approval client for: {}", approvalRequest);
        PresentationPostFilterApprovalsResponse approvalsResponse = approvalService
            .postFilterApprovals(parametersHolder.getFrom(), parametersHolder.getCursor(), parametersHolder.getSize(),
                approvalRequest);

        if (!approvalsResponse.getApprovals().isEmpty()) {
            List<PresentationApprovalItem> pendingApprovalsFromPandp = getPendingApprovalsFromPandp(approvalsResponse);
            ApprovalsListDto approvalsListDto = new ApprovalsListDto(approvalsResponse.getCursor(),
                pendingApprovalsFromPandp);
            response.setData(approvalsListDto);
        }

        return response;
    }

    private List<PresentationApprovalItem> getPendingApprovalsFromPandp(
        PresentationPostFilterApprovalsResponse approvalsResponse) {

        List<GetUser> users = getUsersForApprovals(approvalsResponse);

        Map<String, String> userFullNameMap = users.stream()
            .collect(Collectors.toMap(GetUser::getId,
                GetUser::getFullName));

        return createApprovalResponse(approvalsResponse, userFullNameMap);
    }

    private List<GetUser> getUsersForApprovals(
        PresentationPostFilterApprovalsResponse approvalsResponse) {
        String userIds = approvalsResponse.getApprovals()
            .stream()
            .map(ApprovalDto::getUserId)
            .distinct()
            .collect(Collectors.joining(","));
        return userManagementService.getUsers(userIds).getUsers();
    }

    private List<PresentationApprovalItem> createApprovalResponse(
        PresentationPostFilterApprovalsResponse approvalsResponse, Map<String, String> userFullName) {
        return approvalsResponse.getApprovals().stream()
            .map(approvalEntry -> new PresentationApprovalItem()
                .withApprovalId(approvalEntry.getId())
                .withCreatedAt(approvalEntry.getCreatedAt())
                .withCategory(PresentationApprovalCategory.fromValue(approvalEntry.getFunction()))
                .withAction(PresentationApprovalAction.valueOf(
                    Objects.nonNull(approvalEntry.getAction()) ? approvalEntry.getAction() : ASSIGN_PERMISSIONS_ACTION))
                .withCreatorUserId(approvalEntry.getUserId())
                .withCreatorUserFullName(userFullName.get(approvalEntry.getUserId()))
            ).collect(Collectors.toList());
    }

    private PresentationPostFilterApprovalsRequest getPresentationPostFilterApprovalsRequest(
        ApprovalsParametersHolder parametersHolder) {
        return new PresentationPostFilterApprovalsRequest()
            .userId(parametersHolder.getUserId())
            .serviceAgreementId(parametersHolder.getServiceAgreementId())
            .canReject(true)
            .canApprove(true)
            .functions(Lists.newArrayList(
                PresentationApprovalCategory.ASSIGN_PERMISSIONS.toString(),
                PresentationApprovalCategory.MANAGE_DATA_GROUPS.toString(),
                PresentationApprovalCategory.MANAGE_FUNCTION_GROUPS.toString(),
                PresentationApprovalCategory.MANAGE_LIMITS.toString(),
                PresentationApprovalCategory.MANAGE_SHADOW_LIMITS.toString(),
                PresentationApprovalCategory.MANAGE_SERVICE_AGREEMENTS.toString(),
                PresentationApprovalCategory.UNLOCK_USER.toString()));
    }
}
