package com.backbase.accesscontrol.util;

import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.backbase.accesscontrol.mappers.SelfApprovalPolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserContextPermissionsFactory {

    private final SelfApprovalPolicyMapper selfApprovalPolicyMapper;

    public List<UserContextPermissions> createUserContextPermissions(
        UserAssignedFunctionGroup userAssignedFunctionGroup) {
        String functionGroupId = userAssignedFunctionGroup.getFunctionGroupId();

        if (userAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations().isEmpty()) {
            UserContextPermissions userContextPermissions = new UserContextPermissions();
            userContextPermissions.setFunctionGroupId(functionGroupId);
            return Collections.singletonList(userContextPermissions);
        }

        return userAssignedFunctionGroup.getUserAssignedFunctionGroupCombinations().stream()
            .map(combination -> createUserContextPermissions(functionGroupId, combination))
            .collect(Collectors.toList());
    }

    private UserContextPermissions createUserContextPermissions(String functionGroupId, UserAssignedFunctionGroupCombination combination) {
        return new UserContextPermissions(functionGroupId, combination.getDataGroupIds(),
                selfApprovalPolicyMapper.map(combination.getSelfApprovalPolicies()));
    }
}
