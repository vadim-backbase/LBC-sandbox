package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import org.springframework.stereotype.Service;

@Service
public class ApproveCreateFunctionGroup extends ApprovalItem<ApprovalFunctionGroup, FunctionGroupBase> {

    private FunctionGroupService functionGroupService;
    private ApprovalPermissionUtil approvalPermissionUtil;

    public ApproveCreateFunctionGroup(AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        FunctionGroupService functionGroupService,
        ApprovalPermissionUtil approvalPermissionUtil,EventBus eventBus) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.functionGroupService = functionGroupService;
        this.approvalPermissionUtil = approvalPermissionUtil;
    }

    @Override
    protected FunctionGroupEvent approveItem(FunctionGroupBase body) {
        return new FunctionGroupEvent().withId(functionGroupService.addFunctionGroup(body)).withAction(Action.ADD);
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.CREATE, ApprovalCategory.MANAGE_FUNCTION_GROUPS);
    }

    @Override
    protected FunctionGroupBase getApprovedData(ApprovalFunctionGroup approvalRequest) {

        return approvalPermissionUtil
            .convertAndReturnFunctionGroupBase(approvalRequest);
    }
}
