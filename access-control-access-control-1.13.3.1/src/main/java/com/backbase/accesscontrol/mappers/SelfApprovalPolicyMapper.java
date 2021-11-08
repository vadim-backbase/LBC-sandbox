package com.backbase.accesscontrol.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface SelfApprovalPolicyMapper {

    @Mapping(target = "businessFunctionCode", source = "businessFunctionCode")
    com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationSelfApprovalPolicy map(
            com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy selfApprovalPolicy, String businessFunctionCode);

    Set<com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy> map(Set<com.backbase.accesscontrol.domain.SelfApprovalPolicy> selfApprovalPolicies);

    @Mapping(target = "businessFunctionName", source = "functionGroupItem.applicableFunctionPrivilege.businessFunctionName")
    @Mapping(target = "bounds", source = "approvalPolicyBounds")
    com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy mapPolicy(com.backbase.accesscontrol.domain.SelfApprovalPolicy selfApprovalPolicy);

    Set<com.backbase.accesscontrol.domain.dto.Bound> mapBounds(Set<com.backbase.accesscontrol.domain.SelfApprovalPolicyBound> bounds);

    @Mapping(target = "amount", source = "upperBound")
    com.backbase.accesscontrol.domain.dto.Bound mapBound(com.backbase.accesscontrol.domain.SelfApprovalPolicyBound bound);
}
