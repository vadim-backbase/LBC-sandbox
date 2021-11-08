package com.backbase.accesscontrol.api.client.it.approval;

import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.FunctionGroupItemId;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.approval.api.client.v2.model.PolicyDetailsDto;
import com.backbase.dbs.approval.api.client.v2.model.PolicyItemDetailsDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.dbs.approval.api.client.v2.model.RecordDto;
import com.backbase.dbs.approval.api.client.v2.model.RecordStatus;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalLogItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupsDataGroupsPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Bound;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationSelfApprovalPolicy;
import com.google.common.collect.Sets;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class GetPermissionsApprovalDetailsItemIT extends TestDbWireMock {

    private static final String USER = "USER";

    private String approvalId = "b3e4692d-6772-4fb2-9446-b8f4607f49b1";

    @Test
    public void shouldGetPermissionsApprovalById() throws Exception {
        ApplicableFunctionPrivilege afpManageFg = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "view");

        LinkedHashSet<GroupedFunctionPrivilege> groupedFunctionPrivilegeList1 = new LinkedHashSet<>();
        groupedFunctionPrivilegeList1
            .add(GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege(null, afpManageFg, null));

        FunctionGroup functionGroup = new FunctionGroup()
            .withType(FunctionGroupType.DEFAULT)
            .withName("fg")
            .withDescription("desc")
            .withServiceAgreement(rootMsa)
            .withPermissions(groupedFunctionPrivilegeList1);
        functionGroup = functionGroupJpaRepository.save(functionGroup);

        FunctionGroup functionGroup2 = new FunctionGroup()
                .withType(FunctionGroupType.DEFAULT)
                .withName("fg2")
                .withDescription("desc2")
                .withServiceAgreement(rootMsa)
                .withPermissions(groupedFunctionPrivilegeList1);
        functionGroup2 = functionGroupJpaRepository.save(functionGroup2);

        DataGroup dataGroup = DataGroupUtil
            .createDataGroup("dg", "ARRANGEMENTS", "desc", rootMsa);
        dataGroup.setDataItemIds(Collections.singleton("item-1"));
        dataGroup = dataGroupJpaRepository.save(dataGroup);

        DataGroup dataGroup2 = DataGroupUtil
            .createDataGroup("dg2", "ARRANGEMENTS", "desc2", rootMsa);
        dataGroup2.setDataItemIds(Collections.singleton("item-2"));
        dataGroup2 = dataGroupJpaRepository.save(dataGroup2);

        ApplicableFunctionPrivilege afp = applicableFunctionPrivilegeJpaRepository.findById("41").orElseThrow();

        FunctionGroupItemEntity fge = (new FunctionGroupItemEntity(new FunctionGroupItemId(functionGroup.getId(), afp.getId()), functionGroup, afp));
        fge = functionGroupItemEntityRepository.save(fge);

        ApprovalSelfApprovalPolicy approvalSelfApprovalPolicy = new ApprovalSelfApprovalPolicy();
        approvalSelfApprovalPolicy.setFunctionGroupItem(fge);
        approvalSelfApprovalPolicy.setCanSelfApprove(true);
        ApprovalSelfApprovalPolicyBound bound = new ApprovalSelfApprovalPolicyBound();
        bound.setCurrencyCode("EUR");
        bound.setUpperBound(new BigDecimal(10000));        
        approvalSelfApprovalPolicy.addBounds(Sets.newHashSet(bound));
        
        ApprovalUserContext approvalUserContext = new ApprovalUserContext();
        approvalUserContext.setServiceAgreementId(rootMsa.getId());
        approvalUserContext.setLegalEntityId(rootLegalEntity.getId());
        approvalUserContext.setUserId(contextUserId);
        approvalUserContext.setApprovalId(approvalId);
        approvalUserContext = approvalUserContextJpaRepository.save(approvalUserContext);

        ApprovalUserContextAssignFunctionGroup assignFunctionGroup = new ApprovalUserContextAssignFunctionGroup()
            .withApprovalUserContext(approvalUserContext)
            .withFunctionGroupId(functionGroup.getId())
            .withDataGroups(Sets.newHashSet(dataGroup.getId(), dataGroup2.getId()));
        assignFunctionGroup.addPolicies(Sets.newHashSet(approvalSelfApprovalPolicy));
        approvalUserContextAssignFunctionGroupJpaRepository.save(assignFunctionGroup);

        approvalUserContextAssignFunctionGroupJpaRepository.save(new ApprovalUserContextAssignFunctionGroup()
                .withApprovalUserContext(approvalUserContext)
                .withFunctionGroupId(functionGroup2.getId()));

        String serviceAgreementId = rootMsa.getId();
        String userId = contextUserId;

        UserContext userContext = new UserContext(userId, serviceAgreementId);
        userContext = userContextJpaRepository.save(userContext);

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(uafg);

        UserAssignedFunctionGroup uafg2 = new UserAssignedFunctionGroup(functionGroup2, userContext);
        userAssignedFunctionGroupJpaRepository.save(uafg2);

        String getUserUrl = "/service-api/v2/users/{id}";
        String getApprovalUrl = "/service-api/v2/approvals/{approvalId}";
        String url = "/accessgroups/approvals/{approvalId}/permissions";

        GetUser userResponse = new GetUser();
        userResponse.setId(userId);
        userResponse.setFullName("userFullName");
        userResponse.setLegalEntityId("leId");
        userResponse.setExternalId(USER);

        GetUser userResponse1 = new GetUser();
        userResponse1.setId(userId);
        userResponse1.setFullName("userFullName");
        userResponse1.setLegalEntityId("le-2");
        userResponse1.setExternalId("username");

        addStubGet(new UrlBuilder(getUserUrl)
                .addPathParameter(userId)
                .addQueryParameter("skipHierarchyCheck","true")
                .build(),
            userResponse1, 200);


        Date createdAt = new Date();

        PolicyDetailsDto presentationPolicyDto = new PolicyDetailsDto();
        presentationPolicyDto.setName("policy1");
        presentationPolicyDto.setItems(singletonList(new PolicyItemDetailsDto()
            .numberOfApprovals(2)));

        PresentationGetApprovalDetailResponse detailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .createdAt(createdAt)
                .function("Assign Permissions")
                .userId(userResponse.getId())
                .userFullName("fullName")
                .serviceAgreementId(serviceAgreementId)
                .records(singletonList(new RecordDto()
                    .createdAt(createdAt)
                    .status(RecordStatus.APPROVED)
                    .userId("approverId1")
                    .userFullName("username1")))
                .policy(presentationPolicyDto));

        addStubGet(new UrlBuilder(getApprovalUrl)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", serviceAgreementId)
                .addQueryParameter("userId", userResponse.getId())
                .addQueryParameter("enrichUsersWithFullName", String.valueOf(true))
                .build(),
            detailResponse, 200);

        addStubGet(
            new UrlBuilder(getApprovalUrl)
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", serviceAgreementId)
                .addQueryParameter("userId", userId)
                .addQueryParameter("enrichUsersWithFullName", "false")
                .build(),

            new PresentationGetApprovalDetailResponse()
                .approvalDetails(new PresentationApprovalDetailDto()
                    .function("Assign Permissions")
                    .userId(userId)
                    .serviceAgreementId(serviceAgreementId)),
            200);

        String contentAsString = executeClientRequest(
            new UrlBuilder(url).addPathParameter(approvalId).build(),
            HttpMethod.GET, "USER", FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_APPROVE);

        PresentationPermissionsApprovalDetailsItem returnedApproval = readValue(contentAsString,
            PresentationPermissionsApprovalDetailsItem.class);

        assertThat(returnedApproval,
            getApprovalItemMatcher(
                is("fullName"),
                is(userResponse.getId()),
                is(createdAt),
                is(userResponse.getId()),
                is(userResponse.getFullName()),
                is(rootMsa.getId()),
                is(rootMsa.getName()),
                is(rootMsa.getDescription()),
                is(approvalId),
                is(2),
                is(1),
                hasItems(getApprovalLogItemMatcher(
                    is("approverId1"),
                    is("username1"),
                    is(createdAt)
                )))
        );

        List<PresentationFunctionGroupsDataGroupsPair> unmodifiedFunctionGroupsDataGroupsPairList = returnedApproval.getUnmodifiedFunctionGroups();
        assertThat(unmodifiedFunctionGroupsDataGroupsPairList, hasSize(1));
        assertThat(unmodifiedFunctionGroupsDataGroupsPairList, hasItem(hasProperty("name", is("fg2"))));
        assertThat(unmodifiedFunctionGroupsDataGroupsPairList.get(0).getDataGroups(), is(empty()));
        assertThat(unmodifiedFunctionGroupsDataGroupsPairList.get(0).getSelfApprovalPolicies(), is(empty()));

        List<PresentationFunctionGroupsDataGroupsPair> removedFunctionGroupsDataGroupsPairList = returnedApproval.getRemovedFunctionGroups();
        assertThat(removedFunctionGroupsDataGroupsPairList, hasItem(hasProperty("name", is("fg"))));
        assertThat(removedFunctionGroupsDataGroupsPairList.get(0).getDataGroups(), is(empty()));
        assertThat(removedFunctionGroupsDataGroupsPairList.get(0).getSelfApprovalPolicies(), is(empty()));

        List<PresentationFunctionGroupsDataGroupsPair> newFunctionGroupsDataGroupsPairList = returnedApproval.getNewFunctionGroups();
        assertThat(newFunctionGroupsDataGroupsPairList, hasSize(1));
        assertThat(newFunctionGroupsDataGroupsPairList, hasItem(hasProperty("name", is("fg"))));
        assertThat(newFunctionGroupsDataGroupsPairList.get(0).getDataGroups(), hasSize(2));
        assertThat(newFunctionGroupsDataGroupsPairList.get(0).getDataGroups().get(0).getType(), is("ARRANGEMENTS"));

        List<PresentationSelfApprovalPolicy> selfApprovalPolicies = newFunctionGroupsDataGroupsPairList.get(0)
            .getSelfApprovalPolicies();
        assertThat(selfApprovalPolicies, is(notNullValue()));
        assertThat(selfApprovalPolicies, hasSize(1));
        PresentationSelfApprovalPolicy selfApprovalPolicy = selfApprovalPolicies.get(0);
        assertThat(selfApprovalPolicy.getBusinessFunctionCode(), is("intra.company.payments"));
        assertThat(selfApprovalPolicy.getBounds(), hasSize(1));
        Bound bound1 = selfApprovalPolicy.getBounds().get(0);
        assertThat(bound1.getCurrencyCode(), is("EUR"));
        assertThat(bound1.getAmount(), comparesEqualTo(new BigDecimal(10000)));
    }

    private Matcher<PresentationPermissionsApprovalDetailsItem> getApprovalItemMatcher(
        Matcher<?> creatorUserFullNameMatcher,
        Matcher<?> creatorUserIdMatcher,
        Matcher<?> createdAtMatcher,
        Matcher<?> userIdMatcher,
        Matcher<?> userFullNameMatcher,
        Matcher<?> serviceAgreementIdMatcher,
        Matcher<?> serviceAgreementNameMatcher,
        Matcher<?> serviceAgreementDescriptionMatcher,
        Matcher<?> approvalIdMatcher,
        Matcher<?> requiredApprovesMatcher,
        Matcher<?> completedApprovesMatcher,
        Matcher<?> approvalLogMatcher
    ) {
        return allOf(
            hasProperty("creatorUserFullName", creatorUserFullNameMatcher),
            hasProperty("creatorUserId", creatorUserIdMatcher),
            hasProperty("createdAt", createdAtMatcher),
            hasProperty("userId", userIdMatcher),
            hasProperty("userFullName", userFullNameMatcher),
            hasProperty("serviceAgreementId", serviceAgreementIdMatcher),
            hasProperty("serviceAgreementName", serviceAgreementNameMatcher),
            hasProperty("serviceAgreementDescription", serviceAgreementDescriptionMatcher),
            hasProperty("approvalId", approvalIdMatcher),
            hasProperty("requiredApproves", requiredApprovesMatcher),
            hasProperty("completedApproves", completedApprovesMatcher),
            hasProperty("approvalLog", approvalLogMatcher)

        );
    }

    private Matcher<PresentationApprovalLogItem> getApprovalLogItemMatcher(Matcher<?> approverIdMatcher,
        Matcher<?> approverFullNameMatcher,
        Matcher<?> approvedAtMatcher) {
        return allOf(
            hasProperty("approverId", approverIdMatcher),
            hasProperty("approverFullName", approverFullNameMatcher),
            hasProperty("approvedAt", approvedAtMatcher)
        );
    }
}
