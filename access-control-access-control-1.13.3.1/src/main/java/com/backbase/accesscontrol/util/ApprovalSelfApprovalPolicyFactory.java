package com.backbase.accesscontrol.util;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicy;
import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.FunctionGroupItemId;
import com.backbase.accesscontrol.domain.dto.Bound;
import com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy;
import com.backbase.accesscontrol.repository.FunctionGroupItemEntityRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.exceptions.SelfApprovalPolicyFactoryException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalSelfApprovalPolicyFactory {

    private static final String APPROVE = "approve";
    private final BusinessFunctionCache businessFunctionCache;
    private final FunctionGroupItemEntityRepository functionGroupItemEntityRepository;

    public ApprovalSelfApprovalPolicy createPolicy(String functionGroupId, SelfApprovalPolicy selfApprovalPolicy) {
        String businessFunctionName = selfApprovalPolicy.getBusinessFunctionName();
        String applicableFunctionPrivilegeId = businessFunctionCache.getAllApplicableFunctionPrivileges().stream()
            .filter(afp -> afp.getPrivilegeName().equals(APPROVE))
            .filter(afp -> afp.getBusinessFunctionName().equals(businessFunctionName))
            .map(ApplicableFunctionPrivilege::getId)
            .findFirst()
            .orElseThrow(() -> {
                log.warn("Unable to create ApprovalSelfApprovalPolicy for businessFunction {} with privilege {}",
                    businessFunctionName, APPROVE);
                return new SelfApprovalPolicyFactoryException("Unable to create ApprovalSelfApprovalPolicy");
            });

        ApprovalSelfApprovalPolicy approvalPolicy = new ApprovalSelfApprovalPolicy();
        approvalPolicy.setCanSelfApprove(selfApprovalPolicy.getCanSelfApprove());
        approvalPolicy.addBounds(createBounds(selfApprovalPolicy.getBounds()));
        FunctionGroupItemEntity functionGroupItem = functionGroupItemEntityRepository
            .findById(new FunctionGroupItemId(functionGroupId, applicableFunctionPrivilegeId))
            .orElseThrow(() -> {
                log.warn("Unable to create ApprovalSelfApprovalPolicy. FunctionGroup {} not assigned with afpId {}",
                    functionGroupId, applicableFunctionPrivilegeId);
                return new SelfApprovalPolicyFactoryException("Unable to create ApprovalSelfApprovalPolicy");
            });
        approvalPolicy.setFunctionGroupItem(functionGroupItem);
        return approvalPolicy;
    }

    private Set<ApprovalSelfApprovalPolicyBound> createBounds(Set<Bound> bounds) {
        if (bounds == null) {
            return Collections.emptySet();
        }

        return bounds.stream()
            .map(this::createApprovalSelfApprovalPolicyBound)
            .collect(Collectors.toSet());
    }

    private ApprovalSelfApprovalPolicyBound createApprovalSelfApprovalPolicyBound(Bound approvalBound) {
        ApprovalSelfApprovalPolicyBound bound = new ApprovalSelfApprovalPolicyBound();
        bound.setUpperBound(approvalBound.getAmount());
        bound.setCurrencyCode(approvalBound.getCurrencyCode());
        return bound;
    }
}
