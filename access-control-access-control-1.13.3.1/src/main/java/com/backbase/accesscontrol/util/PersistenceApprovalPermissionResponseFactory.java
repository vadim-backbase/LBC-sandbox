package com.backbase.accesscontrol.util;

import com.backbase.accesscontrol.domain.SelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissionsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Bound;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PersistenceApprovalPermissionResponseFactory {

    public List<PersistenceApprovalPermissionsGetResponseBody> createPersistenceApprovalPermissions(
        UserAssignedFunctionGroup userAssignedFunctionGroup) {

        if (userAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations().isEmpty()) {
            return Collections.singletonList(new PersistenceApprovalPermissionsGetResponseBody()
                .withFunctionGroupId(userAssignedFunctionGroup.getFunctionGroupId()));
        }

        return userAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations().stream()
            .map(this::createPersistenceApprovalPermissionsResponseBody)
            .collect(Collectors.toList());
    }

    private PersistenceApprovalPermissionsGetResponseBody createPersistenceApprovalPermissionsResponseBody(
        UserAssignedFunctionGroupCombination combination) {
        List<SelfApprovalPolicy> selfApprovalPolicies = combination.getSelfApprovalPolicies().stream()
            .map(this::createPolicy)
            .collect(Collectors.toList());

        PersistenceApprovalPermissionsGetResponseBody permissionResponse = new PersistenceApprovalPermissionsGetResponseBody();
        permissionResponse.setFunctionGroupId(combination.getUserAssignedFunctionGroup().getFunctionGroupId());
        permissionResponse.setDataGroupIds(new ArrayList<>(combination.getDataGroupIds()));
        permissionResponse.setSelfApprovalPolicies(selfApprovalPolicies);
        return permissionResponse;
    }

    private SelfApprovalPolicy createPolicy(com.backbase.accesscontrol.domain.SelfApprovalPolicy policy) {
        List<Bound> bounds = policy.getApprovalPolicyBounds().stream()
            .map(this::createBound)
            .collect(Collectors.toList());

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName(
            policy.getFunctionGroupItem().getApplicableFunctionPrivilege().getBusinessFunctionName());
        selfApprovalPolicy.setCanSelfApprove(policy.isCanSelfApprove());
        selfApprovalPolicy.setBounds(bounds);
        return selfApprovalPolicy;
    }

    private Bound createBound(SelfApprovalPolicyBound policyBound) {
        Bound bound = new Bound();
        bound.setAmount(policyBound.getUpperBound());
        bound.setCurrencyCode(policyBound.getCurrencyCode());
        return bound;
    }
}
