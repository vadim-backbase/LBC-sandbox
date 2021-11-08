package com.backbase.accesscontrol.api.client.it.usercontext;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.client.rest.spec.model.ApprovalStatus;
import com.backbase.accesscontrol.client.rest.spec.model.Bound;
import com.backbase.accesscontrol.client.rest.spec.model.ListOfFunctionGroupsWithDataGroups;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalStatus;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationFunctionDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationGenericObjectId;
import com.backbase.accesscontrol.client.rest.spec.model.SelfApprovalPolicy;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostApprovalResponse;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class AssignUserPermissionsApprovalOnIT extends TestDbWireMock {

    private static final String ASSIGN_USERS_PERMISSIONS = "/accessgroups/service-agreements/{id}/users/{userId}/permissions";
    private static final String GET_USERS_URL = "/service-api/v2/users/{id}";
    private static final String getApprovalUrl = "/service-api/v2/approvals";

    private final String USER_ID = "userId";
    private FunctionGroup functionGroup1;
    private DataGroup dataGroup1;
    private DataGroup dataGroup2;

    @Before
    public void setUp() {
        initServiceAgreementWithAssignablePermissionSet();
        initFunctionGroups();
        initDataGroups();
    }

    @Test
    public void shouldSaveUsersPermissionsWithSelfApprovalPoliciesToApprovalTables() throws IOException, JSONException {
        Bound bound1 = new Bound();
        bound1.setCurrencyCode("EUR");
        bound1.setAmount(BigDecimal.TEN);

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setBusinessFunctionName("SEPA CT");
        selfApprovalPolicy.setBounds(List.of(bound1));

        ListOfFunctionGroupsWithDataGroups newUsersPermissions = new ListOfFunctionGroupsWithDataGroups();
        PresentationFunctionDataGroup presentationFunctionDataGroup = new PresentationFunctionDataGroup();
        presentationFunctionDataGroup.setFunctionGroupId(functionGroup1.getId());
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

        ApprovalDto approval = new ApprovalDto()
            .id("approvalId")
            .serviceAgreementId(rootMsa.getId())
            .status(com.backbase.dbs.approval.api.client.v2.model.ApprovalStatus.PENDING);
        PresentationPostApprovalResponse approvalResponse = new PresentationPostApprovalResponse().approval(approval);

        String assignPermissionsUrl = new UrlBuilder(ASSIGN_USERS_PERMISSIONS)
            .addPathParameter(rootMsa.getId()).addPathParameter(USER_ID).build();

        String getUserUrl = new UrlBuilder(GET_USERS_URL).addPathParameter(USER_ID)
            .addQueryParameter("skipHierarchyCheck", "true").build();

        addStubGet(getUserUrl, user, 200);

        addStubPost(new UrlBuilder(getApprovalUrl).build(), approvalResponse, 200);

        String response = executeClientRequest(assignPermissionsUrl, HttpMethod.PUT, newUsersPermissions, null,
            FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_EDIT);

        PresentationApprovalStatus status = objectMapper.readValue(response, PresentationApprovalStatus.class);

        assertThat(status.getApprovalStatus(), equalTo(ApprovalStatus.PENDING));

        ApprovalUserContext approvalUserContext = approvalUserContextJpaRepository
            .findUserContextByUserIdServiceAgreementIdWithAssignedPermissions(USER_ID, rootMsa.getId()).get();

        assertThat(approvalUserContext.getApprovalUserContextAssignFunctionGroups(), hasSize(1));

        ApprovalUserContextAssignFunctionGroup assignFunctionGroup = approvalUserContext.getApprovalUserContextAssignFunctionGroups()
            .iterator().next();

        assertThat(assignFunctionGroup.getFunctionGroupId(), equalTo(functionGroup1.getId()));
        assertThat(assignFunctionGroup.getDataGroups(), containsInAnyOrder(dataGroup1.getId(), dataGroup2.getId()));

        assertThat(assignFunctionGroup.getApprovalSelfApprovalPolicies(), hasSize(1));

        ApprovalSelfApprovalPolicy policy = assignFunctionGroup.getApprovalSelfApprovalPolicies().iterator().next();

        assertThat(policy.isCanSelfApprove(), is(equalTo(true)));
        assertThat(policy.getFunctionGroupItem().getApplicableFunctionPrivilege().getBusinessFunctionName(), equalTo("SEPA CT"));
        assertThat(policy.getFunctionGroupItem().getApplicableFunctionPrivilege().getPrivilegeName(), equalTo("approve"));

        assertThat(policy.getApprovalSelfApprovalPolicyBounds(), hasSize(1));

        ApprovalSelfApprovalPolicyBound bound = policy.getApprovalSelfApprovalPolicyBounds().iterator().next();

        assertThat(bound.getCurrencyCode(), equalTo("EUR"));
        assertThat(bound.getUpperBound(), comparesEqualTo(BigDecimal.TEN));
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

        functionGroup1 = functionGroupJpaRepository.save(functionGroup1);
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
