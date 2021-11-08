package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.FunctionGroupItemId;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.SelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class UpdateFunctionGroupWithAssignedSelfApprovalPoliciesIT extends TestDbWireMock {

    private static final String FUNCTION_GROUP_URL = "/accessgroups/function-groups/{id}";

    private FunctionGroup functionGroup;
    private ServiceAgreement customSA;
    private UserContext userContext;
    private LegalEntity legalEntity;

    private final String manageFGAfpView = "59";
    private final String manageFGAfpEdit = "61";
    private final String manageFGAfpApprove = "63";
    private final String manageFunctionGroupsBFId = "1020";

    private final String batchSepaAfpView = "119";
    private final String batchSepaAfpApprove = "123";
    private final String batchSepaBFId = "1037";

    @Before
    public void setUp() {
        setupServiceAgreementWithAssignablePermissionSet();

        setupFunctionGroupWithPermissions();

        setUpUserPermissionsWithSelfApprovalPolicies();
    }

    @Test
    public void shouldUpdateFunctionGroupAndRemoveManageFunctionGroupSelfApprovalPolicy() throws Exception {
        Privilege viewPrivilege = new Privilege().withPrivilege("view");
        Privilege approvePrivilege = new Privilege().withPrivilege("approve");

        Permission permission = new Permission();
        permission.setFunctionId(batchSepaBFId);
        permission.setAssignedPrivileges(List.of(viewPrivilege, approvePrivilege));

        FunctionGroupByIdPutRequestBody functionGroupBody = new FunctionGroupByIdPutRequestBody()
            .withName("updatedName")
            .withDescription("updatedDescription")
            .withServiceAgreementId(customSA.getId())
            .withPermissions(List.of(permission));

        UrlBuilder updateFunctionGroupUrl = new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId());

        String requestAsString = objectMapper.writeValueAsString(functionGroupBody);

        executeClientRequestWithContext(updateFunctionGroupUrl.build(), HttpMethod.PUT, requestAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT, userContext, legalEntity.getId());

        FunctionGroup updatedFunctionGroup = functionGroupJpaRepository
            .findByIdWithPermissions(functionGroup.getId()).get();

        Set<String> afpIds = extractAssignedPermissionsAfpIds(updatedFunctionGroup);

        assertThat(afpIds, hasSize(2));
        assertThat(afpIds, containsInAnyOrder(batchSepaAfpApprove, batchSepaAfpView));

        List<UserAssignedFunctionGroupCombination> combinations = userAssignedCombinationRepository
            .findAllCombinationsByFunctionGroupId(functionGroup.getId());

        assertThat(combinations, hasSize(1));

        UserAssignedFunctionGroupCombination assignedCombination = combinations.get(0);
        Set<SelfApprovalPolicy> selfApprovalPolicies = assignedCombination.getSelfApprovalPolicies();

        assertThat(selfApprovalPolicies, hasSize(1));
        SelfApprovalPolicy policy = selfApprovalPolicies.iterator().next();

        assertThat(policy.isCanSelfApprove(), is(true));
        ApplicableFunctionPrivilege privilege = policy.getFunctionGroupItem().getApplicableFunctionPrivilege();

        assertThat(privilege.getPrivilegeName(), equalTo("approve"));
        assertThat(privilege.getBusinessFunctionName(), equalTo("Batch - SEPA CT"));

        assertThat(policy.getApprovalPolicyBounds(), hasSize(1));
        assertThat(policy.getApprovalPolicyBounds().iterator().next().getCurrencyCode(), equalTo("USD"));
        assertThat(policy.getApprovalPolicyBounds().iterator().next().getUpperBound(), comparesEqualTo(BigDecimal.valueOf(20)));
    }

    @Test
    public void shouldUpdateFunctionGroupAndRemoveSepaSelfApprovalPolicyWithBound() throws Exception {
        Privilege viewPrivilege = new Privilege().withPrivilege("view");
        Privilege approvePrivilege = new Privilege().withPrivilege("approve");

        Permission permission = new Permission();
        permission.setFunctionId(manageFunctionGroupsBFId);
        permission.setAssignedPrivileges(List.of(viewPrivilege, approvePrivilege));

        FunctionGroupByIdPutRequestBody functionGroupBody = new FunctionGroupByIdPutRequestBody()
            .withName("updatedName")
            .withDescription("updatedDescription")
            .withServiceAgreementId(customSA.getId())
            .withPermissions(List.of(permission));

        UrlBuilder updateFunctionGroupUrl = new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId());

        String requestAsString = objectMapper.writeValueAsString(functionGroupBody);

        executeClientRequestWithContext(updateFunctionGroupUrl.build(), HttpMethod.PUT, requestAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT, userContext, legalEntity.getId());

        FunctionGroup updatedFunctionGroup = functionGroupJpaRepository
            .findByIdWithPermissions(functionGroup.getId()).get();

        Set<String> afpIds = extractAssignedPermissionsAfpIds(updatedFunctionGroup);

        assertThat(afpIds, hasSize(2));
        assertThat(afpIds, containsInAnyOrder(manageFGAfpApprove, manageFGAfpView));

        List<UserAssignedFunctionGroupCombination> combinations = userAssignedCombinationRepository
            .findAllCombinationsByFunctionGroupId(functionGroup.getId());

        assertThat(combinations, hasSize(1));

        UserAssignedFunctionGroupCombination assignedCombination = combinations.get(0);
        Set<SelfApprovalPolicy> selfApprovalPolicies = assignedCombination.getSelfApprovalPolicies();

        assertThat(selfApprovalPolicies, hasSize(1));
        SelfApprovalPolicy policy = selfApprovalPolicies.iterator().next();

        assertThat(policy.isCanSelfApprove(), is(false));
        ApplicableFunctionPrivilege privilege = policy.getFunctionGroupItem().getApplicableFunctionPrivilege();

        assertThat(privilege.getPrivilegeName(), equalTo("approve"));
        assertThat(privilege.getBusinessFunctionName(), equalTo("Manage Function Groups"));
        assertThat(policy.getApprovalPolicyBounds(), empty());
    }

    @Test
    public void shouldUpdateFunctionGroupAndNotRemoveAssignedSelfApprovalPolicies() throws Exception{
        Privilege approvePrivilege = new Privilege().withPrivilege("approve");

        Permission manageFGApprovePermission = new Permission();
        manageFGApprovePermission.setFunctionId(manageFunctionGroupsBFId);
        manageFGApprovePermission.setAssignedPrivileges(List.of(approvePrivilege));

        Permission batchSepaApprovePermission = new Permission();
        batchSepaApprovePermission.setFunctionId(batchSepaBFId);
        batchSepaApprovePermission.setAssignedPrivileges(List.of(approvePrivilege));

        FunctionGroupByIdPutRequestBody functionGroupBody = new FunctionGroupByIdPutRequestBody()
            .withName("updatedName")
            .withDescription("updatedDescription")
            .withServiceAgreementId(customSA.getId())
            .withPermissions(List.of(manageFGApprovePermission, batchSepaApprovePermission));

        UrlBuilder updateFunctionGroupUrl = new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId());

        String requestAsString = objectMapper.writeValueAsString(functionGroupBody);

        executeClientRequestWithContext(updateFunctionGroupUrl.build(), HttpMethod.PUT, requestAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT, userContext, legalEntity.getId());

        FunctionGroup updatedFunctionGroup = functionGroupJpaRepository
            .findByIdWithPermissions(functionGroup.getId()).get();

        Set<String> afpIds = extractAssignedPermissionsAfpIds(updatedFunctionGroup);

        assertThat(afpIds, hasSize(2));
        assertThat(afpIds, containsInAnyOrder(manageFGAfpApprove, batchSepaAfpApprove));

        List<UserAssignedFunctionGroupCombination> combinations = userAssignedCombinationRepository
            .findAllCombinationsByFunctionGroupId(functionGroup.getId());

        assertThat(combinations, hasSize(1));

        UserAssignedFunctionGroupCombination assignedCombination = combinations.get(0);
        Set<SelfApprovalPolicy> selfApprovalPolicies = assignedCombination.getSelfApprovalPolicies();

        assertThat(selfApprovalPolicies, hasSize(2));
        SelfApprovalPolicy sepaPolicy = findPolicyByAfpId(selfApprovalPolicies, batchSepaAfpApprove);

        assertThat(sepaPolicy.isCanSelfApprove(), is(true));
        ApplicableFunctionPrivilege batchSepaAfp = sepaPolicy.getFunctionGroupItem().getApplicableFunctionPrivilege();

        assertThat(batchSepaAfp.getPrivilegeName(), equalTo("approve"));
        assertThat(batchSepaAfp.getBusinessFunctionName(), equalTo("Batch - SEPA CT"));

        assertThat(sepaPolicy.getApprovalPolicyBounds(), hasSize(1));
        assertThat(sepaPolicy.getApprovalPolicyBounds().iterator().next().getCurrencyCode(), equalTo("USD"));
        assertThat(sepaPolicy.getApprovalPolicyBounds().iterator().next().getUpperBound(), comparesEqualTo(BigDecimal.valueOf(20)));

        SelfApprovalPolicy manageFGPolicy = findPolicyByAfpId(selfApprovalPolicies, manageFGAfpApprove);

        assertThat(manageFGPolicy.isCanSelfApprove(), is(false));
        ApplicableFunctionPrivilege manageFGAfp = manageFGPolicy.getFunctionGroupItem().getApplicableFunctionPrivilege();

        assertThat(manageFGAfp.getPrivilegeName(), equalTo("approve"));
        assertThat(manageFGAfp.getBusinessFunctionName(), equalTo("Manage Function Groups"));
        assertThat(manageFGPolicy.getApprovalPolicyBounds(), empty());
    }

    @Test
    public void shouldUpdateFunctionGroupAndRemoveAssignedSelfApprovalPoliciesWithCombination() throws Exception{
        Privilege privilegeView = new Privilege().withPrivilege("view");

        Permission manageFGViewPermission = new Permission();
        manageFGViewPermission.setFunctionId(manageFunctionGroupsBFId);
        manageFGViewPermission.setAssignedPrivileges(List.of(privilegeView));

        Permission batchSepaViewPermission = new Permission();
        batchSepaViewPermission.setFunctionId(batchSepaBFId);
        batchSepaViewPermission.setAssignedPrivileges(List.of(privilegeView));

        FunctionGroupByIdPutRequestBody functionGroupBody = new FunctionGroupByIdPutRequestBody()
            .withName("updatedName")
            .withDescription("updatedDescription")
            .withServiceAgreementId(customSA.getId())
            .withPermissions(List.of(manageFGViewPermission, batchSepaViewPermission));

        UrlBuilder updateFunctionGroupUrl = new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId());

        String requestAsString = objectMapper.writeValueAsString(functionGroupBody);

        executeClientRequestWithContext(updateFunctionGroupUrl.build(), HttpMethod.PUT, requestAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT, userContext, legalEntity.getId());

        FunctionGroup updatedFunctionGroup = functionGroupJpaRepository
            .findByIdWithPermissions(functionGroup.getId()).get();

        Set<String> afpIds = extractAssignedPermissionsAfpIds(updatedFunctionGroup);

        assertThat(afpIds, hasSize(2));
        assertThat(afpIds, containsInAnyOrder(manageFGAfpView, batchSepaAfpView));

        List<UserAssignedFunctionGroupCombination> combinations = userAssignedCombinationRepository
            .findAllCombinationsByFunctionGroupId(functionGroup.getId());

        assertThat(combinations, empty());
    }

    @Test
    public void shouldUpdateFunctionGroupAndRemoveOnlyAssignedPoliciesFromCombinationWhenDataGroupIsAssignedInCombination()
        throws Exception {
        DataGroup dataGroup = new DataGroup();
        dataGroup.setDescription("dgDescription");
        dataGroup.setName("dgName");
        dataGroup.setServiceAgreement(customSA);
        dataGroup.setDataItemType("CUSTOMERS");

        dataGroup = dataGroupJpaRepository.save(dataGroup);

        UserAssignedFunctionGroup assignedFunctionGroup = userContext.getUserAssignedFunctionGroups().iterator().next();
        UserAssignedFunctionGroupCombination combination = assignedFunctionGroup.getUserAssignedFunctionGroupCombinations()
            .iterator().next();

        combination.getDataGroups().add(dataGroup);

        userAssignedCombinationRepository.save(combination);

        Privilege privilegeView = new Privilege().withPrivilege("view");

        Permission manageFGViewPermission = new Permission();
        manageFGViewPermission.setFunctionId(manageFunctionGroupsBFId);
        manageFGViewPermission.setAssignedPrivileges(List.of(privilegeView));

        Permission batchSepaViewPermission = new Permission();
        batchSepaViewPermission.setFunctionId(batchSepaBFId);
        batchSepaViewPermission.setAssignedPrivileges(List.of(privilegeView));

        FunctionGroupByIdPutRequestBody functionGroupBody = new FunctionGroupByIdPutRequestBody()
            .withName("updatedName")
            .withDescription("updatedDescription")
            .withServiceAgreementId(customSA.getId())
            .withPermissions(List.of(manageFGViewPermission, batchSepaViewPermission));

        UrlBuilder updateFunctionGroupUrl = new UrlBuilder(FUNCTION_GROUP_URL).addPathParameter(functionGroup.getId());

        String requestAsString = objectMapper.writeValueAsString(functionGroupBody);

        executeClientRequestWithContext(updateFunctionGroupUrl.build(), HttpMethod.PUT, requestAsString, "user",
            ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_EDIT, userContext, legalEntity.getId());

        FunctionGroup updatedFunctionGroup = functionGroupJpaRepository
            .findByIdWithPermissions(functionGroup.getId()).get();

        Set<String> afpIds = extractAssignedPermissionsAfpIds(updatedFunctionGroup);

        assertThat(afpIds, hasSize(2));
        assertThat(afpIds, containsInAnyOrder(manageFGAfpView, batchSepaAfpView));

        List<UserAssignedFunctionGroupCombination> combinations = userAssignedCombinationRepository
            .findAllCombinationsByFunctionGroupId(functionGroup.getId());

        assertThat(combinations, hasSize(1));
        assertThat(combinations.iterator().next().getSelfApprovalPolicies(), empty());
        assertThat(combinations.iterator().next().getDataGroups(), hasSize(1));

        DataGroup assignedDataGroup = combinations.iterator().next().getDataGroups().iterator().next();

        assertThat(assignedDataGroup.getName(), equalTo("dgName"));
        assertThat(assignedDataGroup.getDescription(), equalTo("dgDescription"));
    }

    private void setupServiceAgreementWithAssignablePermissionSet() {
        AssignablePermissionSet permissionSet = new AssignablePermissionSet();
        permissionSet.setName("permissionSet");
        permissionSet.setType(AssignablePermissionType.CUSTOM);
        permissionSet.setDescription("testPermissionSet");
        permissionSet.setPermissions(Set.of(manageFGAfpView, manageFGAfpEdit, manageFGAfpApprove,
            batchSepaAfpView, batchSepaAfpApprove));

        permissionSet = assignablePermissionSetJpaRepository.save(permissionSet);

        legalEntity = new LegalEntity();
        legalEntity.setName("leName");
        legalEntity.setExternalId("leExternalId");
        legalEntity.setParent(rootLegalEntity);
        legalEntity.setType(LegalEntityType.CUSTOMER);

        legalEntityJpaRepository.save(legalEntity);

        customSA = new ServiceAgreement();
        customSA.setName("saName");
        customSA.setExternalId("saExternalId");
        customSA.setDescription("description");
        customSA.setCreatorLegalEntity(rootLegalEntity);
        customSA.addParticipant(ServiceAgreementUtil.createParticipant(true, false, legalEntity));
        customSA.addParticipant(ServiceAgreementUtil.createParticipant(true, true, rootLegalEntity));
        customSA.setMaster(false);
        customSA.setPermissionSetsRegular(Sets.newHashSet(permissionSet));

        customSA = serviceAgreementJpaRepository.save(customSA);
    }

    private void setupFunctionGroupWithPermissions() {
        functionGroup = new FunctionGroup();
        functionGroup.setName("fgName");
        functionGroup.setDescription("fgDescription");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(customSA);
        Set<GroupedFunctionPrivilege> groupedFunctionPrivilegesForFgCsa = getFunctionPrivilegesForFgInCustomSA();
        functionGroup.setPermissions(groupedFunctionPrivilegesForFgCsa);

        functionGroup = functionGroupJpaRepository.save(functionGroup);
    }

    private void setUpUserPermissionsWithSelfApprovalPolicies() {
        String functionGroupId = functionGroup.getId();
        userContext = new UserContext();
        userContext.setUserId("userId");
        userContext.setServiceAgreementId(customSA.getId());

        FunctionGroupItemEntity manageFGItemEntity = new FunctionGroupItemEntity();
        manageFGItemEntity.setFunctionGroupItemId(new FunctionGroupItemId(functionGroupId, manageFGAfpApprove));
        manageFGItemEntity = functionGroupItemEntityJpaRepository.save(manageFGItemEntity);

        SelfApprovalPolicy manageFGSelfApprovalPolicy = new SelfApprovalPolicy();
        manageFGSelfApprovalPolicy.setCanSelfApprove(false);
        manageFGSelfApprovalPolicy.setFunctionGroupItem(manageFGItemEntity);

        FunctionGroupItemEntity sepaFGItemEntity = new FunctionGroupItemEntity();
        sepaFGItemEntity.setFunctionGroupItemId(new FunctionGroupItemId(functionGroupId, batchSepaAfpApprove));
        sepaFGItemEntity = functionGroupItemEntityJpaRepository.save(sepaFGItemEntity);

        SelfApprovalPolicyBound selfApprovalPolicyBound = new SelfApprovalPolicyBound();
        selfApprovalPolicyBound.setUpperBound(BigDecimal.valueOf(20));
        selfApprovalPolicyBound.setCurrencyCode("USD");

        SelfApprovalPolicy sepaSelfApprovalPolicy = new SelfApprovalPolicy();
        sepaSelfApprovalPolicy.setCanSelfApprove(true);
        sepaSelfApprovalPolicy.setFunctionGroupItem(sepaFGItemEntity);
        sepaSelfApprovalPolicy.addBounds(Set.of(selfApprovalPolicyBound));

        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.addPolicies(Set.of(manageFGSelfApprovalPolicy, sepaSelfApprovalPolicy));

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup();
        userAssignedFunctionGroup.setUserContext(userContext);
        userAssignedFunctionGroup.setFunctionGroupId(functionGroupId);
        userAssignedFunctionGroup.setFunctionGroup(functionGroup);
        userAssignedFunctionGroup.addCombination(combination);
        userContext.getUserAssignedFunctionGroups().add(userAssignedFunctionGroup);

        userContext = userContextJpaRepository.save(userContext);
    }

    private SelfApprovalPolicy findPolicyByAfpId(Collection<SelfApprovalPolicy> policies, String afpId) {
        return policies.stream()
            .filter(p -> p.getFunctionGroupItem().getApplicableFunctionPrivilege().getId().equals(afpId))
            .findFirst()
            .get();
    }

    private Set<GroupedFunctionPrivilege> getFunctionPrivilegesForFgInCustomSA() {
        return Stream.of(manageFGAfpView, manageFGAfpEdit, manageFGAfpApprove, batchSepaAfpView, batchSepaAfpApprove)
            .map(afpId -> new GroupedFunctionPrivilege(functionGroup, afpId))
            .collect(Collectors.toSet());
    }

    private Set<String> extractAssignedPermissionsAfpIds(FunctionGroup functionGroup) {
        return functionGroup.getPermissions()
            .stream().map(FunctionGroupItem::getApplicableFunctionPrivilegeId)
            .collect(Collectors.toSet());
    }
}
