package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class ApproveCreateDataGroup extends ApprovalItem<ApprovalDataGroupDetails, DataGroupBase> {

    private DataGroupService dataGroupService;

    public ApproveCreateDataGroup(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        DataGroupService dataGroupService, EventBus eventBus) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.dataGroupService = dataGroupService;
    }

    @Override
    protected DataGroupEvent approveItem(DataGroupBase body) {
        return new DataGroupEvent().withId(dataGroupService.save(body)).withAction(Action.ADD);
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.CREATE, ApprovalCategory.MANAGE_DATA_GROUPS);
    }

    @Override
    protected DataGroupBase getApprovedData(ApprovalDataGroupDetails approvalDataGroup) {
        return new DataGroupBase()
            .withName(approvalDataGroup.getName())
            .withDescription(approvalDataGroup.getDescription())
            .withServiceAgreementId(approvalDataGroup.getServiceAgreementId())
            .withType(approvalDataGroup.getType())
            .withItems(new ArrayList<>(approvalDataGroup.getItems()));
    }
}
