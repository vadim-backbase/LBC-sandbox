package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_111;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_112;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.dto.Bound;
import com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserContextPermissionsSelfApprovalPolicyValidatorTest {

    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private BusinessFunctionValidator businessFunctionValidator;
    @Mock
    private FunctionGroupValidator functionGroupValidator;
    @InjectMocks
    private UserContextPermissionsSelfApprovalPolicyValidator selfApprovalPolicyValidator;

    @Test
    void shouldValidateBusinessFunctionsExist() {
        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("Assign Permissions");
        selfApprovalPolicy.setCanSelfApprove(true);
        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setSelfApprovalPolicies(Set.of(selfApprovalPolicy));

        selfApprovalPolicyValidator.validateSelfApprovalPolicies(Set.of(userContextPermissions));

        verify(businessFunctionValidator).validateBusinessFunctionsExist(Set.of("Assign Permissions"));
    }

    @Test
    void shouldThrowBadRequestWhenSelfApprovalPolicyNumberOfBoundsExceeded() {
        Bound bound1 = new Bound(BigDecimal.ONE, "EUR");
        Bound bound2 = new Bound(BigDecimal.TEN, "EUR");
        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("Assign Permissions");
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setBounds(Set.of(bound1, bound2));

        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setSelfApprovalPolicies(Set.of(selfApprovalPolicy));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> selfApprovalPolicyValidator.validateSelfApprovalPolicies(Set.of(userContextPermissions)));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_112.getErrorMessage(), ERR_ACC_112.getErrorCode()));
    }

    @Test
    void shouldValidateBusinessFunctionsSupportLimits() {
        Bound bound = new Bound(BigDecimal.ONE, "EUR");
        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("Assign Permissions");
        selfApprovalPolicy.setCanSelfApprove(true);
        selfApprovalPolicy.setBounds(Set.of(bound));
        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setSelfApprovalPolicies(Set.of(selfApprovalPolicy));

        selfApprovalPolicyValidator.validateSelfApprovalPolicies(Set.of(userContextPermissions));

        verify(businessFunctionValidator).validateBusinessFunctionsSupportLimits(Set.of("Assign Permissions"));
    }

    @Test
    void shouldThrowBadRequestWhenSelfApprovalPoliciesProvidedWithBoundsAndSelfApprovalFlagEqualsToFalse() {
        Bound bound = new Bound(BigDecimal.ONE, "EUR");
        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("Assign Permissions");
        selfApprovalPolicy.setCanSelfApprove(false);
        selfApprovalPolicy.setBounds(Set.of(bound));
        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setSelfApprovalPolicies(Set.of(selfApprovalPolicy));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> selfApprovalPolicyValidator.validateSelfApprovalPolicies(Set.of(userContextPermissions)));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_111.getErrorMessage(), ERR_ACC_111.getErrorCode()));
    }

    @Test
    void shouldValidateBusinessFunctionsSupportApprovePrivilege() {
        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("Assign Permissions");
        selfApprovalPolicy.setCanSelfApprove(true);
        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setSelfApprovalPolicies(Set.of(selfApprovalPolicy));

        selfApprovalPolicyValidator.validateSelfApprovalPolicies(Set.of(userContextPermissions));

        verify(businessFunctionValidator).validateBusinessFunctionsSupportPrivilege(Set.of("Assign Permissions"), "approve");
    }

    @Test
    void shouldValidateFunctionGroupAssignedWithPrivileges() {
        SelfApprovalPolicy selfApprovalPolicy = new SelfApprovalPolicy();
        selfApprovalPolicy.setBusinessFunctionName("Assign Permissions");
        selfApprovalPolicy.setCanSelfApprove(true);
        UserContextPermissions userContextPermissions = new UserContextPermissions();
        userContextPermissions.setFunctionGroupId("fgId");
        userContextPermissions.setSelfApprovalPolicies(Set.of(selfApprovalPolicy));

        ApplicableFunctionPrivilege privilege = new ApplicableFunctionPrivilege();
        privilege.setPrivilegeName("approve");
        privilege.setBusinessFunctionName("Assign Permissions");

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege));

        selfApprovalPolicyValidator.validateSelfApprovalPolicies(Set.of(userContextPermissions));

        verify(functionGroupValidator).validateFunctionGroupAssignedWithPrivileges("fgId", Set.of(privilege));
    }
}