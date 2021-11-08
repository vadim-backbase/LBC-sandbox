package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_109;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_110;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_114;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
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
public class BusinessFunctionValidator {

    private final BusinessFunctionCache businessFunctionCache;

    public void validateBusinessFunctionsExist(Collection<String> businessFunctionNames) {
        if (CollectionUtils.isNotEmpty(businessFunctionNames)) {
            Set<String> existingBusinessFunctionNames = businessFunctionCache.getAllApplicableFunctionPrivileges()
                .stream()
                .map(ApplicableFunctionPrivilege::getBusinessFunctionName)
                .collect(Collectors.toSet());

            for (String businessFunctionName : businessFunctionNames) {
                if (!existingBusinessFunctionNames.contains(businessFunctionName)) {
                    log.warn("Business Function " + businessFunctionName + " is not supported");
                    String errorMessage = String.format(ERR_ACC_109.getErrorMessage(), businessFunctionName);
                    throw getBadRequestException(errorMessage, ERR_ACC_109.getErrorCode());
                }
            }
        }
    }

    public void validateBusinessFunctionsSupportLimits(Collection<String> businessFunctionNames) {
        if (CollectionUtils.isNotEmpty(businessFunctionNames)) {
            Set<String> businessFunctionsWithLimitsSupport = businessFunctionCache.getAllApplicableFunctionPrivileges()
                .stream()
                .filter(ApplicableFunctionPrivilege::isSupportsLimit)
                .map(ApplicableFunctionPrivilege::getBusinessFunctionName)
                .collect(Collectors.toSet());

            for (String businessFunctionName : businessFunctionNames) {
                if (!businessFunctionsWithLimitsSupport.contains(businessFunctionName)) {
                    log.warn("Business Function " + businessFunctionName + " does not support limits");
                    String errorMessage = String.format(ERR_ACC_110.getErrorMessage(), businessFunctionName);
                    throw getBadRequestException(errorMessage, ERR_ACC_110.getErrorCode());
                }
            }
        }
    }

    public void validateBusinessFunctionsSupportPrivilege(Collection<String> businessFunctionNames, String privilegeName) {
        if (CollectionUtils.isNotEmpty(businessFunctionNames)) {
            Set<String> businessFunctionNamesWithProvidedPrivilege = businessFunctionCache
                .getAllApplicableFunctionPrivileges().stream()
                .filter(afp -> afp.getPrivilegeName().equals(privilegeName))
                .map(ApplicableFunctionPrivilege::getBusinessFunctionName)
                .collect(Collectors.toSet());

            for (String businessFunctionName : businessFunctionNames) {
                if (!businessFunctionNamesWithProvidedPrivilege.contains(businessFunctionName)) {
                    log.warn("Business function " + businessFunctionName + " does not support " + privilegeName + " privilege");
                    String errorMessage = String
                        .format(ERR_ACC_114.getErrorMessage(), businessFunctionName, privilegeName);
                    throw getBadRequestException(errorMessage, ERR_ACC_114.getErrorCode());
                }
            }
        }
    }
}
