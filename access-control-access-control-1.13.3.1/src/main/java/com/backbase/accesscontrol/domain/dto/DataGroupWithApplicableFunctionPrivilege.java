package com.backbase.accesscontrol.domain.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class DataGroupWithApplicableFunctionPrivilege {
    private String dataGroupId;
    private String dataGroupType;
    private String applicableFunctionPrivilegeId;

}
