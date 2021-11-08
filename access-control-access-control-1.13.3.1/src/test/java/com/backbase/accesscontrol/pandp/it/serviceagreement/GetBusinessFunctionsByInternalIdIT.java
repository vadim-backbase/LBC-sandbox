package com.backbase.accesscontrol.pandp.it.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.junit.Assert.assertEquals;
import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.ServiceAgreementsQueryController;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;

/**
 * IT test for {@link ServiceAgreementsQueryController#getIdinternalIdbusinessfunctions(String, HttpServletRequest,
 * HttpServletResponse)}
 */
public class GetBusinessFunctionsByInternalIdIT extends TestConfig {

    private static final String GET_BF_URL = "/service-api/v2/accesscontrol/service-agreements/"
        + "id/%s/business-functions";

    @Test
    @Transactional
    public void shouldGetBusinessFunctionsByExternalServiceAgreementId() throws Exception {
        String LegalEntityName = "service agreement legal entity";
        String serviceAgreementExternalId = "id.external";

        LegalEntity serviceAgreementLegalEntity = createLegalEntity("EX-1", LegalEntityName, null);
        legalEntityJpaRepository.save(serviceAgreementLegalEntity);

        AssignablePermissionSet assignablePermissionSet = createAssignablePermissionSet("APS",
            AssignablePermissionType.CUSTOM, "desc", apfBf1028View.getId(), apfBf1028Create.getId());

        assignablePermissionSet = assignablePermissionSetJpaRepository.save(assignablePermissionSet);

        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", serviceAgreementExternalId, "description", serviceAgreementLegalEntity,
                null, null);
        serviceAgreement.setState(ServiceAgreementState.DISABLED);
        serviceAgreement.setPermissionSetsRegular(asSet(assignablePermissionSet));

        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        String contentAsString = mockMvc.perform(get(String.format(GET_BF_URL, serviceAgreement.getId()))
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<FunctionsGetResponseBody> data = objectMapper.readValue(
            contentAsString, new TypeReference<List<FunctionsGetResponseBody>>() {
            });

        assertEquals(1, data.size());
        assertEquals(bf1028.getId(), data.get(0).getFunctionId());
        assertEquals(2, data.get(0).getPrivileges().size());
    }
}
