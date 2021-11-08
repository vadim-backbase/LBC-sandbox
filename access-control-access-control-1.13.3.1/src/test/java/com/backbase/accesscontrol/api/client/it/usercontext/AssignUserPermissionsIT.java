package com.backbase.accesscontrol.api.client.it.usercontext;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.client.rest.spec.model.Bound;
import com.backbase.accesscontrol.client.rest.spec.model.ListOfFunctionGroupsWithDataGroups;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalStatus;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationFunctionDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationGenericObjectId;
import com.backbase.accesscontrol.client.rest.spec.model.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.SelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class AssignUserPermissionsIT extends TestDbWireMock {

    private static final String ASSIGN_USERS_PERMISSIONS = "/accessgroups/service-agreements/{id}/users/{userId}/permissions";
    private static final String GET_USERS_URL = "/service-api/v2/users/{id}";

    private final String USER_ID = "userId";
    private FunctionGroup functionGroup1;
    private FunctionGroup functionGroup2;
    private DataGroup dataGroup1;
    private DataGroup dataGroup2;

    @Before
    public void setUp() {
        initServiceAgreementWithAssignablePermissionSet();
        initFunctionGroups();
        initDataGroups();
    }

    @Test
    public void shouldAssignUsersPermissionsWithSelfApprovalPolicies() throws IOException, JSONException {
        Bound bound = new Bound();
        bound.setCurrencyCode("EUR");
        bound.setAmount(BigDecimal.TEN);

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setBusinessFunctionName("Batch - SEPA CT");
        selfApprovalPolicy.setBounds(List.of(bound));

        ListOfFunctionGroupsWithDataGroups newUsersPermissions = new ListOfFunctionGroupsWithDataGroups();
        PresentationFunctionDataGroup presentationFunctionDataGroup = new PresentationFunctionDataGroup();
        presentationFunctionDataGroup.setFunctionGroupId(functionGroup1.getId());
        presentationFunctionDataGroup.setDataGroupIds(List.of(new PresentationGenericObjectId().id(dataGroup1.getId())));
        presentationFunctionDataGroup.selfApprovalPolicies(List.of(selfApprovalPolicy));
        newUsersPermissions.addItemsItem(presentationFunctionDataGroup);

        GetUser user = new GetUser();
        user.setExternalId("username");
        user.setId(USER_ID);
        user.setFullName("userFullName");
        user.legalEntityId(rootLegalEntity.getId());

        String assignPermissionsUrl = new UrlBuilder(ASSIGN_USERS_PERMISSIONS)
            .addPathParameter(rootMsa.getId()).addPathParameter(USER_ID).build();

        String getUserUrl = new UrlBuilder(GET_USERS_URL).addPathParameter(USER_ID)
            .addQueryParameter("skipHierarchyCheck", "true").build();

        addStubGet(getUserUrl, user, 200);

        String response = executeClientRequest(assignPermissionsUrl, HttpMethod.PUT, newUsersPermissions, null,
            FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_EDIT);

        PresentationApprovalStatus status = objectMapper.readValue(response, PresentationApprovalStatus.class);

        assertThat(status.getApprovalStatus(), is(nullValue()));

        UserContext updatedUserContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(USER_ID, rootMsa.getId())
            .get();

        Set<UserAssignedFunctionGroup> userAssignedFunctionGroups = updatedUserContext.getUserAssignedFunctionGroups();

        assertThat(userAssignedFunctionGroups, hasSize(1));
        UserAssignedFunctionGroup userAssignedFunctionGroup = userAssignedFunctionGroups.iterator().next();

        assertThat(userAssignedFunctionGroup.getFunctionGroupId(), equalTo(functionGroup1.getId()));

        assertThat(userAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations(), hasSize(1));
        UserAssignedFunctionGroupCombination assignedCombination = userAssignedFunctionGroup
            .getUserAssignedFunctionGroupCombinations().iterator().next();

        assertThat(assignedCombination.getDataGroupIds(), hasItems(dataGroup1.getId()));
        assertThat(assignedCombination.getSelfApprovalPolicies(), hasSize(1));

        com.backbase.accesscontrol.domain.SelfApprovalPolicy policy = assignedCombination.getSelfApprovalPolicies()
            .iterator().next();

        assertThat(policy.isCanSelfApprove(), is(equalTo(true)));
        assertThat(policy.getFunctionGroupItem().getApplicableFunctionPrivilege().getBusinessFunctionName(), equalTo("Batch - SEPA CT"));
        assertThat(policy.getFunctionGroupItem().getApplicableFunctionPrivilege().getPrivilegeName(), equalTo("approve"));

        assertThat(policy.getApprovalPolicyBounds(), hasSize(1));

        SelfApprovalPolicyBound approvalBound = policy.getApprovalPolicyBounds().iterator().next();

        assertThat(approvalBound.getCurrencyCode(), equalTo("EUR"));
        assertThat(approvalBound.getUpperBound(), comparesEqualTo(BigDecimal.TEN));
    }

    @Test
    public void shouldUpdateUsersPermissionsWithNewPermissionsAndSelfApprovalPolicies() throws IOException, JSONException {
        UserContext userContext = new UserContext(USER_ID, rootMsa.getId());
        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.setDataGroups(Set.of(dataGroup1));
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup1, userContext);
        assignedFunctionGroup.addCombination(combination);
        userContext.getUserAssignedFunctionGroups().add(assignedFunctionGroup);

        userContextJpaRepository.save(userContext);

        Bound bound = new Bound();
        bound.setCurrencyCode("EUR");
        bound.setAmount(BigDecimal.TEN);

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setBusinessFunctionName("SEPA CT");
        selfApprovalPolicy.setBounds(List.of(bound));

        ListOfFunctionGroupsWithDataGroups newUsersPermissions = new ListOfFunctionGroupsWithDataGroups();
        PresentationFunctionDataGroup presentationFunctionDataGroup = new PresentationFunctionDataGroup();
        presentationFunctionDataGroup.setFunctionGroupId(functionGroup2.getId());
        presentationFunctionDataGroup.setDataGroupIds(List.of(
            new PresentationGenericObjectId().id(dataGroup1.getId()),
            new PresentationGenericObjectId().id(dataGroup2.getId())
        ));
        presentationFunctionDataGroup.selfApprovalPolicies(List.of(selfApprovalPolicy));
        newUsersPermissions.addItemsItem(presentationFunctionDataGroup);

        GetUser user = new GetUser();
        user.setExternalId("username");
        user.setId(USER_ID);
        user.setFullName("userFullName");
        user.legalEntityId(rootLegalEntity.getId());

        String assignPermissionsUrl = new UrlBuilder(ASSIGN_USERS_PERMISSIONS)
            .addPathParameter(rootMsa.getId()).addPathParameter(USER_ID).build();

        String getUserUrl = new UrlBuilder(GET_USERS_URL).addPathParameter(USER_ID)
            .addQueryParameter("skipHierarchyCheck", "true").build();

        addStubGet(getUserUrl, user, 200);

        String response = executeClientRequest(assignPermissionsUrl, HttpMethod.PUT, newUsersPermissions, null,
            FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_EDIT);

        PresentationApprovalStatus status = objectMapper.readValue(response, PresentationApprovalStatus.class);

        assertThat(status.getApprovalStatus(), is(nullValue()));

        UserContext updatedUserContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(USER_ID, rootMsa.getId())
            .get();

        assertThat(updatedUserContext.getUserAssignedFunctionGroups(), hasSize(1));

        UserAssignedFunctionGroup userAssignedFunctionGroup = updatedUserContext.getUserAssignedFunctionGroups().iterator().next();

        assertThat(userAssignedFunctionGroup.getFunctionGroupId(), equalTo(functionGroup2.getId()));

        assertThat(userAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations(), hasSize(1));
        UserAssignedFunctionGroupCombination assignedCombination = userAssignedFunctionGroup
            .getUserAssignedFunctionGroupCombinations().iterator().next();

        assertThat(assignedCombination.getDataGroupIds(), hasItems(dataGroup1.getId(), dataGroup2.getId()));
        assertThat(assignedCombination.getSelfApprovalPolicies(), hasSize(1));

        com.backbase.accesscontrol.domain.SelfApprovalPolicy policy = assignedCombination.getSelfApprovalPolicies()
            .iterator().next();

        assertThat(policy.isCanSelfApprove(), is(equalTo(true)));
        assertThat(policy.getFunctionGroupItem().getApplicableFunctionPrivilege().getBusinessFunctionName(), equalTo("SEPA CT"));
        assertThat(policy.getFunctionGroupItem().getApplicableFunctionPrivilege().getPrivilegeName(), equalTo("approve"));

        assertThat(policy.getApprovalPolicyBounds(), hasSize(1));

        SelfApprovalPolicyBound approvalBound = policy.getApprovalPolicyBounds().iterator().next();

        assertThat(approvalBound.getCurrencyCode(), equalTo("EUR"));
        assertThat(approvalBound.getUpperBound(), comparesEqualTo(BigDecimal.TEN));
    }

    @Test
    public void shouldRemoveUsersPermissionsWhenPermissionsSetupIsNotFulfilledWithFunctionGroupAndDataGroups()
        throws IOException, JSONException {
        UserContext userContext = new UserContext(USER_ID, rootMsa.getId());
        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.setDataGroups(Set.of(dataGroup1));
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup1, userContext);
        assignedFunctionGroup.addCombination(combination);
        userContext.getUserAssignedFunctionGroups().add(assignedFunctionGroup);

        userContextJpaRepository.save(userContext);

        ListOfFunctionGroupsWithDataGroups newUsersPermissions = new ListOfFunctionGroupsWithDataGroups();

        GetUser user = new GetUser();
        user.setExternalId("username");
        user.setId(USER_ID);
        user.setFullName("userFullName");
        user.legalEntityId(rootLegalEntity.getId());

        String assignPermissionsUrl = new UrlBuilder(ASSIGN_USERS_PERMISSIONS)
            .addPathParameter(rootMsa.getId()).addPathParameter(USER_ID).build();

        String getUserUrl = new UrlBuilder(GET_USERS_URL).addPathParameter(USER_ID)
            .addQueryParameter("skipHierarchyCheck", "true").build();

        addStubGet(getUserUrl, user, 200);

        String response = executeClientRequest(assignPermissionsUrl, HttpMethod.PUT, newUsersPermissions, null,
            FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_EDIT);

        PresentationApprovalStatus status = objectMapper.readValue(response, PresentationApprovalStatus.class);

        assertThat(status.getApprovalStatus(), is(nullValue()));

        Optional<UserContext> updatedUserContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(USER_ID, rootMsa.getId());

        assertThat(updatedUserContext.isEmpty(), is(equalTo(true)));
    }

    private void initServiceAgreementWithAssignablePermissionSet() {
        rootMsa.getPermissionSetsRegular().add(apsDefaultRegular);
        serviceAgreementJpaRepository.save(rootMsa);
    }

    private void initFunctionGroups() {
        functionGroup1 = new FunctionGroup();
        functionGroup1.setServiceAgreement(rootMsa);
        functionGroup1.setName("fg1");
        functionGroup1.setType(FunctionGroupType.TEMPLATE);
        functionGroup1.setDescription("fg1Description");
        functionGroup1.setAssignablePermissionSet(apsDefaultRegular);
        functionGroup1.setPermissions(createPermissions(Set.of("SEPA CT", "Batch - SEPA CT"), "approve"));

        functionGroup2 = new FunctionGroup();
        functionGroup2.setServiceAgreement(rootMsa);
        functionGroup2.setName("fg2");
        functionGroup2.setType(FunctionGroupType.TEMPLATE);
        functionGroup2.setDescription("fg2Description");
        functionGroup2.setAssignablePermissionSet(apsDefaultRegular);
        functionGroup2.setPermissions(createPermissions(Set.of("SEPA CT"), "approve"));

        functionGroup1 = functionGroupJpaRepository.save(functionGroup1);
        functionGroup2 = functionGroupJpaRepository.save(functionGroup2);
    }

    private void initDataGroups() {
        dataGroup1 = createDataGroup("dg1", "ARRANGEMENTS", "dg1", rootMsa);
        dataGroup2 = createDataGroup("dg2", "ARRANGEMENTS", "dg2", rootMsa);

        dataGroup1 = dataGroupJpaRepository.save(dataGroup1);
        dataGroup2 = dataGroupJpaRepository.save(dataGroup2);
    }

    private Set<GroupedFunctionPrivilege> createPermissions(Set<String> functionNames, String privilege) {
        return businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, null, List.of(privilege))
            .stream()
            .map(afpId -> {
                GroupedFunctionPrivilege groupedFunctionPrivilege = new GroupedFunctionPrivilege();
                groupedFunctionPrivilege.setApplicableFunctionPrivilegeId(afpId);
                return groupedFunctionPrivilege;
            })
            .collect(Collectors.toSet());
    }
}