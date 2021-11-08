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
public class ApproveCreateServiceAgreement extends ApprovalItem<ApprovalServiceAgreement, ServiceAgreement> {

    private ApprovalServiceAgreementUtil approvalServiceAgreementUtil;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    public ApproveCreateServiceAgreement(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository,
        ApprovalServiceAgreementUtil approvalServiceAgreementUtil,
        PersistenceServiceAgreementService persistenceServiceAgreementService, EventBus eventBus) {
        super(accessControlApprovalJpaRepository, eventBus);
        this.approvalServiceAgreementUtil = approvalServiceAgreementUtil;
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
    }

    /**
     * Executes the confirmation process.
     *
     * @param body Returned value from getApprovedData.
     */
    @Override
    protected ServiceAgreementEvent approveItem(ServiceAgreement body) {
        String id = persistenceServiceAgreementService.save(body).getId();
        return new ServiceAgreementEvent().withId(id).withAction(Action.ADD);
    }

    /**
     * Generates the key for the factory.
     *
     * @return The approval type.
     */
    @Override
    public ApprovalType getKey() {
        return new ApprovalType(ApprovalAction.CREATE, ApprovalCategory.MANAGE_SERVICE_AGREEMENT);
    }

    /**
     * Converts approve request data to structure required for executing the confirmation.
     *
     * @param approvalRequest - Approval request data
     * @return - Transformed data
     */
    @Override
    protected ServiceAgreement getApprovedData(ApprovalServiceAgreement approvalRequest) {
        return approvalServiceAgreementUtil.transformApprovalServiceAgreementToServiceAgreement(approvalRequest);
    }
}
