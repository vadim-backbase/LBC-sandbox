package com.backbase.accesscontrol.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.SelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissionsGetResponseBody;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PersistenceApprovalPermissionResponseFactoryTest {

    private final PersistenceApprovalPermissionResponseFactory factory = new PersistenceApprovalPermissionResponseFactory();

    @Test
    void shouldCreatePermissionsWhenNoDataGroupsAreProvided() {
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroupId("fgId");

        List<PersistenceApprovalPermissionsGetResponseBody> permissionsResponses = factory
            .createPersistenceApprovalPermissions(assignedFunctionGroup);

        assertThat(permissionsResponses, hasSize(1));
        assertThat(permissionsResponses.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissionsResponses.get(0).getDataGroupIds(), empty());
        assertThat(permissionsResponses.get(0).getSelfApprovalPolicies(), empty());
    }

    @Test
    void shouldCreatePermissionsWithDataGroups() {
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroupId("fgId");
        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.setDataGroupIds(Set.of("dg1", "dg2"));
        combination.setUserAssignedFunctionGroup(assignedFunctionGroup);
        assignedFunctionGroup.getUserAssignedFunctionGroupCombinations().add(combination);

        List<PersistenceApprovalPermissionsGetResponseBody> permissionsResponses = factory
            .createPersistenceApprovalPermissions(assignedFunctionGroup);

        assertThat(permissionsResponses, hasSize(1));
        assertThat(permissionsResponses.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissionsResponses.get(0).getDataGroupIds(), hasSize(2));
        assertThat(permissionsResponses.get(0).getDataGroupIds(), hasItems("dg1", "dg2"));
        assertThat(permissionsResponses.get(0).getSelfApprovalPolicies(), empty());
    }

    @Test
    void shouldCreatePermissionsWithSelfApprovalPolicy() {
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroupId("fgId");

        FunctionGroupItemEntity item = createItem("SEPA");
        SelfApprovalPolicy policy = new SelfApprovalPolicy();
        policy.setFunctionGroupItem(item);
        policy.setCanSelfApprove(true);

        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.setDataGroupIds(Set.of("dg1", "dg2"));
        combination.setUserAssignedFunctionGroup(assignedFunctionGroup);
        assignedFunctionGroup.getUserAssignedFunctionGroupCombinations().add(combination);
        combination.getSelfApprovalPolicies().add(policy);

        List<PersistenceApprovalPermissionsGetResponseBody> permissionsResponses = factory
            .createPersistenceApprovalPermissions(assignedFunctionGroup);

        assertThat(permissionsResponses, hasSize(1));
        assertThat(permissionsResponses.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissionsResponses.get(0).getSelfApprovalPolicies(), hasSize(1));
        assertThat(permissionsResponses.get(0).getSelfApprovalPolicies().get(0).getBusinessFunctionName(), equalTo("SEPA"));
        assertThat(permissionsResponses.get(0).getSelfApprovalPolicies().get(0).getCanSelfApprove(), is(true));
    }

    @Test
    void shouldCreatePermissionsWithSelfApprovalPolicyBounds() {
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroupId("fgId");

        SelfApprovalPolicyBound bound = new SelfApprovalPolicyBound();
        bound.setCurrencyCode("EUR");
        bound.setUpperBound(BigDecimal.TEN);

        FunctionGroupItemEntity item = createItem("SEPA");
        SelfApprovalPolicy policy = new SelfApprovalPolicy();
        policy.setFunctionGroupItem(item);
        policy.setCanSelfApprove(true);
        policy.getApprovalPolicyBounds().add(bound);

        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.setDataGroupIds(Set.of("ag1", "ag2"));
        combination.setUserAssignedFunctionGroup(assignedFunctionGroup);
        assignedFunctionGroup.getUserAssignedFunctionGroupCombinations().add(combination);
        combination.getSelfApprovalPolicies().add(policy);

        List<PersistenceApprovalPermissionsGetResponseBody> permissionsResponses = factory
            .createPersistenceApprovalPermissions(assignedFunctionGroup);

        assertThat(permissionsResponses, hasSize(1));
        assertThat(permissionsResponses.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissionsResponses.get(0).getSelfApprovalPolicies(), hasSize(1));

        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy selfApprovalPolicy = permissionsResponses
            .get(0).getSelfApprovalPolicies().get(0);

        assertThat(selfApprovalPolicy.getBounds(), hasSize(1));
        assertThat(selfApprovalPolicy.getBounds().get(0).getCurrencyCode(), equalTo("EUR"));
        assertThat(selfApprovalPolicy.getBounds().get(0).getAmount(), equalTo(BigDecimal.TEN));
    }

    private FunctionGroupItemEntity createItem(String businessFunctionName) {
        FunctionGroupItemEntity functionGroupItemEntity = mock(FunctionGroupItemEntity.class);
        ApplicableFunctionPrivilege privilege = mock(ApplicableFunctionPrivilege.class);
        doReturn(businessFunctionName).when(privilege).getBusinessFunctionName();
        doReturn(privilege).when(functionGroupItemEntity).getApplicableFunctionPrivilege();
        return functionGroupItemEntity;
    }
}