package com.backbase.accesscontrol.pandp.it.functiongroups.query;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.FunctionGroupQueryController;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.BulkFunctionGroupsPostRequestBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.BulkFunctionGroupsPostResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Test for {@link FunctionGroupQueryController#postBulkFunctionGroups}
 */
public class GetBulkFunctionGroupsIT extends TestConfig {

    private static final String url = "/service-api/v2/accesscontrol/accessgroups/function-groups/bulk";
    private FunctionGroup functionGroup1;
    private FunctionGroup functionGroup2;

    @Before
    public void setUp() throws Exception {
        LegalEntity legalEntity = legalEntityJpaRepository.save(createLegalEntity("ex-id", "le-name", null));
        legalEntityJpaRepository.flush();

        ServiceAgreement serviceAgreement = serviceAgreementJpaRepository.save(ServiceAgreementUtil
            .createServiceAgreement("SA", "external SA 001", "description", legalEntity, null, null));
        serviceAgreementJpaRepository.flush();

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList1 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1020View, null));
        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList2 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList2
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1020Create, null));

        functionGroup1 = getFunctionGroup(null, "fg1", "desc.fg1", groupedFunctionPrivilegeList1,
            FunctionGroupType.DEFAULT, serviceAgreement);
        functionGroup2 = getFunctionGroup(null, "fg2", "desc.fg2", groupedFunctionPrivilegeList2,
            FunctionGroupType.DEFAULT, serviceAgreement);
        functionGroup1 = functionGroupJpaRepository.save(functionGroup1);
        functionGroup2 = functionGroupJpaRepository.save(functionGroup2);
        functionGroupJpaRepository.flush();
    }

    @Test
    public void shouldReturnBulkFunctionGroups() throws Exception {
        HashSet<String> functionGroupIds = Sets.newHashSet(functionGroup1.getId(), functionGroup2.getId());
        BulkFunctionGroupsPostRequestBody data = new BulkFunctionGroupsPostRequestBody()
            .withIds(functionGroupIds);
        String requestAsString = objectMapper
            .writeValueAsString(data);
        String contentAsString = mockMvc.perform(post(url)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON).content(requestAsString))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<BulkFunctionGroupsPostResponseBody> response = objectMapper.readValue(
            contentAsString, new TypeReference<List<BulkFunctionGroupsPostResponseBody>>() {
            });

        assertEquals(2, response.size());
        assertTrue(
            response.stream().anyMatch(responseBody -> responseBody.getId().equals(functionGroup1.getId())));
        assertTrue(
            response.stream().anyMatch(responseBody -> responseBody.getId().equals(functionGroup2.getId())));
    }
}
