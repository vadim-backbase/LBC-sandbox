package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import org.springframework.stereotype.Service;

@Service
public class ApproveDeleteFunctionGroup extends ApprovalItem<ApprovalFunctionGroupRef, String> {

    private FunctionGroupService functionGroupService;

    public ApproveDeleteFunctionGroup(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        FunctionGroupService functionGroupService, EventBus eventBus) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.functionGroupService = functionGroupService;
    }

    @Override
    protected FunctionGroupEvent approveItem(String id) {
        functionGroupService.deleteFunctionGroup(id);
        return new FunctionGroupEvent().withId(id).withAction(Action.DELETE);
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.DELETE, ApprovalCategory.MANAGE_FUNCTION_GROUPS);
    }

    @Override
    protected String getApprovedData(ApprovalFunctionGroupRef approvalRequest) {
        return approvalRequest.getFunctionGroupId();
    }

}
