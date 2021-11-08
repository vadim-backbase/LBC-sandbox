package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.ApprovalServiceAgreementUtil;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import org.springframework.stereotype.Service;

@Service
public class ApproveUpdateServiceAgreement extends ApprovalItem<ApprovalServiceAgreement, ServiceAgreement> {

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ApprovalServiceAgreementUtil approvalServiceAgreementUtil;

    public ApproveUpdateServiceAgreement(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        PersistenceServiceAgreementService persistenceServiceAgreementService,
        ApprovalServiceAgreementUtil approvalServiceAgreementUtil, EventBus eventBus) {
        super(accessControlApprovalJpaRepository,eventBus);
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
        this.approvalServiceAgreementUtil = approvalServiceAgreementUtil;
    }

    @Override
    protected ServiceAgreementEvent approveItem(ServiceAgreement body) {
        persistenceServiceAgreementService.update(body);
        return new ServiceAgreementEvent().withId(body.getId()).withAction(Action.UPDATE);
    }

    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.EDIT, ApprovalCategory.MANAGE_SERVICE_AGREEMENT);
    }

    @Override
    protected ServiceAgreement getApprovedData(ApprovalServiceAgreement approvalRequest) {
        return approvalServiceAgreementUtil.transformApprovalServiceAgreementToServiceAgreement(approvalRequest);
    }

}
