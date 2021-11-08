package com.backbase.accesscontrol.api.client.it.user;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_FUNCTION_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.UsersController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link UsersController#getUserPrivileges}
 */
public class GetUserPrivilegesIT extends TestDbWireMock {

    private String url = "/accessgroups/users/user-privileges";

    private String userId;

    private BusinessFunction serviceAgreementBusinessFunction;

    private ServiceAgreement serviceAgreement;

    @Before
    public void setUp() {

        userId = contextUserId;
        ApplicableFunctionPrivilege viewServiceAgreement = businessFunctionCache.getApplicableFunctionPrivilegeById(
            businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                    SERVICE_AGREEMENT_FUNCTION_NAME, null, Lists.newArrayList("view"))
                .stream().findFirst().get());
        ApplicableFunctionPrivilege createServiceAgreement = businessFunctionCache.getApplicableFunctionPrivilegeById(
            businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                    SERVICE_AGREEMENT_FUNCTION_NAME, null, Lists.newArrayList("create"))
                .stream().findFirst().get());

        serviceAgreementBusinessFunction = viewServiceAgreement.getBusinessFunction();
        // create SA
        serviceAgreement = rootMsa;

        //save function group
        GroupedFunctionPrivilege viewEntitlementsWithLimit = getGroupedFunctionPrivilege(null, viewServiceAgreement, null);
        GroupedFunctionPrivilege createEntitlementsWitLimit = getGroupedFunctionPrivilege(null, createServiceAgreement,
            null);
        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit,
                    createEntitlementsWitLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        // create data group
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement,
            Lists.newArrayList("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        UserContext userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(userId, serviceAgreement.getId())
            .orElseGet(() -> userContextJpaRepository.save(new UserContext(userId, serviceAgreement.getId())));

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedFunctionGroup,
            userContext);
        userAssignedFunctionGroup = userAssignedFunctionGroupJpaRepository.saveAndFlush(userAssignedFunctionGroup);

        userAssignedCombinationRepository
            .saveAndFlush(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));
    }

    @Test
    public void shouldGetUserPrivileges() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(url)
                .addQueryParameter("functionName", serviceAgreementBusinessFunction.getFunctionName())
                .addQueryParameter("resourceName", serviceAgreementBusinessFunction.getResourceName())
                .build(), HttpMethod.GET, "USER"
        );

        List<PrivilegesGetResponseBody> list = readValue(contentAsString,
            new TypeReference<List<PrivilegesGetResponseBody>>() {
            });
        assertThat(list.size(), is(2));
        assertThat(list.stream().map(PrivilegesGetResponseBody::getPrivilege)
            .collect(Collectors.toList()), containsInAnyOrder("view", "create"));
    }

}
