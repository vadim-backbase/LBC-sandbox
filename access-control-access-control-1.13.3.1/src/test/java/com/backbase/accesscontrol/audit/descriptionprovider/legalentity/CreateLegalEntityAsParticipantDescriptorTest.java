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
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.client.rest.spec.model.ExistingCustomServiceAgreement;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantCreateItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantItemId;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityType;
import com.backbase.accesscontrol.client.rest.spec.model.NewCustomServiceAgreement;
import com.backbase.accesscontrol.client.rest.spec.model.NewMasterServiceAgreement;
import com.backbase.accesscontrol.client.rest.spec.model.ParticipantInfo;
import com.backbase.accesscontrol.client.rest.spec.model.ParticipantOf;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityAsParticipantDescriptorTest {

    private static final String NEW_MASTER_SERVICE_AGREEMENT = "newMasterServiceAgreement";
    private static final String NEW_CUSTOM_SERVICE_AGREEMENT = "newCustomServiceAgreement";
    private static final String EXISTING_CUSTOM_SERVICE_AGREEMENT = "existingCustomServiceAgreement";
    private static final String SERVICE_AGREEMENT_VALID_UNTIL_DATE = "2025-12-16";
    private static final String SERVICE_AGREEMENT_TIME = "14:34:00";
    private static final String SERVICE_AGREEMENT_VALID_FROM_DATE = "2020-12-16";
    private static final String LE_ID = "ID";
    private static final String MSA_DESCRIPTION = "MSA Description";
    private static final String MSA_NAME = "MSA name";
    private static final String SA_EXTERNAL_ID = "SA External ID";
    private static final String SA_DESCRIPTION = "SA Description";
    private static final String SA_NAME = "SA name";
    private static final String LE_EXTERNAL_ID = "ExternalId";
    private static final String LE_NAME = "LeName";
    private static final String LE_PARENT_ID = "5643e686d3ae4216b3ff5d66a6ad897d";
    private static final String SA_ID = "SA ID";
    private static final String LEGAL_ENTITY_ID = "Legal Entity ID";
    private static final String LEGAL_ENTITY_EXTERNAL_ID = "External Legal Entity ID";
    private static final String LEGAL_ENTITY_NAME = "Legal Entity Name";
    private static final String LEGAL_ENTITY_PARENT_ID = "Parent Legal Entity ID";
    private static final String LEGAL_ENTITY_TYPE = "Legal Entity Type";
    private static final List<String> serviceAgreementTypes = Stream.of(EXISTING_CUSTOM_SERVICE_AGREEMENT, 
                    NEW_CUSTOM_SERVICE_AGREEMENT, NEW_MASTER_SERVICE_AGREEMENT)
                    .collect(Collectors.toList());

    @InjectMocks
    private CreateLegalEntityAsParticipantDescriptor createLegalEntityAsParticipantDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;
    
    @Test
    public void getInitEventDataList() {    
        for(String serviceAgreementType : serviceAgreementTypes) {
            LegalEntityAsParticipantCreateItem legalEntitiesPostRequestBody = createLegalEntityRequest(serviceAgreementType);

            when(joinPoint.getArgs())
                .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());
            
            List<AuditMessage> expectedEventList = createExpectedEventList(Status.INITIATED, legalEntitiesPostRequestBody, 
                            null);
            List<AuditMessage> actualEventList = createLegalEntityAsParticipantDescriptor
                .getInitEventDataList(joinPoint);

            assertEquals(expectedEventList.size(), actualEventList.size());
            assertEquals(expectedEventList.get(0), actualEventList.get(0));
            assertEquals(expectedEventList.get(1), actualEventList.get(1));
        }
    }

    @Test
    public void getSuccessEventDataList() {    
        for(String serviceAgreementType : serviceAgreementTypes) {
            
            LegalEntityAsParticipantCreateItem legalEntitiesPostRequestBody = createLegalEntityRequest(serviceAgreementType);

            LegalEntityAsParticipantItemId legalEntitiesPostResponseBody = createLegalEntityResponse();

            when(joinPoint.getArgs())
                .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());

            List<AuditMessage> expectedEventList = createExpectedEventList(Status.SUCCESSFUL, legalEntitiesPostRequestBody,
                        legalEntitiesPostResponseBody);

            List<AuditMessage> actualEventList = createLegalEntityAsParticipantDescriptor
                .getSuccessEventDataList(joinPoint, getResponseEntity(legalEntitiesPostResponseBody, HttpStatus.OK));

            assertEquals(expectedEventList.size(), actualEventList.size());
            assertEquals(expectedEventList.get(0), actualEventList.get(0));
            assertEquals(expectedEventList.get(1), actualEventList.get(1));
        }
    }

    @Test
    public void getFailedEventDataList() {
        for(String serviceAgreementType : serviceAgreementTypes) {
            LegalEntityAsParticipantCreateItem legalEntitiesPostRequestBody = createLegalEntityRequest(serviceAgreementType);

            when(joinPoint.getArgs())
                .thenReturn(singletonList(legalEntitiesPostRequestBody).toArray());

            List<AuditMessage> expectedEventList = createExpectedEventList(Status.FAILED, legalEntitiesPostRequestBody, null);

            List<AuditMessage> actualEventList = createLegalEntityAsParticipantDescriptor
                .getFailedEventDataList(joinPoint);

            assertEquals(expectedEventList.size(), actualEventList.size());
            assertEquals(expectedEventList.get(0), actualEventList.get(0));
        }
    }

    private String getDescription(Status status, String legalEntityName, LegalEntityType legalEntityType,
        String externalId) {
        return "Create | Legal Entity | " + status + " | name " + legalEntityName + ", type " + legalEntityType
            + ", external ID " + externalId;
    }
    
    private LegalEntityAsParticipantCreateItem createLegalEntityRequest(String participantType) {
        ParticipantInfo participantInfo = createParticipantInfo();
        ParticipantOf participantOf = null;
          
        switch(participantType) {
            case EXISTING_CUSTOM_SERVICE_AGREEMENT : {
                ExistingCustomServiceAgreement existingCustomServiceAgreement = createExistingCustomServiceAgreement(participantInfo);
                participantOf = createParticipantOf(existingCustomServiceAgreement, null, null);
                break;
            }
            case NEW_CUSTOM_SERVICE_AGREEMENT : {
                NewCustomServiceAgreement newCustomServiceAgreement = createNewCustomerServiceAgreement(participantInfo);
                participantOf = createParticipantOf(null, newCustomServiceAgreement, null);
                break;
            }
            case NEW_MASTER_SERVICE_AGREEMENT : {
                NewMasterServiceAgreement newMasterServiceAgreement = createNewMasterServiceAgreement();
                participantOf = createParticipantOf(null, null, newMasterServiceAgreement);
                break;
            }
            default : {
                break;
            }
        }
            
        LegalEntityAsParticipantCreateItem legalEntitiesPostRequestBody =
                        createLegalEntityAsParticipantCreateItem(participantOf);
        return legalEntitiesPostRequestBody;
    }

    private LegalEntityAsParticipantCreateItem createLegalEntityAsParticipantCreateItem(ParticipantOf participantOf) {
        LegalEntityAsParticipantCreateItem legalEntitiesPostRequestBody = new LegalEntityAsParticipantCreateItem();
        legalEntitiesPostRequestBody.setLegalEntityParentId(LE_PARENT_ID);
        legalEntitiesPostRequestBody.setLegalEntityName(LE_NAME);
        legalEntitiesPostRequestBody.setLegalEntityType(LegalEntityType.BANK);
        legalEntitiesPostRequestBody.setLegalEntityExternalId(LE_EXTERNAL_ID);
        legalEntitiesPostRequestBody.setParticipantOf(participantOf);
        return legalEntitiesPostRequestBody;
    }

    private ParticipantOf createParticipantOf(ExistingCustomServiceAgreement existingCustomServiceAgreement, 
                    NewCustomServiceAgreement newCustomServiceAgreement, NewMasterServiceAgreement newMasterServiceAgreement) {
        ParticipantOf participantOf = new ParticipantOf();
        
        if (Objects.nonNull(existingCustomServiceAgreement)) {
            participantOf.setExistingCustomServiceAgreement(existingCustomServiceAgreement);
        } else if (Objects.nonNull(newCustomServiceAgreement)) {
            participantOf.setNewCustomServiceAgreement(newCustomServiceAgreement);
        } else if (Objects.nonNull(newMasterServiceAgreement)) {
            participantOf.setNewMasterServiceAgreement(newMasterServiceAgreement);
        }
        
        return participantOf;
    }
    
    private ExistingCustomServiceAgreement createExistingCustomServiceAgreement(ParticipantInfo participantInfo) {
        ExistingCustomServiceAgreement existingCustomServiceAgreement = new ExistingCustomServiceAgreement();
        existingCustomServiceAgreement.setParticipantInfo(participantInfo);
        existingCustomServiceAgreement.setServiceAgreementId(SA_ID);
        return existingCustomServiceAgreement;
    }

    private NewCustomServiceAgreement createNewCustomerServiceAgreement(ParticipantInfo participantInfo) {
        NewCustomServiceAgreement newCustomServiceAgreement = new NewCustomServiceAgreement();
        newCustomServiceAgreement.setServiceAgreementName(SA_NAME);
        newCustomServiceAgreement.setServiceAgreementDescription(SA_DESCRIPTION);
        newCustomServiceAgreement.setServiceAgreementExternalId(SA_EXTERNAL_ID);
        newCustomServiceAgreement.setServiceAgreementValidFromDate(SERVICE_AGREEMENT_VALID_FROM_DATE);
        newCustomServiceAgreement.setServiceAgreementValidFromTime(SERVICE_AGREEMENT_TIME);
        newCustomServiceAgreement.setServiceAgreementValidUntilDate(SERVICE_AGREEMENT_VALID_UNTIL_DATE);
        newCustomServiceAgreement.setServiceAgreementValidUntilTime(SERVICE_AGREEMENT_TIME);
        newCustomServiceAgreement.setParticipantInfo(participantInfo);
        return newCustomServiceAgreement;
    }
    
    private NewMasterServiceAgreement createNewMasterServiceAgreement() {
        NewMasterServiceAgreement newMasterServiceAgreement = new NewMasterServiceAgreement();
        newMasterServiceAgreement.setServiceAgreementName(MSA_NAME);
        newMasterServiceAgreement.setServiceAgreementDescription(MSA_DESCRIPTION);
        newMasterServiceAgreement.setServiceAgreementExternalId(SA_EXTERNAL_ID);
        newMasterServiceAgreement.setServiceAgreementState(com.backbase.accesscontrol.client.rest.spec.model.Status.ENABLED);
        newMasterServiceAgreement.setServiceAgreementValidFromDate(SERVICE_AGREEMENT_VALID_FROM_DATE);
        newMasterServiceAgreement.setServiceAgreementValidFromTime(SERVICE_AGREEMENT_TIME);
        newMasterServiceAgreement.setServiceAgreementValidUntilDate(SERVICE_AGREEMENT_VALID_UNTIL_DATE);
        newMasterServiceAgreement.setServiceAgreementValidUntilTime(SERVICE_AGREEMENT_TIME);
        return newMasterServiceAgreement;
    }

    private ParticipantInfo createParticipantInfo() {
        ParticipantInfo participantInfo = new ParticipantInfo();
        participantInfo.setShareAccounts(true);
        participantInfo.setShareUsers(true);
        return participantInfo;
    }
    
    private LegalEntityAsParticipantItemId createLegalEntityResponse() {
        LegalEntityAsParticipantItemId legalEntitiesPostResponseBody = new LegalEntityAsParticipantItemId();
        legalEntitiesPostResponseBody.setLegalEntityId(LE_ID);
        legalEntitiesPostResponseBody.setServiceAgreementId(SA_ID);
        return legalEntitiesPostResponseBody;
    }
    
    private List<AuditMessage> createExpectedEventList(Status status, LegalEntityAsParticipantCreateItem legalEntitiesPostRequestBody, 
                    LegalEntityAsParticipantItemId legalEntitiesPostResponseBody) {
        List<AuditMessage> expectedEventList = new ArrayList<>();
        AuditMessage auditMessageLE = createAuditMessageLegalEntity(status, legalEntitiesPostRequestBody,
                        legalEntitiesPostResponseBody, expectedEventList);
        expectedEventList.add(auditMessageLE);
        if (status != Status.FAILED) {
            AuditMessage auditMessageSA = createAuditMessageServiceAgreement(status, legalEntitiesPostRequestBody,
                            legalEntitiesPostResponseBody, expectedEventList);
            expectedEventList.add(auditMessageSA);
        }
        return expectedEventList;
    }

    private AuditMessage createAuditMessageLegalEntity(Status status,
                    LegalEntityAsParticipantCreateItem legalEntitiesPostRequestBody,
                    LegalEntityAsParticipantItemId legalEntitiesPostResponseBody,
                    List<AuditMessage> expectedEventList) {
        AuditMessage auditMessageLE = new AuditMessage()
            .withStatus(status)
            .withEventDescription(getDescription(status, legalEntitiesPostRequestBody.getLegalEntityName(),
                legalEntitiesPostRequestBody.getLegalEntityType(), legalEntitiesPostRequestBody.getLegalEntityExternalId()))
            .withEventMetaDatum(LEGAL_ENTITY_EXTERNAL_ID, legalEntitiesPostRequestBody.getLegalEntityExternalId())
            .withEventMetaDatum(LEGAL_ENTITY_NAME, legalEntitiesPostRequestBody.getLegalEntityName())
            .withEventMetaDatum(LEGAL_ENTITY_PARENT_ID, legalEntitiesPostRequestBody.getLegalEntityParentId())
            .withEventMetaDatum(LEGAL_ENTITY_TYPE, legalEntitiesPostRequestBody.getLegalEntityType().toString());
        if (status == Status.SUCCESSFUL) {
            auditMessageLE.withEventMetaDatum(LEGAL_ENTITY_ID, legalEntitiesPostResponseBody.getLegalEntityId());
        }
        return auditMessageLE;
    }
    
    private AuditMessage createAuditMessageServiceAgreement(Status status,
                    LegalEntityAsParticipantCreateItem legalEntitiesAsParticipantPostRequestBody,
                    LegalEntityAsParticipantItemId legalEntitiesAsParticipantPostResponseBody,
                    List<AuditMessage> expectedEventList) {
        ParticipantOf participant = legalEntitiesAsParticipantPostRequestBody.getParticipantOf();
        AuditMessage auditMessageSA = new AuditMessage()
            .withStatus(status)
            .withEventDescription(createServiceAgreementDescription(legalEntitiesAsParticipantPostRequestBody,status));
        if (Objects.nonNull(participant.getExistingCustomServiceAgreement())) {
            auditMessageSA
            .withEventMetaDatum(MASTER_SERVICE_AGREEMENT_FIELD_NAME, "false")
            .withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, 
                            String.valueOf(participant.getExistingCustomServiceAgreement().getParticipantInfo().getShareUsers()))
            .withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME, 
                            String.valueOf(participant.getExistingCustomServiceAgreement().getParticipantInfo().getShareAccounts()))
            .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, participant.getExistingCustomServiceAgreement().getServiceAgreementId());
        } else if (Objects.nonNull(participant.getNewCustomServiceAgreement())) {
            auditMessageSA
            .withEventMetaDatum(MASTER_SERVICE_AGREEMENT_FIELD_NAME, "false")
            .withEventMetaDatum(SERVICE_AGREEMENT_NAME_FIELD_NAME, 
                            participant.getNewCustomServiceAgreement().getServiceAgreementName())
            .withEventMetaDatum(SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, 
                            participant.getNewCustomServiceAgreement().getServiceAgreementDescription())
            .withEventMetaDatum(PARTICIPANT_SHARING_USERS_FIELD_NAME, 
                            String.valueOf(participant.getNewCustomServiceAgreement().getParticipantInfo().getShareUsers()))
            .withEventMetaDatum(PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME, 
                            String.valueOf(participant.getNewCustomServiceAgreement().getParticipantInfo().getShareAccounts()))
            .withEventMetaDatum(SERVICE_AGREEMENT_STATE_FIELD_NAME, 
                            participant.getNewCustomServiceAgreement().getServiceAgreementState().toString())
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, 
                            SERVICE_AGREEMENT_VALID_FROM_DATE + " " + SERVICE_AGREEMENT_TIME)
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, 
                            SERVICE_AGREEMENT_VALID_UNTIL_DATE + " " + SERVICE_AGREEMENT_TIME);
            if (status == Status.SUCCESSFUL) {
                auditMessageSA
                .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, legalEntitiesAsParticipantPostResponseBody.getServiceAgreementId());
            }
        } else if (Objects.nonNull(participant.getNewMasterServiceAgreement())) {
            auditMessageSA
            .withEventMetaDatum(MASTER_SERVICE_AGREEMENT_FIELD_NAME, "true")
            .withEventMetaDatum(SERVICE_AGREEMENT_NAME_FIELD_NAME, 
                            participant.getNewMasterServiceAgreement().getServiceAgreementName())
            .withEventMetaDatum(SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME, 
                            participant.getNewMasterServiceAgreement().getServiceAgreementDescription())
            .withEventMetaDatum(SERVICE_AGREEMENT_STATE_FIELD_NAME, 
                            participant.getNewMasterServiceAgreement().getServiceAgreementState().toString())
            .withEventMetaDatum(START_DATE_TIME_FIELD_NAME, 
                            SERVICE_AGREEMENT_VALID_FROM_DATE + " " + SERVICE_AGREEMENT_TIME)
            .withEventMetaDatum(END_DATE_TIME_FIELD_NAME, 
                            SERVICE_AGREEMENT_VALID_UNTIL_DATE + " " + SERVICE_AGREEMENT_TIME);
            if (status == Status.SUCCESSFUL) {
                auditMessageSA
                .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, legalEntitiesAsParticipantPostResponseBody.getServiceAgreementId());
            }
        }
            
        
        return auditMessageSA;
    }
    
    private String createServiceAgreementDescription(
                    LegalEntityAsParticipantCreateItem legalEntityAsParticipantCreateItem, Status status) {
        String description = null;
        if (Objects.nonNull(legalEntityAsParticipantCreateItem.getParticipantOf().getNewCustomServiceAgreement())) {
            description = EventAction.CREATE.getActionEvent() + " | Service Agreement | " + status
                            + " | name " + legalEntityAsParticipantCreateItem.getParticipantOf().getNewCustomServiceAgreement()
                            .getServiceAgreementName();
        } else if (Objects.nonNull(legalEntityAsParticipantCreateItem.getParticipantOf().getNewMasterServiceAgreement())) {
            description = EventAction.CREATE.getActionEvent() + " | Service Agreement | " + status
                            + " | name " + legalEntityAsParticipantCreateItem.getParticipantOf().getNewMasterServiceAgreement()
                            .getServiceAgreementName();
        } else if (Objects.nonNull(legalEntityAsParticipantCreateItem.getParticipantOf().getExistingCustomServiceAgreement())) {
            description = EventAction.UPDATE.getActionEvent() + " | Service Agreement | " + status
                            + " | id " + legalEntityAsParticipantCreateItem.getParticipantOf().getExistingCustomServiceAgreement()
                            .getServiceAgreementId();
        }
        return description;
    }
}
