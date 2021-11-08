package com.backbase.accesscontrol.pandp.it.serviceagreement;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.ServiceAgreementQueryController;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import java.util.HashSet;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link ServiceAgreementQueryController#getServiceAgreementAdmins}
 */
public class GetServiceAgreementAdminsIT extends TestConfig {

    private static final String SERVICE_AGREEMENT_ADMIN_URL = "/service-api/v2/accesscontrol/accessgroups/serviceagreements/";
    private LegalEntity legalEntity;
    private ServiceAgreement serviceAgreement;
    private static final String USER_ID = UUID.randomUUID().toString();
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        tearDown();

        legalEntity = new LegalEntity();
        legalEntity.setName("le-name");
        legalEntity.setExternalId("le-name");
        legalEntity.setType(LegalEntityType.CUSTOMER);

        legalEntity = legalEntityJpaRepository.save(legalEntity);

        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setName("sa-01");
        serviceAgreement.setDescription("sa-01");

        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        userContext = userContextJpaRepository.save(new UserContext(USER_ID, serviceAgreement.getId()));
    }

    @Test
    public void testGetServiceAgreementAdmins() throws Exception {
        String sharingUsersFgName = String.format("System_%s_consumer", serviceAgreement.getId());
        String sharingAccountsFgName = String.format("System_%s_provider", serviceAgreement.getId());
        FunctionGroup consumerSystemFG = FunctionGroupUtil
            .getFunctionGroup(null, sharingUsersFgName, "desc", new HashSet<>(), FunctionGroupType.SYSTEM,
                serviceAgreement);
        FunctionGroup providerSystemFG = FunctionGroupUtil
            .getFunctionGroup(null, sharingAccountsFgName, "desc", new HashSet<>(), FunctionGroupType.SYSTEM,
                serviceAgreement);

        functionGroupJpaRepository.saveAll(asList(consumerSystemFG, providerSystemFG));

        UserAssignedFunctionGroup providerAdmin = new UserAssignedFunctionGroup(providerSystemFG, userContext);
        UserAssignedFunctionGroup consumerAdmin = new UserAssignedFunctionGroup(consumerSystemFG, userContext);

        Participant participant = new Participant();
        participant.setServiceAgreement(serviceAgreement);
        participant.setLegalEntity(legalEntity);
        participant.addAdmin(USER_ID);

        participantJpaRepository.save(participant);

        userAssignedFunctionGroupJpaRepository.saveAll(asList(consumerAdmin, providerAdmin));

        String contentAsString = mockMvc.perform(get(SERVICE_AGREEMENT_ADMIN_URL + serviceAgreement.getId() + "/admins")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ServiceAgreementAdminsGetResponseBody responseBody = objectMapper
            .readValue(contentAsString, ServiceAgreementAdminsGetResponseBody.class);

        assertThat(responseBody.getAdmins(), hasSize(1));
        assertThat(responseBody.getAdmins(), hasItem(USER_ID));
    }

}
