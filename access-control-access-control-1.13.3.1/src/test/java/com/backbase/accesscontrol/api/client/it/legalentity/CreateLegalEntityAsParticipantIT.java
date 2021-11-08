package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_FUNCTION_NAME;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.LegalEntityController;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantCreateItem;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ExistingCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewCustomServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewMasterServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantInfo;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantOf;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link LegalEntityController#postLegalEntitiesAsParticipant(LegalEntityAsParticipantCreateItem)}
 *
 */
public class CreateLegalEntityAsParticipantIT extends TestDbWireMock {
    
    private static final String MULTIPLE_SA_PROVIDED_ERROR_MESSAGE =
                    "Must provide exactly one of existingCustomServiceAgreement, newCustomServiceAgreement or newMasterServiceAgreement";
    private static final String CREATE_LEGAL_ENTITY_AS_PARTICIPANT_URL = "/legalentities/as-participant";
    private static final String USER_EXT_ID_ADMIN = "admin";
    
    @Test
    public void shouldCreateLegalEntityAsParticipantOfNewMasterServiceAgreement() throws Exception {
        
        String parentLegalEntityId = rootLegalEntity.getId();
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
                        .withLegalEntityName("name")
                        .withLegalEntityExternalId("external")
                        .withLegalEntityParentId(parentLegalEntityId)
                        .withLegalEntityType(LegalEntityType.BANK)
                        .withParticipantOf(new ParticipantOf()
                                        .withNewMasterServiceAgreement(new NewMasterServiceAgreement()
                                                        .withServiceAgreementName("name")
                                                        .withServiceAgreementDescription("desc")
                                                        .withServiceAgreementExternalId("external")
                                                        .withServiceAgreementState(Status.ENABLED)));
        
        Map<String, String> functionPrivileges = Map.of(MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_CREATE,
                        SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_CREATE);
        String contentAsString = executeClientRequest(CREATE_LEGAL_ENTITY_AS_PARTICIPANT_URL, HttpMethod.POST,
                        requestBody, USER_EXT_ID_ADMIN, functionPrivileges);
        
        LegalEntityAsParticipantPostResponseBody responseBody =
                        readValue(contentAsString, LegalEntityAsParticipantPostResponseBody.class);
        assertNotNull(responseBody);
        
        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
                        .withAction(ADD)
                        .withId(responseBody.getLegalEntityId())));
        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
                        .withAction(ADD)
                        .withId(responseBody.getServiceAgreementId())));
    }

    @Test
    public void shouldSuccessfullyValidateNewMasterServiceAgreementFields() throws Exception {
        String parentLegalEntityId = rootLegalEntity.getId();
        NewMasterServiceAgreement newMasterServiceAgreement = new NewMasterServiceAgreement()
                .withServiceAgreementName("name")
                .withServiceAgreementDescription("desc")
                .withServiceAgreementExternalId("external")
                .withServiceAgreementState(Status.ENABLED);
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
                .withLegalEntityName("name")
                .withLegalEntityExternalId("external")
                .withLegalEntityParentId(parentLegalEntityId)
                .withLegalEntityType(LegalEntityType.BANK)
                .withParticipantOf(new ParticipantOf()
                        .withNewMasterServiceAgreement(newMasterServiceAgreement));

        Map<String, String> functionPrivileges = Map.of(MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_CREATE,
                SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_CREATE);
        String contentAsString = executeClientRequest(CREATE_LEGAL_ENTITY_AS_PARTICIPANT_URL, HttpMethod.POST,
                requestBody, USER_EXT_ID_ADMIN, functionPrivileges);

        LegalEntityAsParticipantPostResponseBody responseBody =
                readValue(contentAsString, LegalEntityAsParticipantPostResponseBody.class);
        assertNotNull(responseBody);

        Optional<ServiceAgreement> msa = serviceAgreementJpaRepository
                .findByExternalId(newMasterServiceAgreement.getServiceAgreementExternalId());
        assertEquals("external", msa.get().getExternalId());
        assertEquals("desc", msa.get().getDescription());
        assertEquals("name", msa.get().getName());
    }

    
    @Test
    public void shouldCreateLegalEntityAsParticipantOfNewCustomServiceAgreement() throws Exception {
        
        String parentLegalEntityId = rootLegalEntity.getId();
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
                        .withLegalEntityName("customname")
                        .withLegalEntityExternalId("customexternal")
                        .withLegalEntityParentId(parentLegalEntityId)
                        .withLegalEntityType(LegalEntityType.BANK)
                        .withParticipantOf(new ParticipantOf()
                                        .withNewCustomServiceAgreement(new NewCustomServiceAgreement()
                                                        .withServiceAgreementName("name")
                                                        .withServiceAgreementDescription("desc")
                                                        .withServiceAgreementExternalId("external")
                                                        .withServiceAgreementState(Status.ENABLED)
                                                        .withParticipantInfo(new ParticipantInfo()
                                                                        .withShareAccounts(true)
                                                                        .withShareUsers(true))));
        
        Map<String, String> functionPrivileges = Map.of(MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_CREATE,
                        SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_CREATE);
        String contentAsString = executeClientRequest(CREATE_LEGAL_ENTITY_AS_PARTICIPANT_URL, HttpMethod.POST,
                        requestBody, USER_EXT_ID_ADMIN, functionPrivileges);
        
        LegalEntityAsParticipantPostResponseBody responseBody =
                        readValue(contentAsString, LegalEntityAsParticipantPostResponseBody.class);
        assertNotNull(responseBody);
        
        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
                        .withAction(ADD)
                        .withId(responseBody.getLegalEntityId())));
        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
                        .withAction(ADD)
                        .withId(responseBody.getServiceAgreementId())));
    }
    
    @Test
    public void shouldCreateLegalEntityAsParticipantOfExistingCustomServiceAgreement() throws Exception {
         String serviceAgreementId = serviceAgreementJpaRepository.save(new ServiceAgreement()
                        .withName("sa-name")
                        .withDescription("description")
                        .withCreatorLegalEntity(rootLegalEntity)).getId();
        
        String parentLegalEntityId = rootLegalEntity.getId();
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
                        .withLegalEntityName("newlename")
                        .withLegalEntityExternalId("External")
                        .withLegalEntityParentId(parentLegalEntityId)
                        .withLegalEntityType(LegalEntityType.BANK)
                        .withParticipantOf(new ParticipantOf()
                                        .withExistingCustomServiceAgreement(new ExistingCustomServiceAgreement()
                                                        .withServiceAgreementId(serviceAgreementId)
                                                        .withParticipantInfo(new ParticipantInfo()
                                                                        .withShareAccounts(true)
                                                                        .withShareUsers(false))));
        
        Map<String, String> functionPrivileges = Map.of(MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_CREATE,
                        SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_EDIT);
        String contentAsString2 = executeClientRequest(CREATE_LEGAL_ENTITY_AS_PARTICIPANT_URL, HttpMethod.POST,
                        requestBody, USER_EXT_ID_ADMIN, functionPrivileges);
        
        LegalEntityAsParticipantPostResponseBody responseBody =
                        readValue(contentAsString2, LegalEntityAsParticipantPostResponseBody.class);
        assertNotNull(responseBody);
        
        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
                        .withAction(ADD)
                        .withId(responseBody.getLegalEntityId())));
        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
                        .withAction(UPDATE)
                        .withId(responseBody.getServiceAgreementId())));
    }
    
    @Test
    public void shouldThrowExceptionWhenProvidingMultipleServiceAgreementOptions() throws Exception {
        String parentLegalEntityId = rootLegalEntity.getId();
        LegalEntityAsParticipantPostRequestBody requestBody = new LegalEntityAsParticipantPostRequestBody()
                        .withLegalEntityName("name")
                        .withLegalEntityExternalId("external")
                        .withLegalEntityParentId(parentLegalEntityId)
                        .withLegalEntityType(LegalEntityType.BANK)
                        .withParticipantOf(new ParticipantOf()
                                        .withNewCustomServiceAgreement(new NewCustomServiceAgreement()
                                                        .withServiceAgreementName("name")
                                                        .withServiceAgreementDescription("desc")
                                                        .withServiceAgreementExternalId("external")
                                                        .withServiceAgreementState(Status.ENABLED)
                                                        .withParticipantInfo(new ParticipantInfo()
                                                                        .withShareAccounts(true)
                                                                        .withShareUsers(false)))
                                        .withNewMasterServiceAgreement(new NewMasterServiceAgreement()
                                                        .withServiceAgreementName("name")
                                                        .withServiceAgreementDescription("desc")));
        
        Map<String, String> functionPrivileges = Map.of(MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_CREATE,
                        SERVICE_AGREEMENT_FUNCTION_NAME, PRIVILEGE_CREATE);
        BadRequestException exception = assertThrows(BadRequestException.class, 
                        () -> executeClientRequest(CREATE_LEGAL_ENTITY_AS_PARTICIPANT_URL, HttpMethod.POST,
                        requestBody, USER_EXT_ID_ADMIN, functionPrivileges));
        
        assertEquals(MULTIPLE_SA_PROVIDED_ERROR_MESSAGE, exception.getErrors().get(0).getMessage());
    }

}
