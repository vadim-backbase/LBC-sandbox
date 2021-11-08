package com.backbase.accesscontrol.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroupItemEntity;
import com.backbase.accesscontrol.domain.FunctionGroupItemId;
import com.backbase.accesscontrol.domain.SelfApprovalPolicyBound;
import com.backbase.accesscontrol.domain.dto.Bound;
import com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy;
import com.backbase.accesscontrol.repository.FunctionGroupItemEntityRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.exceptions.SelfApprovalPolicyFactoryException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SelfApprovalPolicyFactoryTest {

    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private FunctionGroupItemEntityRepository functionGroupItemEntityRepository;
    @InjectMocks
    private SelfApprovalPolicyFactory selfApprovalPolicyFactory;

    @Test
    void shouldCreateSelfApprovalPolicy() {
        ApplicableFunctionPrivilege privilege = new ApplicableFunctionPrivilege();
        privilege.setPrivilegeName("approve");
        privilege.setBusinessFunctionName("SEPA CT");
        privilege.setId("afpId");
        FunctionGroupItemId functionGroupItemId = new FunctionGroupItemId("fgId", "afpId");
        FunctionGroupItemEntity entity = new FunctionGroupItemEntity();
        entity.setFunctionGroupItemId(functionGroupItemId);

        Bound bound = new Bound();
        bound.setCurrencyCode("EUR");
        bound.setAmount(BigDecimal.TEN);

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setBusinessFunctionName("SEPA CT");
        selfApprovalPolicy.setBounds(Set.of(bound));

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege));
        when(functionGroupItemEntityRepository.findById(functionGroupItemId)).thenReturn(Optional.of(entity));

        com.backbase.accesscontrol.domain.SelfApprovalPolicy policy = selfApprovalPolicyFactory
            .createPolicy("fgId", selfApprovalPolicy);

        assertThat(policy.isCanSelfApprove(), is(true));
        assertThat(policy.getFunctionGroupItem().getFunctionGroupItemId(), equalTo(functionGroupItemId));
        assertThat(policy.getApprovalPolicyBounds(), hasSize(1));

        SelfApprovalPolicyBound policyBound = policy.getApprovalPolicyBounds().iterator().next();

        assertThat(policyBound.getCurrencyCode(), equalTo("EUR"));
        assertThat(policyBound.getUpperBound(), equalTo(BigDecimal.TEN));
    }

    @Test
    void shouldThrowExceptionWhenBusinessFunctionWithPrivilegeNotExist() {
        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("SEPA CT");

        ApplicableFunctionPrivilege privilege = new ApplicableFunctionPrivilege();
        privilege.setPrivilegeName("view");
        privilege.setBusinessFunctionName("SEPA CT");

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege));

        SelfApprovalPolicyFactoryException exception = assertThrows(SelfApprovalPolicyFactoryException.class,
            () -> selfApprovalPolicyFactory.createPolicy("fgId", selfApprovalPolicy));

        assertThat(exception.getMessage(), equalTo("Unable to create SelfApprovalPolicy"));
    }

    @Test
    void shouldThrowExceptionWhenFunctionGroupNotAssignedWithApplicableFunctionPrivilege() {
        FunctionGroupItemId functionGroupItemId = new FunctionGroupItemId("fgId", "afpId");

        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("SEPA CT");
        selfApprovalPolicy.setCanSelfApprove(true);

        ApplicableFunctionPrivilege privilege = new ApplicableFunctionPrivilege();
        privilege.setPrivilegeName("approve");
        privilege.setBusinessFunctionName("SEPA CT");
        privilege.setId("afpId");


        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege));
        when(functionGroupItemEntityRepository.findById(functionGroupItemId)).thenReturn(Optional.empty());

        SelfApprovalPolicyFactoryException exception = assertThrows(SelfApprovalPolicyFactoryException.class,
            () -> selfApprovalPolicyFactory.createPolicy("fgId", selfApprovalPolicy));

        assertThat(exception.getMessage(), equalTo("Unable to create SelfApprovalPolicy"));
    }
}