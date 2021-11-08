package com.backbase.accesscontrol.audit.descriptionprovider.legalentity;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.END_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.MASTER_SERVICE_AGREEMENT_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.PARTICIPANT_SHARING_USERS_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_STATE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.START_DATE_TIME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils.getArgument;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantCreateItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantItemId;
import com.backbase.accesscontrol.client.rest.spec.model.ParticipantOf;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CreateLegalEntityAsParticipantDescriptor extends AbstractDescriptionProvider {

    private static final String EXTERNAL_ID_DESCRIPTION = ", external ID ";
    private static final String LEGAL_ENTITY_DESCRIPTION = " | Legal Entity | ";
    private static final String TYPE_DESCRIPTION = ", type ";
    private static final String ID_DESCRIPTION = " | id ";
    private static final String NAME_DESCRIPTION = " | name ";
    private static final String SERVICE_AGREEMENT_DESCRIPTION = " | Service Agreement | ";
    private static final String LEGAL_ENTITY_ID = "Legal Entity ID";
    private static final String LEGAL_ENTITY_EXTERNAL_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String LEGAL_ENTITY_PARENT_ID = "Parent Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";

    @Override
    public AuditEventAction getAuditEventAction() {
        return new AuditEventAction()
            .withObjectType(AuditObjectType.LEGAL_ENTITY_CREATE_AS_PARTICIPANT)
            .withEventAction(EventAction.CREATE);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem = getArgument(
            joinPoint, LegalEntityAsParticipantCreateItem.class);
        List<AuditMessage> auditMessages = new ArrayList<>();
        auditMessages.add(createAuditMessage(Status.INITIATED, legalEntityAsParticipantCreateItem, null, true));
        auditMessages.add(createAuditMessage(Status.INITIATED, legalEntityAsParticipantCreateItem, null, false));
        return auditMessages;
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem = getArgument(
            joinPoint, LegalEntityAsParticipantCreateItem.class);
        LegalEntityAsParticipantItemId legalEntityAsParticipantItemId = ((ResponseEntity<LegalEntityAsParticipantItemId>) actionResult)
            .getBody();
        List<AuditMessage> auditMessages = new ArrayList<>();
        auditMessages.add(createAuditMessage(Status.SUCCESSFUL, legalEntityAsParticipantCreateItem, legalEntityAsParticipantItemId, true));
        auditMessages.add(createAuditMessage(Status.SUCCESSFUL, legalEntityAsParticipantCreateItem, legalEntityAsParticipantItemId, false));
        return auditMessages;
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem = getArgument(
            joinPoint, LegalEntityAsParticipantCreateItem.class);
        return singletonList(createAuditMessage(Status.FAILED, legalEntityAsParticipantCreateItem, null, true)
            );
    }

    private String getDescription(
                    LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem,
                    Status status, boolean isCreateLegalEntity) {
        String description = null;
        if (isCreateLegalEntity) {
            description =  EventAction.CREATE.getActionEvent() + LEGAL_ENTITY_DESCRIPTION + status
            + NAME_DESCRIPTION + legalEntityAsParticipantCreateItem.getLegalEntityName()
            + TYPE_DESCRIPTION + legalEntityAsParticipantCreateItem.getLegalEntityType() + EXTERNAL_ID_DESCRIPTION
            + legalEntityAsParticipantCreateItem.getLegalEntityExternalId();
        } else {           
            description = createServiceAgreementDescription(legalEntityAsParticipantCreateItem, status, description);       
        }
        return description;
    }

    private String createServiceAgreementDescription(
                    LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem, Status status,
                    String description) {
        if (Objects.nonNull(legalEntityAsParticipantCreateItem.getParticipantOf().getNewCustomServiceAgreement())) {
            description = EventAction.CREATE.getActionEvent() + SERVICE_AGREEMENT_DESCRIPTION + status
                            + NAME_DESCRIPTION + legalEntityAsParticipantCreateItem.getParticipantOf().getNewCustomServiceAgreement()
                            .getServiceAgreementName();
        } else if (Objects.nonNull(legalEntityAsParticipantCreateItem.getParticipantOf().getNewMasterServiceAgreement())) {
            description = EventAction.CREATE.getActionEvent() + SERVICE_AGREEMENT_DESCRIPTION + status
                            + NAME_DESCRIPTION + legalEntityAsParticipantCreateItem.getParticipantOf().getNewMasterServiceAgreement()
                            .getServiceAgreementName();
        } else if (Objects.nonNull(legalEntityAsParticipantCreateItem.getParticipantOf().getExistingCustomServiceAgreement())) {
            description = EventAction.UPDATE.getActionEvent() + SERVICE_AGREEMENT_DESCRIPTION + status
                            + ID_DESCRIPTION + legalEntityAsParticipantCreateItem.getParticipantOf().getExistingCustomServiceAgreement()
                            .getServiceAgreementId();
        }
        return description;
    }
    
    private AuditMessage createAuditMessage(Status status, 
                    LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem, 
                    LegalEntityAsParticipantItemId legalEntityAsParticipantItemId, boolean isCreateLegalEntity) {
        
        AuditMessage auditMessage = new AuditMessage()
        .withStatus(status)
        .withEventDescription(getDescription(legalEntityAsParticipantCreateItem, status, isCreateLegalEntity));
        
        if (isCreateLegalEntity) {
            createLegalEntityAuditMessage(legalEntityAsParticipantCreateItem, auditMessage);
            
            if (status == Status.SUCCESSFUL) {
                auditMessage.withEventMetaDatum(LEGAL_ENTITY_ID, legalEntityAsParticipantItemId.getLegalEntityId());
            }
            
        } else {
            
            ParticipantOf participant = legalEntityAsParticipantCreateItem.getParticipantOf();
            
            if (Objects.nonNull(participant.getExistingCustomServiceAgreement())) {
                createExistingCustomServiceAgreementAuditMessage(auditMessage, participant);
            } else if (Objects.nonNull(participant.getNewCustomServiceAgreement())) {
                createNewCustomServiceAgreementAuditMessage(auditMessage, participant);              
            } else if (Objects.nonNull(participant.getNewMasterServiceAgreement())) {
                createNewMasterServiceAgreementAuditMessage(auditMessage, participant);    
            }
            
            if (status == Status.SUCCESSFUL && Objects.nonNull(legalEntityAsParticipantItemId.getServiceAgreementId())) {
                auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, legalEntityAsParticipantItemId.getServiceAgreementId());
            }
        }
        
        return auditMessage;
    }

    private void createLegalEntityAuditMessage(LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem,
                    AuditMessage auditMessage) {
        auditMessage.withEventMetaDatum(LEGAL_ENTITY_EXTERNAL_ID, legalEntityAsParticipantCreateItem.getLegalEntityExternalId())
        .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntityAsParticipantCreateItem.getLegalEntityName())
        .withEventMetaDatum(LEGAL_ENTITY_PARENT_ID, legalEntityAsParticipantCreateItem.getLegalEntityParentId())
        .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntityAsParticipantCreateItem.getLegalEntityType().toString());
    }

    private void createExistingCustomServiceAgreementAuditMessage(AuditMessage auditMessage, 
                    ParticipantOf participant) {
        auditMessage.withEventMetaDatum(MASTER_SERVICE_AGREEMENT_FIELD_NAME, String.valueOf(false));
        auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, 
                        String.valueOf(participant.getExistingCustomServiceAgreement().getParticipantInfo().getShareUsers()));
        auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME, 
                        String.valueOf(participant.getExistingCustomServiceAgreement().getParticipantInfo().getShareAccounts()));
        auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, 
                        String.valueOf(participant.getExistingCustomServiceAgreement().getServiceAgreementId()));
    }
    
    private void createNewCustomServiceAgreementAuditMessage(AuditMessage auditMessage, ParticipantOf participant) {
        auditMessage.withEventMetaDatum(MASTER_SERVICE_AGREEMENT_FIELD_NAME, String.valueOf(false));
        auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_NAME_FIELD_NAME, 
                        participant.getNewCustomServiceAgreement().getServiceAgreementName());
        auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, 
                        participant.getNewCustomServiceAgreement().getServiceAgreementDescription());
        auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, 
                        String.valueOf(participant.getNewCustomServiceAgreement().getParticipantInfo().getShareUsers()));
        auditMessage.withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME, 
                        String.valueOf(participant.getNewCustomServiceAgreement().getParticipantInfo().getShareAccounts()));
        auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_STATE_FIELD_NAME,
                        getStateElseUseDefault(participant.getNewCustomServiceAgreement().getServiceAgreementState()));
        auditMessage.withEventMetaDatum(START_DATE_TIME_FIELD_NAME, 
                        participant.getNewCustomServiceAgreement().getServiceAgreementValidFromDate() 
                        + " " + participant.getNewCustomServiceAgreement().getServiceAgreementValidFromTime());
        auditMessage.withEventMetaDatum(END_DATE_TIME_FIELD_NAME, 
                        participant.getNewCustomServiceAgreement().getServiceAgreementValidUntilDate() 
                        + " " + participant.getNewCustomServiceAgreement().getServiceAgreementValidUntilTime());
    }
    
    private void createNewMasterServiceAgreementAuditMessage(AuditMessage auditMessage, ParticipantOf participant) {
        auditMessage.withEventMetaDatum(MASTER_SERVICE_AGREEMENT_FIELD_NAME, String.valueOf(true));
        auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_NAME_FIELD_NAME, 
                        participant.getNewMasterServiceAgreement().getServiceAgreementName());
        auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, 
                        participant.getNewMasterServiceAgreement().getServiceAgreementDescription());
        auditMessage.withEventMetaDatum(SERVICE_AGREEMENT_STATE_FIELD_NAME,
                        getStateElseUseDefault(participant.getNewMasterServiceAgreement().getServiceAgreementState()));
        auditMessage.withEventMetaDatum(START_DATE_TIME_FIELD_NAME, 
                        participant.getNewMasterServiceAgreement().getServiceAgreementValidFromDate()
                        + " " + participant.getNewMasterServiceAgreement().getServiceAgreementValidFromTime());
        auditMessage.withEventMetaDatum(END_DATE_TIME_FIELD_NAME, 
                        participant.getNewMasterServiceAgreement().getServiceAgreementValidUntilDate()
                        + " " + participant.getNewMasterServiceAgreement().getServiceAgreementValidUntilTime());
    }
    
    private String getStateElseUseDefault(com.backbase.accesscontrol.client.rest.spec.model.Status status) {
        return String.valueOf(
                        status == null ? com.backbase.accesscontrol.client.rest.spec.model.Status.ENABLED : status);
    }
}
