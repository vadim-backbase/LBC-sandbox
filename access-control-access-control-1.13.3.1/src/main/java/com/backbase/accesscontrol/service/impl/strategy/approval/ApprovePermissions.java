package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.mappers.ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.PermissionService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ApprovePermissions extends ApprovalItem<ApprovalUserContext, AssignPermissionsData> {

    private PermissionService permissionService;
    private ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper userContextPermissionsMapper;

    public ApprovePermissions(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        PermissionService permissionService, EventBus eventBus,
        ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper userContextPermissionsMapper) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.permissionService = permissionService;
        this.userContextPermissionsMapper = userContextPermissionsMapper;
    }

    @Override
    protected AssignPermissionsData getApprovedData(ApprovalUserContext approvalUserContext) {
        Set<UserContextPermissions> userContextPermissions = approvalUserContext
            .getApprovalUserContextAssignFunctionGroups().stream()
            .map(userContextPermissionsMapper::map)
            .collect(Collectors.toSet());

        return new AssignPermissionsData(
            approvalUserContext.getServiceAgreementId(),
            approvalUserContext.getUserId(),
            approvalUserContext.getLegalEntityId(),
            userContextPermissions
        );
    }

    @Override
    protected UserContextEvent approveItem(AssignPermissionsData data) {
        permissionService
            .assignUserContextPermissions(data.getServiceAgreementId(),
                data.getUserId(),
                data.getLegalEntityId(),
                data.getPermissions());

        return new UserContextEvent()
                .withUserId(data.getUserId())
                .withServiceAgreementId(data.getServiceAgreementId());
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.EDIT, ApprovalCategory.ASSIGN_PERMISSIONS);
    }
}
