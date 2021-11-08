package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class EditServiceAgreementApprovalDescriptor extends EditServiceAgreementDescriptor {

    /**
     * Autowire constructor.
     */
    public EditServiceAgreementApprovalDescriptor(DateTimeService dateTimeService) {
        super(dateTimeService);
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_APPROVAL)
            .withEventAction(EventAction.UPDATE_PENDING);
    }

    @Override
    protected String getDescription(String name, Status status) {
        return EventAction.UPDATE_PENDING.getActionEvent() + " | Service Agreement | " + status
            + " | name " + name;
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(DescriptorUtils.getArgument(joinPoint, String.class, 1));
    }
}