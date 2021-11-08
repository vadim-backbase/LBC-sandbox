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
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.backbase.accesscontrol.mappers.SelfApprovalPolicyMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserContextPermissionsFactoryTest {

    private final SelfApprovalPolicyMapper selfApprovalPolicyMapper = Mappers.getMapper(SelfApprovalPolicyMapper.class);
    private final UserContextPermissionsFactory factory = new UserContextPermissionsFactory(selfApprovalPolicyMapper);

    @Test
    void shouldCreateUserContextPermissionsWhenNoDataGroupsAreProvided() {
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroupId("fgId");

        List<UserContextPermissions> permissions = factory.createUserContextPermissions(assignedFunctionGroup);

        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissions.get(0).getDataGroupIds(), empty());
        assertThat(permissions.get(0).getSelfApprovalPolicies(), empty());
    }

    @Test
    void shouldCreateUserContextPermissionsWithDataGroups() {
        UserAssignedFunctionGroup assignedFunctionGroup = new UserAssignedFunctionGroup();
        assignedFunctionGroup.setFunctionGroupId("fgId");
        UserAssignedFunctionGroupCombination combination = new UserAssignedFunctionGroupCombination();
        combination.setDataGroupIds(Set.of("dg1", "dg2"));
        combination.setUserAssignedFunctionGroup(assignedFunctionGroup);
        assignedFunctionGroup.getUserAssignedFunctionGroupCombinations().add(combination);

        List<UserContextPermissions> permissions = factory.createUserContextPermissions(assignedFunctionGroup);

        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissions.get(0).getDataGroupIds(), hasSize(2));
        assertThat(permissions.get(0).getDataGroupIds(), hasItems("dg1", "dg2"));
        assertThat(permissions.get(0).getSelfApprovalPolicies(), empty());
    }

    @Test
    void shouldCreateUserContextPermissionsWithSelfApprovalPolicy() {
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

        List<UserContextPermissions> permissions = factory.createUserContextPermissions(assignedFunctionGroup);

        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissions.get(0).getSelfApprovalPolicies(), hasSize(1));
        assertThat(permissions.get(0).getSelfApprovalPolicies().iterator().next().getBusinessFunctionName(), equalTo("SEPA"));
        assertThat(permissions.get(0).getSelfApprovalPolicies().iterator().next().getCanSelfApprove(), is(true));
    }

    @Test
    void shouldCreateUserContextPermissionsWithSelfApprovalPolicyBounds() {
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

        List<UserContextPermissions> permissions = factory.createUserContextPermissions(assignedFunctionGroup);

        assertThat(permissions, hasSize(1));
        assertThat(permissions.get(0).getFunctionGroupId(), equalTo("fgId"));
        assertThat(permissions.get(0).getSelfApprovalPolicies(), hasSize(1));

        com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy selfApprovalPolicy = permissions
            .get(0).getSelfApprovalPolicies().iterator().next();

        assertThat(selfApprovalPolicy.getBounds(), hasSize(1));
        assertThat(selfApprovalPolicy.getBounds().iterator().next().getCurrencyCode(), equalTo("EUR"));
        assertThat(selfApprovalPolicy.getBounds().iterator().next().getAmount(), equalTo(BigDecimal.TEN));
    }

    private FunctionGroupItemEntity createItem(String businessFunctionName) {
        FunctionGroupItemEntity functionGroupItemEntity = mock(FunctionGroupItemEntity.class);
        ApplicableFunctionPrivilege privilege = mock(ApplicableFunctionPrivilege.class);
        doReturn(businessFunctionName).when(privilege).getBusinessFunctionName();
        doReturn(privilege).when(functionGroupItemEntity).getApplicableFunctionPrivilege();
        return functionGroupItemEntity;
    }
}