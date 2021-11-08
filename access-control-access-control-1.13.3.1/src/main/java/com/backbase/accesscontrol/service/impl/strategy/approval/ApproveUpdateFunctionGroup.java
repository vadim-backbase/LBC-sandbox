package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupUpdate;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import org.springframework.stereotype.Service;

@Service
public class ApproveUpdateFunctionGroup extends ApprovalItem<ApprovalFunctionGroup, FunctionGroupUpdate> {

    private FunctionGroupService functionGroupService;
    private ApprovalPermissionUtil approvalPermissionUtil;

    public ApproveUpdateFunctionGroup(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        FunctionGroupService functionGroupService,
        ApprovalPermissionUtil approvalPermissionUtil, EventBus eventBus) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.functionGroupService = functionGroupService;
        this.approvalPermissionUtil = approvalPermissionUtil;
    }

    @Override
    protected FunctionGroupUpdate getApprovedData(ApprovalFunctionGroup approvalFunctionGroup) {
        FunctionGroupBase functionGroupBase = approvalPermissionUtil
            .convertAndReturnFunctionGroupBase(approvalFunctionGroup);

        return new FunctionGroupUpdate(approvalFunctionGroup.getFunctionGroupId(), functionGroupBase);
    }

    @Override
    protected FunctionGroupEvent approveItem(FunctionGroupUpdate body) {
        functionGroupService.updateFunctionGroup(body.getFunctionGroupId(), body.getFunctionGroupBase());
        return new FunctionGroupEvent().withId(body.getFunctionGroupId()).withAction(Action.UPDATE);
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.EDIT, ApprovalCategory.MANAGE_FUNCTION_GROUPS);
    }
}
