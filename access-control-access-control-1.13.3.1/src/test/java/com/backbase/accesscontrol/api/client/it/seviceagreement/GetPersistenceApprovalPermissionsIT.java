package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.oneOf;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementsController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
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
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementsController#getAssignUsersPermissions}
 */
public class GetPersistenceApprovalPermissionsIT extends TestDbWireMock {

    private static final String ENDPOINT_URL = "/accessgroups/service-agreements/{serviceAgreementId}/users/{userId}/permissions";

    private UserContext userContext;
    private FunctionGroup functionGroup;
    private DataGroup dataGroup;
    private DataGroup dataGroup2;

    private String approvalId = "approvalId";
    private ServiceAgreement serviceAgreement;
    private ApplicableFunctionPrivilege apfBf1020View;
    private ApplicableFunctionPrivilege apfBf1002Create;
    private ApplicableFunctionPrivilege apfBf1020Approve;
    private ApplicableFunctionPrivilege apfBf1003View;

    @Before
    public void setUp() {

        apfBf1020View = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "view");
        apfBf1020Approve = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "approve");
        apfBf1002Create = businessFunctionCache.getByFunctionIdAndPrivilege("1002", "create");
        apfBf1003View = businessFunctionCache.getByFunctionIdAndPrivilege("1003", "view");

        LegalEntity legalEntity = rootLegalEntity;

        // create SA
        serviceAgreement =
            createServiceAgreement("BB between self", "id.external", "desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        //save function group
        GroupedFunctionPrivilege approveManageFunctionGroups = getGroupedFunctionPrivilege(null, apfBf1020Approve, null);
        GroupedFunctionPrivilege viewEntitlementsWithLimit = getGroupedFunctionPrivilege(null, apfBf1020View, null);
        GroupedFunctionPrivilege createEntitlementsWitLimit = getGroupedFunctionPrivilege(null, apfBf1002Create, null);
        GroupedFunctionPrivilege viewProductsWithLimit = getGroupedFunctionPrivilege(null, apfBf1003View, null);

        functionGroup = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    approveManageFunctionGroups,
                    viewEntitlementsWithLimit,
                    createEntitlementsWitLimit,
                    viewProductsWithLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        // create data group
        dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);

        dataGroup2 = DataGroupUtil.createDataGroup("name2", "ARRANGEMENTS", "desc2", serviceAgreement);
        dataGroup2.setDataItemIds(newHashSet("00004", "00005", "00006"));
        dataGroup2 = dataGroupJpaRepository.saveAndFlush(dataGroup2);
        userContext = new UserContext("user", serviceAgreement.getId());

        FunctionGroupItemId functionGroupItemId = new FunctionGroupItemId(functionGroup.getId(), apfBf1020Approve.getId());
        FunctionGroupItemEntity functionGroupItem = new FunctionGroupItemEntity();
        functionGroupItem.setFunctionGroupItemId(functionGroupItemId);

        SelfApprovalPolicyBound policyBound = new SelfApprovalPolicyBound();
        policyBound.setUpperBound(BigDecimal.TEN);
        policyBound.setCurrencyCode("EUR");

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setFunctionGroupItem(functionGroupItem);
        selfApprovalPolicy.getApprovalPolicyBounds().add(policyBound);
        policyBound.setSelfApprovalPolicy(selfApprovalPolicy);

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(functionGroup, userContext);
        UserAssignedFunctionGroupCombination combination1 = new UserAssignedFunctionGroupCombination(
            newHashSet(dataGroup.getId()), uafg);
        combination1.getSelfApprovalPolicies().add(selfApprovalPolicy);
        selfApprovalPolicy.setUserAssignedFunctionGroupCombination(combination1);

        UserAssignedFunctionGroupCombination combination2 = new UserAssignedFunctionGroupCombination(
            newHashSet(dataGroup2.getId()), uafg);

        uafg.setUserAssignedFunctionGroupCombinations(newHashSet(combination1, combination2));

        userContext.setUserAssignedFunctionGroups(newHashSet(uafg));
        userContext = userContextJpaRepository.save(userContext);

        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup =
            new ApprovalUserContextAssignFunctionGroup()
                .withFunctionGroupId(functionGroup.getId())
                .withDataGroups(Sets.newHashSet(dataGroup.getId(), dataGroup2.getId()));

        ApprovalUserContext approvalUserContext = new ApprovalUserContext();
        approvalUserContext.setApprovalId(approvalId);
        approvalUserContext.setUserId(userContext.getUserId());
        approvalUserContext.setLegalEntityId(legalEntity.getId());
        approvalUserContext.setServiceAgreementId(serviceAgreement.getId());

        approvalUserContextAssignFunctionGroup.setApprovalUserContext(approvalUserContext);

        approvalUserContext.getApprovalUserContextAssignFunctionGroups().add(approvalUserContextAssignFunctionGroup);

        approvalUserContextJpaRepository.save(approvalUserContext);
    }


    @Test
    public void getPersistenceApprovalPermissions() throws Exception {
        String serviceAgreementId = serviceAgreement.getId();
        String userId = userContext.getUserId();

        String contentAsString =
            executeClientRequest(
                new UrlBuilder(ENDPOINT_URL).addPathParameter(serviceAgreementId).addPathParameter(userId).build()
                , HttpMethod.GET, "user", FUNCTION_ASSIGN_PERMISSONS, PRIVILEGE_VIEW);

        PresentationApprovalPermissions presentationApprovalPermissions = objectMapper
            .readValue(contentAsString, PresentationApprovalPermissions.class);

        assertEquals(approvalId, presentationApprovalPermissions.getApprovalId());
        assertEquals(functionGroup.getId(), presentationApprovalPermissions.getItems().get(0).getFunctionGroupId());
        assertEquals(functionGroup.getId(), presentationApprovalPermissions.getItems().get(1).getFunctionGroupId());
        assertEquals(2, presentationApprovalPermissions.getItems().size());
        assertThat(presentationApprovalPermissions.getItems().get(0).getDataGroupIds(),
            contains(oneOf(new PresentationGenericObjectId().withId(dataGroup.getId()),
                new PresentationGenericObjectId().withId(dataGroup2.getId()))));
        assertThat(presentationApprovalPermissions.getItems().get(1).getDataGroupIds(),
            contains(oneOf(new PresentationGenericObjectId().withId(dataGroup.getId()),
                new PresentationGenericObjectId().withId(dataGroup2.getId()))));

        PresentationFunctionDataGroup functionDataGroupWithSelfApproval = presentationApprovalPermissions.getItems()
            .stream().filter(item -> !item.getSelfApprovalPolicies().isEmpty()).findFirst().get();

        assertEquals(1, functionDataGroupWithSelfApproval.getDataGroupIds().size());
        assertEquals(functionDataGroupWithSelfApproval.getDataGroupIds().get(0),
            new PresentationGenericObjectId().withId(dataGroup.getId()));
        assertEquals(1, functionDataGroupWithSelfApproval.getSelfApprovalPolicies().size());

        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy policy = functionDataGroupWithSelfApproval
            .getSelfApprovalPolicies().get(0);

        assertEquals(apfBf1020Approve.getBusinessFunctionName(), policy.getBusinessFunctionName());
        assertTrue(policy.getCanSelfApprove());
        assertEquals(1, policy.getBounds().size());
        assertEquals("EUR", policy.getBounds().get(0).getCurrencyCode());
        assertThat(policy.getBounds().get(0).getAmount(), comparesEqualTo(BigDecimal.TEN));
    }
}
