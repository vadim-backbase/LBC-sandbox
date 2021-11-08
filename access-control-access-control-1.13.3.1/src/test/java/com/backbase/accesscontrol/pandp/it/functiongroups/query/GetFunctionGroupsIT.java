package com.backbase.accesscontrol.pandp.it.functiongroups.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.FunctionGroupQueryController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Test for {@link FunctionGroupQueryController#getFunctionGroups}
 */
public class GetFunctionGroupsIT extends TestConfig {

    private static final String url = "/service-api/v2/accesscontrol/accessgroups/function-groups";

    private FunctionGroup functionGroup1;
    private FunctionGroup functionGroup2;
    private FunctionGroup functionGroup3;
    private FunctionGroup templateFg;
    private FunctionGroup templateFgChild;

    private LegalEntity legalEntity;
    private LegalEntity childLegalEntity;

    private ServiceAgreement rootServiceAgreement;
    private ServiceAgreement serviceAgreement;

    private ApplicableFunctionPrivilege applicableFunctionPrivilege2;

    @Before
    public void setUp() throws Exception {
        repositoryCleaner.clean();
        applicableFunctionPrivilege2 = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "view");

        legalEntity = legalEntityJpaRepository.save(LegalEntityUtil.createLegalEntity("ex-id", "le-name", null));
        legalEntityJpaRepository.flush();

        childLegalEntity = legalEntityJpaRepository
            .save(LegalEntityUtil.createLegalEntity("ex-child", "le-child", legalEntity));
        legalEntityJpaRepository.flush();

        rootServiceAgreement = new ServiceAgreement();
        rootServiceAgreement.setName("SA-root");
        rootServiceAgreement.setDescription("description");
        rootServiceAgreement.setCreatorLegalEntity(legalEntity);
        rootServiceAgreement.setMaster(true);
        rootServiceAgreement.setPermissionSetsRegular(Sets.newHashSet(assignablePermissionSetRegular));
        rootServiceAgreement = serviceAgreementJpaRepository.save(rootServiceAgreement);
        serviceAgreementJpaRepository.flush();

        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName("SA-Name");
        serviceAgreement.setDescription("description");
        serviceAgreement.setCreatorLegalEntity(childLegalEntity);
        serviceAgreement.setMaster(true);
        serviceAgreement.setPermissionSetsRegular(Sets.newHashSet(assignablePermissionSetRegular));
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);
        serviceAgreementJpaRepository.flush();

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList1 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1028View, null));
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1028Create, null));
        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList2 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList2
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, applicableFunctionPrivilege2, null));

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList3 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList3
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, applicableFunctionPrivilege2, null));

        functionGroup1 = FunctionGroupUtil
            .getFunctionGroup(null, "fg1", "desc.fg1", groupedFunctionPrivilegeList1,
                FunctionGroupType.DEFAULT, null);
        functionGroup1.setServiceAgreement(serviceAgreement);
        functionGroup2 = FunctionGroupUtil
            .getFunctionGroup(null, "fg2", "desc.fg2", groupedFunctionPrivilegeList2,
                FunctionGroupType.DEFAULT, null);
        functionGroup2.setServiceAgreement(serviceAgreement);
        functionGroup3 = FunctionGroupUtil
            .getFunctionGroup(null, "fg3", "desc.fg3", groupedFunctionPrivilegeList3,
                FunctionGroupType.SYSTEM, null);
        templateFg = FunctionGroupUtil
            .getFunctionGroup(null, "fg-template", "desc.fg1", groupedFunctionPrivilegeList1,
                FunctionGroupType.TEMPLATE, rootServiceAgreement);
        templateFgChild = FunctionGroupUtil
            .getFunctionGroup(null, "fg-template-2", "desc.fg2", groupedFunctionPrivilegeList1,
                FunctionGroupType.TEMPLATE, serviceAgreement);
        templateFg.setAssignablePermissionSet(assignablePermissionSetRegular);
        templateFgChild.setAssignablePermissionSet(assignablePermissionSetRegular);

        functionGroup3.setServiceAgreement(serviceAgreement);
        functionGroup1 = functionGroupJpaRepository.save(functionGroup1);
        functionGroup2 = functionGroupJpaRepository.save(functionGroup2);
        functionGroup3 = functionGroupJpaRepository.save(functionGroup3);
        templateFg = functionGroupJpaRepository.save(templateFg);
        templateFgChild = functionGroupJpaRepository.save(templateFgChild);
        functionGroupJpaRepository.flush();
    }

    @Test
    public void shouldListFunctionGroupsByServiceAgreementId() throws Exception {

        String contentAsString = mockMvc.perform(get(url)
            .param("serviceAgreementId", serviceAgreement.getId())
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<FunctionGroupsGetResponseBody> data = objectMapper.readValue(
            contentAsString, new TypeReference<>() {
            });

        assertEquals(5, data.size());
        assertTrue(data.stream().anyMatch(functionGroup -> functionGroup.getId().equals(functionGroup1.getId())));
        assertTrue(data.stream().anyMatch(functionGroup -> functionGroup.getId().equals(functionGroup2.getId())));
        assertTrue(data.stream().anyMatch(functionGroup -> functionGroup.getId().equals(templateFg.getId())));
        assertTrue(data.stream().anyMatch(functionGroup -> functionGroup.getId().equals(templateFgChild.getId())));
        assertTrue(data.stream().anyMatch(functionGroup -> functionGroup.getId().equals(functionGroup3.getId())));
    }
}
