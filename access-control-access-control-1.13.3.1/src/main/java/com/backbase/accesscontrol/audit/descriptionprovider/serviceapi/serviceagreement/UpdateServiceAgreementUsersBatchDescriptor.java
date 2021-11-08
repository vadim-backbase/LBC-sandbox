package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_CODE;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.ERROR_MESSAGE;
import static com.google.common.collect.Lists.reverse;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceAgreementUsersBatchDescriptor extends UpdateUsersBase {

    private static final String ENTITY_UPDATED = "User";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.SERVICE_AGREEMENT_USERS_UPDATE_BATCH)
            .withEventAction(EventAction.UPDATE_USERS);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        return getInitEventData(joinPoint, ENTITY_UPDATED);
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        List<AuditMessage> auditMessages = new ArrayList<>();
        List<BatchResponseItemExtended> responses = ((ResponseEntity<List<BatchResponseItemExtended>>) actionResult)
            .getBody();

        for (int i = responses.size() - 1; i >= 0; i--) {
            if (!responses.get(i).getStatus().equals(StatusEnum.HTTP_STATUS_OK)) {
                AuditMessage failedEventDatumRemoveAdminsFromServiceAgreement =
                    createBaseEventAddRemoveUsersServiceAgreement(ENTITY_UPDATED,
                        responses.get(i), Status.FAILED);
                failedEventDatumRemoveAdminsFromServiceAgreement
                    .withEventMetaDatum(ERROR_CODE, responses.get(i).getStatus().toString())
                    .withEventMetaDatum(ERROR_MESSAGE,
                        responses.get(i).getErrors().isEmpty() ? "" : responses.get(i).getErrors().get(0));
                auditMessages.add(failedEventDatumRemoveAdminsFromServiceAgreement);
            } else {
                AuditMessage successEventDatumRemoveAdminsFromServiceAgreement =
                    createBaseEventAddRemoveUsersServiceAgreement(ENTITY_UPDATED,
                        responses.get(i), Status.SUCCESSFUL);
                auditMessages.add(successEventDatumRemoveAdminsFromServiceAgreement);
            }
        }
        return reverse(auditMessages);
    }
}
