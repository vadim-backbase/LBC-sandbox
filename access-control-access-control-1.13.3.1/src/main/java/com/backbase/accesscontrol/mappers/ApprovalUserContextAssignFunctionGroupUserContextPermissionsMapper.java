package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.dto.Bound;
import com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface ApprovalUserContextAssignFunctionGroupUserContextPermissionsMapper {

    @Mapping(target = "dataGroupIds", source = "dataGroups")
    @Mapping(target = "selfApprovalPolicies", source = "approvalSelfApprovalPolicies")
    UserContextPermissions map(ApprovalUserContextAssignFunctionGroup userContextAssignFunctionGroup);

    Set<SelfApprovalPolicy> map(Set<ApprovalSelfApprovalPolicy> approvalSelfApprovalPolicies);

    @Mapping(target = "businessFunctionName", source = "functionGroupItem.applicableFunctionPrivilege.businessFunctionName")
    @Mapping(target = "bounds", source = "approvalSelfApprovalPolicyBounds")
    SelfApprovalPolicy map(ApprovalSelfApprovalPolicy approvalSelfApprovalPolicy);

    @Mapping(target = "amount", source = "upperBound")
    @Mapping(target = "currencyCode", source = "currencyCode")
    Bound map(ApprovalSelfApprovalPolicyBound bound);
}
