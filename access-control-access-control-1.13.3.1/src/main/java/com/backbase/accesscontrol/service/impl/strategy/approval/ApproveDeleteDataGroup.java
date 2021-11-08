package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import org.springframework.stereotype.Service;

@Service
public class ApproveDeleteDataGroup extends ApprovalItem<ApprovalDataGroup, String> {

    private DataGroupService dataGroupService;

    public ApproveDeleteDataGroup(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        DataGroupService dataGroupService, EventBus eventBus) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.dataGroupService = dataGroupService;
    }

    @Override
    protected DataGroupEvent approveItem(String id) {
        dataGroupService.delete(id);
        return new DataGroupEvent().withAction(Action.DELETE).withId(id);
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.DELETE, ApprovalCategory.MANAGE_DATA_GROUPS);
    }

    @Override
    protected String getApprovedData(ApprovalDataGroup approvalRequest) {
        return approvalRequest.getDataGroupId();
    }

}
