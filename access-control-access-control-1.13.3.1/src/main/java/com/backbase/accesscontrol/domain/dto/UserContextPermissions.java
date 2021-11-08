package com.backbase.accesscontrol.domain.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContextPermissions {

    private String functionGroupId;
    private Set<String> dataGroupIds = new HashSet<>();
    private Set<SelfApprovalPolicy> selfApprovalPolicies = new HashSet<>();
}
