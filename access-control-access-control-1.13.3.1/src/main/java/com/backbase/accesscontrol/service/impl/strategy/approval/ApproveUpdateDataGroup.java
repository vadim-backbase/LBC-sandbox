package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import java.util.ArrayList;
import org.springframework.stereotype.Service;


@Service
public class ApproveUpdateDataGroup extends ApprovalItem<ApprovalDataGroupDetails, DataGroupByIdPutRequestBody> {

    private DataGroupService dataGroupService;

    public ApproveUpdateDataGroup(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        DataGroupService dataGroupService, EventBus eventBus) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.dataGroupService = dataGroupService;
    }

    @Override
    protected DataGroupEvent approveItem(DataGroupByIdPutRequestBody dataGroupBase) {
        dataGroupService.update(dataGroupBase.getId(), dataGroupBase);
        return new DataGroupEvent().withId(dataGroupBase.getId()).withAction(Action.UPDATE);
    }

    @Override
    protected DataGroupByIdPutRequestBody getApprovedData(ApprovalDataGroupDetails body) {
        return new DataGroupByIdPutRequestBody()
            .withId(body.getDataGroupId())
            .withName(body.getName())
            .withDescription(body.getDescription())
            .withServiceAgreementId(body.getServiceAgreementId())
            .withType(body.getType())
            .withId(body.getDataGroupId())
            .withItems(new ArrayList<>(body.getItems()));
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.EDIT, ApprovalCategory.MANAGE_DATA_GROUPS);
    }

}
