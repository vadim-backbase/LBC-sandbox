package com.backbase.accesscontrol.pandp.it.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.ServiceAgreementsQueryController;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementByPermissionSet;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;


/**
 * IT test for {@link ServiceAgreementsQueryController#getGetServiceAgremeentByPermissionSetId}
 */
public class GetServiceAgreementByPermissionSetIT extends TestConfig {

    private static final String URL = "/service-api/v2/accesscontrol/service-agreements/permission-sets/";

    public AssignablePermissionSet assignablePermissionSet;
    public ServiceAgreement serviceAgreement1;
    public ServiceAgreement serviceAgreement2;
    public ServiceAgreement serviceAgreement3;
    public ServiceAgreement serviceAgreement4;

    @Before
    @Transactional
    public void setUp() {
        assignablePermissionSet = createAssignablePermissionSet("aps2",
            AssignablePermissionType.CUSTOM, "desc", apfBf1028View.getId(), apfBf1028Create.getId());
        assignablePermissionSet = assignablePermissionSetJpaRepository.save(assignablePermissionSet);

        LegalEntity le1 = createLegalEntity("prLe1", "providerName1", null);
        le1 = legalEntityJpaRepository.save(le1);

        serviceAgreement1 = createServiceAgreement("sa1", "id.external1", "desc1",
            le1, null, null);
        serviceAgreement2 = createServiceAgreement("sa2", "id.external2", "desc1",
            le1, null, null);
        serviceAgreement3 = createServiceAgreement("sa3", "id.external3", "desc1",
            le1, null, null);
        serviceAgreement4 = createServiceAgreement("sa4", "id.external4", "desc1",
            le1, null, null);

        serviceAgreement1 = serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreement1.getPermissionSetsAdmin().add(assignablePermissionSet);
        serviceAgreement1.getPermissionSetsRegular().add(assignablePermissionSet);
        serviceAgreementJpaRepository.save(serviceAgreement1);

        serviceAgreement2 = serviceAgreementJpaRepository.save(serviceAgreement2);
        serviceAgreement2.getPermissionSetsRegular().add(assignablePermissionSet);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        serviceAgreement3 = serviceAgreementJpaRepository.save(serviceAgreement3);
        serviceAgreement3.getPermissionSetsRegular().add(assignablePermissionSet);
        serviceAgreementJpaRepository.save(serviceAgreement3);

    }

    @Test
    public void getServiceAgreementByPermissionSetId() throws Exception {

        String id = assignablePermissionSet.getId().toString();

        String url = URL + "id/" + id + "?from=0&size=2";

        MockHttpServletResponse response = mockMvc.perform(get(url)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse();

        List<ServiceAgreementByPermissionSet> result = objectMapper.readValue(response.getContentAsString(),
            new TypeReference<List<ServiceAgreementByPermissionSet>>() {
            });

        assertEquals(singletonList("3"), response.getHeaders("X-Total-Count"));
        assertEquals(2, result.size());
    }

}
