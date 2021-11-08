package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_111;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_112;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.dto.SelfApprovalPolicy;
import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserContextPermissionsSelfApprovalPolicyValidator {

    private static final String APPROVE = "approve";
    private static final int ALLOWED_NUMBER_OF_BOUNDS = 1;
    private final BusinessFunctionCache businessFunctionCache;
    private final BusinessFunctionValidator businessFunctionValidator;
    private final FunctionGroupValidator functionGroupValidator;

    public void validateSelfApprovalPolicies(Set<UserContextPermissions> userContextPermissions) {
        validateBusinessFunctionsExist(userContextPermissions);

        validateSelfApprovalPolicyNumberOfBounds(userContextPermissions);

        validateBusinessFunctionsSupportLimits(userContextPermissions);

        validateSelfApprovalPoliciesBoundsWithSelfApproveFlagFalse(userContextPermissions);

        validateFunctionGroupsAssignedWithBusinessFunctionsAndApprovePrivilege(userContextPermissions);
    }

    private void validateBusinessFunctionsExist(Set<UserContextPermissions> userContextPermissions) {
        Set<String> businessFunctions = userContextPermissions.stream()
            .map(UserContextPermissions::getSelfApprovalPolicies)
            .flatMap(Collection::stream)
            .map(SelfApprovalPolicy::getBusinessFunctionName)
            .collect(Collectors.toSet());

        businessFunctionValidator.validateBusinessFunctionsExist(businessFunctions);
    }

    private void validateSelfApprovalPolicyNumberOfBounds(Set<UserContextPermissions> userContextPermissions) {
        boolean matchAllowedNumberOfBounds = userContextPermissions.stream()
            .map(UserContextPermissions::getSelfApprovalPolicies)
            .flatMap(Collection::stream)
            .filter(policy -> policy.getBounds() != null)
            .allMatch(policy -> policy.getBounds().size() <= ALLOWED_NUMBER_OF_BOUNDS);

        if (!matchAllowedNumberOfBounds) {
            log.warn("You can not add more than one bound per policy");
            throw getBadRequestException(ERR_ACC_112.getErrorMessage(), ERR_ACC_112.getErrorCode());
        }
    }

    private void validateBusinessFunctionsSupportLimits(Set<UserContextPermissions> userContextPermissions) {
        Set<String> businessFunctionsToCheckLimitSupport = userContextPermissions.stream()
            .map(UserContextPermissions::getSelfApprovalPolicies)
            .flatMap(Collection::stream)
            .filter(policy -> CollectionUtils.isNotEmpty(policy.getBounds()))
            .map(SelfApprovalPolicy::getBusinessFunctionName)
            .collect(Collectors.toSet());

        businessFunctionValidator.validateBusinessFunctionsSupportLimits(businessFunctionsToCheckLimitSupport);
    }

    private void validateSelfApprovalPoliciesBoundsWithSelfApproveFlagFalse(Set<UserContextPermissions> userContextPermissions) {
        boolean boundsEmpty = userContextPermissions.stream()
            .map(UserContextPermissions::getSelfApprovalPolicies)
            .flatMap(Collection::stream)
            .filter(policy -> !policy.getCanSelfApprove())
            .allMatch(policy -> CollectionUtils.isEmpty(policy.getBounds()));

        if (!boundsEmpty) {
            log.warn("You can not add bounds to SelfApprovalPolicy and set flag canSelfApprove to false");
            throw getBadRequestException(ERR_ACC_111.getErrorMessage(), ERR_ACC_111.getErrorCode());
        }
    }

    private void validateFunctionGroupsAssignedWithBusinessFunctionsAndApprovePrivilege(
        Set<UserContextPermissions> userContextPermissions) {
        for (UserContextPermissions permissions : userContextPermissions) {
            if (!permissions.getSelfApprovalPolicies().isEmpty()) {
                String functionGroupId = permissions.getFunctionGroupId();
                Set<String> businessFunctionNames = extractBusinessFunctionNames(permissions);

                businessFunctionValidator.validateBusinessFunctionsSupportPrivilege(businessFunctionNames, APPROVE);

                Set<ApplicableFunctionPrivilege> privileges = businessFunctionCache
                    .getAllApplicableFunctionPrivileges().stream()
                    .filter(afp -> afp.getPrivilegeName().equals(APPROVE))
                    .filter(afp -> businessFunctionNames.contains(afp.getBusinessFunctionName()))
                    .collect(Collectors.toSet());

                functionGroupValidator.validateFunctionGroupAssignedWithPrivileges(functionGroupId, privileges);
            }
        }
    }

    private Set<String> extractBusinessFunctionNames(UserContextPermissions permissions) {
        return permissions.getSelfApprovalPolicies().stream()
            .map(SelfApprovalPolicy::getBusinessFunctionName)
            .collect(Collectors.toSet());
    }
}