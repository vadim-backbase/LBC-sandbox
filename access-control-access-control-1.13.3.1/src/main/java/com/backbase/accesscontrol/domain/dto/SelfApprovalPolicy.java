package com.backbase.accesscontrol.domain.dto;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelfApprovalPolicy {

    private String businessFunctionName;
    private Boolean canSelfApprove;
    private Set<Bound> bounds = new HashSet<>();
}