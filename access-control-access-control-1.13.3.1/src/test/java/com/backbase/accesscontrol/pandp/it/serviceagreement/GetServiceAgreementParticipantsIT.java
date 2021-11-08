package com.backbase.accesscontrol.pandp.it.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.ServiceAgreementQueryController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpHeaders;


/**
 * Test for {@link ServiceAgreementQueryController#getServiceAgreementParticipants}
 */
public class GetServiceAgreementParticipantsIT extends TestConfig {

    private static final String GET_SERVICE_AGREEMENT_PARTICIPANTS_URL = "/service-api/v2/accesscontrol/accessgroups/serviceagreements/";
    private static final String EX_1 = "EX-1";
    private static final String EX_2 = "EX-2";
    private static final String EX_3 = "EX-3";
    private static final String SA_ID_EXTERNAL = "id.external";
    private static final String SA_NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String CONSUMER_ADMIN_ID = "CA-1";
    private static final String PROVIDER1_ADMIN_ID = "PA-1";
    private static final String PROVIDER2_ADMIN_ID = "PA-2";
    private static final String LEGAL_ENTITY_NAME = "service agreement legal entity";
    private static final String CONSUMER_LE_NAME = "consumer entity";
    private static final String PROVIDER_LE_NAME = "provider entity";

    @Test
    public void shouldGetServiceAgreementParticipants() throws Exception {
        LegalEntity serviceAgreementLegalEntity = createLegalEntity(EX_1, LEGAL_ENTITY_NAME, null);
        LegalEntity consumerAndProviderLegalEntity = createLegalEntity(EX_2, CONSUMER_LE_NAME,
            serviceAgreementLegalEntity);
        LegalEntity providerLegalEntity = createLegalEntity(EX_3, PROVIDER_LE_NAME, serviceAgreementLegalEntity);
        com.backbase.accesscontrol.domain.Participant provider1 = ServiceAgreementUtil
            .createParticipantWithAdmin(PROVIDER1_ADMIN_ID, true, false);
        com.backbase.accesscontrol.domain.Participant provider2 = ServiceAgreementUtil
            .createParticipantWithAdmin(PROVIDER2_ADMIN_ID, true, false);

        legalEntityJpaRepository.save(serviceAgreementLegalEntity);
        legalEntityJpaRepository.save(consumerAndProviderLegalEntity);
        legalEntityJpaRepository.save(providerLegalEntity);

        ServiceAgreementUtil.createParticipantWithAdmin(CONSUMER_ADMIN_ID, false, true);

        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement(SA_NAME, SA_ID_EXTERNAL, DESCRIPTION, serviceAgreementLegalEntity, null, null);
        serviceAgreement.addParticipant(provider1, providerLegalEntity.getId(), true, false);
        serviceAgreement.addParticipant(provider2, consumerAndProviderLegalEntity.getId(), true, true);

        serviceAgreementJpaRepository.save(serviceAgreement);

        String contentAsString = mockMvc
            .perform(get(GET_SERVICE_AGREEMENT_PARTICIPANTS_URL + serviceAgreement.getId() + "/participants")
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Participant> participants = objectMapper
            .readValue(contentAsString, new TypeReference<List<Participant>>() {
            });

        assertTrue(containsParticipantWithRoles(participants, providerLegalEntity, true, false));
        assertTrue(containsParticipantWithRoles(participants, consumerAndProviderLegalEntity, true, true));
    }

    @Test
    public void shouldGetMasterServiceAgreementParticipants() throws Exception {
        LegalEntity serviceAgreementLegalEntity = createLegalEntity(EX_1, LEGAL_ENTITY_NAME, null);
        LegalEntity consumerAndProviderLegalEntity = createLegalEntity(EX_2, CONSUMER_LE_NAME,
            serviceAgreementLegalEntity);
        LegalEntity providerLegalEntity = createLegalEntity(EX_3, PROVIDER_LE_NAME, serviceAgreementLegalEntity);
        com.backbase.accesscontrol.domain.Participant participant = ServiceAgreementUtil
            .createParticipantWithAdmin(PROVIDER1_ADMIN_ID, true, true);

        legalEntityJpaRepository.save(serviceAgreementLegalEntity);
        legalEntityJpaRepository.save(consumerAndProviderLegalEntity);
        legalEntityJpaRepository.save(providerLegalEntity);

        ServiceAgreementUtil.createParticipantWithAdmin(CONSUMER_ADMIN_ID, false, true);

        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement(SA_NAME, SA_ID_EXTERNAL, DESCRIPTION, serviceAgreementLegalEntity, null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.addParticipant(participant, serviceAgreementLegalEntity.getId(), true, true);

        serviceAgreementJpaRepository.save(serviceAgreement);

        String contentAsString = mockMvc
            .perform(get(GET_SERVICE_AGREEMENT_PARTICIPANTS_URL + serviceAgreement.getId() + "/participants")
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Participant> participants = objectMapper
            .readValue(contentAsString, new TypeReference<List<Participant>>() {
            });

        assertEquals(1, participants.size());
        assertTrue(containsParticipantWithRoles(participants, serviceAgreementLegalEntity, true, true));
    }

    private boolean containsParticipantWithRoles(
        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant> participants,
        LegalEntity legalEntity, boolean sharingUsers, boolean sharingAccounts) {
        return participants
            .stream()
            .anyMatch(participant ->
                participant.getId().equals(legalEntity.getId())
                    && participant.getName().equals(legalEntity.getName())
                    && participant.getExternalId().equals(legalEntity.getExternalId())
                    && participant.getSharingUsers() == sharingUsers
                    && participant.getSharingAccounts() == sharingAccounts
            );
    }

}
