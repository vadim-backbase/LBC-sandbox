package com.backbase.accesscontrol.service.impl.strategy.approval;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;

public abstract class ApprovalItem<T extends AccessControlApproval, D> {

    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    protected EventBus eventBus;

    public ApprovalItem(
        AccessControlApprovalJpaRepository accessControlApprovalJpaRepository, EventBus eventBus) {
        this.accessControlApprovalJpaRepository = accessControlApprovalJpaRepository;
        this.eventBus = eventBus;
    }

    /**
     * Executes the confirmation process.
     *
     * @param body Returned value from getApprovedData.
     */
    protected abstract Event approveItem(D body);

    /**
     * Generates the key for the factory.
     *
     * @return The approval type.
     */
    public abstract ApprovalType getKey();

    /**
     * Converts approve request data to structure required for executing the confirmation.
     *
     * @param approvalRequest - Approval request data
     * @return - Transformed data
     */
    protected abstract D getApprovedData(T approvalRequest);

    /**
     * Executes the confirmation od approval.
     *
     * @param approvalRequest - Approval request data.
     */
    public final void execute(T approvalRequest) {

        D approvedData = getApprovedData(approvalRequest);
        accessControlApprovalJpaRepository.delete(approvalRequest);
        processEvent(approveItem(approvedData));
    }

    private void processEvent(Event event) {
        if (event == null) {
            return;
        }
        EnvelopedEvent<Event> envelopedEvent = new EnvelopedEvent<>();
        envelopedEvent.setEvent(event);
        envelopedEvent.setOriginatorContext(null);

        eventBus.emitEvent(envelopedEvent);
    }
}
