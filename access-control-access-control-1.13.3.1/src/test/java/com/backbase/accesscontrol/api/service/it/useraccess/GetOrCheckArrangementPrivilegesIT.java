package com.backbase.accesscontrol.api.service.it.useraccess;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class GetOrCheckArrangementPrivilegesIT extends TestDbWireMock {

    private static final String GET_ARRANGEMENT_PRIVILEGES_URL = "/accessgroups/users/privileges/arrangements";

    private UserContext userContext;
    private FunctionGroup functionGroup;
    private DataGroup dataGroup;
    private DataGroup dataGroup2;
    private String userId = "001";
    private ServiceAgreement serviceAgreement;

    @Before
    public void setUp() {

        ApplicableFunctionPrivilege apfBf1020View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "view");
        ApplicableFunctionPrivilege apfBf1002Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1002", "create");
        ApplicableFunctionPrivilege apfBf1003View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "view");

        serviceAgreement = rootMsa;

        //save function group
        GroupedFunctionPrivilege viewEntitlementsWithLimit = getGroupedFunctionPrivilege(null, apfBf1020View, null);
        GroupedFunctionPrivilege createEntitlementsWitLimit = getGroupedFunctionPrivilege(null, apfBf1002Create, null);
        GroupedFunctionPrivilege viewProductsWithLimit = getGroupedFunctionPrivilege(null, apfBf1003View, null);

        functionGroup = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit,
                    createEntitlementsWitLimit,
                    viewProductsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        // create data group
        dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(newHashSet("00001", "00002", "00003"));
        dataGroupJpaRepository.saveAndFlush(dataGroup);
        dataGroup2 = DataGroupUtil.createDataGroup("name2", "ARRANGEMENTS", "desc2", serviceAgreement);
        dataGroup2.setDataItemIds(newHashSet("00004", "00005", "00006"));
        dataGroupJpaRepository.saveAndFlush(dataGroup2);

        userContext = new UserContext(userId, serviceAgreement.getId());

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(functionGroup, userContext);
        uafg.setUserAssignedFunctionGroupCombinations(newHashSet(
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), uafg),
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup2.getId()), uafg)));

        userContext.setUserAssignedFunctionGroups(newHashSet(uafg));
        userContext = userContextJpaRepository.save(userContext);

    }

    @Test
    public void shouldReturnExpectedDataTypes() throws Exception {
        String resourceName = "Entitlements";
        String functionName = "Manage Function Groups";

        String returnedResponse = executeRequest(
            new UrlBuilder(GET_ARRANGEMENT_PRIVILEGES_URL)
                .addQueryParameter("userId", userId)
                .addQueryParameter("serviceAgreementId", serviceAgreement.getId())
                .addQueryParameter("resourceName", resourceName)
                .addQueryParameter("functionName", functionName)
                .build(), "",
            HttpMethod.GET);

        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody> responseData = readValue(
            returnedResponse,
            new TypeReference<List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody>>() {
            });

        assertThat(responseData,
            Matchers.containsInAnyOrder(
                hasProperty("arrangementId", is("00001")),
                hasProperty("arrangementId", is("00002")),
                hasProperty("arrangementId", is("00003")),
                hasProperty("arrangementId", is("00004")),
                hasProperty("arrangementId", is("00005")),
                hasProperty("arrangementId", is("00006"))
            )
        );

        assertThat(responseData.get(0), instanceOf(
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody.class));
        assertThat(responseData.get(1), instanceOf(
            com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody.class));
    }

}
