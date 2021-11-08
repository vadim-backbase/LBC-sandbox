package com.backbase.accesscontrol.pandp.it.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.ServiceAgreementQueryController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link ServiceAgreementQueryController#getServiceAgreementUsers}
 */
public class GetServiceAgreementUsersIT extends TestConfig {

    private static final String SHARING_USERS_USER1 = "1";
    private static final String SHARING_USERS_USER2 = "2";
    private static final String SHARING_USERS_USER3 = "3";

    private ServiceAgreement serviceAgreement1;

    private static final String GET_SERVICE_AGREEMENT_USERS_URL = "/service-api/v2/accesscontrol/accessgroups/serviceagreements/";

    @Before
    public void setUp() {
        String LegalEntityName1 = "service agreement legal entity 1";
        String providerAdminId1 = "PA-1";
        String providerAdminId2 = "PA-2";
        String key = "externalId";
        String value = "123456789";
        Map<String, String> addition = new HashMap<>();
        addition.put(key, value);
        addition.put("second", "asdasdasd");

        LegalEntity serviceAgreementLegalEntity1 = createLegalEntity("EX-LE-1", LegalEntityName1, null);

        LegalEntity providerLE1 =
            createLegalEntity("prLe1", "providerName1", null);
        LegalEntity providerLE2 =
            createLegalEntity("prLe2", "providerName2", null);

        Participant provider1 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId1, true, false);
        List<String> providerUsers1 = Arrays.asList(SHARING_USERS_USER1, SHARING_USERS_USER2);
        provider1.addParticipantUsers(providerUsers1);

        Participant provider2 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId2, true, false);
        List<String> providerUsers2 = Arrays.asList(SHARING_USERS_USER2, SHARING_USERS_USER3);
        provider2.addParticipantUsers(providerUsers2);

        legalEntityJpaRepository.save(providerLE1);
        legalEntityJpaRepository.save(providerLE2);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity1);

        serviceAgreement1 =
            createServiceAgreement(
                "sa1",
                "id.external1",
                "desc1",
                serviceAgreementLegalEntity1,
                null,
                null);

        serviceAgreement1.addParticipant(provider1, providerLE1.getId(), true, false);
        serviceAgreement1.addParticipant(provider2, providerLE2.getId(), true, false);

        serviceAgreement1.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        serviceAgreement1.setState(ServiceAgreementState.DISABLED);
        provider1.setServiceAgreement(serviceAgreement1);
        serviceAgreement1.setAdditions(addition);
        serviceAgreementJpaRepository.save(serviceAgreement1);
    }

    @Test
    public void shouldThrowBadRequestIfSAIDNotExists() throws Exception {

        mockMvc.perform(get(GET_SERVICE_AGREEMENT_USERS_URL + "not_exist_sa_id" + "/users")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isBadRequest())
            .andDo(mvcResult -> {
                BadRequestException exception = (BadRequestException) mvcResult.getResolvedException();
                assertEquals(ERR_ACC_029.getErrorCode(), Objects.requireNonNull(exception).getErrors().get(0).getKey());
                assertEquals(ERR_ACC_029.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldGetProviderUsers() throws Exception {

        String contentAsString = mockMvc
            .perform(get(GET_SERVICE_AGREEMENT_USERS_URL + serviceAgreement1.getId() + "/users")
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ServiceAgreementUsersGetResponseBody response = objectMapper
            .readValue(contentAsString, ServiceAgreementUsersGetResponseBody.class);

        assertEquals(3, response.getUserIds().size());
        assertTrue(response.getUserIds().contains(SHARING_USERS_USER1));
        assertTrue(response.getUserIds().contains(SHARING_USERS_USER2));
        assertTrue(response.getUserIds().contains(SHARING_USERS_USER3));
    }

}
