package com.backbase.accesscontrol.domain.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class UserAssignedFunctionGroupDataGroupPermissions {

    @Include
    private Long userAssignedFunctionGroupId;
    private Set<String> applicableFunctionPrivilegeIds;
    private Set<FunctionGroupDataGroupCombinations> fgDgCombinations;
}
