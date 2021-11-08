package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class ListFunctionGroupIT extends TestDbWireMock {

    private static final String url = "/accessgroups/function-groups";

    private FunctionGroup functionGroup1;
    private FunctionGroup functionGroup2;
    private FunctionGroup functionGroup3;
    private FunctionGroup templateFg;
    private FunctionGroup templateFgChild;

    private LegalEntity legalEntity;
    private LegalEntity childLegalEntity;

    private ServiceAgreement rootServiceAgreement;
    private ServiceAgreement serviceAgreement;
    private ApplicableFunctionPrivilege apfBf1028View;
    private ApplicableFunctionPrivilege apfBf1028Create;
    private ApplicableFunctionPrivilege apfBf1020View;
    private ApplicableFunctionPrivilege apfBf1020Create;

    @Before
    public void setUp() throws Exception {
        legalEntity = rootLegalEntity;

        apfBf1028View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1028", "view");
        apfBf1028Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1028", "create");
        apfBf1020View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "view");
        apfBf1020Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "create");

        childLegalEntity = legalEntityJpaRepository
            .save(LegalEntityUtil.createLegalEntity("ex-child", "le-child", legalEntity));
        legalEntityJpaRepository.flush();

        rootServiceAgreement = rootMsa;

        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setName("SA-Name");
        serviceAgreement.setDescription("description");
        serviceAgreement.setCreatorLegalEntity(childLegalEntity);
        serviceAgreement.setMaster(true);
        serviceAgreement.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        serviceAgreement = serviceAgreementJpaRepository.saveAndFlush(serviceAgreement);

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList1 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1028View, null));
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1028Create, null));
        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList2 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList2
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1020View, null));

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList3 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList3
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, apfBf1020Create, null));

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
        templateFg.setAssignablePermissionSet(apsDefaultRegular);
        templateFgChild.setAssignablePermissionSet(apsDefaultRegular);

        functionGroup3.setServiceAgreement(serviceAgreement);
        functionGroup1 = functionGroupJpaRepository.save(functionGroup1);
        functionGroup2 = functionGroupJpaRepository.save(functionGroup2);
        functionGroup3 = functionGroupJpaRepository.save(functionGroup3);
        templateFg = functionGroupJpaRepository.save(templateFg);
        templateFgChild = functionGroupJpaRepository.save(templateFgChild);
        functionGroupJpaRepository.flush();
    }

    @Test
    public void getAllFunctionGroupsByServiceAgreementId() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(url)
                .addQueryParameter("serviceAgreementId", serviceAgreement.getId()).build(),
            HttpMethod.GET,
            "user", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_VIEW
        );

        List<FunctionGroupsGetResponseBody> response = objectMapper.readValue(
            contentAsString, new TypeReference<List<FunctionGroupsGetResponseBody>>() {
            });

        assertEquals(5, response.size());
        assertTrue(
            response.stream().anyMatch(responseBody -> responseBody.getId().equals(functionGroup1.getId())));
        assertTrue(
            response.stream().anyMatch(responseBody -> responseBody.getId().equals(functionGroup2.getId())));
        assertTrue(
            response.stream().anyMatch(responseBody -> responseBody.getId().equals(templateFg.getId())));
        assertTrue(
            response.stream().anyMatch(responseBody -> responseBody.getId().equals(templateFgChild.getId())));

    }
}
