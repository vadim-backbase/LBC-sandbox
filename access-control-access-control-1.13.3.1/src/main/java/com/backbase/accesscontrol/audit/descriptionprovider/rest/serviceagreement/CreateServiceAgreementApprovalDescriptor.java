package com.backbase.accesscontrol.audit.descriptionprovider.rest.serviceagreement;

import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import java.util.List;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class CreateServiceAgreementApprovalDescriptor extends CreateServiceAgreementDescriptor {

    /**
     * Autowire constructor.
     */
    public CreateServiceAgreementApprovalDescriptor(DateTimeService dateTimeService) {
        super(dateTimeService);
    }

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_APPROVAL)
            .withEventAction(EventAction.CREATE_PENDING);
    }

    @Override
    protected String getDescription(String name, Status status) {
        return EventAction.CREATE_PENDING.getActionEvent() + " | Service Agreement | " + status
            + " | name " + name;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        ServiceAgreementPostRequestBody request = getServiceAgreementPostRequestBody(joinPoint);
        return request.getParticipants().stream()
            .map(participant -> createSuccessMessage(participant, request, Status.SUCCESSFUL))
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(DescriptorUtils.getArgument(joinPoint, String.class, 2));
    }
}