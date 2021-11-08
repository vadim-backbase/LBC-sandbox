package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class UserAssignedFunctionGroupDto {
    private String serviceAgreementId;
    private String functionGroupId;
    private Integer from;
    private Integer size;
}