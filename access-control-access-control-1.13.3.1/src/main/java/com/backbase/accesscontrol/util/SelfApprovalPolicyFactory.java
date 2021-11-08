package com.backbase.accesscontrol.util;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.FunctionGroupItemId;
import com.backbase.accesscontrol.domain.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.SelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.dto.Bound;
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
public class SelfApprovalPolicyFactory {

    private static final String APPROVE = "approve";
    private final BusinessFunctionCache businessFunctionCache;
    private final FunctionGroupItemEntityRepository functionGroupItemEntityRepository;

    public SelfApprovalPolicy createPolicy(String functionGroupId,
        com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy selfApprovalPolicy) {
        String businessFunctionName = selfApprovalPolicy.getBusinessFunctionName();
        String applicableFunctionPrivilegeId = businessFunctionCache.getAllApplicableFunctionPrivileges().stream()
            .filter(afp -> afp.getPrivilegeName().equals(APPROVE))
            .filter(afp -> afp.getBusinessFunctionName().equals(businessFunctionName))
            .map(ApplicableFunctionPrivilege::getId)
            .findFirst()
            .orElseThrow(() -> {
                log.warn("Unable to create SelfApprovalPolicy for businessFunction {} with privilege {}",
                    businessFunctionName, APPROVE);
                return new SelfApprovalPolicyFactoryException("Unable to create SelfApprovalPolicy");
            });

        SelfApprovalPolicy approvalPolicy = new SelfApprovalPolicy();
        approvalPolicy.setCanSelfApprove(selfApprovalPolicy.getCanSelfApprove());
        approvalPolicy.addBounds(createBounds(selfApprovalPolicy.getBounds()));
        FunctionGroupItemEntity functionGroupItem = functionGroupItemEntityRepository
            .findById(new FunctionGroupItemId(functionGroupId, applicableFunctionPrivilegeId))
            .orElseThrow(() -> {
                log.warn("Unable to create SelfApprovalPolicy. FunctionGroup {} not assigned with afpId {}",
                    functionGroupId, applicableFunctionPrivilegeId);
                return new SelfApprovalPolicyFactoryException("Unable to create SelfApprovalPolicy");
            });
        approvalPolicy.setFunctionGroupItem(functionGroupItem);
        return approvalPolicy;
    }

    private Set<SelfApprovalPolicyBound> createBounds(Set<Bound> bounds) {
        if (bounds == null) {
            return Collections.emptySet();
        }

        return bounds.stream()
            .map(this::createSelfApprovalPolicyBound)
            .collect(Collectors.toSet());
    }

    private SelfApprovalPolicyBound createSelfApprovalPolicyBound(Bound bound) {
        SelfApprovalPolicyBound approvalBound = new SelfApprovalPolicyBound();
        approvalBound.setUpperBound(bound.getAmount());
        approvalBound.setCurrencyCode(bound.getCurrencyCode());
        return approvalBound;
    }
}