package com.backbase.accesscontrol.pandp.it.users.query;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.UserContextQueryController;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Test for {@link UserContextQueryController#getUserContexts}
 */
public class GetUserContextsIT extends TestConfig {

    private static final String url = "/service-api/v2/accesscontrol/accessgroups/usercontext/%s/serviceAgreements";

    @Before
    public void setUp() {
        String legalEntityName1 = "service agreement legal entity 1";
        String legalEntityName2 = "service agreement legal entity 2";
        String providerAdminId1 = "PA-1";
        String providerAdminId2 = "PA-2";
        String providerAdminId3 = "PA-3";
        LegalEntity serviceAgreementLegalEntity1 = createLegalEntity("EX-LE-1", legalEntityName1, null);
        LegalEntity serviceAgreementLegalEntity2 = createLegalEntity("EX-LE-2", legalEntityName2, null);
        LegalEntity providerLE1 = createLegalEntity("prLe1", "providerName1", null);
        LegalEntity providerLE2 = createLegalEntity("prLe2", "providerName2", null);
        LegalEntity providerLE3 = createLegalEntity("prLe3", "providerName3", null);

        Participant provider1 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId1, true, false);
        List<String> providerUsers1 = Arrays.asList("1", "2");
        provider1.addParticipantUsers(providerUsers1);

        Participant provider2 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId2, true, false);
        List<String> providerUsers2 = Arrays.asList("3", "4");
        provider2.addParticipantUsers(providerUsers2);

        Participant provider3 = ServiceAgreementUtil.createParticipantWithAdmin(providerAdminId3, true, false);
        List<String> providerUsers3 = Arrays.asList("5", "6", "7");
        provider3.addParticipantUsers(providerUsers3);

        legalEntityJpaRepository.save(providerLE1);
        legalEntityJpaRepository.save(providerLE2);
        legalEntityJpaRepository.save(providerLE3);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity1);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity2);

        ServiceAgreement serviceAgreement1 = createServiceAgreement("sa1", "id.external1", "desc1",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement1.addParticipant(provider1, providerLE1.getId(), true, false);
        serviceAgreement1.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        provider1.setServiceAgreement(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement1);

        ServiceAgreement serviceAgreement2 = createServiceAgreement("sa2", "id.external2", "desc2",
            serviceAgreementLegalEntity1, null, null);
        serviceAgreement2.addParticipant(new Participant(), serviceAgreementLegalEntity2.getId(), false, true);
        serviceAgreement2.addParticipant(provider2, providerLE2.getId(), true, false);
        provider2.setServiceAgreement(serviceAgreement2);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        ServiceAgreement serviceAgreement3 = createServiceAgreement("sa3", "id.external3", "desc3",
            serviceAgreementLegalEntity2, null, null);
        serviceAgreement3.addParticipant(provider3, providerLE3.getId(), true, false);
        provider3.setServiceAgreement(serviceAgreement3);
        serviceAgreement3.addParticipant(new Participant(), serviceAgreementLegalEntity1.getId(), false, true);
        serviceAgreementJpaRepository.save(serviceAgreement3);
        addPermissionToServiceAgreement("6", serviceAgreement3);
    }

    @Test
    public void shouldGetUserContextByUserId() throws Exception {

        String contentAsString = mockMvc.perform(get(String.format(url, "6"))
            .param("from", "0")
            .param("size", "10")
            .param("query", "s")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        UserContextsGetResponseBody response = objectMapper
            .readValue(contentAsString, UserContextsGetResponseBody.class);

        assertEquals("sa3", response.getElements().get(0).getServiceAgreementName());
    }

    @Test
    public void shouldGetNothingByUserIdAndWrongQuery() throws Exception {
        String contentAsString = mockMvc.perform(get(String.format(url, "6"))
            .param("from", "0")
            .param("size", "10")
            .param("query", "wrong pattern")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        UserContextsGetResponseBody response = objectMapper
            .readValue(contentAsString, UserContextsGetResponseBody.class);

        assertEquals(0, response.getTotalElements().longValue());
    }

    @Test
    public void shouldGetUserContextByUserIdAndEmptyQuery() throws Exception {

        String contentAsString = mockMvc.perform(get(String.format(url, "6"))
            .param("from", "0")
            .param("size", "10")
            .param("query", "")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        UserContextsGetResponseBody response = objectMapper
            .readValue(contentAsString, UserContextsGetResponseBody.class);

        assertEquals("sa3", response.getElements().get(0).getServiceAgreementName());
    }

    @Test
    public void shouldGetUserContextByUserIdAndNullQuery() throws Exception {

        String contentAsString = mockMvc.perform(get(String.format(url, "6"))
            .param("from", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        UserContextsGetResponseBody response = objectMapper
            .readValue(contentAsString, UserContextsGetResponseBody.class);

        assertEquals("sa3", response.getElements().get(0).getServiceAgreementName());
    }

    @Test
    public void shouldGetNothingByWrongUserId() throws Exception {

        String contentAsString = mockMvc.perform(get(String.format(url, "wrongId"))
            .param("from", "0")
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        UserContextsGetResponseBody response = objectMapper
            .readValue(contentAsString, UserContextsGetResponseBody.class);

        assertEquals(0, response.getElements().size());
    }

    private void addPermissionToServiceAgreement(String userId, ServiceAgreement serviceAgreement) {

        GroupedFunctionPrivilege viewEntitlementsWithLimit = getGroupedFunctionPrivilege(null, apfBf1019View, null);
        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(
            getFunctionGroup(null, RandomStringUtils.randomAlphabetic(8), "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        UserContext userContext = userContextJpaRepository.save(new UserContext(userId,
            serviceAgreement.getId()));
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedFunctionGroup,
            userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);
    }
}
