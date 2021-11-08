package com.backbase.accesscontrol.pandp.it.serviceagreement;

import static com.backbase.accesscontrol.matchers.ServiceAgreementMatcher.getServiceAgreementMatcher;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.ServiceAgreementQueryController;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link ServiceAgreementQueryController#getServiceAgreement}
 */
public class GetServiceAgreementByIdIT extends TestConfig {

    private static final String SERVICE_AGREEMENT_URL = "/service-api/v2/accesscontrol/accessgroups/serviceagreements/";

    @Test
    public void shouldGetServiceAgreementById() throws Exception {
        String consumerAdminId = "CA-1";
        String providerAdminId = "PA-1";
        String LegalEntityName = "service agreement legal entity";
        String consumerLEName = "consumer entity";
        String key = "externalId";
        String value = "123456789";
        Map<String, String> additions = new HashMap<>();
        additions.put(key, value);

        LegalEntity serviceAgreementLegalEntity = createLegalEntity("EX-1", LegalEntityName, null);
        LegalEntity consumerLegalEntity = createLegalEntity("EX-2", consumerLEName, serviceAgreementLegalEntity);
        String providerLEName = "provider entity";
        LegalEntity providerLegalEntity = createLegalEntity("EX-3", providerLEName, serviceAgreementLegalEntity);
        Participant provider = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId, true, false);
        List<String> providerUsers = Arrays.asList("1", "2");
        provider.addParticipantUsers(providerUsers);

        legalEntityJpaRepository.save(serviceAgreementLegalEntity);
        legalEntityJpaRepository.save(consumerLegalEntity);
        legalEntityJpaRepository.save(providerLegalEntity);

        Participant consumer = ServiceAgreementUtil.createParticipantWithAdmin(consumerAdminId, false, true);

        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "id.external", "description", serviceAgreementLegalEntity, null, null);
        serviceAgreement.setState(ServiceAgreementState.DISABLED);
        serviceAgreement.addParticipant(consumer, consumerLegalEntity.getId(), false, true);
        serviceAgreement.addParticipant(provider, providerLegalEntity.getId(), true, false);
        serviceAgreement.setAdditions(additions);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        FunctionGroup functionGroup1 = new FunctionGroup();
        functionGroup1.setName("name");
        functionGroup1.setDescription("description");
        functionGroup1.setType(FunctionGroupType.DEFAULT);
        functionGroup1.setServiceAgreement(serviceAgreement);
        functionGroupJpaRepository.save(functionGroup1);

        dataGroupJpaRepository
            .save(DataGroupUtil.createDataGroup("DG name", "ARRANGEMENTS", "description", serviceAgreement));

        String contentAsString = mockMvc.perform(get(SERVICE_AGREEMENT_URL + serviceAgreement.getId())
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ServiceAgreementItem ServiceAgreementItem = objectMapper
            .readValue(contentAsString, ServiceAgreementItem.class);

        assertThat(serviceAgreement,
            getServiceAgreementMatcher(equalTo(ServiceAgreementItem.getId()), equalTo(ServiceAgreementItem.getName()),
                equalTo(ServiceAgreementItem.getDescription()),
                hasProperty("id", equalTo(ServiceAgreementItem.getCreatorLegalEntity()))));
        ServiceAgreementState returnedState = ServiceAgreementState
            .valueOf(ServiceAgreementItem.getStatus().toString());
        assertEquals(serviceAgreement.getState(), returnedState);
        assertThat(ServiceAgreementItem.getAdditions().size(), is(1));
        assertTrue(ServiceAgreementItem.getAdditions().containsKey(key));
        assertTrue(ServiceAgreementItem.getAdditions().containsValue(value));
    }

    @Test
    public void shouldGetServiceAgreementByIdWhenOnlyFunctionGroupIsExposed() throws Exception {
        String consumerAdminId = "CA-1";
        String providerAdminId = "PA-1";
        String LegalEntityName = "service agreement legal entity";
        String consumerLEName = "consumer entity";
        String key = "externalId";
        String value = "123456789";
        Map<String, String> additions = new HashMap<>();
        additions.put(key, value);

        LegalEntity serviceAgreementLegalEntity = createLegalEntity("EX-1", LegalEntityName, null);
        LegalEntity consumerLegalEntity = createLegalEntity("EX-2", consumerLEName, serviceAgreementLegalEntity);
        String providerLEName = "provider entity";
        LegalEntity providerLegalEntity = createLegalEntity("EX-3", providerLEName, serviceAgreementLegalEntity);
        Participant provider = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId, true, false);
        List<String> providerUsers = Arrays.asList("1", "2");
        provider.addParticipantUsers(providerUsers);

        legalEntityJpaRepository.save(serviceAgreementLegalEntity);
        legalEntityJpaRepository.save(consumerLegalEntity);
        legalEntityJpaRepository.save(providerLegalEntity);

        Participant consumer = ServiceAgreementUtil.createParticipantWithAdmin(consumerAdminId, false, true);

        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "id.external", "description", serviceAgreementLegalEntity, null, null);
        serviceAgreement.setState(ServiceAgreementState.DISABLED);
        serviceAgreement.addParticipant(consumer, consumerLegalEntity.getId(), false, true);
        serviceAgreement.addParticipant(provider, providerLegalEntity.getId(), true, false);
        serviceAgreement.setAdditions(additions);
        serviceAgreementJpaRepository.save(serviceAgreement);

        FunctionGroup functionGroup1 = new FunctionGroup();
        functionGroup1.setName("name");
        functionGroup1.setDescription("description");
        functionGroup1.setType(FunctionGroupType.DEFAULT);
        functionGroup1.setServiceAgreement(serviceAgreement);
        functionGroupJpaRepository.save(functionGroup1);

        String contentAsString = mockMvc.perform(get(SERVICE_AGREEMENT_URL + serviceAgreement.getId())
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ServiceAgreementItem serviceAgreementItem = objectMapper.readValue(contentAsString, ServiceAgreementItem.class);

        assertThat(serviceAgreement,
            getServiceAgreementMatcher(equalTo(serviceAgreementItem.getId()), equalTo(serviceAgreementItem.getName()),
                equalTo(serviceAgreementItem.getDescription()),
                hasProperty("id", equalTo(serviceAgreementItem.getCreatorLegalEntity()))));

        ServiceAgreementState returnedState = ServiceAgreementState
            .valueOf(serviceAgreementItem.getStatus().toString());
        assertEquals(serviceAgreement.getState(), returnedState);

        assertThat(serviceAgreementItem.getAdditions().size(), is(1));
        assertTrue(serviceAgreementItem.getAdditions().containsKey(key));
        assertTrue(serviceAgreementItem.getAdditions().containsValue(value));
    }
}
