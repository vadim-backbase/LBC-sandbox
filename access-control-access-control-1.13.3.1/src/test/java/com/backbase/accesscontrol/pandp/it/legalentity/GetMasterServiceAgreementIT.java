package com.backbase.accesscontrol.pandp.it.legalentity;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.LegalEntityQueryController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link LegalEntityQueryController#getMasterServiceAgreement}
 */
public class GetMasterServiceAgreementIT extends TestConfig {

    private static final String GET_MASTER_SERVICE_AGREEMENT_URL = "/service-api/v2/accesscontrol/legalentities/%s/serviceagreements/master";
    private ServiceAgreement serviceAgreement;
    private String key = "externalId";
    private String value = "123456789";
    private Map<String, String> additions = new HashMap<>();
    private LegalEntity bank;

    @Before
    public void setUp() {
        additions.put(key, value);

        bank = createLegalEntity("bank", "bank", null);
        legalEntityJpaRepository.save(bank);
        serviceAgreement = createServiceAgreement("name", "id.external", "des", bank, null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement.setState(ServiceAgreementState.DISABLED);
        serviceAgreement.setAdditions(additions);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);
    }

    @Test
    public void getMasterServiceAgreement() throws Exception {

        String contentAsString = mockMvc.perform(get(String.format(GET_MASTER_SERVICE_AGREEMENT_URL, bank.getId()))
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ServiceAgreementItem serviceAgreementItem = objectMapper
            .readValue(contentAsString, ServiceAgreementItem.class);

        assertEquals(serviceAgreement.getId(), serviceAgreementItem.getId());
        assertEquals(serviceAgreement.getExternalId(), serviceAgreementItem.getExternalId());
        assertEquals(bank.getId(), serviceAgreementItem.getCreatorLegalEntity());
        assertEquals(serviceAgreement.getName(), serviceAgreementItem.getName());
        assertEquals(serviceAgreement.getDescription(), serviceAgreementItem.getDescription());
        assertEquals(serviceAgreement.getState().toString(), serviceAgreementItem.getStatus().toString());

        assertThat(serviceAgreementItem.getAdditions().size(), is(1));
        assertTrue(serviceAgreementItem.getAdditions().containsKey(key));
        assertTrue(serviceAgreementItem.getAdditions().containsValue(value));
    }

}
