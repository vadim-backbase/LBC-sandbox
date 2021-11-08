package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_115;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
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
public class FunctionGroupValidator {

    private final FunctionGroupJpaRepository functionGroupJpaRepository;

    public void validateFunctionGroupAssignedWithPrivileges(String functionGroupId,
        Collection<ApplicableFunctionPrivilege> applicableFunctionPrivileges) {
        if (CollectionUtils.isNotEmpty(applicableFunctionPrivileges)) {
            FunctionGroup functionGroup = functionGroupJpaRepository.findByIdWithPermissions(functionGroupId)
                .orElseThrow(() -> {
                    log.warn("Function group {} not found", functionGroupId);
                    return getNotFoundException(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode());
                });

            Set<String> assignedApplicableFunctionPrivilegesIds = extractApplicableFunctionPrivilegeIds(functionGroup);

            for (ApplicableFunctionPrivilege privilege : applicableFunctionPrivileges) {
                if (!assignedApplicableFunctionPrivilegesIds.contains(privilege.getId())) {
                    String businessFunction = privilege.getBusinessFunctionName();
                    String privilegeName = privilege.getPrivilegeName();
                    log.warn("Business Function " + businessFunction + " with privilege " + privilegeName
                        + " not assigned to functionGroup");
                    String errorMessage = String.format(ERR_ACC_115.getErrorMessage(), businessFunction, privilegeName);
                    throw getBadRequestException(errorMessage, ERR_ACC_115.getErrorCode());
                }
            }
        }
    }

    private Set<String> extractApplicableFunctionPrivilegeIds(FunctionGroup functionGroup) {
        return functionGroup.getPermissions().stream()
            .map(FunctionGroupItem::getApplicableFunctionPrivilegeId)
            .collect(Collectors.toSet());
    }
}
